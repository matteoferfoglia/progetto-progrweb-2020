package it.units.progrweb.utils.csrf;

import it.units.progrweb.utils.Cookie;
import it.units.progrweb.utils.jwt.JwtToken;

import java.util.NoSuchElementException;

import static it.units.progrweb.utils.jwt.JwtToken.creaJwtTokenDaStringaCodificata;

// TODO : refactoring : separare meglio la parte di csrf token da jwt token da cookie

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
    private static final String NOME_COOKIE_CSRF = "CSRF-TOKEN-JWT";       // TODO : creare variabile d'ambiente (parametrizzare)

    /** Nome del del cookie mantenuto con questo server per verificare l'identità del client
     * ({@link #creaCookieContenenteIdentificativoClient(String)}).*/
    public static final String NOME_COOKIE_CSRF_SUBJECT = "CSRF-TOKEN-SUBJECT";// TODO : creare variabile d'ambiente (parametrizzare)


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


    public static Cookie creaCookieContenenteJwtToken(JwtToken jwtToken) {
        return new Cookie(NOME_COOKIE_CSRF, jwtToken.generaTokenJsonCodificatoBase64UrlEncoded(),
                "Cookie contenente il token JWT il cui payload contiene il token CSRF");
    }

    public static Cookie creaCookieContenenteIdentificativoClient(String valoreIdentificativoClient) {
        return new Cookie(NOME_COOKIE_CSRF_SUBJECT, valoreIdentificativoClient,
                "Cookie contenente un valore identificativo per il client");
    }

    /**
     * Verifica un token CSRF. La verifica viene fatta in base ai cookie
     * (uno dei due cookie contiene il token JWT il cui payload contiene il
     * token CSRF inizialmente emesso ed un identificativo per il client
     * a cui quel CSRF token era stato rilasciato, l'altro cookie contiene
     * il sopracitato identificativo del client).
     * @param valoreTokenCsrfDaVerificare Il valore del token CSRF.
     * @param cookieHeader Cookie header ricevuto.
     * @param nomeClaimCsrfTokenInJwtPayload Nome del claim contenente il Csrf Token all'interno del token JWT.
     * @return true se il token csrf dato è valido (con riferimento ai cookie).
     */
    static boolean isCsrfTokenValido(String valoreTokenCsrfDaVerificare,
                                     String cookieHeader,
                                     String nomeClaimCsrfTokenInJwtPayload) {

        try {
            Cookie[] cookies = Cookie.trovaCookiesDaHeader(cookieHeader);
            Cookie cookieConJwtConCsrf = Cookie.cercaCookiePerNomeERestituiscilo(NOME_COOKIE_CSRF, cookies);
            JwtToken jwtTokenOttenutoDaCookie = creaJwtTokenDaStringaCodificata( cookieConJwtConCsrf.getValue() );
            String identificativoClientOttenutoDaCookie = Cookie.cercaCookiePerNomeERestituiscilo(NOME_COOKIE_CSRF_SUBJECT, cookies).getValue();

            return isCsrfTokenValido(
                    valoreTokenCsrfDaVerificare,
                    jwtTokenOttenutoDaCookie,
                    identificativoClientOttenutoDaCookie,
                    nomeClaimCsrfTokenInJwtPayload
            );
        } catch (NoSuchElementException eccezioneCookieNonTrovati) {
            // Restituisce false se non trova i cookie cercati
            return false;
        }

    }

    /**
     * Verifica che il CSRF token passato come parametro corrisponda a
     * quello indicato nel relativo token jwt e che sia stato usato dal
     * client a cui quel CSRF token era stato rilasciato.
     * Si verifica anche la validità del token JWT.
     * @param valoreCsrfTokenDaVerificare Token CSRF da verificare.
     * @param jwtTokenRicevutoContenenteValoreCsrfEValoreIdentificativoClient Token JWT contenente nel payload il CSRF token e l'identificativo del client.
     * @param valoreIdentificativoClientRicevuto Identificativo del client.
     * @param nomeClaimCsrfTokenInJwtPayload Nome del claim contenente il Csrf Token all'interno del token JWT.
     * @return true se il token specificato nel primo parametro è verificato
     * e valido, false altrimenti.
     */
    private static boolean isCsrfTokenValido(String valoreCsrfTokenDaVerificare,
                                             JwtToken jwtTokenRicevutoContenenteValoreCsrfEValoreIdentificativoClient,
                                             String valoreIdentificativoClientRicevuto,
                                             String nomeClaimCsrfTokenInJwtPayload) {

        String valoreCsrfTokenDaJwt = (String)jwtTokenRicevutoContenenteValoreCsrfEValoreIdentificativoClient
                .getValoreClaimByName(nomeClaimCsrfTokenInJwtPayload);

        String valoreIdentificativoClientDaJwt = (String)jwtTokenRicevutoContenenteValoreCsrfEValoreIdentificativoClient
                .getSubjectClaim();

        return jwtTokenRicevutoContenenteValoreCsrfEValoreIdentificativoClient.isTokenValido()
                && valoreCsrfTokenDaJwt.equals(valoreCsrfTokenDaVerificare)
                && valoreIdentificativoClientDaJwt.equals(valoreIdentificativoClientRicevuto);
    }
}
