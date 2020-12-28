package it.units.progrweb.utils;

import it.units.progrweb.utils.JWT.JwtToken;
import it.units.progrweb.utils.JWT.component.JwtPayload;
import it.units.progrweb.utils.JWT.component.claim.JwtClaim;
import it.units.progrweb.utils.JWT.component.claim.JwtExpirationTimeClaim;
import it.units.progrweb.utils.JWT.component.claim.JwtSubjectClaim;

import javax.ws.rs.core.NewCookie;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.NoSuchElementException;

import static it.units.progrweb.utils.JWT.JwtToken.creaJwtTokenDaStringaCodificata;
import static it.units.progrweb.utils.TokenGenerator.generaTokenAlfanumerico;


/**
 * Questa classe rappresenta un token CSRF.
 * Tale token viene inviato tramite token JWT (il token CSRF sarà
 * un claim nel payload del token JWT) ed il token JWT a sua volta
 * sarà inviato al client tramite cookie, infatti questa classe
 * fornisce il metodo per la creazione di tale cookie
 * ({@link #creaCookieContenenteJWTToken() metodo per la creazione
 * del cookie con il token JWT}).
 * Inoltre, per verificare che il client che richiede il token CSRF
 * sia lo stesso ad usarlo, questa classe genera un identificativo per
 * il client e fornisce il {@link #creaCookieContenenteIdentificativoClient()
 * metodo per la creazione del cookie avente per valore l'identificativo
 * del client}.
 * Quest'ultimo valore identificativo del client viene aggiunto anche
 * come claim del JWT token di cui sopra (così è possibile effettuare
 * una "verifica incrociata" sull'identità del client).
 */
public class CsrfToken {

    /** Nome del cookie che si occuperà di trasmettere il token CSRF. {@link #creaCookieContenenteJWTToken()}. */
    private static final String NOME_COOKIE_CSRF = "CSRF-TOKEN-JWT";       // TODO : creare variabile d'ambiente (parametrizzare)

    /** Nome del claim JWT il cui valore è il CSRF-token. */
    private static final String NOME_CLAIM_CSRF_TOKEN = "CSRF-TOKEN";      // TODO : creare variabile d'ambiente (parametrizzare)

    /** Nome del del cookie mantenuto con questo server per verificare l'identità del client. */
    private static final String NOME_COOKIE_CSRF_SUBJECT = "CSRF-TOKEN-SUBJECT";// TODO : creare variabile d'ambiente (parametrizzare)

    /** Durata in secondi dei cookie e del token JWT. */
    private static final int DURATA_TOKEN_IN_SECONDI = 30*60;               // TODO : creare variabile d'ambiente (parametrizzare)


    /** Valore di questa istanza di CSRF token.*/
    private final String CSRFToken;

    /** Valore identificativo per il client a cui questo CSRF token è stato rilasciato.*/
    private final String tokenIdentificativoClient;

    /** JWT token contenente il valore di questa istanza di CSRF token
     * e dell'identificativo del client che lo ha richiesto. */
    private final JwtToken jwtToken;


    /**
     * Crea il CSRF token.
     * @param csrf_token_length Lunghezza del token CSRF.
     * @param id_client_length Lunghezza dell'identificativo del client che ha richiesto il token CSRF.
     * @throws InvalidKeyException generata da {@link it.units.progrweb.utils.SecurityManager#hmacSha256(String)}
     * @throws NoSuchAlgorithmException generata da {@link it.units.progrweb.utils.SecurityManager#hmacSha256(String)}
     */
    public CsrfToken(int csrf_token_length, int id_client_length)
            throws NoSuchAlgorithmException, InvalidKeyException {

        this.CSRFToken = generaTokenAlfanumerico(csrf_token_length);
        this.tokenIdentificativoClient = generaTokenAlfanumerico(id_client_length);
        this.jwtToken = creaJwtToken();
    }



    /**
     * Crea un token JWT nel cui payload vi sono il {@link #CSRFToken CSRF token}
     * e l'{@link #tokenIdentificativoClient identificativo del client} che
     * ha fatto richiesta del token CSRF.
     * @throws InvalidKeyException generata da {@link it.units.progrweb.utils.SecurityManager#hmacSha256(String)}
     * @throws NoSuchAlgorithmException generata da {@link it.units.progrweb.utils.SecurityManager#hmacSha256(String)}
     */
    public JwtToken creaJwtToken()
            throws InvalidKeyException, NoSuchAlgorithmException {

        JwtPayload jwtPayload;
        {
            // Creazione payload JWT
            jwtPayload = new JwtPayload();
            jwtPayload.addClaim(new JwtExpirationTimeClaim(DURATA_TOKEN_IN_SECONDI));
            jwtPayload.addClaim(new JwtSubjectClaim(tokenIdentificativoClient));
            jwtPayload.addClaim(new JwtClaim(NOME_CLAIM_CSRF_TOKEN, CSRFToken));
        }

        return new JwtToken(jwtPayload);
    }

    public String getValoreCSRFToken() {
        return CSRFToken;
    }

    /**
     * Verifica che il CSRF token passato come parametro corrisponda a
     * quello indicato nel relativo cookie e che sia stato usato dal
     * client a cui quel CSRF token era stato rilasciato.
     * Si verifica anche la validità del token JWT.
     * @param valoreCsrfTokenDaVerificare Token CSRF da verificare.
     * @param cookieConJwtToken Cookie con il token JWT contenente il CSRF token nel payload.
     * @param cookiePerVerificaIdentitaClient Cookie con l'identificativo del client.
     * @return true se il token specificato nel primo parametro è verificato
     * e valido, false altrimenti.
     */
    private static boolean isCSRFTokenValido(String valoreCsrfTokenDaVerificare,
                                             NewCookie cookieConJwtToken,
                                             NewCookie cookiePerVerificaIdentitaClient) {

        // Nell'implementazione attuale, il cookieConCsrfToken contiene il token JWT
        //  nel cui payload è specificato il valore del token csrf e l'identificativo
        //  del client a cui era stato rilasciato.
        boolean isTokenValido;

        try {
            JwtToken jwtToken = creaJwtTokenDaStringaCodificata(cookieConJwtToken.getValue());
            String valoreCsrfTokenDalCookie = jwtToken.getValoreClaimByName(NOME_CLAIM_CSRF_TOKEN);
            String identificativoClient = jwtToken.getSubjectClaim();

            isTokenValido = jwtToken.isTokenValido()
                                && valoreCsrfTokenDalCookie.equals(valoreCsrfTokenDaVerificare)
                                && identificativoClient.equals(cookiePerVerificaIdentitaClient.getValue());

        } catch (NoSuchElementException noSuchElementException) {
            isTokenValido = false;
        }

        return isTokenValido;
    }

    /**
     * Verifica un token CSRF. La verifica viene fatta in base ai cookie
     * (vedere {@link CsrfToken questa classe}).
     * @param valoreTokenCsrf Il valore del token CSRF.
     * @param cookieHeader Cookie header ricevuto.
     * @return true se il token csrf dato è valido (con riferimento ai cookie).
     */
    public static boolean isCsrfTokenValido(String valoreTokenCsrf, String cookieHeader) {

        // cookieHeader è una stringa del tipo: nomeCookie1=valoreCookie1; nomeCookie2=valoreCookie2; ...
        NewCookie[] cookies = Arrays.stream(cookieHeader.split("; "))
                                                  .map(NewCookie::valueOf)
                                                  .toArray(NewCookie[]::new);

        try {
            return isCSRFTokenValido(valoreTokenCsrf,
                    cercaCookiePerNomeERestituisciIlCookie(NOME_COOKIE_CSRF, cookies),
                    cercaCookiePerNomeERestituisciIlCookie(NOME_COOKIE_CSRF_SUBJECT, cookies));
        } catch (NoSuchElementException noSuchElementException) {
            return false;
        }

    }

    /**
     * @throws NoSuchElementException se il cookie cercato non è presente.
     */
    private static NewCookie cercaCookiePerNomeERestituisciIlCookie(String nomeCookieDaCercare, NewCookie[] cookies)
                    throws NoSuchElementException{

        return Arrays.stream(cookies)
                     .filter(cookie -> cookie.getName().equals(nomeCookieDaCercare))
                     .findAny()
                     .orElseThrow(NoSuchElementException::new);
    }

    public NewCookie creaCookieContenenteJWTToken() {
        return creaCookie(NOME_COOKIE_CSRF, jwtToken.generaTokenJson(),
                "Cookie contenente un valore identificativo per il client");
    }

    public NewCookie creaCookieContenenteIdentificativoClient() {
        return creaCookie(NOME_COOKIE_CSRF_SUBJECT, tokenIdentificativoClient,
                "Cookie contenente il token JWT il cui payload contiene il token CSRF");
    }

    private NewCookie creaCookie(String nomeCookie, String valoreCookie, String descrizioneCookie){

        // TODO : creare una classe helper per i cookie

        // Parametri per il cookie (https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Set-Cookie)
        final String percorsoCookie = "/";  // The forward slash (/) character is interpreted as a directory separator, and subdirectories will be matched as well: for Path=/docs, /docs, /docs/Web/, and /docs/Web/HTTP will all match.
        final String hostDomain = "";       // Host to which the cookie will be sent. If omitted, defaults to the host of the current document URL, not including subdomains.
        final int maxAge = DURATA_TOKEN_IN_SECONDI;
        final boolean secureCookie = false; // TODO : metterlo come variabile d'ambiente
        final boolean httpOnly = true;       // TODO : metterlo come variabile d'ambiente

        return new NewCookie(nomeCookie, valoreCookie, percorsoCookie, hostDomain,
                descrizioneCookie, maxAge, secureCookie, httpOnly);

    }

}