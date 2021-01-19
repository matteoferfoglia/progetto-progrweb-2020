package it.units.progrweb.api.consumer;

import it.units.progrweb.entities.RelazioneUploaderConsumerFile;
import it.units.progrweb.entities.attori.nonAdministrator.consumer.Consumer;
import it.units.progrweb.entities.attori.nonAdministrator.uploader.Uploader;
import it.units.progrweb.persistence.DatabaseHelper;
import it.units.progrweb.persistence.NotFoundException;
import it.units.progrweb.utils.Autenticazione;
import it.units.progrweb.utils.UtilitaGenerale;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Map;

/**
 * Servizi per richiedere le informazioni sugli Uploader.
 * @author Matteo Ferfoglia
 */
@Path("/consumer")                          // TODO : variabile d'ambiente
public class RichiestaInfoSuUploader {

    /** Dato come @PathParam l'identificativo di un Uploader, restituisce
     * la rappresentazione codificata in base64 del suo logo.*/
    @Path("/logoUploader/{identificativoUploader}")
    @GET
    public Response getLogoUploaderBase64(@PathParam("identificativoUploader") Long identificativoUploader) {

        // TODO : verificare - va bene così senza mediatype ? Il browser che riceve? L'entity nel NOT_FOUND?

        try {
            Uploader uploader = (Uploader) DatabaseHelper.getById( identificativoUploader, Uploader.class );
            return Response.ok()
                           .entity( uploader.getImmagineLogoBase64() )
                           .build();

        } catch (NotFoundException notFoundException) {
            return NotFoundException.creaResponseNotFound("Uploader non trovato.");
        }

    }


    /** In base all'header della richiesta, capisce da quale
     * Consumer essa provenga, quindi cerca tutti gli Uploader
     * che hanno caricato almeno un documento per quel Consumer
     * e li restituisce in un oggetto JSON in cui ogni property
     * ha come nome l'identificativo dell'Uploader e come
     * valore l'array contenente gli identificativi di tutti i
     * file caricati dall'Uploader identificato dal nome della
     * property e destinati al Consumer da cui proviene la
     * richiesta.
     */
    @Path("/mappaId-uploader-files")        // TODO : variabile d'ambiente
    @GET
    // Response costruita senza @Produces per serializzare i dati in modo personalizzato
    public Response getMappa_idUploader_fileInviatiAlConsumer(@Context HttpServletRequest httpServletRequest) {

        // TODO : verificare correttezza

        Consumer consumer = (Consumer) Autenticazione.getAttoreDaHttpServletRequest(httpServletRequest);
        Map<Long, Long[]> mappa_idUploader_arrayIdFileCaricatiDaUploaderPerQuestoConsumer = null;

        if ( consumer != null) {
            List<RelazioneUploaderConsumerFile> risultatoQuery =
                    RelazioneUploaderConsumerFile.getOccorrenzeFiltratePerConsumer(consumer.getIdentificativoAttore());

           mappa_idUploader_arrayIdFileCaricatiDaUploaderPerQuestoConsumer =
                    RelazioneUploaderConsumerFile.mappa_identificativoUploader_arrayIdFile(risultatoQuery);

        }

        return UtilitaGenerale.rispostaJsonConMappa(mappa_idUploader_arrayIdFileCaricatiDaUploaderPerQuestoConsumer);

    }

    /** Dato l'identificativo di un Uploader, restituisce l'oggetto JSON
     * con le properties di quell'Uploader.*/
    @Path("/proprietaUploader/{identificativoUploader}")        // TODO : variabile d'ambiente
    @GET
    // Produces omesso perché la serializzazione in JSON è personalizzata
    public Response getUploader(@PathParam("identificativoUploader") Long identificativoUploader) {

        Uploader uploader = Uploader.cercaUploaderDaIdentificativo(identificativoUploader);
        Map<String,?> mappaProprietaUploader_nome_valore = uploader.getMappaAttributi_Nome_Valore();
        return UtilitaGenerale.rispostaJsonConMappa(mappaProprietaUploader_nome_valore);

    }

}
