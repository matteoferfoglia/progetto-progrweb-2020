package it.units.progrweb.utils.csrf;

import it.units.progrweb.utils.GestoreSicurezza;
import it.units.progrweb.utils.jwt.JwtToken;
import it.units.progrweb.utils.jwt.componenti.JwtPayload;
import it.units.progrweb.utils.jwt.componenti.claim.JwtClaim;
import it.units.progrweb.utils.jwt.componenti.claim.JwtExpirationTimeClaim;
import it.units.progrweb.utils.jwt.componenti.claim.JwtSubjectClaim;

import javax.ws.rs.core.Response;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import static it.units.progrweb.utils.GeneratoreTokenCasuali.generaTokenAlfanumerico;


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
    public static final String NOME_CLAIM_CSRF_TOKEN = "CSRF-TOKEN";        // TODO : creare variabile d'ambiente (parametrizzare)

    /** Nome del claim JWT il cui valore è l'indirizzo IP
     * del client a cui è stato rilasciato il token CSRF.*/
    public static final String NOME_CLAIM_IP_CLIENT = "IP-CLIENT";          // TODO : creare variabile d'ambiente (parametrizzare)

    /** Durata in secondi del token JWT. */
    private static final int DURATA_TOKEN_IN_SECONDI = 30*60;               // TODO : creare variabile d'ambiente (parametrizzare)


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
        this.jwtToken = creaJwtToken(); // TODO : questo dovrebbe essere un metodo statico di JwtToken
            // TODO : rendere creaJwtToken() metodo statico di JWT token e passargli i claim del payload
    }



    /**
     * Crea un token JWT nel cui payload vi sono il {@link #valoreCsrfToken CSRF token}
     * e l'{@link #valoreIdentificativoClient identificativo del client} che
     * ha fatto richiesta del token CSRF.
     * @throws InvalidKeyException generata da {@link GestoreSicurezza#hmacSha256(String)}
     * @throws NoSuchAlgorithmException generata da {@link GestoreSicurezza#hmacSha256(String)}
     */
    private JwtToken creaJwtToken()
            throws InvalidKeyException, NoSuchAlgorithmException {

        JwtPayload jwtPayload = creaJwtPayload(valoreIdentificativoClient, valoreCsrfToken, indirizzoIPClient);
        return new JwtToken(jwtPayload);
    }

    /**
     * Crea il JWT payload per {@link #creaJwtToken()} ()}.
     * @throws InvalidKeyException generata da {@link GestoreSicurezza#hmacSha256(String)}
     * @throws NoSuchAlgorithmException generata da {@link GestoreSicurezza#hmacSha256(String)}
     */
    private static JwtPayload creaJwtPayload(String valoreIdentificativoClient,
                                             String valoreCsrfToken,
                                             String indirizzoIPClient) {

        JwtPayload jwtPayload = new JwtPayload();

        jwtPayload.aggiungiClaim(new JwtExpirationTimeClaim(DURATA_TOKEN_IN_SECONDI));
        jwtPayload.aggiungiClaim(new JwtSubjectClaim(valoreIdentificativoClient));
        jwtPayload.aggiungiClaim(new JwtClaim(NOME_CLAIM_CSRF_TOKEN, valoreCsrfToken));
        jwtPayload.aggiungiClaim(new JwtClaim(NOME_CLAIM_IP_CLIENT, indirizzoIPClient));

        return jwtPayload;
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
     * Vedere{@link CsrfCookies#isCsrfTokenValido(String, String, String, String, String)}.
     */
    public static boolean isCsrfTokenValido(String valoreTokenCsrf,
                                            String cookieHeader,
                                            String indirizzoIPClient) {

        return CsrfCookies.isCsrfTokenValido(valoreTokenCsrf, cookieHeader, indirizzoIPClient,
                                             NOME_CLAIM_CSRF_TOKEN, NOME_CLAIM_IP_CLIENT);

    }


    /**
     * Vedere la descrizione delle classe {@link CsrfCookies CsrfCookies}.
     */
    public CsrfCookies creaCookiesPerCsrf() {

        return new CsrfCookies(
                CsrfCookies.creaCookieContenenteJwtToken(jwtToken),
                CsrfCookies.creaCookieContenenteIdentificativoClient(valoreIdentificativoClient),
                DURATA_TOKEN_IN_SECONDI
        );
    }


    /** Crea una {@link javax.ws.rs.core.Response} per segnalare
     * l'invalidità del token CSRF fornito.*/
    public static Response creaResponseCsrfTokenInvalido() {
        return Response.status(Response.Status.FORBIDDEN)
                       .entity("Token CSRF invalido. Ricaricare il form e riprovare.")
                       .build();
    }

}