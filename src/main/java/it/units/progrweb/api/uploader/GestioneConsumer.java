package it.units.progrweb.api.uploader;

import it.units.progrweb.api.administrator.GestioneAttori;
import it.units.progrweb.entities.RelazioneUploaderConsumerFile;
import it.units.progrweb.entities.attori.Attore;
import it.units.progrweb.entities.attori.nonAdministrator.consumer.Consumer;
import it.units.progrweb.entities.attori.nonAdministrator.consumer.ConsumerProxy;
import it.units.progrweb.entities.attori.nonAdministrator.uploader.Uploader;
import it.units.progrweb.persistence.DatabaseHelper;
import it.units.progrweb.persistence.NotFoundException;
import it.units.progrweb.utils.Autenticazione;
import it.units.progrweb.utils.Logger;
import it.units.progrweb.utils.UtilitaGenerale;
import org.glassfish.jersey.media.multipart.FormDataParam;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Classe per la gestione dei {@link Consumer} da parte
 * degli {@link Uploader}.
 * @author Matteo Ferfoglia
 */
@Path("/uploader")
public class GestioneConsumer {

    /** Riconosce l'{@link Uploader} da cui proviene la richiesta
     * e cerca nel database i {@link Consumer} per i quali questo
     * {@link Uploader} ha caricato dei file, quindi restituisce
     * l'array dei Consumer associati.
     */
    @Path("/elencoConsumer")
    @GET
    @Produces( MediaType.APPLICATION_JSON )
    public List<Long> getElencoConsumerDiUploader(@Context HttpServletRequest httpServletRequest) {

        // TODO : verificare correttezza

        // TODO : fare refactoring (se necessario): c'è un metodo molto simile in consumer.RichiestaInfoSuUploader

        Long identificativoUploader = Autenticazione.getIdentificativoAttoreDaTokenAutenticazione(httpServletRequest);

        return RelazioneUploaderConsumerFile.getListaConsumerDiUploader( identificativoUploader );

    }

    /** Restituisce un array di username di Consumer inizianti
     * con i caratteri dati nel @PathParam.*/
    @Path("/ricercaConsumer/{caratteriInizialiUsername}")
    @GET
    @Produces( MediaType.APPLICATION_JSON )
    public String[] ricercaConsumerDaInizialiUsername( @PathParam("caratteriInizialiUsername") String caratteriInizialiUsername ) {

        // TODO verificare

        final int MAX_NUMERO_RISULTATI_DA_RESTITUIRE = 10;

        if( caratteriInizialiUsername.length() > 0 ) {

            // Definizione del range di username compatibili con la ricerca
            final String primoUsernameValido    = caratteriInizialiUsername.toLowerCase();

            // Costruzione primo username non valido
            char[] caratteri = caratteriInizialiUsername.toCharArray();
            caratteri[caratteri.length-1] = (char) (caratteri[caratteri.length-1] + 1); // ultimo carattere incrementato
            final String primoUsernameNONValido = String.copyValueOf( caratteri ).toLowerCase();

            // Ricerca nel database
            List<String> suggerimenti = DatabaseHelper.queryAnd(
                            Consumer.class, MAX_NUMERO_RISULTATI_DA_RESTITUIRE,
                            Consumer.getNomeFieldUsernameAttore(), DatabaseHelper.OperatoreQuery.MAGGIOREOUGUALE, primoUsernameValido,
                            Consumer.getNomeFieldUsernameAttore(), DatabaseHelper.OperatoreQuery.MINORE, primoUsernameNONValido
                    )
                    .stream()
                    .map( consumer -> ((Consumer)consumer).getUsername() )
                    .collect(Collectors.toList());

            String[] daRestituire = new String[suggerimenti.size()];
            return suggerimenti.toArray(daRestituire);

        } else {
            return new String[]{};
        }


    }

    /** Dato l'identificativo di un {@link Consumer} come @PathParam
     * restituisce l'oggetto JSON in cui ogni proprietà dell'oggetto
     * rappresenta un attributo di quel {@link Consumer}.
     */
    @Path("/proprietaConsumer/{identificativoConsumer}")
    @GET
    @Produces( MediaType.APPLICATION_JSON )
    public Consumer getConsumer(@PathParam("identificativoConsumer") Long identificativoConsumer) {

        // TODO : verificare correttezza

        // TODO : fare refactoring (se necessario): c'è un metodo molto simile in consumer.RichiestaInfoSuUploader

        return Consumer.getAttoreDaIdentificativo(identificativoConsumer);
        /*
        // TODO : cancellare questa parte
        Map<String,?> mappaProprieta_nome_valore;
        if( consumer != null )
            mappaProprieta_nome_valore = consumer.getMappaAttributi_Nome_Valore();
        else mappaProprieta_nome_valore = new HashMap<>(0);

        return UtilitaGenerale.rispostaJsonConMappa(mappaProprieta_nome_valore);

         */

    }

    /** Creazione di un {@link Consumer} che deve essere già registrato
     * in questa piattaforma. Se la richiesta va a buon fine, restituisce
     * l'identificativo del Consumer creato.*/
    @Path("/aggiungiConsumerPerQuestoUploader")
    @POST
    @Consumes( MediaType.APPLICATION_JSON )
    public Response aggiungiConsumer(ConsumerProxy consumerDaAggiungere,
                                     @Context HttpServletRequest httpServletRequest) {

        // TODO : rivedere ed eventualmente semplificare questo metodo
        // TODO : metodo simile in ModificaInformazioniAttore

        String nomeAttributoIdentificativoAttore = "identificativoAttore";  // campo non presente nei dati della richiesta
                                                                            // Sarà null nell'istanza deserializzata, prima però verifico che esista nella classe

        if( ! UtilitaGenerale.esisteAttributoInClasse( nomeAttributoIdentificativoAttore, Attore.class ) ) {
            Logger.scriviEccezioneNelLog(this.getClass(), new NoSuchFieldException());
            return Response.serverError().build();
        }

        // Verifica corretta deserializzazione degli attributi
        Field[] attributi = Arrays.stream(Attore.class.getDeclaredFields())
                                  .filter( field -> ! field.getName().equals(nomeAttributoIdentificativoAttore) )
                                  .toArray(Field[]::new);

        for ( Field attributo : attributi ) {
            attributo.setAccessible(true);
            try {
                if( attributo.get(consumerDaAggiungere) == null ) {
                    // Errore di deserializzazione
                    return Response.status( Response.Status.BAD_REQUEST )
                                   .entity( "Valori di input inseriti non validi." )
                                   .build();
                }
            } catch (IllegalAccessException exception) {}
        }

        String usernameConsumerDaAggiungere = consumerDaAggiungere.getUsername();
        Long identificativoUploader = Autenticazione.getIdentificativoAttoreDaTokenAutenticazione( httpServletRequest );

        try {

            // Verifica se il Consumer esiste nella piattaforma (altrimenti eccezione)
            Consumer consumerDalDB = Consumer.getAttoreDaUsername(usernameConsumerDaAggiungere);

            if( consumerDalDB != null &&
                    consumerDalDB.equals(consumerDaAggiungere) ) {

                // Verifica che il Consumer NON sia già associato all'Uploader della richiesta (altrimenti non serve aggiungerlo di nuovo)
                if (!RelazioneUploaderConsumerFile.isConsumerServitoDaUploader(identificativoUploader, consumerDalDB.getIdentificativoAttore())) {

                    // SE precedenti controlli ok, ALLORA aggiungi il consumer
                    RelazioneUploaderConsumerFile.aggiungiConsumerAdUploader(consumerDalDB.getIdentificativoAttore(), identificativoUploader);

                    return Response
                            .ok()
                            .entity(consumerDalDB.getIdentificativoAttore())
                            .build();

                } else {
                    return Response
                            .status(Response.Status.BAD_REQUEST)
                            .entity("Il Consumer " + consumerDalDB.getUsername() + " è già associato all'Uploader che ne ha fatto richiesta.")
                            .build();
                }

            } else {
                throw new NotFoundException();
            }

        } catch (Exception e) {
            return Response
                    .status(Response.Status.BAD_REQUEST )
                    .entity("Il Consumer richiesto non è registrato nella piattaforma.")    // TODO : var ambienTe con messaggi errore
                    .build();
        }

    }


    /** Eliminazione di un {@link Consumer} dalla lista di quelli serviti
     * dall'Uploader che ne ha fatto richiesta. */
    @Path("/cancellaConsumerPerQuestoUploader/{identificativoConsumerDaEliminare}")
    @DELETE
    public Response eliminaConsumer(@PathParam("identificativoConsumerDaEliminare") Long identificativoConsumerDaEliminare,
                                    @Context HttpServletRequest httpServletRequest) {

        // TODO : che succede se il consumer che si sta cercando di eliminare non esiste ?? aggiungere controllo

        Long identificativoUploader = Autenticazione.getIdentificativoAttoreDaTokenAutenticazione(httpServletRequest);
        RelazioneUploaderConsumerFile.dissociaConsumerDaUploader(identificativoConsumerDaEliminare, identificativoUploader);

        return Response
                   .status( Response.Status.OK )// Fonte (200 nella risposta): https://tools.ietf.org/html/rfc7231#section-4.3.5
                   .entity("Consumer eliminato")    // TODO : var ambiene con messaggi errore
                   .build();


    }

    /** Modifica di un {@link Consumer} dalla lista di quelli serviti
     * dall'Uploader che ne ha fatto richiesta. */
    @Path("/modificaConsumer")
    @POST   // non si è usato PUT perché l'url identifica il servizio che gestisce la modifica della risorsa
            // Fonte (https://tools.ietf.org/html/rfc7231#section-4.3.3):
            // The target resource in a POST request is intended to handle the
            //   enclosed representation according to the resource's own semantics,
            //   whereas the enclosed representation in a PUT request is defined as
            //   replacing the state of the target resource.
        // TODO : valutare se sostituire con metodo PUT
    @Consumes( MediaType.MULTIPART_FORM_DATA )
    public Response modificaConsumer(@FormDataParam("username")   String nuovoUsername,
                                     @FormDataParam("nominativo") String nuovoNominativo,
                                     @FormDataParam("email")      String nuovaEmail,
                                     @FormDataParam("identificativoAttore") Long identificativoConsumerDaModificare,
                                     @Context HttpServletRequest  httpServletRequest) {

        if( identificativoConsumerDaModificare != null ) {
            Long identificativoUploader = Autenticazione.getIdentificativoAttoreDaTokenAutenticazione(httpServletRequest);

            if( RelazioneUploaderConsumerFile.
                    isConsumerServitoDaUploader(identificativoUploader,
                            identificativoConsumerDaModificare) ) {

                Consumer consumer_attualmenteSalvatoInDB =
                        (Consumer) Attore.getAttoreDaIdentificativo( identificativoConsumerDaModificare );

                if( consumer_attualmenteSalvatoInDB != null ) {

                    Consumer consumer_conModificheRichiesteDalClient = (Consumer) consumer_attualmenteSalvatoInDB.clone();
                    consumer_conModificheRichiesteDalClient.setNominativo(nuovoNominativo);
                    consumer_conModificheRichiesteDalClient.setEmail(nuovaEmail);
                    consumer_attualmenteSalvatoInDB.setUsername(nuovoUsername);

                    return GestioneAttori.modificaAttore_metodoStatico( consumer_conModificheRichiesteDalClient,
                                                                        consumer_attualmenteSalvatoInDB         );

                } else {
                    Logger.scriviEccezioneNelLog( GestioneConsumer.class,
                                                 "Consumer [" + identificativoConsumerDaModificare + "] " +
                                                     "collegato ad un Uploader ma non trovato nel sistema.",
                                                 new NotFoundException() );

                    return Response.serverError().build();

                }

            } else {

                return Response
                        .status( Response.Status.BAD_REQUEST )
                        .entity("Consumer non gestito dall'Uploader che ne ha richiesto la modifica.")    // TODO : var ambiene con messaggi errore
                        .build();

            }
        } else {
            return Response
                    .status( Response.Status.BAD_REQUEST )
                    .entity("Specirficare l'identificativo del Consumer da specificare.")    // TODO : var ambiene con messaggi errore
                    .build();
        }

    }


    /** Dato l'identificativo del consumer come @PathParam, restituisce il nome.*/
    @Path("/nomeConsumer/{identificativoConsumer}")
    @GET
    @Produces( MediaType.TEXT_PLAIN )
    public String getNomeUploader( @PathParam("identificativoConsumer") Long identificativoConsumer ) {

        return Consumer.getNominativoDaIdentificativo( identificativoConsumer );

    }

}
