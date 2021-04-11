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
public class CsrfCookie {

    /** Nome del cookie che si occuperà di trasmettere il token CSRF ({@link #creaCookieContenenteJwtToken(JwtToken)}). */
    public static final String NOME_COOKIE_CSRF = "CSRF-TOKEN-JWT";

    /** Conterrà il JWT token nel cui payload vi è il token CSRF ed un identificativo del client. */
    private final Cookie cookieCSRF;

    public CsrfCookie(Cookie cookieCSRF, int maxAge) {
        this.cookieCSRF = new Cookie(cookieCSRF, maxAge, true);
    }

    public Cookie getCookieCSRF() {
        return cookieCSRF;
    }

    public static Cookie creaCookieContenenteJwtToken(JwtToken jwtToken) {
        return new Cookie(NOME_COOKIE_CSRF, jwtToken.generaTokenJsonCodificatoBase64UrlEncoded(),
                "Cookie contenente il token JWT il cui payload contiene il token CSRF");
    }

}
