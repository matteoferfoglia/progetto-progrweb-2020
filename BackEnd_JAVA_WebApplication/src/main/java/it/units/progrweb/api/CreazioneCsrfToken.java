package it.units.progrweb.api;

import it.units.progrweb.utils.Cookie;
import it.units.progrweb.utils.Logger;
import it.units.progrweb.utils.ResponseHelper;
import it.units.progrweb.utils.csrf.CsrfCookie;
import it.units.progrweb.utils.csrf.CsrfToken;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * Classe per la gestione dei token CSRF.
 * I token CSRF vengono inviati al client nel payload di un token JWT
 * firmato (non ci sono variabili di sessione, ma bisogna poter in
 * seguito verificare il valore del token).
 * Oltre al cookie contenente il token JWT il cui payload contiene il
 * token CSRF, si crea anche un cookie il cui valore è un identificativo
 * per il client che ha richiesto il token CSRF. Il valore di questo
 * secondo cookie viene aggiunto al token JWT e permetterà di verificare
 * che il client che fa uso del token CSRF sia lo stesso che ne ha fatto
 * richiesta.
 *
 * @author Matteo Ferfoglia
 */
@Path("/CSRFToken")
public class CreazioneCsrfToken {

    /** Numero di caratteri che formeranno il CSRF token. */
    public static final short LUNGHEZZA_TOKEN_CSRF = 64;


    /**
     * Risponde con un cookie contenente il token JWT (firmato da questo
     * server) contenente nel payload il token CSRF
     * (<a href="https://stackoverflow.com/a/28004533">Fonte</a>).
     * Un ulteriore cookie è usato per l'identità del client.
     * Vedere {@link CsrfToken} e {@link CsrfCookie} per i dettagli.
     * <a href="https://stackoverflow.com/a/14255549">Fonte
     * (Recupero indirizzo IP del client in JAX-RS)</a>.
     */
    @GET
    @Path("/generaCSRFToken")
    @Produces(MediaType.TEXT_PLAIN)
    public static Response creaCookiesConCsrfTokenInJWT(@Context HttpServletRequest httpServletRequest){

        try {

            String IPClient = httpServletRequest.getRemoteAddr();
            CsrfToken csrfToken = new CsrfToken(LUNGHEZZA_TOKEN_CSRF, IPClient);

            // Creazione cookie contenente il token JWT contenente token CSRF nel payload
            CsrfCookie csrfCookie = csrfToken.creaCookiePerCsrf();

            // Invia risposta positiva ed aggiungi il cookie creato sopra
            return ResponseHelper.creaResponseOk(csrfToken.getValoreCsrfToken(), new Cookie[]{csrfCookie.getCookieCSRF()});

        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            Logger.scriviEccezioneNelLog(CreazioneCsrfToken.class,e.getMessage(), e);
        }

        return ResponseHelper.creaResponseServerError("");
    }

}