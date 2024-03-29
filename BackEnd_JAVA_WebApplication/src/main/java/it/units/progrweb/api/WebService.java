package it.units.progrweb.api;

import it.units.progrweb.api.autenticazioneERegistrazione.CampiFormLogin;
import it.units.progrweb.api.uploader.GestioneDocumenti;
import it.units.progrweb.entities.attori.Attore;
import it.units.progrweb.utils.Autenticazione;
import it.units.progrweb.utils.ResponseHelper;
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
import java.util.InputMismatchException;

import static it.units.progrweb.api.uploader.GestioneConsumer.associaConsumerAdUploader;

/**
 * Classe che implementa i web services richiesti.
 * @author Matteo Ferfoglia
 */
@Path("/webService")
public class WebService {

    /** Metodo per eseguire l'autenticazione del client.
     * @return Il token di autenticazione, da presentare nelle
     *          successive richieste a questo WebService.*/
    @Path("/login")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response login( CampiFormLogin campiFormLogin ) {
        return Autenticazione.creaResponseAutenticazione(campiFormLogin.getUsername(), campiFormLogin.getPassword());
    }

    /** Questo metodo ha le stesse finalità di
     * {@link GestioneDocumenti#uploadFile(HttpServletRequest, InputStream, FormDataContentDisposition, Long, String, String)},
     * con la differenza che questo metodo riceve come parametro il nomeCognome e codice fiscale di un
     * Consumer anziché il suo identificativo.*/
    @Path("/uploadDocumento")
    @POST
    @Consumes( MediaType.MULTIPART_FORM_DATA )
    public Response uploadFile(@Context HttpServletRequest httpServletRequest,
                               @FormDataParam("codiceFiscaleConsumerDestinatario")  String codiceFiscaleConsumerDestinatario,
                               @FormDataParam("emailConsumerDestinatario")          String emailConsumerDestinatario,
                               @FormDataParam("nomeCognomeConsumerDestinatario")    String nomeCognomeConsumerDestinatario,
                               @FormDataParam("nomeFile")                           String nomeFile,
                               @FormDataParam("listaHashtag")                       String listaHashtag,
                               @FormDataParam("contenutoFile")                      InputStream contenuto,
                               @FormDataParam("contenutoFile")                      FormDataContentDisposition dettagliFile ) {

        // Autorizzazione verificata da filtro Uploader
        Attore mittente = Autenticazione.getAttoreDaDatabase(httpServletRequest);

        try {

            // Non crea nulla se il consumer esiste già
            CreazioneAttore.CampiFormAggiuntaAttore campiFormAggiuntaAttore
                    = associaConsumerAdUploader(
                            httpServletRequest,
                            new CreazioneAttore.CampiFormAggiuntaAttore(
                                codiceFiscaleConsumerDestinatario,
                                null,
                                nomeCognomeConsumerDestinatario,
                                emailConsumerDestinatario,
                                Attore.TipoAttore.Consumer,
                                null
                            ),
                            mittente.getIdentificativoAttore()
                    );
            return GestioneDocumenti.uploadFile(httpServletRequest, contenuto, dettagliFile, nomeFile, listaHashtag,
                    mittente.getIdentificativoAttore(), campiFormAggiuntaAttore.getIdentificativoAttore());

        } catch (InputMismatchException e) {
            // Consumer trovato nel sistema, ma incoerenza nei campi
            return ResponseHelper.creaResponseBadRequest( e.getMessage() );
        } catch (MessagingException | NoSuchAlgorithmException |
                InvalidKeyException | UnsupportedEncodingException e) {
            return ResponseHelper.creaResponseServerError( e.getMessage() );
        }

    }

}
