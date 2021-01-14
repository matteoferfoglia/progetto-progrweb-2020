package it.units.progrweb.api.uploader;

import it.units.progrweb.entities.RelazioneUploaderConsumerFile;
import it.units.progrweb.entities.attori.Attore;
import it.units.progrweb.entities.attori.nonAdministrator.consumer.Consumer;
import it.units.progrweb.entities.attori.nonAdministrator.uploader.Uploader;
import it.units.progrweb.persistence.DatabaseHelper;
import it.units.progrweb.utils.Autenticazione;
import it.units.progrweb.utils.UtilitaGenerale;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Map;

/**
 * Classe per la gestione dei {@link Consumer} da parte
 * degli {@link Uploader}.
 * @author Matteo Ferfoglia
 */
@Path("/uploader")
public class GestioneConsumer {

    /** Riconosce l'{@link Uploader} da cui proviene la richiesta
     * e cerca nel database i {@link Consumer} per i quali questo
     * {@link Uploader} ha carivato dei file, quindi restituisce
     * una mappa { idConsumer => [idfilesCaricatiPerQuestoConsumer] }.
     */
    @Path("/mappaId-consumer-files")
    @GET
    // Risposta costruita in modo personalizzato
    public Response getMappa_idConsumer_fileInviatiAlConsumer(@Context HttpServletRequest httpServletRequest) {

        // TODO : verificare correttezza

        // TODO : fare refactoring (se necessario): c'è un metodo molto simile in consumer.RichiestaUploader

        Uploader uploader;
        if( ( uploader = isUploader(httpServletRequest) ) != null ) {

            List<RelazioneUploaderConsumerFile> risultatoQuery =
                    RelazioneUploaderConsumerFile.getOccorrenzeFiltratePerUploader(uploader.getIdentificativoAttore());

            Map<Long, Long[]> mappa_idConsumer_arrayIdFileCaricatiPerConsumerDaQuestoUploader =
                    RelazioneUploaderConsumerFile.mappa_idConsumer_arrayIdFile(risultatoQuery);

            return UtilitaGenerale.rispostaJsonConMappa(mappa_idConsumer_arrayIdFileCaricatiPerConsumerDaQuestoUploader);

        } else {
            return Autenticazione.creaResponseForbidden("Servizio riservato agli Uploader autenticati.");
        }

    }

    /** Dato l'identificativo di un {@link Consumer} come @PathParam
     * restituisce l'oggetto JSON in cui ogni proprietà dell'oggetto
     * rappresenta un {@link Consumer} ed il corrispettivo valore è
     * un oggetto con le proprietà di quel {@link Consumer}.
     */
    @Path("/proprietaConsumer/{identificativoConsumer}")
    @GET
    // Risposta costruita in modo personalizzato
    public Response getConsumer(@PathParam("identificativoConsumer") Long identificativoConsumer) {

        // TODO : verificare correttezza

        // TODO : fare refactoring (se necessario): c'è un metodo molto simile in consumer.RichiestaUploader

        Consumer consumer = Consumer.cercaConsumerById(identificativoConsumer);
        Map<String,?> mappaProprietaUploader_nome_valore = consumer.getMappaAttributi_Nome_Valore();
        return UtilitaGenerale.rispostaJsonConMappa(mappaProprietaUploader_nome_valore);

    }

    /** Eliminazione di un Consumer dalla lista di quelli serviti
     * dall'Uploader che ne ha fatto richiesta. */
    @Path("/cancellaConsumerPerQuestoUploader/{identificativoConsumerDaEliminare}")
    @DELETE
    public Response eliminaConsumer(@PathParam("identificativoConsumerDaEliminare") Long identificativoConsumerDaEliminare,
                                    @Context HttpServletRequest httpServletRequest) {

        Uploader uploader;
        if( ( uploader = isUploader(httpServletRequest) ) != null ) {

            List<RelazioneUploaderConsumerFile> occorrenzeDaEliminare =
                    RelazioneUploaderConsumerFile.getOccorrenzeFiltratePerUploaderEConsumer(uploader.getIdentificativoAttore(), identificativoConsumerDaEliminare);

            DatabaseHelper.cancellaEntita(occorrenzeDaEliminare);

            // TODO : elimare la relazione tra consumer ed uploader

            return Response.ok()
                           .entity("Consumer " + identificativoConsumerDaEliminare + "eliminato")
                           .build();

        } else {
            return Autenticazione.creaResponseForbidden("Servizio riservato agli Uploader autenticati.");
        }

    }

    /** Estrae l'attore dalla richiesta e se si tratta di un
     * {@link Consumer} lo restituisce, altrimenti restituisce
     * null.*/
    private static Uploader isUploader(HttpServletRequest httpServletRequest){

        Attore attore = Autenticazione.getAttoreDaHttpServletRequest(httpServletRequest);

        if( attore instanceof Uploader )
            return (Uploader) attore;
        else
            return null;

    }


    /** Con riferimento a {@link #getConsumer(Long)}, questo metodo restituisce
     * il nome della proprietà contenente il nome del {@link Consumer}.*/
    @Path("/nomeProprietaNomeUploader")        // TODO : variabile d'ambiente
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String getNomeFieldNomeConsumer() {
        return Consumer.getNomeFieldNomeConsumer();
    }
    // TODO : metodi analoghi in RichiestaUploader ... refactoring?

    /** Con riferimento a {@link #getConsumer(Long)}, questo metodo restituisce
     * il nome della proprietà contenente l'email del {@link Consumer}.*/
    @Path("/nomeProprietaEmailUploader")        // TODO : variabile d'ambiente
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String getNomeFieldLogoConsumer() {
        return Consumer.getNomeFieldEmailConsumer();
    }
    // TODO : metodi analoghi in RichiestaUploader ... refactoring?

    /** Con riferimento a {@link #getConsumer(Long)}, questo metodo restituisce
     * il nome della proprietà contenente lo username del {@link Consumer}.*/
    @Path("/nomeProprietaUsernamelUploader")        // TODO : variabile d'ambiente
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String getNomeFieldUsernameConsumer() {
        return Consumer.getNomeFieldUsernameConsumer();
    }
    // TODO : metodi analoghi in RichiestaUploader ... refactoring?

}
