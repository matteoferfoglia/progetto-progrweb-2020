package it.units.progrweb.api.administrator;

import it.units.progrweb.api.CreazioneAttore;
import it.units.progrweb.entities.attori.Attore;
import it.units.progrweb.entities.attori.administrator.Administrator;
import it.units.progrweb.entities.attori.uploader.Uploader;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.util.List;

/**
 * Questa classe espone agli {@link Administrator} i servizi per
 * la gestione degli attori.
 * @author Matteo Ferfoglia
 */
@Path("/administrator")
public class GestioneAttori {

    /** Eliminazione di un {@link Attore} dal sistema. */
    @Path("/eliminaAttore/{identificativoAttoreDaEliminare}")
    @DELETE
    public Response eliminaAttore(@PathParam("identificativoAttoreDaEliminare") Long identificativoAttoreDaEliminare,
                                  @Context HttpServletRequest httpServletRequest) {

        // Ricerca dell'attore da eliminare
        boolean attoreEliminato = Attore.eliminaAttoreDaIdentificativo( identificativoAttoreDaEliminare );

        if( attoreEliminato ) {

            return Response
                    .status( Response.Status.OK )
                    .entity("Attore eliminato")
                    .build();

        } else {
            return Response
                    .status( Response.Status.BAD_REQUEST )
                    .entity( "Impossibile eliminare l'attore " + identificativoAttoreDaEliminare )
                    .build();
        }


    }

    /** Metodo per l'aggiunta di un {@link Attore}.
     * Se la procedura va a buon fine, restituisce l'identificativo
     * dell'{@link Attore} appena creato, altrimenti l'errore.*/
    @Path("/aggiungiAttore")
    @POST
    @Consumes( MediaType.APPLICATION_JSON )
    public Response aggiungiAttore( @Context HttpServletRequest httpServletRequest,
                                    CreazioneAttore.CampiFormAggiuntaAttore campiFormAggiuntaAttore ) {

        return CreazioneAttore.creaNuovoAttoreECreaResponse(httpServletRequest, campiFormAggiuntaAttore, Attore.TipoAttore.Administrator);

    }

    /** Modifica di un {@link Attore} di quelli presenti nel sistema. */
    @Path("/modificaAttore")
    @POST
    @Consumes( MediaType.MULTIPART_FORM_DATA )
    public Response modificaAttore(@Context HttpServletRequest httpServletRequest,
                                   @FormDataParam("identificativoAttore") Long identificativoAttore,
                                   @FormDataParam("nominativo")           String nuovoNominativo,
                                   @FormDataParam("email")                String nuovaEmail,
                                   @FormDataParam("immagineLogo")         InputStream nuovoLogo,
                                   @FormDataParam("immagineLogo")         FormDataContentDisposition dettagliNuovoLogo) {

        if( identificativoAttore != null ) {

            return Attore.modificaAttore(
                    nuovoNominativo,
                    nuovaEmail,
                    identificativoAttore,
                    nuovoLogo,
                    dettagliNuovoLogo,
                    false
            );

        } else {
            return Response
                    .status( Response.Status.BAD_REQUEST )
                    .entity("Specificare l'identificativo del Consumer da specificare.")
                    .build();
        }


    }

    /** Restituisce un array con gli identificativi di tutti gli Attori del
     * tipo indicato nel @PathParam.*/
    @Path("/elencoAttori/{tipoAttoriDiCuiMostrareElenco}")
    @GET
    @Produces( MediaType.APPLICATION_JSON )
    public long[] getElencoIdentificativiUploader( @PathParam("tipoAttoriDiCuiMostrareElenco") String tipoAttoriDiCuiMostrareElenco) {

        List<Long> listaIdentificativiAttoriDaRestituire;

        if( tipoAttoriDiCuiMostrareElenco.equals( Attore.TipoAttore.Uploader.name() ) ) {
            listaIdentificativiAttoriDaRestituire =
                    Uploader.getListaIdentificativiTuttiGliUploaderNelSistema();

        } else if ( tipoAttoriDiCuiMostrareElenco.equals( Attore.TipoAttore.Administrator.name() ) ) {
            listaIdentificativiAttoriDaRestituire =
                    Administrator.getListaIdentificativiTuttiGliAdministratorNelSistema();

        } else {
            return new long[0];
        }

        return listaIdentificativiAttoriDaRestituire.stream().mapToLong(i -> i).toArray();

    }

    /** Dato l'identificativo di un {@link Administrator}, restituisce l'oggetto JSON
     * con le properties di quell'{@link Administrator}.*/
    @Path("/proprietaAdministrator/{identificativoAdministrator}")
    @GET
    @Produces(MediaType.APPLICATION_JSON )
    public Administrator getAdministrator(@PathParam("identificativoAdministrator") Long identificativoAdministrator,
                                @Context HttpServletRequest httpServletRequest ) {

        return Administrator.cercaAdministratorDaIdentificativo(identificativoAdministrator);

    }


}

