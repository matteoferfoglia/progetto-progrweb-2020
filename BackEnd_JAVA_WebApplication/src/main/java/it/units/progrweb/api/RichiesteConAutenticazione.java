package it.units.progrweb.api;

import it.units.progrweb.api.uploader.GestioneConsumer;
import it.units.progrweb.entities.RelazioneUploaderConsumer;
import it.units.progrweb.entities.attori.Attore;
import it.units.progrweb.entities.attori.AttoreProxy;
import it.units.progrweb.entities.attori.consumer.Consumer;
import it.units.progrweb.entities.file.File;
import it.units.progrweb.utils.Autenticazione;
import it.units.progrweb.utils.Logger;
import it.units.progrweb.utils.ResponseHelper;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.lang.reflect.Field;

/**
 * Classe che espone i servizi di richieste informazioni,
 * indipendentemente dal tipo di attore da cui provengono
 * (eventuali autorizzazioni particolari gestite direttamente
 * da questa classe), con l'obiettivo di evitare duplicazione
 * di codice (molte richieste sono comuni o simili tra tutti
 * i tipi di attori).
 * Esempio di utilizzo: restituire i nomi dei parametri
 * attesi dai servizi esposti dalle altre classi cosicché
 * tali nomi non vengano "cablati" sul client costituendo
 * una duplicazione di codice.
 *
 * @author Matteo Ferfoglia
 */
@Path("/info")
public class RichiesteConAutenticazione {

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

    /** Dato come @PathParam l'identificativo di un {@link Attore}, se l'utente
     * da cui proviene la richiesta è autorizzato a saperlo, restituisce un oggetto
     * JSON con le proprietà dell'attore target.*/
    @Path("proprietaAttoreTarget/{idAttoreTarget}")
    @GET
    public Response getProprietaAttoreCorrispondenteAIdentificativoSeRichiedenteAutorizzato(@PathParam("idAttoreTarget") Long idAttoreTarget,
                                                                                            @Context HttpServletRequest httpServletRequest) {

        Attore attoreTarget = Attore.getAttoreDaIdentificativo(idAttoreTarget);
        Long idAttoreAutenticato = Autenticazione.getIdentificativoAttoreDaHttpServletRequest(httpServletRequest);

        Long idConsumer = null; // usati in seguito per determinare se consumer ed uploader sono in relazione
        Long idUploader = null; //   nel caso in cui uno di questi ruoli stia facendo una richiesta sull'altro

        if (attoreTarget == null)
            return ResponseHelper.creaResponseNotFound(idAttoreTarget + " non trovato nel sistema.");
        else {

            try {

                Attore.TipoAttore tipoAttoreRichiedente =
                        Attore.TipoAttore.valueOf( Autenticazione.getTipoAttoreDaHttpServletRequest(httpServletRequest) );
                Attore.TipoAttore tipoAttoreTarget =
                        Attore.TipoAttore.valueOf( attoreTarget.getTipoAttore() );

                boolean isRichiedenteAutorizzato = false;

                switch( tipoAttoreRichiedente ) {
                    case Consumer:
                        // Consumer può vedere solo Uploader e non deve poter vedere username dell'Uploader
                        if( tipoAttoreTarget==Attore.TipoAttore.Uploader ) {
                            try {
                                Field fieldUsername = Attore.class.getDeclaredField("username");
                                fieldUsername.setAccessible(true);
                                fieldUsername.set(attoreTarget, null);
                            } catch (IllegalAccessException | NoSuchFieldException exception) {
                                Logger.scriviEccezioneNelLog(RichiesteConAutenticazione.class,
                                        "Potrebbe essere stato modificato, nella classe, l'attributo con lo username di un attore.",
                                        exception);
                            }
                            idConsumer = idAttoreAutenticato;
                            idUploader = idAttoreTarget;
                            isRichiedenteAutorizzato = true;
                        }
                        break;

                    case Uploader:
                        if( tipoAttoreTarget==Attore.TipoAttore.Consumer ) {
                            isRichiedenteAutorizzato = true;
                            idUploader = idAttoreAutenticato;
                            idConsumer = idAttoreTarget;
                        }
                        break;

                    case Administrator:
                        isRichiedenteAutorizzato = true;
                        break;

                    default:
                        isRichiedenteAutorizzato = false;
                        break;

                }

                // uploader e consumer devono essere in relazione tra loro per vedere uno le info dell'altro
                if (isRichiedenteAutorizzato && idConsumer != null && idUploader != null)
                    isRichiedenteAutorizzato = RelazioneUploaderConsumer.isConsumerServitoDaUploader(idUploader, idConsumer);

                if( isRichiedenteAutorizzato )
                    return ResponseHelper.creaResponseOk(new AttoreProxy(attoreTarget), MediaType.APPLICATION_JSON_TYPE);    // Proxy nasconde implementazione vera
                else
                    return ResponseHelper.creaResponseForbidden("Il ruolo del richiedente non è autorizzato ad ottenere le informazioni richieste.");

            } catch (IllegalArgumentException tipoAttoreInvalidoException) {
                // Eccezione generabile da Attore.TipoAttore.valueOf
                Logger.scriviEccezioneNelLog(this.getClass(), "Il tipo attore non è valido.", tipoAttoreInvalidoException);
                return ResponseHelper.creaResponseServerError("");
            }
        }

    }

}
