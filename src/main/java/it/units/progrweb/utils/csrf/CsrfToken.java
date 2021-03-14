package it.units.progrweb.utils.csrf;

import it.units.progrweb.utils.Cookie;
import it.units.progrweb.utils.GestoreSicurezza;
import it.units.progrweb.utils.jwt.JwtToken;
import it.units.progrweb.utils.jwt.componenti.JwtPayload;
import it.units.progrweb.utils.jwt.componenti.claim.JwtClaim;
import it.units.progrweb.utils.jwt.componenti.claim.JwtExpirationTimeClaim;
import it.units.progrweb.utils.jwt.componenti.claim.JwtSubjectClaim;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.NoSuchElementException;

import static it.units.progrweb.utils.GeneratoreTokenCasuali.generaTokenAlfanumerico;
import static it.units.progrweb.utils.jwt.JwtToken.creaJwtTokenDaStringaCodificata;


/**
 * Questa classe rappresenta un token CSRF.
 * Tale token viene inviato tramite token JWT (il token CSRF sarà
 * un claim nel payload del token JWT) ed il token JWT a sua volta
 * sarà inviato al client tramite cookie (vedere
 * {@link CsrfCookies}).
 * Inoltre, per verificare che il client che richiede il token CSRF
 * sia lo stesso ad usarlo, questa classe genera un identificativo per
 * il client e crea un apposito cookie (vedere {@link CsrfCookies}).
 * Quest'ultimo valore identificativo del client viene aggiunto anche
 * come claim del JWT token di cui sopra (così è possibile effettuare
 * una "verifica incrociata" sull'identità del client).
 * All'interno del token JWT vi è anche un claim contenente l'indirizzo
 * IP del client che ha richiesto il token CSRF: al momento della verifica
 * del token CSRF, si verifica che venga utilizzato dal client con lo
 * stesso indirizzo IP (è una misura di sicurezza aggiuntiva).
 *
 * @author Matteo Ferfoglia
 */
public class CsrfToken {

    /** Nome del claim JWT il cui valore è il CSRF-token. */
    public static final String NOME_CLAIM_CSRF_TOKEN = "CSRF-TOKEN";

    /** Nome del claim JWT il cui valore è l'indirizzo IP
     * del client a cui è stato rilasciato il token CSRF.*/
    public static final String NOME_CLAIM_IP_CLIENT = "IP-CLIENT";

    /** Durata in secondi del token JWT. */
    private static final int DURATA_TOKEN_CSRF_IN_SECONDI = 30*60;


    /** Valore di questa istanza di CSRF token.*/
    private final String valoreCsrfToken;

    /** Valore identificativo per il client a cui questo CSRF token è stato rilasciato.*/
    private final String valoreIdentificativoClient;

    /** Valore indirizzo IP del client a cui questo CSRF token è stato rilasciato.*/
    private final String indirizzoIPClient;

    /** JWT token contenente il valore di questa istanza di CSRF token
     * e dell'identificativo del client che lo ha richiesto. */
    private final JwtToken jwtToken;


    /**
     * Crea il CSRF token.
     * @param csrf_token_length Lunghezza del token CSRF.
     * @param id_client_length Lunghezza dell'identificativo del client che ha richiesto il token CSRF.
     * @param indirizzoIPClient Indirizzo IP del client che ha fatto richiesta del CSRF token.
     * @throws InvalidKeyException generata da {@link GestoreSicurezza#hmacSha256(String)}
     * @throws NoSuchAlgorithmException generata da {@link GestoreSicurezza#hmacSha256(String)}
     */
    public CsrfToken(int csrf_token_length, int id_client_length, String indirizzoIPClient)
            throws NoSuchAlgorithmException, InvalidKeyException {

        this.valoreCsrfToken = generaTokenAlfanumerico(csrf_token_length);
        this.valoreIdentificativoClient = generaTokenAlfanumerico(id_client_length);
        this.indirizzoIPClient = indirizzoIPClient;

        {
            // Creazione del token JWT contenente il token CSRF
            JwtPayload jwtPayload = new JwtPayload();
            jwtPayload.aggiungiClaim(new JwtExpirationTimeClaim(DURATA_TOKEN_CSRF_IN_SECONDI));
            jwtPayload.aggiungiClaim(new JwtSubjectClaim(valoreIdentificativoClient));
            jwtPayload.aggiungiClaim(new JwtClaim<>(NOME_CLAIM_CSRF_TOKEN, valoreCsrfToken));
            jwtPayload.aggiungiClaim(new JwtClaim<>(NOME_CLAIM_IP_CLIENT, indirizzoIPClient));

            this.jwtToken = new JwtToken(jwtPayload);
        }

    }

    public String getValoreCsrfToken() {
        return valoreCsrfToken;
    }

    public String getValoreIPClient() {
        return indirizzoIPClient;
    }

    public JwtToken getJwtToken() {
        return jwtToken;
    }

    public String getValoreIdentificativoClient() {
        return valoreIdentificativoClient;
    }

    /**
     * Verifica la validità del token CSRF, il quale è gestito tramite
     * cookie, quindi è la classe {@link CsrfCookies} ad occuparsene.
     * Vedere{@link #isCsrfTokenValido(String, JwtToken, String, String, String, String)}.
     * @param valoreTokenCsrf Valore del token CSRF da verificare
     * @param cookieHeader Headere della richiesta HTTP che necessita delle validazione CSRF.
     * @param indirizzoIPClient Indirizzo IP del client da cui proviene la richiesta.
     */
    public static boolean isCsrfTokenValido(String valoreTokenCsrf,
                                            String cookieHeader,
                                            String indirizzoIPClient) {

        try {
            Cookie[] cookies = Cookie.trovaCookiesDaHeader(cookieHeader);
            Cookie cookieConJwtConCsrf = Cookie.cercaCookiePerNomeERestituiscilo(CsrfCookies.NOME_COOKIE_CSRF, cookies);
            JwtToken jwtTokenOttenutoDaCookie = creaJwtTokenDaStringaCodificata( cookieConJwtConCsrf.getValue() );
            String identificativoClientOttenutoDaCookie = Cookie.cercaCookiePerNomeERestituiscilo(CsrfCookies.NOME_COOKIE_ID_CLIENT, cookies).getValue();

            return isCsrfTokenValido(
                        valoreTokenCsrf,
                        jwtTokenOttenutoDaCookie,
                        identificativoClientOttenutoDaCookie,
                        indirizzoIPClient,
                        NOME_CLAIM_CSRF_TOKEN,
                        NOME_CLAIM_IP_CLIENT
                    );
        } catch (NoSuchElementException | IllegalArgumentException e) {
            // Restituisce false se non trova i cookie cercati o se il token di autenticazione è mal formato
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
     * @param valoreIndirizzoIPClientRicevuto Indirizzo IP del client che sta presentando il token CSRF da verificare.
     * @param nomeClaimIPClientInJwtPayload Nome del claim contenente l'indirizzo IP del client a cui era stato
     *                                      rilasciato il token CSRF all'interno del token JWT.
     * @return true se il token specificato nel primo parametro è verificato
     * e valido, false altrimenti.
     */
    private static boolean isCsrfTokenValido(String valoreCsrfTokenDaVerificare,
                                             JwtToken jwtTokenRicevutoContenenteValoreCsrfEValoreIdentificativoClient,
                                             String valoreIdentificativoClientRicevuto,
                                             String valoreIndirizzoIPClientRicevuto,
                                             String nomeClaimCsrfTokenInJwtPayload,
                                             String nomeClaimIPClientInJwtPayload ) {

        String valoreCsrfTokenDaJwt = (String)jwtTokenRicevutoContenenteValoreCsrfEValoreIdentificativoClient
                .getValoreClaimByName(nomeClaimCsrfTokenInJwtPayload);

        String valoreIdentificativoClientDaJwt = String.valueOf( jwtTokenRicevutoContenenteValoreCsrfEValoreIdentificativoClient
                .getValoreSubjectClaim() );

        String valoreIndirizzoIPClientDaJwt = (String)jwtTokenRicevutoContenenteValoreCsrfEValoreIdentificativoClient
                .getValoreClaimByName(nomeClaimIPClientInJwtPayload);

        return jwtTokenRicevutoContenenteValoreCsrfEValoreIdentificativoClient.isTokenValido()
                && valoreCsrfTokenDaJwt.equals(valoreCsrfTokenDaVerificare)
                && valoreIdentificativoClientDaJwt.equals(valoreIdentificativoClientRicevuto)
                && valoreIndirizzoIPClientDaJwt.equals(valoreIndirizzoIPClientRicevuto);
    }


    /**
     * Vedere la descrizione delle classe {@link CsrfCookies CsrfCookies}.
     */
    public CsrfCookies creaCookiesPerCsrf() {

        return new CsrfCookies(
                CsrfCookies.creaCookieContenenteJwtToken(jwtToken),
                CsrfCookies.creaCookieContenenteIdentificativoClient(valoreIdentificativoClient),
                DURATA_TOKEN_CSRF_IN_SECONDI
        );
    }

}