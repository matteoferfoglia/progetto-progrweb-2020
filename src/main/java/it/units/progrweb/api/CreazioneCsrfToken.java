package it.units.progrweb.api;

import it.units.progrweb.utils.Logger;
import it.units.progrweb.utils.csrf.CsrfCookies;
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
 * // TODO : verificare che questo meccanismo con due cookie sia sicuro.
 *
 * @author Matteo Ferfoglia
 */
@Path("/CSRFToken")
public class CreazioneCsrfToken {

    /** Numero di caratteri che formeranno il CSRF token. */
    public static final short CSRF_TOKEN_LENGTH = 64;      // TODO : creare variabile d'ambiente (parametrizzare)

    /** Numero di caratteri che formeranno il token identificativo per il client. */
    public static final short CLIENT_ID_TOKEN_LENGTH = 64; // TODO : creare variabile d'ambiente (parametrizzare)


    /**
     * Risponde con un cookie contenente il token JWT (firmato da questo
     * server) contenente nel payload il token CSRF
     * (<a href="https://stackoverflow.com/a/28004533">Fonte</a>).
     * Un ulteriore cookie è usato per l'identità del client.
     * Vedere {@link CsrfToken} e {@link CsrfCookies} per i dettagli.
     * <a href="https://stackoverflow.com/a/14255549">Fonte
     * (Recupero indirizzo IP del client in JAX-RS)</a>.
     */
    @GET
    @Path("/generaCSRFToken")
    @Produces(MediaType.TEXT_PLAIN)
    public static Response creaCookiesConCsrfTokenEdIdentificativoClient(@Context HttpServletRequest httpServletRequest){

        try {

            String IPClient = httpServletRequest.getRemoteAddr();
            CsrfToken csrfToken = new CsrfToken(CSRF_TOKEN_LENGTH, CLIENT_ID_TOKEN_LENGTH, IPClient);

            // Creazione cookies
            CsrfCookies csrfCookies = csrfToken.creaCookiesPerCsrf();

            // Invia risposta positiva ed aggiungi il cookie creato sopra
            return Response .ok()
                            .cookie(csrfCookies.getCookieCSRF())
                            .cookie(csrfCookies.getCookieVerificaIdentitaClient())
                            .entity(csrfToken.getValoreCsrfToken())      // CSRF token anche nel body della response
                            .build();

        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            Logger.scriviEccezioneNelLog(CreazioneCsrfToken.class,e.getMessage(), e);
        }

        return Response.serverError().build();
    }

}