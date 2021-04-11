package it.units.progrweb.utils.csrf;

import it.units.progrweb.utils.Cookie;
import it.units.progrweb.utils.GestoreSicurezza;
import it.units.progrweb.utils.jwt.JwtToken;
import it.units.progrweb.utils.jwt.componenti.JwtPayload;
import it.units.progrweb.utils.jwt.componenti.claims.JwtClaim;
import it.units.progrweb.utils.jwt.componenti.claims.JwtExpirationTimeClaim;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.NoSuchElementException;

import static it.units.progrweb.utils.GeneratoreTokenCasuali.generaTokenAlfanumerico;
import static it.units.progrweb.utils.jwt.JwtToken.creaJwtTokenDaStringaCodificata;


/**
 * Questa classe rappresenta un token CSRF.
 * Tale token viene inviato tramite token JWT (il token CSRF sarà
 * un claims nel payload del token JWT) ed il token JWT a sua volta
 * sarà inviato al client tramite cookie (vedere
 * {@link CsrfCookie}).
 * Inoltre, per verificare che il client che richiede il token CSRF
 * sia lo stesso ad usarlo, questa classe genera un identificativo per
 * il client e crea un apposito cookie (vedere {@link CsrfCookie}).
 * Quest'ultimo valore identificativo del client viene aggiunto anche
 * come claims del JWT token di cui sopra (così è possibile effettuare
 * una "verifica incrociata" sull'identità del client).
 * All'interno del token JWT vi è anche un claims contenente l'indirizzo
 * IP del client che ha richiesto il token CSRF: al momento della verifica
 * del token CSRF, si verifica che venga utilizzato dal client con lo
 * stesso indirizzo IP (è una misura di sicurezza aggiuntiva).
 *
 * @author Matteo Ferfoglia
 */
public class CsrfToken {

    /** Durata in secondi del token JWT. */
    private static final int DURATA_TOKEN_CSRF_IN_SECONDI = 30*60;


    /** Valore di questa istanza di CSRF token.*/
    private final String valoreCsrfToken;

    /** JWT token contenente il valore di questa istanza di CSRF token
     * e dell'identificativo del client che lo ha richiesto. */
    private final JwtToken jwtToken;


    /**
     * Crea il CSRF token.
     * @param csrf_token_length Lunghezza del token CSRF.
     * @param indirizzoIPClient Indirizzo IP del client che ha fatto richiesta del CSRF token.
     * @throws InvalidKeyException generata da {@link GestoreSicurezza#hmacSha256(String)}
     * @throws NoSuchAlgorithmException generata da {@link GestoreSicurezza#hmacSha256(String)}
     */
    public CsrfToken(int csrf_token_length, String indirizzoIPClient)
            throws NoSuchAlgorithmException, InvalidKeyException {

        this.valoreCsrfToken = generaTokenAlfanumerico(csrf_token_length);
        this.jwtToken = creaTokenJwt(indirizzoIPClient);

    }

    /** Metodo per la creazione del token JWT contenente il token CSRF. */
    private JwtToken creaTokenJwt(String indirizzoIPClient)
            throws NoSuchAlgorithmException, InvalidKeyException {

        JwtPayload jwtPayload = new JwtPayload();
        jwtPayload.aggiungiClaim(new JwtExpirationTimeClaim(DURATA_TOKEN_CSRF_IN_SECONDI));
        jwtPayload.aggiungiClaim(new JwtClaim<>(JwtClaim.NomeClaim.CSRF_TOKEN.nomeClaim(), valoreCsrfToken));
        jwtPayload.aggiungiClaim(new JwtClaim<>(JwtClaim.NomeClaim.IP_CLIENT.nomeClaim(), indirizzoIPClient));

        return new JwtToken(jwtPayload);
    }

    public String getValoreCsrfToken() {
        return valoreCsrfToken;
    }

    /**
     * Verifica la validità del token CSRF, il quale è gestito tramite
     * cookie, quindi è la classe {@link CsrfCookie} ad occuparsene.
     * @param valoreTokenCsrf Valore del token CSRF da verificare
     * @param cookieHeader Headere della richiesta HTTP che necessita delle validazione CSRF.
     * @param indirizzoIPClient Indirizzo IP del client da cui proviene la richiesta.
     */
    public static boolean isCsrfTokenValido(String valoreTokenCsrf,
                                            String cookieHeader,
                                            String indirizzoIPClient) {

        try {
            Cookie[] cookies = Cookie.trovaCookiesDaHeader(cookieHeader);
            Cookie cookieConJwtConCsrf = Cookie.cercaCookiePerNomeERestituiscilo(CsrfCookie.NOME_COOKIE_CSRF, cookies);

            String jwtTokenBase64UrlEncoded = cookieConJwtConCsrf.getValue();   // JWT con CSRF token nel payload

            JwtToken jwtTokenRicevutoContenenteValoreCsrfEValoreIdentificativoClient =
                    creaJwtTokenDaStringaCodificata( jwtTokenBase64UrlEncoded );

            String valoreCsrfTokenDaJwt = String.valueOf( jwtTokenRicevutoContenenteValoreCsrfEValoreIdentificativoClient
                    .getValoreClaimByName(JwtClaim.NomeClaim.CSRF_TOKEN) );

            String valoreIndirizzoIPClientDaJwt = String.valueOf( jwtTokenRicevutoContenenteValoreCsrfEValoreIdentificativoClient
                    .getValoreClaimByName(JwtClaim.NomeClaim.IP_CLIENT) );

            return JwtToken.isTokenValido(jwtTokenBase64UrlEncoded)
                    && valoreCsrfTokenDaJwt.equals(valoreTokenCsrf)
                    && valoreIndirizzoIPClientDaJwt.equals(indirizzoIPClient);

        } catch (NoSuchElementException | IllegalArgumentException e) {
            // Restituisce false se non trova i cookie cercati o se il token di autenticazione è mal formato
            return false;
        }

    }


    /**
     * Vedere la descrizione delle classe {@link CsrfCookie CsrfCookie}.
     */
    public CsrfCookie creaCookiePerCsrf() {

        return new CsrfCookie(
                CsrfCookie.creaCookieContenenteJwtToken(jwtToken),
                DURATA_TOKEN_CSRF_IN_SECONDI
        );
    }

}