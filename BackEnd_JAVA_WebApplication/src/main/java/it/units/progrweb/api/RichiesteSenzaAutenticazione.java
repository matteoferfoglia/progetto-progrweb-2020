package it.units.progrweb.api;

import it.units.progrweb.entities.attori.uploader.Uploader;
import it.units.progrweb.entities.file.File;
import it.units.progrweb.persistence.NotFoundException;
import it.units.progrweb.utils.Logger;
import it.units.progrweb.utils.ResponseHelper;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.net.URI;
import java.net.URISyntaxException;

import static it.units.progrweb.EnvironmentVariables.API_NOAUTH_SERVLET_PATH;
import static it.units.progrweb.api.uploader.GestioneDocumenti.URL_PATH_DOWNLOAD_DOCUMENTO;

/**
 * Questa classe espone le API accessibili dai client
 * anche se non autenticati.
 *
 * @author Matteo Ferfoglia
 */
@Path(API_NOAUTH_SERVLET_PATH)
public class RichiesteSenzaAutenticazione {

    /** Gestisce il download di un file richiesto senza autenticazione del client,
     * ma fornendo un token casuale associato al file richiesto come @PathParam.*/
    @Path(URL_PATH_DOWNLOAD_DOCUMENTO+"/{idFile}/{tokenCasualeFile}")
    @GET
    // Response costruita senza @Produces per serializzare i dati in modo personalizzato
    public Response getFileDownloadDiretto(@PathParam("idFile")           Long idFile,
                                           @PathParam("tokenCasualeFile") String tokenCasualeFile,
                                           @Context HttpServletRequest httpServletRequest) {
        return File.creaResponseConFile(idFile, httpServletRequest, tokenCasualeFile, true);
    }

    /** Dato come @PathParam l'identificativo di un Uploader, restituisce
     * il suo logo.*/
    @Path("/logoUploader/{identificativoUploader}")
    @GET
    public Response getLogoUploader(@PathParam("identificativoUploader") Long identificativoUploader) {

        Uploader uploader = Uploader.getAttoreDaIdentificativo( identificativoUploader );
        if( uploader != null) {

            byte[] logo = uploader.getImmagineLogo();
            String mediatypeLogo = uploader.getMediatypeImmagineLogo();

            if( logo!=null && logo.length>0  && !mediatypeLogo.equals("") ) {
                // se mediatype non specificato

                return ResponseHelper.creaResponseOk(logo, MediaType.valueOf(mediatypeLogo));
            }
            else {
                try {
                    return ResponseHelper.creaResponseSeeOther(new URI("/logoDefault.svg"));
                } catch (URISyntaxException e) {
                    Logger.scriviEccezioneNelLog(this.getClass(),
                            "Errore nella restituzione del logo di default",
                            e);
                    return ResponseHelper.creaResponseServerError("");
                }
            }
        } else {
            return NotFoundException.creaResponseNotFound("Uploader non trovato.");
        }

    }


}
