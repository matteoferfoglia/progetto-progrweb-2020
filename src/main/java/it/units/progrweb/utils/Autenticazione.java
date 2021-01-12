package it.units.progrweb.utils;

import it.units.progrweb.entities.AuthenticationDatabaseEntry;
import it.units.progrweb.entities.attori.Attore;
import it.units.progrweb.entities.attori.nonAdministrator.consumer.Consumer;
import it.units.progrweb.entities.attori.nonAdministrator.consumer.ConsumerProxy;
import it.units.progrweb.utils.csrf.CsrfCookies;
import it.units.progrweb.utils.jwt.JwtToken;
import it.units.progrweb.utils.jwt.componenti.JwtPayload;
import it.units.progrweb.utils.jwt.componenti.claim.JwtClaim;
import it.units.progrweb.utils.jwt.componenti.claim.JwtExpirationTimeClaim;
import it.units.progrweb.utils.jwt.componenti.claim.JwtSubjectClaim;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
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

    /** Rappresentazione di un attore non presente nel database
     * di autenticazione (es. se credenziali non sono valide),
     * oppure di un attore non autenticato.*/
    private static final Attore ATTORE_NON_AUTENTICATO = null;

    /** Tempo in secondi durante il quale ad un attore che si è autenticato
     * non verranno chieste nuovamente le credenziali. */
    private static final int TIMEOUT_AUTENTICAZIONE_IN_SECONDI = 1800;

    /** Lunghezza del token CSRF associato al token di autenticazione di un client.*/
    private static final int LUNGHEZZA_TOKEN_CSRF_AUTENTICAZIONE = 128;

    /** Nome del claim contenente l'hash del token associato al client nel JWT di autenticazione.
     * Vedere anche {@link #creaJwtTokenAutenticazionePerAttore(Attore, String)}.*/
    private static final String NOME_CLAIM_JWT_CON_HASH_COOKIE_AUTENTICAZIONE = "hash-csrf-autenticazione";

    /** Nome del cookie contenente un token per il client. L'hash di questo valore
     * è presente nel token di autenticazione del client, nel claim con nome specificato
     * in {@link #NOME_CLAIM_JWT_CON_HASH_COOKIE_AUTENTICAZIONE}.*/
    private static final String NOME_COOKIE_CLIENT_TOKEN = "TOKEN-ID-CLIENT-AUTENTICAZIONE";

    /** Nelle richieste HTTP, è il nome dell' header contenente il token di autenticazione.*/
    private static final String NOME_HEADER_AUTHORIZATION = "Authorization";

    /** Nelle risposte HTTP (server verso client), è il nome dell' header
     * che richiede l'autenticazione del client.*/
    private static final String NOME_HEADER_AUTHENTICATE = "WWW-Authenticate";

    /** Tipo di Autenticazione HTTP richiesta.*/
    private static final String TIPO_AUTENTICAZIONE_RICHIESTA = "Bearer";



    /** Restituisce l'attore corrispondente alle credenziali date
     * come parametri se le credenziali sono valide, {@link #ATTORE_NON_AUTENTICATO} altrimenti.*/
    public static Attore getAttoreDaCredenziali(String username, String password) {
        // TODO : cercare le credenziali nel database di autenticazione e restituire l'attore corrispondente
        // TODO : restituire ATTORE_NON_AUTENTICATO se credenziali invalide

        if( password.equals("pippo") ) {  // TODO : questo metodo è da implementare !!! (TUTTO!! )
             // Creazione di un consumer (ed imposto id non nullo) // TODO : cancellare questa parte (qua bisognerebbe prelevare utente da db, non crearlo)
            Consumer consumer = new ConsumerProxy("username", "UtenteTest", "test@example.com");
            {
                try {
                    Field idField = consumer.getClass().getSuperclass().getSuperclass() // field di Attore
                                            .getDeclaredField("identificativoAttore");
                    idField.setAccessible(true);
                    Long valoreId = Long.valueOf(10);   // todo : VALORE A CASO MESSO SOLO PER FAR FUNZIONARE, MA ANCORA DA IMPLEMENTARE QUESTO METODO
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
                       .header(NOME_HEADER_AUTHENTICATE, TIPO_AUTENTICAZIONE_RICHIESTA)   // invita il client ad autenticarsi
                       .entity("Credenziali invalide")                                    // body della response
                       .build();
    }

    /** Crea una risposta HTTP grazie alla quale sovrascrive il cookie il cui
     * valore è necessario per validare il token di autenticazione (vedere
     * {@link #creaResponseAutenticazione(String, String)}), rendendo
     * quest'ultimo inutilizzabile dal client.
     * Questo metodo non ha alcun effetto se il browser usato dal client non
     * sovrascrive il cookie come prescritto dall'header <i>SetCookie</i>
     * della risposta creata da questo metodo.*/    // TODO : creare db di token invalidi ed aggiungere un token quando si fa logout e mantenerlo in quel db finché non scade
    public static Response creaResponseLogout() {

        Cookie cookieIdClientSovrascritto = CsrfCookies.creaCookieContenenteIdentificativoClient(NOME_COOKIE_CLIENT_TOKEN, "deleted",0);

        return Response.ok()
                       .cookie(cookieIdClientSovrascritto)
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
            Cookie cookieIdClientPerAutenticazione = CsrfCookies.creaCookieContenenteIdentificativoClient(NOME_COOKIE_CLIENT_TOKEN,
                                                                                                          valoreTokenCsrfAutenticazione,
                                                                                                          TIMEOUT_AUTENTICAZIONE_IN_SECONDI);
            String tokenAutenticazione = Autenticazione.creaJwtTokenAutenticazionePerAttore(attore, valoreTokenCsrfAutenticazione);

            return Response.ok()
                           .cookie(cookieIdClientPerAutenticazione)
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
     * Inoltre, la request deve contenere il cookie il cui hash del valore corrisponde
     * a quello indicato nel claim di nome specificato in
     * {@link #NOME_CLAIM_JWT_CON_HASH_COOKIE_AUTENTICAZIONE} contenuto nel payload
     * del token appena verificato.
     * @return true se il client è autenticato, false altrimenti.*/
    public static boolean isClientAutenticato(HttpServletRequest httpServletRequest) {

        String tokenAutenticazioneBearer = getTokenAutenticazioneBearer(httpServletRequest);

        if (tokenAutenticazioneBearer == null)
            return false;

        // Calcolo del JWT token ottenuto dall'authorization header
        JwtToken jwtTokenAutenticazione = null;
        {
            boolean isStringaNonNullaNonVuota = ! (tokenAutenticazioneBearer==null
                                                    || tokenAutenticazioneBearer.trim().isEmpty()) ;
            if( isStringaNonNullaNonVuota )
                jwtTokenAutenticazione = JwtToken.creaJwtTokenDaStringaCodificata(tokenAutenticazioneBearer);
        }

        // Cookie ricevuti in questa richiesta HTTP
        Cookie[] cookiesDaQuestaRichiestaHttp = Cookie.getCookieDaRichiestaHttp(httpServletRequest);

        return jwtTokenAutenticazione!= null
                && jwtTokenAutenticazione.isTokenValido()
                && isStessoHashCookieIdNelToken(jwtTokenAutenticazione, cookiesDaQuestaRichiestaHttp);

    }

    /** Data una HttpServletRequest, restituisce il token di autenticazione
     * se presente, null altrimenti.*/
    private static String getTokenAutenticazioneBearer(HttpServletRequest httpServletRequest) {
        String tokenAutenticazioneBearer = httpServletRequest.getHeader(NOME_HEADER_AUTHORIZATION);  // prende header con token
        if( tokenAutenticazioneBearer==null )
            return null;

        tokenAutenticazioneBearer = tokenAutenticazioneBearer.replaceAll(".+ ", "");    // rimuove tutto ciò che precede il token (rimuove "Bearer ")
        return tokenAutenticazioneBearer;
    }

    /** Verifica che l'hash (calcolato con la password del client) del valore del cookie
     * contenente un token casuale associato al client (vedere
     * {@link #creaResponseAutenticazione(String, String)}) corrisponda al valore indicato
     * nel token di autenticazione.
     * @param jwtTokenAutenticazione Il token ricevuto nell'header di autenticazione.
     * @param cookies Array di cookie ricevuti dal client in questa richiesta HTTP.
     * @return true se la verifica va a buon fine, false altrimenti.*/
    private static boolean isStessoHashCookieIdNelToken(@NotNull JwtToken jwtTokenAutenticazione,
                                                        Cookie[] cookies) {

        String identificativoAttoreDaToken = jwtTokenAutenticazione.getValoreSubjectClaim();
        String hashNelTokenAutenticazione = (String) jwtTokenAutenticazione.getValoreClaimByName(NOME_CLAIM_JWT_CON_HASH_COOKIE_AUTENTICAZIONE);
        String valoreCookieId = Cookie.cercaCookiePerNomeERestituiscilo(NOME_COOKIE_CLIENT_TOKEN, cookies)
                                      .getValue();
        Attore attoreCheStaAutenticandosi = Attore.getAttoreById(Long.valueOf(identificativoAttoreDaToken));

        String hashPasswordClient = AuthenticationDatabaseEntry.getHashedSaltedPasswordDellAttore(attoreCheStaAutenticandosi);
        String hashValoreCookieId;
        try {
            hashValoreCookieId = GestoreSicurezza.hmacSha256(valoreCookieId, hashPasswordClient);
        } catch (NoSuchAlgorithmException|InvalidKeyException e) {
            Logger.scriviEccezioneNelLog(Autenticazione.class, e);
            return false;
        }

        return hashNelTokenAutenticazione.equals(hashValoreCookieId);

    }

    /** Crea un Jwt Token che certifica l'autenticazione dell'attore indicato
     * nel parametro, quindi lo codifica in base64 url-encoded e lo restituisce.
     * @param attore
     * @param valoreCookieId è il valore del cookie associato all'attore che si sta autenticando.
     * @throws InvalidKeyException generata da {@link GestoreSicurezza#hmacSha256(String)}.
     * @throws NoSuchAlgorithmException generata da {@link GestoreSicurezza#hmacSha256(String)}.*/
    private static String creaJwtTokenAutenticazionePerAttore(Attore attore,
                                                              String valoreCookieId)
            throws InvalidKeyException, NoSuchAlgorithmException {

        String hashPasswordAttore = AuthenticationDatabaseEntry.getHashedSaltedPasswordDellAttore(attore);

        JwtPayload jwtPayload = new JwtPayload();
        jwtPayload.aggiungiClaim(new JwtSubjectClaim(String.valueOf(attore.getIdentificativoAttore())));
        jwtPayload.aggiungiClaim(new JwtExpirationTimeClaim(TIMEOUT_AUTENTICAZIONE_IN_SECONDI));
        jwtPayload.aggiungiClaim(new JwtClaim(NOME_CLAIM_JWT_CON_HASH_COOKIE_AUTENTICAZIONE,
                                              GestoreSicurezza.hmacSha256(valoreCookieId, hashPasswordAttore)) );

        return new JwtToken(jwtPayload).generaTokenJsonCodificatoBase64UrlEncoded();
        
    }

    /** Ricerca l'attore nel database in base al token di autenticazione
     * della richiesta giunta dal client: se trova l'attore nel database,
     * lo restituisce, altrimenti restituisce {@link #ATTORE_NON_AUTENTICATO}.*/
    public static Attore getAttoreDaTokenAutenticazione(@NotNull String tokenAutenticazione) {

        // TODO : da testare

        JwtToken jwtTokenAutenticazione = JwtToken.creaJwtTokenDaStringaCodificata(tokenAutenticazione);
        String idAttore = jwtTokenAutenticazione.getValoreSubjectClaim();
        Attore attore = Attore.getAttoreById(Long.valueOf(idAttore));

        return attore;
    }

    /** Data una HttpServletRequest, restituisce l'attore autenticato
     * per quella HttpServletRequest. Se la richiesta proviene da un client
     * che non si è autenticato oppure se l'autenticazione non è valida,
     * allora restituisce {@link #ATTORE_NON_AUTENTICATO}.*/
    public static Attore getAttoreDaHttpServletRequest(HttpServletRequest httpServletRequest) {

        // TODO : da testare

        String tokenAutenticazione = getTokenAutenticazioneBearer(httpServletRequest);
        Attore attore = getAttoreDaTokenAutenticazione(tokenAutenticazione);

        return attore;

    }

    /** Invia una risposta al client indicando che non è autorizzato.*/
    public static void rispondiNonAutorizzato(HttpServletResponse response)
            throws IOException {
        response.sendError( Response.Status.UNAUTHORIZED.getStatusCode(),
                            Response.Status.UNAUTHORIZED.getReasonPhrase() );
    }
}
