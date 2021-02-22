package it.units.progrweb.api;

import it.units.progrweb.api.uploader.GestioneDocumenti;
import it.units.progrweb.entities.attori.Attore;
import it.units.progrweb.entities.attori.nonAdministrator.uploader.Uploader;
import it.units.progrweb.utils.Autenticazione;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import static it.units.progrweb.api.uploader.GestioneConsumer.associaConsumerAdUploader;

/**
 * Classe che implementa i web services richiesti.
 * @author Matteo Ferfoglia
 */
@Path("/webService")
public class WebService {

    /** Questo metodo ha le stesse finalità di
     * {@link GestioneDocumenti#uploadFile(HttpServletRequest, InputStream, FormDataContentDisposition, Long, String, String)},
     * con la differenza che questo metodo riceve come parametro il nomeCognome e codice fiscale di un
     * Consumer anziché il suo identificativo.*/
    @Path("/uploadDocumento")
    @POST
    @Consumes( MediaType.MULTIPART_FORM_DATA )
    public Response uploadFile(@Context HttpServletRequest httpServletRequest,
                               @FormDataParam("usernameUploader")                   String usernameUploader,
                               @FormDataParam("passwordUploader")                   String passwordUploader,
                               @FormDataParam("contenutoFile")                      InputStream contenuto,
                               @FormDataParam("contenutoFile")                      FormDataContentDisposition dettagliFile,
                               @FormDataParam("nomeCognomeConsumerDestinatario")    String nomeCognomeConsumerDestinatario,
                               @FormDataParam("codiceFiscaleConsumerDestinatario")  String codiceFiscaleConsumerDestinatario,
                               @FormDataParam("emailConsumerDestinatario")          String emailConsumerDestinatario,
                               @FormDataParam("nomeFile")                           String nomeFile,
                               @FormDataParam("listaHashtag")                       String listaHashtag ) {

    Attore mittente = Autenticazione.getAttoreDaCredenziali(usernameUploader, passwordUploader);
    if(!(mittente instanceof Uploader)) // check autenticazione uploader mittente
        return Autenticazione.creaResponseUnauthorized();

        try {

            // Non crea nulla se il consumer esiste già
            CreazioneAttore.CampiFormAggiuntaAttore campiFormAggiuntaAttore
                    = associaConsumerAdUploader( new CreazioneAttore.CampiFormAggiuntaAttore(
                                                                   codiceFiscaleConsumerDestinatario,
                                                                   null,
                                                                   nomeCognomeConsumerDestinatario,
                                                                   emailConsumerDestinatario,
                                                                   Attore.TipoAttore.Consumer.getTipoAttore(),
                                                                   null),
                                                 mittente.getIdentificativoAttore() );
            return GestioneDocumenti.uploadFile(httpServletRequest, contenuto, dettagliFile, nomeFile, listaHashtag,
                    mittente.getIdentificativoAttore(), campiFormAggiuntaAttore.getIdentificativoAttore());

        } catch ( MessagingException | NoSuchAlgorithmException |
                  InvalidKeyException | UnsupportedEncodingException e ) {
            return Response.serverError().entity(e).build();
        }
    }

}
