package it.units.progrweb.api;

import it.units.progrweb.api.uploader.GestioneDocumenti;
import it.units.progrweb.entities.attori.Attore;
import it.units.progrweb.entities.attori.nonAdministrator.consumer.Consumer;
import it.units.progrweb.entities.attori.nonAdministrator.uploader.Uploader;
import it.units.progrweb.utils.Autenticazione;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.InputStream;

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

        Consumer destinatario = Consumer.getAttoreDaUsername(codiceFiscaleConsumerDestinatario);
        if( destinatario!=null &&
                destinatario.getNominativo().equals(nomeCognomeConsumerDestinatario) &&
                destinatario.getEmail().equals(emailConsumerDestinatario) )
            return GestioneDocumenti.uploadFile(httpServletRequest, contenuto, dettagliFile, nomeFile, listaHashtag,
                    mittente.getIdentificativoAttore(), destinatario.getIdentificativoAttore());

        return Response.status( Response.Status.BAD_REQUEST )           // TODO : refactoring: creare un metodo che invia BAD_REQUEST
                       .entity( "Consumer non trovato nel sistema." )
                       .build();


    }

}
