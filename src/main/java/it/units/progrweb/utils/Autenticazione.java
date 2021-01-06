package it.units.progrweb.utils;

import it.units.progrweb.entities.AuthenticationDatabaseEntry;
import it.units.progrweb.entities.attori.Attore;
import it.units.progrweb.entities.attori.Consumer;
import it.units.progrweb.utils.csrf.CsrfCookies;
import it.units.progrweb.utils.jwt.JwtToken;
import it.units.progrweb.utils.jwt.componenti.JwtPayload;
import it.units.progrweb.utils.jwt.componenti.claim.JwtClaim;
import it.units.progrweb.utils.jwt.componenti.claim.JwtExpirationTimeClaim;
import it.units.progrweb.utils.jwt.componenti.claim.JwtSubjectClaim;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.lang.reflect.Field;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import static it.units.progrweb.utils.GeneratoreTokenCasuali.generaTokenAlfanumerico;

/**
 * Classe di utilità per gestire l'autenticazione degli utenti.
 * Per la procedura di autenticazione di un client, vedere il
 * metodo {@link #creaResponseAutenticazione(String, String)}.
 *
 * @author Matteo Ferfoglia
 */
public class Autenticazione {

    // TODO implementare questa classe

    /** Rappresentazione di un attore non autenticato (es. credenziali non valide)*/
    private static final Attore ATTORE_NON_AUTENTICATO = null;

    /** Tempo in secondi durante il quale ad un attore che si è autenticato
     * non verranno chieste nuovamente le credenziali. */
    private static final int TIMEOUT_AUTENTICAZIONE_IN_SECONDI = 1800;

    /** Lunghezza del token CSRF associato al token di autenticazione di un client.*/
    private static final int LUNGHEZZA_TOKEN_CSRF_AUTENTICAZIONE = 128;

    /** Nome del claim contenente l'hash del token CSRF nel JWT di autenticazione.
     * Vedere anche {@link #creaJwtTokenAutenticazionePerAttore(Attore, String)}.*/
    private static final String NOME_CLAIM_JWT_CON_HASH_CSRF_AUTENTICAZIONE = "hash-csrf-autenticazione";


    /** Restituisce l'attore corrispondente alle credenziali date
     * come parametri se le credenziali sono valide, {@link #ATTORE_NON_AUTENTICATO} altrimenti.*/
    public static Attore getAttoreDaCredenziali(String username, String password) {
        // TODO : cercare le credenziali nel database di autenticazione e restituire l'attore corrispondente
        // TODO : restituire ATTORE_NON_AUTENTICATO se credenziali invalide

        if( password.equals("pippo") ) {  // TODO : questo metodo è da implementare !!!
             // Creazione di un consumer (ed imposto id non nullo) // TODO : cancellare questa parte (qua bisognerebbe prelevare utente da db, non crearlo)
            Consumer consumer = new Consumer("username", "UtenteTest", "test@example.com");
            {
                try {
                    Field idField = consumer.getClass().getSuperclass().getSuperclass() // field di Attore
                                            .getDeclaredField("identificativoAttore");
                    idField.setAccessible(true);
                    Long valoreId = Long.valueOf(10);
                    idField.set(consumer, valoreId);
                } catch (NoSuchFieldException | IllegalAccessException e) {/* non dovrebbe mai capitare questa eccezione*/ }
            }
            return consumer;
        } else {
            return ATTORE_NON_AUTENTICATO;
        }

    }

    /** Restituisce true se l'attore è autenticato, false altrimenti.*/
    public static boolean isAttoreAutenticato(Attore attore) {
        return attore!=null && !attore.equals(ATTORE_NON_AUTENTICATO);    // TODO : verificare corretto funzionamento di questo metodo
    }

    /** Date le credenziali, restituisce una {@link javax.ws.rs.core.Response}
     * per un attore che si sta autenticando.
     * Procedura di autenticazione di un client: si verifica la validità delle
     * credenziali e, se <strong>non</strong> valide, si risponde con esito
     * negativo, altrimenti (se valide) si segue la seguente procedura:
     * <ol>
     *     <li>
     *         Creare un cookie (<i>HttpOnly</i>) contenente un token CSRF di
     *         lunghezza specificata in {@link #LUNGHEZZA_TOKEN_CSRF_AUTENTICAZIONE}
     *         (questo cookie sarà inviato al client insieme alla response).
     *     </li>
     *     <li>
     *         Calcolare, con la password (hashed e salted) del client, l'hash del
     *         token CSRF del punto precedente.
     *     </li>
     *     <li>
     *         Generare un token JWT (di autenticazione) con le informazioni del
     *         client ed aggiungervi il claim contenente l'hash del token CSRF
     *         calcolato al punto precedente.
     *     </li>
     *     <li>
     *         Inviare la response HTTP appena creata, con il token JWT nel body
     *         della response.
     *     </li>
     * </ol>
     * Il token di autenticazione è trasmesso nel body della response.
     * Il server che verificherà l'autenticazione del client dovrà ricalcolare l'hash
     * del token CSRF (valore nel cookie) e verificare che combaci con il valore nel
     * token JWT di autenticazione per poter considerare il client autenticato.*/
    public static Response creaResponseAutenticazione(String username, String password) {
        Attore attore = Autenticazione.getAttoreDaCredenziali(username, password);

        if(Autenticazione.isAttoreAutenticato(attore))
            return creaResponseAutenticazionePerAttoreAutenticato(attore);

        // Autenticazione fallita
        return Response.status(Response.Status.UNAUTHORIZED)
                       .header("WWW-Authenticate","Bearer")   // invita il client ad autenticarsi
                       .entity("Credenziali invalide")              // body della response
                       .build();
    }

    /** Metodo da invocare se un attore ha fornito le credenziali corrette.
     *
     * @param attore che ha fornito le credenziali corrette.
     * @return Response con token e cookie di autenticazione per l'attore.
     */
    private static Response creaResponseAutenticazionePerAttoreAutenticato(Attore attore) {

        try {
            String valoreTokenCsrfAutenticazione = generaTokenAlfanumerico(LUNGHEZZA_TOKEN_CSRF_AUTENTICAZIONE);
            Cookie cookieCsrfAutenticazione = CsrfCookies.creaCookieContenenteIdentificativoClient(valoreTokenCsrfAutenticazione,
                                                                                                   TIMEOUT_AUTENTICAZIONE_IN_SECONDI);
            String tokenAutenticazione = Autenticazione.creaJwtTokenAutenticazionePerAttore(attore, valoreTokenCsrfAutenticazione);

            return Response.ok()
                           .cookie(cookieCsrfAutenticazione)
                           .entity(tokenAutenticazione)
                           .type(MediaType.APPLICATION_FORM_URLENCODED) // token non è propriamente application/json
                           .build();
        } catch (NoSuchAlgorithmException|InvalidKeyException e) {
            Logger.scriviEccezioneNelLog(Autenticazione.class,
                    "Impossibile creare il token JWT di autenticazione.", e);

            return Response.serverError()
                           .build();
        }

    }

    /** Verifica se il client è autenticato in base agli header della richiesta HTTP.
     * La verifica dell'autenticazione è basata sul Bearer Token che dovrebbe essere
     * presente nell'header <i>Authorization</i> della request.
     * @return true se il client è autenticato, false altrimenti.*/
    public static boolean isClientAutenticato(HttpServletRequest httpServletRequest) {

        String tokenAutenticazioneBearer = httpServletRequest.getHeader("Authorization");  // prende header con token
        if( tokenAutenticazioneBearer==null )
            return false;

        tokenAutenticazioneBearer = tokenAutenticazioneBearer.replaceAll(".+ ", "");    // rimuove tutto ciò che precede il token (rimuove "Bearer ")
        return isTokenAutenticazioneValido(tokenAutenticazioneBearer);

    }

    /** Verifica la validità del token di autenticazione presentato da un client.*/
    private static boolean isTokenAutenticazioneValido(String tokenAutenticazione) {

        boolean isStringaNonNullaNonVuota = ! (tokenAutenticazione==null || tokenAutenticazione.trim().isEmpty()) ;
        if( isStringaNonNullaNonVuota ) {
            JwtToken jwtTokenAutenticazione = JwtToken.creaJwtTokenDaStringaCodificata(tokenAutenticazione);
            return jwtTokenAutenticazione.isTokenValido();
        }
        return false;
    }

    /** Crea un Jwt Token che certifica l'autenticazione dell'attore indicato
     * nel parametro, quindi lo codifica in base64 url-encoded e lo restituisce.
     * @throws InvalidKeyException generata da {@link GestoreSicurezza#hmacSha256(String)}.
     * @throws NoSuchAlgorithmException generata da {@link GestoreSicurezza#hmacSha256(String)}.*/
    private static String creaJwtTokenAutenticazionePerAttore(Attore attore,
                                                              String valoreTokenCsrfAutenticazione)
            throws InvalidKeyException, NoSuchAlgorithmException {

        String hashPasswordAttore = AuthenticationDatabaseEntry.getHashedSaltedPasswordDellAttore(attore);

        JwtPayload jwtPayload = new JwtPayload();
        jwtPayload.aggiungiClaim(new JwtSubjectClaim(String.valueOf(attore.getIdentificativoAttore())));
        jwtPayload.aggiungiClaim(new JwtExpirationTimeClaim(TIMEOUT_AUTENTICAZIONE_IN_SECONDI));
        jwtPayload.aggiungiClaim(new JwtClaim(NOME_CLAIM_JWT_CON_HASH_CSRF_AUTENTICAZIONE,
                                              GestoreSicurezza.hmacSha256(valoreTokenCsrfAutenticazione, hashPasswordAttore)) );

        return new JwtToken(jwtPayload).generaTokenJsonCodificatoBase64UrlEncoded();
        
    }

    /** Ricerca l'attore nel database in base all'authorization header
     * della richiesta giunta dal client: se trova l'attore nel database,
     * lo restituisce, altrimenti restituisce null.*/
    public static Attore getAttoreDaAuthorizationHeader(String authorizationHeader) {

        // TODO : da testare

        if(authorizationHeader==null)
            return null;

        String tokenAutorizzazioneComeString = authorizationHeader.replaceAll(".+ ", "");
        JwtToken tokenAutenticazione = JwtToken.creaJwtTokenDaStringaCodificata(tokenAutorizzazioneComeString);
        String idAttore = tokenAutenticazione.getValoreSubjectClaim();
        Attore attore = Attore.getAttoreById(Long.valueOf(idAttore));

        return attore;
    }
}
