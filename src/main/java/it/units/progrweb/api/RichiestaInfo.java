package it.units.progrweb.api;

import it.units.progrweb.api.consumer.RichiestaDocumenti;
import it.units.progrweb.api.consumer.RichiestaInfoSuUploader;
import it.units.progrweb.api.uploader.GestioneConsumer;
import it.units.progrweb.entities.attori.nonAdministrator.consumer.Consumer;
import it.units.progrweb.entities.attori.nonAdministrator.uploader.Uploader;
import it.units.progrweb.entities.file.File;
import it.units.progrweb.utils.Autenticazione;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

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


    /** Con riferimento a {@link RichiestaInfoSuUploader#getUploader(Long)}, questo metodo restituisce
     * il nome della proprietà contenente il nome dell'Uploader.*/
    @Path("/nomeProprietaNomeUploader")        // TODO : variabile d'ambiente
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String getNomeFieldNomeUploader() {
        return Uploader.getNomeFieldNomeUploader();
    }

    /** Con riferimento a {@link RichiestaInfoSuUploader#getUploader(Long)}, questo metodo restituisce
     * il nome della proprietà contenente il logo dell'Uploader.*/
    @Path("/nomeProprietaLogoUploader")        // TODO : variabile d'ambiente
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String getNomeFieldLogoUploader() {
        return Uploader.getNomeFieldLogoUploader();
    }


    /** Con riferimento a {@link GestioneConsumer#getConsumer(Long)}, questo metodo restituisce
     * il nome della proprietà contenente il nome del {@link Consumer}.*/
    @Path("/nomeProprietaNomeConsumer")        // TODO : variabile d'ambiente
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String getNomeFieldNomeConsumer() {
        return Consumer.getNomeFieldNominativoConsumer();
    }
    // TODO : metodi analoghi in RichiestaInfoSuUploader ... refactoring?

    /** Con riferimento a {@link GestioneConsumer#getConsumer(Long)}, questo metodo restituisce
     * il nome della proprietà contenente lo username del {@link Consumer}.*/
    @Path("/nomeProprietaUsernameConsumer")        // TODO : variabile d'ambiente
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String getNomeFieldUsernameConsumer() {
        return Consumer.getNomeFieldUsernameConsumer();
    }
    // TODO : metodi analoghi in RichiestaInfoSuUploader ... refactoring?

    /** Con riferimento a {@link GestioneConsumer#getConsumer(Long)}, questo metodo restituisce
     * il nome della proprietà contenente lo username del {@link Consumer}.*/
    @Path("/nomeProprietaEmailConsumer")        // TODO : variabile d'ambiente
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String getNomeFieldEmailConsumer() {
        return Consumer.getNomeFieldEmailConsumer();
    }
    // TODO : metodi analoghi in RichiestaInfoSuUploader ... refactoring?


}
