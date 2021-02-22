package it.units.progrweb.api.administrator;

import it.units.progrweb.api.CreazioneAttore;
import it.units.progrweb.entities.attori.Attore;
import it.units.progrweb.entities.attori.administrator.Administrator;
import it.units.progrweb.entities.attori.nonAdministrator.uploader.Uploader;
import it.units.progrweb.utils.UtilitaGenerale;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
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
    public Response eliminaConsumer(@PathParam("identificativoAttoreDaEliminare") Long identificativoAttoreDaEliminare,
                                    @Context HttpServletRequest httpServletRequest) {

        // TODO : questo metodo è molto simile a quello per l'eliminazione dei Consumer da parte di un Uploader (Refactoring??)

        // Ricerca dell'attore da eliminare
        boolean attoreEliminato = Attore.eliminaAttoreDaIdentificativo( identificativoAttoreDaEliminare );

        if( attoreEliminato ) {

            return Response
                    .status( Response.Status.OK )
                    .entity("Attore eliminato")    // TODO : var ambiente con messaggi errore
                    .build();

        } else {
            return Response
                    .status( Response.Status.BAD_REQUEST )  // TODO : fare un metodo che gestisca le BAD_REQUEST con tutte quelle usate nell'intero progetto, che prenda il messaggio d'errore come parametro
                                                        // TODO : vedere se bisogna modificare dei BAD_REQUEST con dei NOT_FOUND
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
    public Response aggiungiAttore( CreazioneAttore.CampiFormAggiuntaAttore campiFormAggiuntaAttore ) {

        return CreazioneAttore.creaNuovoAttoreECreaResponse(campiFormAggiuntaAttore, Attore.TipoAttore.Administrator);

    }

    /** Modifica di un {@link Attore} di quelli presenti nel sistema. */
    @Path("/modificaAttore")    // TODO : non si può usare direttamente il metodo in ModficaInformazioniAttore?
    @POST  // TODO : valutare se sostituire con metodo PUT (perché in realtà è idempotente)
    @Consumes( MediaType.MULTIPART_FORM_DATA )
    public Response modificaAttore(@Context HttpServletRequest httpServletRequest,
                                   @FormDataParam("identificativoAttore") Long identificativoAttore,
                                   @FormDataParam("username")             String nuovoUsername,
                                   @FormDataParam("nominativo")           String nuovoNominativo,
                                   @FormDataParam("email")                String nuovaEmail,
                                   @FormDataParam("immagineLogo")         InputStream nuovoLogo,                  // TODO : vedere dove è usato il componente Form*.vue che si occupa di inviare queste informazioni e cercare di creare un unico servizio sul server che gestisca tutto
                                   @FormDataParam("immagineLogo")         FormDataContentDisposition dettagliNuovoLogo) {

        // TODO : in Vue aggiungere l'input[type=file] per modificare l'immagine logo

        Attore attoreDaModificare_attualmenteSalvatoInDB = Attore.getAttoreDaIdentificativo( identificativoAttore );
        if( attoreDaModificare_attualmenteSalvatoInDB != null ) {

            // Creazione dell'attore con le modifiche richieste dal client (Attore è
            // una classe astratta, Jersey non riesce a deserializzare )
            // TODO : trovare modo migliore

            Attore attore_conModificheRichiesteDalClient = attoreDaModificare_attualmenteSalvatoInDB.clone();
            attore_conModificheRichiesteDalClient.setUsername( nuovoUsername );
            attore_conModificheRichiesteDalClient.setNominativo( nuovoNominativo );
            attore_conModificheRichiesteDalClient.setEmail( nuovaEmail );

            if( attoreDaModificare_attualmenteSalvatoInDB instanceof Uploader &&
                nuovoLogo != null && dettagliNuovoLogo != null) {

                try {
                    ((Uploader)attoreDaModificare_attualmenteSalvatoInDB)
                            .setImmagineLogo(UtilitaGenerale.convertiInputStreamInByteArray( nuovoLogo ),
                                             UtilitaGenerale.getEstensioneDaNomeFile(dettagliNuovoLogo.getFileName()));
                } catch (IOException e) {
                    // Dimensione immagine logo eccessiva   // TODO : testare
                    return Response.status( Response.Status.REQUEST_ENTITY_TOO_LARGE )
                                   .entity( e.getMessage() )
                                   .build();
                }

            }

            return Attore.modificaAttore(attore_conModificheRichiesteDalClient, attoreDaModificare_attualmenteSalvatoInDB);

        } else {
            // Attore non trovato nel DB
            return Response.status( Response.Status.NOT_FOUND )
                           .entity( "Attore [" + identificativoAttore + "] non trovato." )
                           .build();
        }

    }

    /** Restituisce un array con gli identificativi di tutti gli Attori del
     * tipo indicato nel @PathParam.*/
    @Path("/elencoAttori/{tipoAttoriDiCuiMostrareElenco}")
    @GET
    @Produces( MediaType.APPLICATION_JSON )
    public long[] getElencoIdentificativiUploader( @PathParam("tipoAttoriDiCuiMostrareElenco") String tipoAttoriDiCuiMostrareElenco) {

        // TODO : refactoring c'è un metodo molto simile in Uploader

        List<Long> listaIdentificativiAttoriDaRestituire;

        if( tipoAttoriDiCuiMostrareElenco.equals( Attore.TipoAttore.Uploader.getTipoAttore() ) ) {
            listaIdentificativiAttoriDaRestituire =
                    Uploader.getListaIdentificativiTuttiGliUploaderNelSistema();

        } else if ( tipoAttoriDiCuiMostrareElenco.equals( Attore.TipoAttore.Administrator.getTipoAttore() ) ) {
            listaIdentificativiAttoriDaRestituire =
                    Administrator.getListaIdentificativiTuttiGliAdministratorNelSistema();

        } else {
            return new long[]{};
        }

        return listaIdentificativiAttoriDaRestituire.stream().mapToLong(i -> i).toArray();

    }

    /** Dato l'identificativo di un {@link Administrator}, restituisce l'oggetto JSON
     * con le properties di quell'{@link Administrator}.*/
    @Path("/proprietaAdministrator/{identificativoAdministrator}")        // TODO : variabile d'ambiente
    @GET
    @Produces(MediaType.APPLICATION_JSON )
    public Administrator getAdministrator(@PathParam("identificativoAdministrator") Long identificativoAdministrator,
                                @Context HttpServletRequest httpServletRequest ) {

        Administrator administrator = Administrator.cercaAdministratorDaIdentificativo(identificativoAdministrator);
        return administrator;

    }


}

