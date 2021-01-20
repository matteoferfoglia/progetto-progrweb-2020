package it.units.progrweb.utils;

import it.units.progrweb.entities.AuthenticationDatabaseEntry;
import it.units.progrweb.entities.attori.Attore;
import it.units.progrweb.entities.attori.nonAdministrator.consumer.Consumer;
import it.units.progrweb.entities.attori.nonAdministrator.consumer.ConsumerProxy;
import it.units.progrweb.entities.attori.nonAdministrator.uploader.Uploader;
import it.units.progrweb.entities.attori.nonAdministrator.uploader.UploaderProxy;
import it.units.progrweb.utils.csrf.CsrfCookies;
import it.units.progrweb.utils.jwt.JwtToken;
import it.units.progrweb.utils.jwt.componenti.JwtPayload;
import it.units.progrweb.utils.jwt.componenti.claim.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.lang.reflect.Field;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.NoSuchElementException;

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
     * come parametri se le credenziali sono valide, {@link #ATTORE_NON_AUTENTICATO} altrimenti.
     * Vedere anche {@link #creaResponseAutenticazione(String, String)}.*/
    public static Attore getAttoreDaCredenziali(String username, String password) {

        boolean credenzialiCorrette =
                    AuthenticationDatabaseEntry.verificaCredenziali(username, password);

        if( credenzialiCorrette ) {
            return Attore.getAttorebyUsername( username );
        } else {
            //return ATTORE_NON_AUTENTICATO;


            // TODO : -----------------------------------------------------------------------
            //  -----------------------------------------------------------------------------
            //  SCOMMENTARE RIGA PRECEDENTE E
            //  CANCELLARE TUTTO IL SEGUITO (momentaneamente lasciato, finché siamo in sviluppo)
            //   (TUTTO CIò CHE è IN QUESTO RAMO DELL'IF )
            //  -----------------------------------------------------------------------------
            //  -----------------------------------------------------------------------------


            if( password.equals("pippo") ) {  // TODO : questo metodo è da implementare !!! (TUTTO!! )
                // Creazione di un consumer (ed imposto id non nullo) // TODO : cancellare questa parte (qua bisognerebbe prelevare utente da db, non crearlo)
                Consumer consumer = new ConsumerProxy("username", "UtenteTest", "test@example.com");
                try {
                    Field identificativoField = consumer.getClass().getSuperclass().getSuperclass().getSuperclass() // field di Attore
                            .getDeclaredField("identificativoAttore");
                    identificativoField.setAccessible(true);
                    Long valoreId = Long.valueOf(10);   // todo : VALORE A CASO MESSO SOLO PER FAR FUNZIONARE, MA ANCORA DA IMPLEMENTARE QUESTO METODO
                    identificativoField.set(consumer, valoreId);
                    Field nomeField = consumer.getClass().getSuperclass().getSuperclass().getSuperclass() // field di Attore
                            .getDeclaredField("nominativo");
                    nomeField.setAccessible(true);
                    String nome = "ConsumerTest";   // todo : VALORE A CASO MESSO SOLO PER FAR FUNZIONARE, MA ANCORA DA IMPLEMENTARE QUESTO METODO
                    nomeField.set(consumer, nome);
                } catch (NoSuchFieldException | IllegalAccessException e) {/* non dovrebbe mai capitare questa eccezione*/ }
                return consumer;
            } else if( password.equals("pluto") ) {       // TODO : questo metodo è da implementare !!! (TUTTO!! )
                // Creazione di un uploader (ed imposto id non nullo) // TODO : cancellare questa parte (qua bisognerebbe prelevare utente da db, non crearlo)
                Uploader uploader = new UploaderProxy("20", "MioNomeESuperUploader", "test@example.com", "data:image/jpeg;base64,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" );
                try {
                    Field identificativoField = uploader.getClass().getSuperclass().getSuperclass().getSuperclass() // field di Attore
                            .getDeclaredField("identificativoAttore");
                    identificativoField.setAccessible(true);
                    Long valoreId = Long.valueOf(55);   // todo : VALORE A CASO MESSO SOLO PER FAR FUNZIONARE, MA ANCORA DA IMPLEMENTARE QUESTO METODO
                    identificativoField.set(uploader, valoreId);
                    Field nomeField = uploader.getClass().getSuperclass().getSuperclass().getSuperclass() // field di Attore
                            .getDeclaredField("nominativo");
                    nomeField.setAccessible(true);
                    String nome = "UploaderTest";   // todo : VALORE A CASO MESSO SOLO PER FAR FUNZIONARE, MA ANCORA DA IMPLEMENTARE QUESTO METODO
                    nomeField.set(uploader, nome);
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    // non dovrebbe mai capitare questa eccezione
                }
                return uploader;
            }else {
                return ATTORE_NON_AUTENTICATO;
            }



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
        return creaResponseUnauthorized();
    }

    public static Response creaResponseUnauthorized() { // TODO : c'è anche il metodo rispondiNonAutorizzato() ... servono entrambi?
        return Response.status(Response.Status.UNAUTHORIZED)
                       .header(NOME_HEADER_AUTHENTICATE, TIPO_AUTENTICAZIONE_RICHIESTA)   // invita il client ad autenticarsi
                       .entity("Credenziali invalide")                                    // body della response
                       .build();
    }

    public static Response creaResponseForbidden(String messaggio) {
        return Response.status(Response.Status.FORBIDDEN)
                .entity(messaggio)
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

        try {
            JwtToken jwtTokenAutenticazione = getTokenDaHttpServletRequest( httpServletRequest );

            if( jwtTokenAutenticazione != null ) {
                // Cookie ricevuti in questa richiesta HTTP
                Cookie[] cookiesDaQuestaRichiestaHttp = Cookie.getCookieDaRichiestaHttp(httpServletRequest);

                return jwtTokenAutenticazione.isTokenValido()
                        && isStessoHashCookieIdNelToken(jwtTokenAutenticazione, cookiesDaQuestaRichiestaHttp);
            } else {
                return false;
            }
        } catch (Exception e) {
            // Verifica autenticazione può generare eccezioni
            return false;
        }

    }

    /** Data una HttpServletRequest, restituisce il token di autenticazione
     * se presente, null altrimenti.*/
    private static String getTokenAutenticazioneBearer(HttpServletRequest httpServletRequest) {
        String tokenAutenticazioneBearer = httpServletRequest.getHeader(NOME_HEADER_AUTHORIZATION);  // prende header con token
        if( tokenAutenticazioneBearer==null )
            return null;

        tokenAutenticazioneBearer = tokenAutenticazioneBearer.replaceAll(".+ ", "");    // rimuove tutto ciò che precede il token (prima dello spazio, cioè rimuove "Bearer ")
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

        Long identificativoAttoreDaToken = (Long)jwtTokenAutenticazione.getValoreSubjectClaim();
        String hashNelTokenAutenticazione = (String) jwtTokenAutenticazione.getValoreClaimByName(NOME_CLAIM_JWT_CON_HASH_COOKIE_AUTENTICAZIONE);
        String valoreCookieId;
        try {
            valoreCookieId = Cookie.cercaCookiePerNomeERestituiscilo(NOME_COOKIE_CLIENT_TOKEN, cookies)
                                    .getValue();
        } catch ( NoSuchElementException e) {
            // Cookie non trovato
            return false;
        }
        Attore attoreCheStaAutenticandosi = Attore.getAttoreById(identificativoAttoreDaToken);

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
        jwtPayload.aggiungiClaim(new JwtSubjectClaim(attore.getIdentificativoAttore()));
        jwtPayload.aggiungiClaim(new JwtExpirationTimeClaim(TIMEOUT_AUTENTICAZIONE_IN_SECONDI));
        jwtPayload.aggiungiClaim(new JwtClaim(NOME_CLAIM_JWT_CON_HASH_COOKIE_AUTENTICAZIONE,
                                              GestoreSicurezza.hmacSha256(valoreCookieId, hashPasswordAttore)) );

        // Aggiunge gli attributi dell'attore non sensibili
        jwtPayload.aggiungiClaim( new JwtNomeSubjectClaim(attore.getNominativo()) );
        jwtPayload.aggiungiClaim( new JwtTipoAttoreClaim( attore.getTipoAttore() ) );

        return new JwtToken(jwtPayload).generaTokenJsonCodificatoBase64UrlEncoded();
        
    }

    /** Ricerca l'attore nel database in base al token di autenticazione
     * della richiesta giunta dal client: se trova l'attore nel database,
     * lo restituisce, altrimenti restituisce {@link #ATTORE_NON_AUTENTICATO}.*/
    public static Attore getAttoreDaTokenAutenticazione(@NotNull String tokenAutenticazione) {

        // TODO : da testare

        JwtToken jwtTokenAutenticazione = JwtToken.creaJwtTokenDaStringaCodificata(tokenAutenticazione);
        Long idAttore = (Long)jwtTokenAutenticazione.getValoreSubjectClaim();
        Attore attore = Attore.getAttoreById(idAttore);

        return attore;
    }

    /** Data una HttpServletRequest, restituisce l'attore autenticato
     * per quella HttpServletRequest. Se la richiesta proviene da un client
     * che non si è autenticato oppure se l'autenticazione non è valida,
     * allora restituisce {@link #ATTORE_NON_AUTENTICATO}.*/
    public static Attore getAttoreDaHttpServletRequest(HttpServletRequest httpServletRequest) {

        // TODO : da testare

        // TODO : vedere dov'è stato usato questo metodo (getTokenAutenticazioneBearer fa accessi costosi al database)

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

    /** Restituisce il tipo di {@link Attore} in base alle informazioni contenute
     * nel token JWT. */
    public static String getTipoAttoreDaHttpServletRequest(HttpServletRequest httpServletRequest) {
        JwtToken jwtTokenAutenticazione = getTokenDaHttpServletRequest(httpServletRequest);
        return (String) jwtTokenAutenticazione.getValoreClaimByName( JwtClaim.JWT_TIPO_ATTORE_CLAIM_NAME );
    }

    /** Restituisce il nome dell'{@link Attore} in base alle informazioni contenute
     * nel token JWT. */
    public static String getNomeAttoreDaHttpServletRequest(HttpServletRequest httpServletRequest) {
        JwtToken jwtTokenAutenticazione = getTokenDaHttpServletRequest(httpServletRequest);
        return (String) jwtTokenAutenticazione.getValoreClaimByName( JwtClaim.JWT_NOME_SUBJECT_CLAIM_NAME );
    }

    /** Restituisce lo username dell'{@link Attore} in base alle informazioni contenute
     * nel token JWT. */
    public static Long getIdentificativoAttoreDaTokenAutenticazione(HttpServletRequest httpServletRequest) {
        JwtToken jwtTokenAutenticazione = getTokenDaHttpServletRequest(httpServletRequest);
        return (Long)jwtTokenAutenticazione.getValoreClaimByName( JwtClaim.JWT_SUBJECT_CLAIM_NAME );
    }

    /** Data la HttpServletRequest, restituisce il token di autenticazione
     * oppure null se non è presente.*/
    private static JwtToken getTokenDaHttpServletRequest(HttpServletRequest httpServletRequest) {
        String tokenAutenticazioneBearer = getTokenAutenticazioneBearer(httpServletRequest);

        if (tokenAutenticazioneBearer == null)
            return null;

        // Calcolo del JWT token ottenuto dall'authorization header
        JwtToken jwtTokenAutenticazione = null;
        {
            boolean isStringaNonNullaNonVuota = ! tokenAutenticazioneBearer.trim().isEmpty();
            if( isStringaNonNullaNonVuota )
                jwtTokenAutenticazione = JwtToken.creaJwtTokenDaStringaCodificata(tokenAutenticazioneBearer);
        }

        return jwtTokenAutenticazione;
    }

}
