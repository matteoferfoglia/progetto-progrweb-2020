package it.units.progrweb.api.uploader;

import it.units.progrweb.entities.RelazioneUploaderConsumerFile;
import it.units.progrweb.entities.attori.Attore;
import it.units.progrweb.entities.attori.nonAdministrator.consumer.Consumer;
import it.units.progrweb.entities.attori.nonAdministrator.consumer.ConsumerProxy;
import it.units.progrweb.entities.attori.nonAdministrator.uploader.Uploader;
import it.units.progrweb.persistence.DatabaseHelper;
import it.units.progrweb.persistence.NotFoundException;
import it.units.progrweb.utils.Autenticazione;
import it.units.progrweb.utils.UtilitaGenerale;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
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
    public List<String> getElencoConsumerDiUploader(@Context HttpServletRequest httpServletRequest) {

        // TODO : verificare correttezza

        // TODO : fare refactoring (se necessario): c'è un metodo molto simile in consumer.RichiestaInfoSuUploader

        String usernameUploader = Autenticazione.getUsernameAttoreDaTokenAutenticazione(httpServletRequest);

        return RelazioneUploaderConsumerFile.getListaConsumerDiUploader( usernameUploader );

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
                            Consumer.getNomeFieldUsernameConsumer(), DatabaseHelper.OperatoreQuery.MAGGIOREOUGUALE, primoUsernameValido,
                            Consumer.getNomeFieldUsernameConsumer(), DatabaseHelper.OperatoreQuery.MINORE, primoUsernameNONValido
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

    /** Dato lo username di un {@link Consumer} come @PathParam
     * restituisce l'oggetto JSON in cui ogni proprietà dell'oggetto
     * rappresenta un {@link Consumer} ed il corrispettivo valore è
     * un oggetto con le proprietà di quel {@link Consumer}.
     */
    @Path("/proprietaConsumer/{usernameConsumer}")
    @GET
    // Risposta costruita in modo personalizzato
    public Response getConsumer(@PathParam("usernameConsumer") String usernameConsumer) {

        // TODO : verificare correttezza

        // TODO : fare refactoring (se necessario): c'è un metodo molto simile in consumer.RichiestaInfoSuUploader

        Consumer consumer = Consumer.cercaConsumerDaUsername(usernameConsumer);
        Map<String,?> mappaProprietaUploader_nome_valore = consumer.getMappaAttributi_Nome_Valore();
        return UtilitaGenerale.rispostaJsonConMappa(mappaProprietaUploader_nome_valore);

    }

    /** Creazione di un {@link Consumer} che deve essere già registrato
     * in questa piattaforma.*/
    @Path("/aggiungiConsumerPerQuestoUploader")
    @POST
    @Consumes( MediaType.APPLICATION_JSON )
    public Response aggiungiConsumer(ConsumerProxy consumerDaAggiungere,
                                     @Context HttpServletRequest httpServletRequest) {

        // Verifica corretta deserializzazione degli attributi
        Field[] attributi = Attore.class.getDeclaredFields();
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
        String usernameUploader = Autenticazione.getUsernameAttoreDaTokenAutenticazione( httpServletRequest );

        try {

            // Verifica se il Consumer esiste nella piattaforma (altrimenti eccezione)
            Consumer consumerDalDB = (Consumer) DatabaseHelper.getById(usernameConsumerDaAggiungere, Consumer.class);

            if( consumerDalDB != null &&
                    consumerDalDB.equals(consumerDaAggiungere) ) {

                // Verifica che il Consumer NON sia già associato all'Uploader della richiesta (altrimenti non serve aggiungerlo di nuovo)
                if (!RelazioneUploaderConsumerFile.isConsumerServitoDaUploader(usernameUploader, consumerDaAggiungere.getUsername())) {

                    // SE precedenti controlli ok, ALLORA aggiungi il consumer
                    RelazioneUploaderConsumerFile.aggiungiConsumerAdUploader(consumerDaAggiungere.getUsername(), usernameUploader);

                    return Response
                            .status(Response.Status.NO_CONTENT)// Fonte ("204 No Content" nella risposta, "to indicate successful completion of the request"): https://tools.ietf.org/html/rfc7231#section-4.3.3
                            .entity("Consumer " + usernameConsumerDaAggiungere + " aggiunto.")    // TODO : var ambiene con messaggi
                            .build();

                } else {
                    return Response
                            .status(Response.Status.BAD_REQUEST)
                            .entity("Il Consumer " + usernameConsumerDaAggiungere + " è già associato all'Uploader " + usernameUploader + ".")
                            .build();
                }

            } else {
                throw new NotFoundException();
            }

        } catch (NotFoundException e) {
            return Response
                    .status(Response.Status.BAD_REQUEST )
                    .entity("Il Consumer " + usernameConsumerDaAggiungere + " non è registrato nella piattaforma.")    // TODO : var ambiene con messaggi errore
                    .build();
        }

    }


    /** Eliminazione di un {@link Consumer} dalla lista di quelli serviti
     * dall'Uploader che ne ha fatto richiesta. */
    @Path("/cancellaConsumerPerQuestoUploader/{usernameConsumerDaEliminare}")
    @DELETE
    public Response eliminaConsumer(@PathParam("usernameConsumerDaEliminare") String usernameConsumerDaEliminare,
                                    @Context HttpServletRequest httpServletRequest) {


        String usernameUploader = Autenticazione.getUsernameAttoreDaTokenAutenticazione(httpServletRequest);
        RelazioneUploaderConsumerFile.dissociaConsumerDaUploader(usernameConsumerDaEliminare, usernameUploader);

        return Response
                   .status( Response.Status.OK )// Fonte (200 nella risposta): https://tools.ietf.org/html/rfc7231#section-4.3.5
                   .entity("Consumer " + usernameConsumerDaEliminare + "eliminato")    // TODO : var ambiene con messaggi errore
                   .build();


    }


    /** Dato lo username del consumer come @PathParam, restituisce il nome.*/
    @Path("/nomeConsumer/{usernameConsumer}")
    @GET
    @Produces( MediaType.TEXT_PLAIN )
    public String getNomeUploader( @PathParam("usernameConsumer") String usernameConsumer ) {

        return Consumer.getNominativoDaUsername( usernameConsumer );

    }

}
