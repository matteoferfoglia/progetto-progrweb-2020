package it.units.progrweb.api.consumer;

import it.units.progrweb.entities.RelazioneUploaderConsumerFile;
import it.units.progrweb.entities.attori.nonAdministrator.consumer.Consumer;
import it.units.progrweb.entities.attori.nonAdministrator.uploader.Uploader;
import it.units.progrweb.entities.file.File;
import it.units.progrweb.utils.Autenticazione;
import it.units.progrweb.utils.UtilitaGenerale;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
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
public class RichiestaInfoSuUploader {


    // TODO : probabilmente questa classe deve essere cancellata perché (quasi) inutile


    /** Restituisce un array con gli identificativi di tutti gli {@link Uploader}
     * che hanno caricato dei {@link File} per questo {@link Consumer}.*/
    @Path("/elencoUploader")
    @GET
    @Produces( MediaType.APPLICATION_JSON )
    public long[] getElencoIdentificativiUploaderCheHannoCaricatoFilePerQuestoConsumer(@Context HttpServletRequest httpServletRequest) {

        Long identificativoQuestoConsumer =
                Autenticazione.getIdentificativoAttoreDaTokenAutenticazione(httpServletRequest);

        return RelazioneUploaderConsumerFile.getOccorrenzeFiltratePerConsumer( identificativoQuestoConsumer )
                                            .stream()
                                            .mapToLong( RelazioneUploaderConsumerFile::getIdentificativoUploader )
                                            .toArray();

    }


    /** In base all'header della richiesta, capisce da quale
     * Consumer essa provenga, quindi cerca tutti gli Uploader
     * che hanno caricato almeno un documento per quel Consumer
     * e li restituisce in un oggetto JSON in cui ogni property
     * ha come nome l'identificativo dell'Uploader e come
     * valore l'array contenente gli identificativi di tutti i
     * file caricati dall'Uploader identificato dal nome della
     * property e destinati al Consumer da cui proviene la
     * richiesta.*/
    @Path("/mappaId-uploader-files")        // TODO : variabile d'ambiente
    @GET
    // Response costruita senza @Produces per serializzare i dati in modo personalizzato
    public Response getMappa_idUploader_fileInviatiAlConsumer(@Context HttpServletRequest httpServletRequest) {

        // TODO : probabilmente questo servizio è da eliminare

        // TODO : verificare correttezza
        // TODO : rivedere : che cosa va qua e che cosa va nel metodo che restituisce l'elenco degli uploader

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

}
