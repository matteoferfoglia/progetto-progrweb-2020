package it.units.progrweb.api.uploader;

import it.units.progrweb.api.CreazioneAttore;
import it.units.progrweb.entities.RelazioneUploaderConsumer;
import it.units.progrweb.entities.attori.Attore;
import it.units.progrweb.entities.attori.nonAdministrator.consumer.Consumer;
import it.units.progrweb.entities.attori.nonAdministrator.consumer.ConsumerProxy;
import it.units.progrweb.entities.attori.nonAdministrator.uploader.Uploader;
import it.units.progrweb.persistence.DatabaseHelper;
import it.units.progrweb.persistence.NotFoundException;
import it.units.progrweb.utils.Autenticazione;
import it.units.progrweb.utils.Logger;
import org.glassfish.jersey.media.multipart.FormDataParam;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
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

        return RelazioneUploaderConsumer.getListaConsumerDiUploader( identificativoUploader );

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

    }

    /** Associa il {@link ConsumerProxy} passato come parametro all'{@link Uploader}
     * che ha fatto la richiesta. Se il {@link ConsumerProxy} non esiste nel sistema
     * come prima cosa viene creato.
     * Se la richiesta va a buon fine, restituisce l'identificativo del {@link Consumer}
     * creato.*/
    @Path("/aggiungiConsumerPerQuestoUploader")
    @POST
    @Consumes( MediaType.APPLICATION_JSON )
    public Response associaConsumerAdUploader( CreazioneAttore.CampiFormAggiuntaAttore campiFormAggiuntaAttore ,
                                              @Context HttpServletRequest httpServletRequest) {

        // TODO : rivedere ed eventualmente semplificare questo metodo
        // TODO : metodo simile in ModificaInformazioniAttore

        Long identificativoUploader = Autenticazione.getIdentificativoAttoreDaTokenAutenticazione( httpServletRequest );

        try {
            CreazioneAttore.CampiFormAggiuntaAttore consumerAppenaAggiunto
                    = associaConsumerAdUploader(campiFormAggiuntaAttore, identificativoUploader);
            return Response
                    .ok()
                    .entity(consumerAppenaAggiunto.getIdentificativoAttore())
                    .type(MediaType.TEXT_PLAIN)
                    .build();
        } catch ( NoSuchAlgorithmException | InvalidKeyException  |
                  MessagingException | UnsupportedEncodingException e) {
            return Response.serverError().entity("Errore durante la creazione del consumer.").build();
        }


    }

    /** Dato un {@link Consumer} e l'identificativo di un {@link Uploader}, questo metodo
     * associa il {@link Consumer} all'{@link Uploader}. Se il {@link Consumer} è già associato
     * all'{@link Uploader} allora questo metodo non fa nulla.
     * Se il {@link Consumer} specificato non esiste nella piattaforma, come prima cosa lo crea,
     * sfruttando il metodo {@link CreazioneAttore#creaNuovoAttoreECreaResponse(CreazioneAttore.CampiFormAggiuntaAttore, Attore.TipoAttore)}
     * @return L'istanza di {@link CreazioneAttore.CampiFormAggiuntaAttore} rappresentante il
     *          {@link Consumer} appena creato, se l'operazione va a buon fine. */
    public static CreazioneAttore.CampiFormAggiuntaAttore
    associaConsumerAdUploader(CreazioneAttore.CampiFormAggiuntaAttore campiFormAggiuntaAttore,
                              Long identificativoUploader)
            throws MessagingException, NoSuchAlgorithmException,
                   InvalidKeyException, UnsupportedEncodingException {

        // Verifica se il Consumer esiste nella piattaforma
        Consumer consumerDalDB = Consumer.getAttoreDaUsername(campiFormAggiuntaAttore.getUsername());

        if( consumerDalDB==null ) {
            // Creazione del consumer se non esiste già
            try {
                consumerDalDB = (Consumer) CreazioneAttore.creaNuovoAttore( campiFormAggiuntaAttore,
                                                                            Attore.TipoAttore.Uploader );
            } catch ( NoSuchAlgorithmException | InvalidKeyException |
                      UnsupportedEncodingException | MessagingException e ) {
                Logger.scriviEccezioneNelLog(GestioneConsumer.class,
                        "Eccezione durante l'aggiunta di un consumer",
                        e);
                throw e;
            }
        }

        campiFormAggiuntaAttore.setIdentificativoAttore(consumerDalDB.getIdentificativoAttore());

        // Verifica che il Consumer NON sia già associato all'Uploader della richiesta (altrimenti non serve aggiungerlo di nuovo)
        if (!RelazioneUploaderConsumer.isConsumerServitoDaUploader(identificativoUploader, consumerDalDB.getIdentificativoAttore())) {
            // SE precedenti controlli ok, ALLORA aggiungi il consumer
            RelazioneUploaderConsumer.aggiungiConsumerAdUploader(consumerDalDB.getIdentificativoAttore(), identificativoUploader);
        }

        return campiFormAggiuntaAttore;
    }


    /** Eliminazione di un {@link Consumer} dalla lista di quelli serviti
     * dall'Uploader che ne ha fatto richiesta. */
    @Path("/cancellaConsumerPerQuestoUploader/{identificativoConsumerDaEliminare}")
    @DELETE
    public Response eliminaConsumer(@PathParam("identificativoConsumerDaEliminare") Long identificativoConsumerDaEliminare,
                                    @Context HttpServletRequest httpServletRequest) {

        // TODO : che succede se il consumer che si sta cercando di eliminare non esiste ?? aggiungere controllo

        Long identificativoUploader = Autenticazione.getIdentificativoAttoreDaTokenAutenticazione(httpServletRequest);
        RelazioneUploaderConsumer.dissociaConsumerDaUploader(identificativoConsumerDaEliminare, identificativoUploader);

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

            if( RelazioneUploaderConsumer.
                    isConsumerServitoDaUploader(identificativoUploader,
                            identificativoConsumerDaModificare) ) {

                Consumer consumer_attualmenteSalvatoInDB =
                        (Consumer) Attore.getAttoreDaIdentificativo( identificativoConsumerDaModificare );

                if( consumer_attualmenteSalvatoInDB != null ) {

                    Consumer consumer_conModificheRichiesteDalClient = (Consumer) consumer_attualmenteSalvatoInDB.clone();
                    consumer_conModificheRichiesteDalClient.setNominativo(nuovoNominativo);
                    consumer_conModificheRichiesteDalClient.setEmail(nuovaEmail);
                    consumer_attualmenteSalvatoInDB.setUsername(nuovoUsername);

                    return Attore.modificaAttore( consumer_conModificheRichiesteDalClient,
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
