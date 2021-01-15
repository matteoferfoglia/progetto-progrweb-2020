package it.units.progrweb.api.consumer;

import it.units.progrweb.entities.RelazioneUploaderConsumerFile;
import it.units.progrweb.entities.attori.Attore;
import it.units.progrweb.entities.attori.nonAdministrator.consumer.Consumer;
import it.units.progrweb.entities.attori.nonAdministrator.uploader.Uploader;
import it.units.progrweb.utils.Autenticazione;
import it.units.progrweb.utils.UtilitaGenerale;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Map;

/**
 * Servizi per richiedere le informazioni sugli Uploader.
 * @author Matteo Ferfoglia
 */
@Path("/consumer")                          // TODO : variabile d'ambiente
public class RichiestaUploader {



    /** In base all'header della richiesta, capisce da quale
     * Consumer essa provenga, quindi cerca tutti gli Uploader
     * che hanno caricato almeno un documento per quel Consumer
     * e li restituisce in un oggetto JSON in cui ogni property
     * ha come nome lo username dell'Uploader e come
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

        Attore attore = Autenticazione.getAttoreDaHttpServletRequest(httpServletRequest);
        if( attore instanceof Consumer ) {

            Consumer consumer = (Consumer) attore;

            List<RelazioneUploaderConsumerFile> risultatoQuery =
                    RelazioneUploaderConsumerFile.getOccorrenzeFiltratePerConsumer(consumer.getUsername());

            Map<Long, Long[]> mappa_idUploader_arrayIdFileCaricatiDaUploaderPerQuestoConsumer =
                    RelazioneUploaderConsumerFile.mappa_idUploader_arrayIdFile(risultatoQuery);

            return UtilitaGenerale.rispostaJsonConMappa(mappa_idUploader_arrayIdFileCaricatiDaUploaderPerQuestoConsumer);

        } else {
            return Autenticazione.creaResponseForbidden("Servizio riservato ai Consumer autenticati.");
        }

    }

    /** Dato lo username di un Uploader, restituisce l'oggetto JSON
     * con le properties di quell'Uploader.*/
    @Path("/proprietaUploader/{usernameUploader}")        // TODO : variabile d'ambiente
    @GET
    // Produces omesso perché la serializzazione in JSON è personalizzata
    public Response getUploader(@PathParam("usernameUploader") String usernameUploader) {

        Uploader uploader = Uploader.cercaUploaderDaUsername(usernameUploader);
        Map<String,?> mappaProprietaUploader_nome_valore = uploader.getMappaAttributi_Nome_Valore();
        return UtilitaGenerale.rispostaJsonConMappa(mappaProprietaUploader_nome_valore);

    }

    /** Con riferimento a {@link #getUploader(String)}, questo metodo restituisce
     * il nome della proprietà contenente il nome dell'Uploader.*/
    @Path("/nomeProprietaNomeUploader")        // TODO : variabile d'ambiente
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String getNomeFieldNomeUploader() {
        return Uploader.getNomeFieldNomeUploader();
    }

    /** Con riferimento a {@link #getUploader(String)}, questo metodo restituisce
     * il nome della proprietà contenente il logo dell'Uploader.*/
    @Path("/nomeProprietaLogoUploader")        // TODO : variabile d'ambiente
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String getNomeFieldLogoUploader() {
        return Uploader.getNomeFieldLogoUploader();
    }


}
