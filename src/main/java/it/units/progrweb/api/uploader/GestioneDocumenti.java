package it.units.progrweb.api.uploader;

import it.units.progrweb.entities.RelazioneUploaderConsumerFile;
import it.units.progrweb.entities.attori.nonAdministrator.consumer.Consumer;
import it.units.progrweb.entities.attori.nonAdministrator.uploader.Uploader;
import it.units.progrweb.entities.file.File;
import it.units.progrweb.persistence.NotFoundException;
import it.units.progrweb.utils.Autenticazione;
import it.units.progrweb.utils.UtilitaGenerale;
import org.glassfish.jersey.media.multipart.FormDataParam;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

/**
 * Classe per permettere ad un {@link Uploader} di gestire
 * i documenti inviati ad un {@link Consumer}.
 * @author Matteo Ferfoglia
 */
@Path("/uploader/documenti")    // TODO : variabile d'ambiente
public class GestioneDocumenti {

    /** Restituisce il documento il cui identificativo è
     * nel @PathParam.*/
    @Path("/downloadDocumento/{identificativoFile}")
    @GET
    // Mediatype indicato nella response
    public Response getFileById(@PathParam("identificativoFile") Long identificativoFile,
                                @Context HttpServletRequest httpServletRequest) {

        return File.creaResponseConFile(identificativoFile, httpServletRequest);

    }


    /** Eliminazione del {@link File} dalla lista di quelli serviti
     * dall'Uploader che ne ha fatto richiesta. */
    @Path("/cancellaConsumerPerQuestoUploader/{idFileDaEliminare}")
    @DELETE
    public Response eliminaFile(@PathParam("idFileDaEliminare") Long idFileDaEliminare,
                                @Context HttpServletRequest httpServletRequest) {

        String usernameUploader = Autenticazione.getUsernameAttoreDaTokenAutenticazione(httpServletRequest);
        try {

            if( RelazioneUploaderConsumerFile.eliminaFileDiUploader(idFileDaEliminare, usernameUploader) )
                return Response
                        .status( Response.Status.OK )// Fonte (200 nella risposta): https://tools.ietf.org/html/rfc7231#section-4.3.5
                        .entity("File " + idFileDaEliminare + "eliminato")    // TODO : var ambiene con messaggi errore
                        .build();
            else return Autenticazione.creaResponseForbidden("Non autorizzato a cancellare il file.");

        } catch (NotFoundException notFoundException) {
            return NotFoundException.creaResponseNotFound("File non trovato.");
        }

    }

    /** Caricamento di un {@link File}. Questo metodo attende dal
     * client un file, il cui nome è specificato come @FormParam,
     * e destinato al {@link Consumer} il cui username è
     * specificato come @FormParam. L'uploader da cui proviene la
     * richiesta viene determinato dagli header della richiesta
     * stessa
     * (<a href="https://docs.oracle.com/javaee/7/api/javax/ws/rs/FormParam.html">Fonte</a>,
     * <a href="https://stackoverflow.com/a/25889454">Fonte</a>).*/
    @Path("/uploadDocumento")
    @POST
    @Consumes( MediaType.MULTIPART_FORM_DATA )
    public Response caricaFile(@Context HttpServletRequest httpServletRequest,
                               @FormDataParam("contenutoFile")                InputStream contenuto,
                               @FormDataParam("usernameConsumerDestinatario") String usernameConsumerDestinatario,
                               @FormDataParam("nomeFile")                     String nomeFile,
                               @FormDataParam("listaHashtag")                 String listaHashtag) {


        List<String> listaHashtag_list = Arrays.asList( listaHashtag.trim().split(", ") );
        String usernameUploader = Autenticazione.getUsernameAttoreDaTokenAutenticazione( httpServletRequest );

        RelazioneUploaderConsumerFile.aggiungiFile( contenuto, nomeFile, listaHashtag_list,
                                                    usernameUploader, usernameConsumerDestinatario );

        return Response.noContent().build();

    }

    /** Restituisce una mappa in cui ogni entry ha per chiave l'identificativo
     * di un {@link File} e come valore corrispondente l'oggetto che rappresenta
     * il {@link File} in termini delle sue proprietà. Nella mappa sono presenti
     * tutti i file caricati dall'{@link Uploader} attualmente autenticato e
     * destinati al {@link Consumer} il cui username è specificato come
     * PathParam.*/
    @Path("/mappa-idFile-propFile/{usernameConsumer}")
    @GET
    public Response getElencoFile(@Context HttpServletRequest httpServletRequest,
                                  @PathParam("usernameConsumer") String usernameConsumer) {

        // TODO rivere (Method not allowed ??)

        String usernameUploader = Autenticazione.getUsernameAttoreDaTokenAutenticazione( httpServletRequest );

        List<File> fileDaRestituire =
                RelazioneUploaderConsumerFile.getListaFileDaUploaderAConsumer( usernameUploader, usernameConsumer );

        return UtilitaGenerale.rispostaJsonConMappa( File.getMappa_idFile_propFile( fileDaRestituire ) );

    }


}
