package it.units.progrweb.api.uploader;

import it.units.progrweb.entities.RelazioneUploaderConsumerFile;
import it.units.progrweb.entities.attori.nonAdministrator.consumer.Consumer;
import it.units.progrweb.entities.attori.nonAdministrator.uploader.Uploader;
import it.units.progrweb.entities.file.File;
import it.units.progrweb.persistence.NotFoundException;
import it.units.progrweb.utils.Autenticazione;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Classe per permettere ad un {@link Uploader} di gestire
 * i documenti inviati ad un {@link Consumer}.
 * @author Matteo Ferfoglia
 */
@Path("/uploader/documenti")    // TODO : variabile d'ambiente
public class GestioneDocumenti {

    /** Restituisce un array con i nomi di tutte le proprietà
     * presenti nei documenti quando vengono restituiti come
     * oggetti.*/
    @Path("nomiProprietaOgniDocumento")
    @GET
    @Produces(MediaType.APPLICATION_JSON )
    public String[] getNomiPropDocumenti() {

        return File.anteprimaNomiProprietaFile();   // TODO : c'è lo stesso metodo nei servizi per i Consumer

    }

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

    /** Caricamento di un {@link File}. */
    @Path("/uploadDocumento")
    @DELETE
    public Response caricaFile(@Context HttpServletRequest httpServletRequest) {

        // TODO
        return null;

    }

    /** Restituisce una mappa in cui ogni entry ha per chiave l'identificativo
     * di un {@link File} e come valore corrispondente l'oggetto che rappresenta
     * il {@link File} in termini delle sue proprietà. */
    @Path("/mappa-idFile-propFile")
    @DELETE
    public Response eliminaFile(@Context HttpServletRequest httpServletRequest) {

        // TODO
        return null;

    }


}
