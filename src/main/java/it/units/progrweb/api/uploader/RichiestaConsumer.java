package it.units.progrweb.api.uploader;

import it.units.progrweb.entities.RelazioneUploaderConsumerFile;
import it.units.progrweb.entities.attori.Attore;
import it.units.progrweb.entities.attori.nonAdministrator.uploader.Uploader;
import it.units.progrweb.entities.attori.nonAdministrator.consumer.Consumer;
import it.units.progrweb.utils.Autenticazione;
import it.units.progrweb.utils.UtilitaGenerale;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Map;

/**
 * Classe per richiedere informazioni sui Consumer.
 * @author Matteo Ferfoglia
 */
@Path("/uploader")
public class RichiestaConsumer {

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

        Attore attore = Autenticazione.getAttoreDaHttpServletRequest(httpServletRequest);
        if( attore instanceof Uploader ) {

            Uploader consumer = (Uploader) attore;

            List<RelazioneUploaderConsumerFile> risultatoQuery =
                    RelazioneUploaderConsumerFile.getOccorrenzeFiltratePerUploader(consumer.getIdentificativoAttore());

            Map<Long, Long[]> mappa_idConsumer_arrayIdFileCaricatiPerConsumerDaQuestoUploader =
                    RelazioneUploaderConsumerFile.mappa_idConsumer_arrayIdFile(risultatoQuery);

            return UtilitaGenerale.rispostaJsonConMappa(mappa_idConsumer_arrayIdFileCaricatiPerConsumerDaQuestoUploader);

        } else {
            return Autenticazione.creaResponseForbidden("Servizio riservato agli Uploader autenticati.");
        }

    }

}
