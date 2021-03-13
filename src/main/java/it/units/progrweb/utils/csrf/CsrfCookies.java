package it.units.progrweb.utils.csrf;

import it.units.progrweb.utils.Cookie;
import it.units.progrweb.utils.jwt.JwtToken;

/**
 * Classe per la rappresentazione dei cookie necessari
 * per il corretto utilizzo del token CSRF.
 * La verifica del token csrf è fatta usando due cookie:
 * il primo conterrà il token jwt nel cui payload vi è il token
 * CSRF ed un identificativo del client, il secondo conterrà
 * l'identificativo del client.
 *
 * @author Matteo Ferfoglia
 */
public class CsrfCookies {

    /** Nome del cookie che si occuperà di trasmettere il token CSRF ({@link #creaCookieContenenteJwtToken(JwtToken)}). */
    public static final String NOME_COOKIE_CSRF = "CSRF-TOKEN-JWT";

    /** Nome del del cookie mantenuto con questo server per verificare l'identità del client
     * ({@link #creaCookieContenenteIdentificativoClient(String)}).*/
    public static final String NOME_COOKIE_ID_CLIENT = "TOKEN-ID-CLIENT-VERIFICA-CSRF";


    /** Conterrà il JWT token nel cui payload vi è il token CSRF ed un identificativo del client. */
    private final Cookie cookieCSRF;

    /** Conterrà l'identificativo del client.*/
    private final Cookie cookieVerificaIdentitaClient;

    public CsrfCookies(Cookie cookieCSRF, Cookie cookieVerificaIdentitaClient, int maxAge) {
        this.cookieCSRF = new Cookie(cookieCSRF, maxAge, true);
        this.cookieVerificaIdentitaClient = cookieVerificaIdentitaClient;
    }

    public Cookie getCookieCSRF() {
        return cookieCSRF;
    }

    public Cookie getCookieVerificaIdentitaClient() {
        return cookieVerificaIdentitaClient;
    }


    public static Cookie creaCookieContenenteJwtToken(JwtToken<?> jwtToken) {
        return new Cookie(NOME_COOKIE_CSRF, jwtToken.generaTokenJsonCodificatoBase64UrlEncoded(),
                "Cookie contenente il token JWT il cui payload contiene il token CSRF");
    }

    public static Cookie creaCookieContenenteIdentificativoClient(String valoreIdentificativoClient) {
        return creaCookieContenenteIdentificativoClient(NOME_COOKIE_ID_CLIENT, valoreIdentificativoClient, Cookie.MAX_AGE_DEFAULT);
    }

    public static Cookie creaCookieContenenteIdentificativoClient(String nomeCookie, String valoreIdentificativoClient, int maxAgeDelCookieInSecondi) {
        return new Cookie(nomeCookie, valoreIdentificativoClient, maxAgeDelCookieInSecondi,
                "Cookie contenente un valore identificativo per il client");
    }

}
