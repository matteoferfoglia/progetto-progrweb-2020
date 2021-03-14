package it.units.progrweb.api;

import it.units.progrweb.api.uploader.GestioneConsumer;
import it.units.progrweb.entities.RelazioneUploaderConsumer;
import it.units.progrweb.entities.attori.Attore;
import it.units.progrweb.entities.attori.nonAdministrator.consumer.Consumer;
import it.units.progrweb.entities.attori.nonAdministrator.uploader.Uploader;
import it.units.progrweb.entities.file.File;
import it.units.progrweb.persistence.NotFoundException;
import it.units.progrweb.utils.Autenticazione;
import it.units.progrweb.utils.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.lang.reflect.Field;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.function.Function;

/**
 * Classe che espone i servizi di richieste informazioni,
 * indipendentemente dal tipo di attore da cui provengono.
 * Esempio di utilizzo: restituire i nomi dei parametri
 * attesi dai servizi esposti dalle altre classi cosicché
 * tali nomi non vengano "cablati" sul client costituendo
 * una duplicazione di codice.
 * I servizi esposti da questa classe non hanno vincoli di
 * autenticazione (chiunque può farne richiesta).
 * @author Matteo Ferfoglia
 */
@Path("/info")
public class RichiestaInfo {

    /** Restituisce un array contenente i nomi delle proprietà
     * comuni a tutti i documenti. Tale array può essere utilizzato
     * dai client per preparare l'interfaccia grafica (es.: intestazione
     * di una tabella che conterrà un documento per riga.*/
    @Path("/arrayNomiProprietaOgniDocumento")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String[] getNomiProprietaFileInAnteprima(@Context HttpServletRequest httpServletRequest) {

        String tipoAttore = Autenticazione.getTipoAttoreDaHttpServletRequest(httpServletRequest);

        if( Consumer.class.getSimpleName().equals( tipoAttore ) )
            return File.anteprimaNomiProprietaFile( false );

        return File.anteprimaNomiProprietaFile( true );
    }

    /** Restituisce il nome dell'attributo di un {@link File} che
     * contiene l'array di hashtag riferiti a quel {@link File}.*/
    @Path("/nomePropHashtags")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String getNomeAttributoContenenteHashtagNeiFile() {
        return File.getNomeAttributoContenenteHashtagNeiFile();
    }

    /** Restituisce il nome dell'attributo di un {@link File} che
     * contiene la data di caricamento di quel {@link File}.*/
    @Path("/nomePropDataCaricamento")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String getNomeAttributoContenenteDataCaricamentoFile() {
        return File.getNomeAttributoContenenteDataCaricamentoFile();
    }

    /** Restituisce il nome dell'attributo di un {@link File} che
     * contiene la data di visualizzazione di quel {@link File}
     * da parte del Consumer.*/
    @Path("/nomePropDataVisualizzazione")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String getNomeAttributoContenenteDataVisualizzazioneFile() {
        return File.getNomeAttributoContenenteDataVisualizzazioneFile();
    }

    /** Restituisce il nome dell'attributo di un {@link File} che
     * ne contiene il nome.*/
    @Path("/nomeProprietaNomeDocumento")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String getNomeAttributoContenenteNomeFile() {
        return File.getNomeAttributoContenenteNomeFile();
    }


    /** Restituisce il nome della proprietà contenente il nome di un {@link Attore}.*/
    @Path("/nomeProprietaNomeAttore")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String getNomeFieldNomeAttore() {
        return Attore.getNomeFieldNominativoAttore();
    }

    /** Restituisce il nome della proprietà contenente lo username di un {@link Attore}.*/
    @Path("/nomeProprietaUsernameAttore")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String getNomeFieldUsernameAttore() {
        return Attore.getNomeFieldUsernameAttore();
    }

    /** Con riferimento a {@link GestioneConsumer#getConsumer(Long)}, questo metodo restituisce
     * il nome della proprietà contenente lo username del {@link Consumer}.*/
    @Path("/nomeProprietaEmailAttore")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String getNomeFieldEmailAttore() {
        return Attore.getNomeFieldEmailAttore();
    }

    /** Dato l'identificativo di un Uploader, restituisce l'oggetto JSON
     * con le properties di quell'Uploader.*/
    @Path("/proprietaUploader/{identificativoUploader}")
    @GET
    @Produces(MediaType.APPLICATION_JSON )
    public Uploader getUploader(@PathParam("identificativoUploader") Long identificativoUploader,
                                @Context HttpServletRequest httpServletRequest ) {

        Uploader uploader = Uploader.cercaUploaderDaIdentificativo(identificativoUploader);

        if( uploader == null ) {
            Logger.scriviEccezioneNelLog(RichiestaInfo.class, "Uploader non trovato", new NullPointerException());
        } else  {
            if( Autenticazione.getTipoAttoreDaHttpServletRequest(httpServletRequest)
                    .equals(Consumer.class.getSimpleName()) ) {
                // Consumer non deve poter vedere username dell'Uploader

                try {
                    Field fieldUsername = Attore.class.getDeclaredField("username");
                    fieldUsername.setAccessible(true);
                    fieldUsername.set(uploader, null);
                } catch (IllegalAccessException | NoSuchFieldException exception) {
                    Logger.scriviEccezioneNelLog(RichiestaInfo.class,
                            "Potrebbe essere stato modificato, nella classe, l'attributo con lo username di un attore.",
                            exception);
                }

            }
        }

        return uploader;

    }

    /** Dato come @PathParam l'identificativo di un Uploader, restituisce
     * il suo logo.*/
    @Path("/logoUploader/{identificativoUploader}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @GET
    public Response getLogoUploader(@PathParam("identificativoUploader") Long identificativoUploader) {

        Uploader uploader = Uploader.getAttoreDaIdentificativo( identificativoUploader );
        if( uploader != null) {

            byte[] logo = uploader.getImmagineLogo();
            String mediatypeLogo = uploader.getMediatypeImmagineLogo();

            if( logo!=null && logo.length>0  && !mediatypeLogo.equals("") ) {
                                                // se mediatype non specificato

                return Response.ok(logo, mediatypeLogo)
                                   .build();
            }
            else {
                try {
                    return Response.seeOther(new URI("/logoDefault.svg")).build();
                } catch (URISyntaxException e) {
                    Logger.scriviEccezioneNelLog(this.getClass(),
                                                 "Errore nella restituzione del logo di default",
                                                 e);
                    return Response.serverError().build();
                }
            }
        } else {
            return NotFoundException.creaResponseNotFound("Uploader non trovato.");
        }

    }

    /** Dato come @PathParam l'identificativo di un {@link Attore}, se l'utente
     * da cui proviene la richiesta è autorizzato a saperlo, restituisce il tipo
     * di attore di cui è stato fornito l'identificativo.*/
    @Path("tipoAttoreTarget/{idAttoreTarget}")
    @GET
    public Response getTipoAttoreCorrispondenteAIdentificativoSeRichiedenteAutorizzato(@PathParam("idAttoreTarget") Long idAttoreTarget,
                                                                                       @Context HttpServletRequest httpServletRequest) {

        // Lambda function per restituire il tipo dell'attore corrispondente all'id fornito
        Function<Long, Response> getTipoAttoreTarget = idAttore -> {
            Attore attoreTarget = Attore.getAttoreDaIdentificativo(idAttore);
            return attoreTarget==null ?
                    Response.status(Response.Status.NOT_FOUND).entity(idAttore + " non trovato nel sistema.").build() :
                    Response.ok().entity(attoreTarget.getTipoAttore()).type(MediaType.TEXT_PLAIN).build();
        };

        return checkPermessiERestituisciInfoSuAttore(idAttoreTarget, httpServletRequest, getTipoAttoreTarget);

    }

    /** Dato come @PathParam l'identificativo di un {@link Attore}, se l'utente
     * da cui proviene la richiesta è autorizzato a saperlo, restituisce un oggetto
     * JSON con le proprietà dell'attore target.*/
    @Path("proprietaAttoreTarget/{idAttoreTarget}")
    @GET
    public Response getProprietaAttoreCorrispondenteAIdentificativoSeRichiedenteAutorizzato(@PathParam("idAttoreTarget") Long idAttoreTarget,
                                                                                            @Context HttpServletRequest httpServletRequest) {

        // Lambda function per restituire il tipo dell'attore corrispondente all'id fornito
        Function<Long, Response> getProprietaAttoreTarget = idAttore -> {
            Attore attoreTarget = Attore.getAttoreDaIdentificativo(idAttore);
            if (attoreTarget == null)
                return Response.status(Response.Status.NOT_FOUND).entity(idAttore + " non trovato nel sistema.").build();
            else
                return Response.ok().entity(attoreTarget).type(MediaType.APPLICATION_JSON).build();
        };

        return checkPermessiERestituisciInfoSuAttore(idAttoreTarget, httpServletRequest, getProprietaAttoreTarget);

    }

    /** Funzione di supporto per
     * {@link #getProprietaAttoreCorrispondenteAIdentificativoSeRichiedenteAutorizzato(Long, HttpServletRequest)}
     * e {@link #getTipoAttoreCorrispondenteAIdentificativoSeRichiedenteAutorizzato(Long, HttpServletRequest)}.*/
    private Response checkPermessiERestituisciInfoSuAttore(Long idAttoreTarget,
                                                           HttpServletRequest httpServletRequest,
                                                           Function<Long, Response> creazioneRisposta) {
        String tipoAttoreAutenticato = Autenticazione.getTipoAttoreDaHttpServletRequest(httpServletRequest);
        if( tipoAttoreAutenticato.equals(Attore.TipoAttore.Administrator.getTipoAttore()) ) {
            return creazioneRisposta.apply(idAttoreTarget);
        } else {
            // uploader e consumer devono essere in relazione per vedere uno le info dell'altro
            Long idAttoreAutenticato = Autenticazione.getIdentificativoAttoreDaHttpServletRequest(httpServletRequest);
            Long idConsumer, idUploader;
            if( tipoAttoreAutenticato.equals(Attore.TipoAttore.Consumer.getTipoAttore()) ) {
                idConsumer = idAttoreAutenticato;
                idUploader = idAttoreTarget;
            } else {
                idUploader = idAttoreAutenticato;
                idConsumer = idAttoreTarget;
            }
            return RelazioneUploaderConsumer.isConsumerServitoDaUploader(idUploader, idConsumer) ?
                    creazioneRisposta.apply(idAttoreTarget) :
                    Autenticazione.creaResponseForbidden("Consumer non in relazione con Uploader.");
        }
    }

}
