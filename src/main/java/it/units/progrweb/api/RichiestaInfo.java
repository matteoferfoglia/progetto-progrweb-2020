package it.units.progrweb.api;

import it.units.progrweb.api.consumer.RichiestaDocumenti;
import it.units.progrweb.api.uploader.GestioneConsumer;
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

/**
 * Classe che espone i servizi di richieste informazioni,
 * indipendentemente dal tipo di attore da cui provengono.
 * Esempio di utilizzo: restituire i nomi dei parametri
 * attesi dai servizi esposti dalle altre classi cosicché
 * tali nomi non vengano "cablati" sul client costituendo
 * una duplicazione di codice.
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
     * contiene l'array di hashtag riferiti a quel {@link File}.
     * Vedere anche il {@link RichiestaDocumenti#getElencoDocumenti(HttpServletRequest,Long)
     * metodo di invio dei file ai client}.*/
    @Path("/nomePropHashtags")     // TODO : variabile d'ambiente
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String getNomeAttributoContenenteHashtagNeiFile() {
        return File.getNomeAttributoContenenteHashtagNeiFile();
    }

    /** Restituisce il nome dell'attributo di un {@link File} che
     * contiene la data di caricamento di quel {@link File}.
     * Vedere anche il {@link RichiestaDocumenti#getElencoDocumenti(HttpServletRequest,Long)
     * metodo di invio dei file ai client}.*/
    @Path("/nomePropDataCaricamento")     // TODO : variabile d'ambiente
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String getNomeAttributoContenenteDataCaricamentoFile() {
        return File.getNomeAttributoContenenteDataCaricamentoFile();
    }

    /** Restituisce il nome dell'attributo di un {@link File} che
     * contiene la data di visualizzazione di quel {@link File}
     * da parte del Consumer.
     * Vedere anche il {@link RichiestaDocumenti#getElencoDocumenti(HttpServletRequest,Long)
     * metodo di invio dei file ai client}.*/
    @Path("/nomePropDataVisualizzazione")     // TODO : variabile d'ambiente
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String getNomeAttributoContenenteDataVisualizzazioneFile() {
        return File.getNomeAttributoContenenteDataVisualizzazioneFile();
    }

    /** Restituisce il nome dell'attributo di un {@link File} che
     * ne contiene il nome.*/
    @Path("/nomeProprietaNomeDocumento")     // TODO : variabile d'ambiente
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String getNomeAttributoContenenteNomeFile() {
        return File.getNomeAttributoContenenteNomeFile();
    }


    /** Restituisce il nome della proprietà contenente il nome di un {@link Attore}.*/
    @Path("/nomeProprietaNomeAttore")        // TODO : variabile d'ambiente
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String getNomeFieldNomeAttore() {
        return Attore.getNomeFieldNominativoAttore();
    }

    /** Restituisce il nome della proprietà contenente il logo dell'Uploader.*/
    @Path("/nomeProprietaLogoUploader")        // TODO : variabile d'ambiente
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String getNomeFieldLogoUploader() {
        return Uploader.getNomeFieldLogoUploader();
    }

    /** Restituisce il nome della proprietà contenente lo username di un {@link Attore}.*/
    @Path("/nomeProprietaUsernameAttore")        // TODO : variabile d'ambiente
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String getNomeFieldUsernameAttore() {
        return Attore.getNomeFieldUsernameAttore();
    }

    /** Con riferimento a {@link GestioneConsumer#getConsumer(Long)}, questo metodo restituisce
     * il nome della proprietà contenente lo username del {@link Consumer}.*/
    @Path("/nomeProprietaEmailAttore")        // TODO : variabile d'ambiente
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String getNomeFieldEmailAttore() {
        return Attore.getNomeFieldEmailAttore();
    }

    /** Dato l'identificativo di un Uploader, restituisce l'oggetto JSON
     * con le properties di quell'Uploader.*/
    @Path("/proprietaUploader/{identificativoUploader}")        // TODO : variabile d'ambiente
    @GET
    @Produces(MediaType.APPLICATION_JSON )
    public Uploader getUploader(@PathParam("identificativoUploader") Long identificativoUploader,
                                @Context HttpServletRequest httpServletRequest ) {

        // TODO : aggiungere Disclaimer che le proprietà di un Uploader sono visibili a tutti gli
        //        gli utenti autenticati della piattaforma che vengano a conoscenza di questo url

        Uploader uploader = Uploader.cercaUploaderDaIdentificativo(identificativoUploader);

        if( Autenticazione.getTipoAttoreDaHttpServletRequest(httpServletRequest)
                .equals(Consumer.class.getSimpleName()) ) { // Consumer non vede username dell'Uploader

            try {
                Field fieldUsername = Attore.class.getDeclaredField("username");
                fieldUsername.setAccessible(true);
                fieldUsername.set(uploader, null);
            } catch (IllegalAccessException | NoSuchFieldException exception) {
                Logger.scriviEccezioneNelLog(RichiestaInfo.class,
                        "Potrebbe essere stato modificato, nella classe, ll'attributo con lo username di un attore.",
                        exception);
            }

        }

        return uploader;

    }

    /** Dato come @PathParam l'identificativo di un Uploader, restituisce
     * la rappresentazione codificata come data URL del suo logo.*/
    @Path("/logoUploader/{identificativoUploader}")
    @GET
    public Response getLogoUploader_dataUrl(@PathParam("identificativoUploader") Long identificativoUploader) {

        // TODO : verificare - va bene così senza mediatype ? Il browser che riceve? L'entity nel NOT_FOUND?

        // TODO : aggiungere Disclaimer che le proprietà di un Uploader sono visibili a tutti gli
        //        gli utenti autenticati della piattaforma che vengano a conoscenza di questo url

        Uploader uploader = Uploader.getAttoreDaIdentificativo( identificativoUploader );
        if( uploader != null) {
            return Response.ok()
                    .entity( uploader.getImmagineLogoBase64() )
                    .build();
        } else {
            return NotFoundException.creaResponseNotFound("Uploader non trovato.");
        }

    }


}
