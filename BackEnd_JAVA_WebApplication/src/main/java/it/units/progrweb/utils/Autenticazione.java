package it.units.progrweb.utils;

import it.units.progrweb.entities.AuthenticationDatabaseEntry;
import it.units.progrweb.entities.attori.Attore;
import it.units.progrweb.persistence.NotFoundException;
import it.units.progrweb.utils.jwt.JwtToken;
import it.units.progrweb.utils.jwt.componenti.JwtPayload;
import it.units.progrweb.utils.jwt.componenti.claims.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
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

    /** Tempo in secondi durante il quale ad un attore che si è autenticato
     * non verranno chieste nuovamente le credenziali. */
    private static final int TIMEOUT_AUTENTICAZIONE_IN_SECONDI = 1800;

    /** Lunghezza del token di autenticazione di un client.*/
    private static final int LUNGHEZZA_TOKEN_AUTENTICAZIONE = 128;

    /** Nome del cookie contenente un token per il client. L'hash di questo valore
     * è presente nel token di autenticazione del client, nel claims con nome specificato
     * in {@link JwtClaim#JWT_HASH_TOKEN_AUTENTICAZIONE_IN_COOKIE_CLAIM_NAME}.*/
    private static final String NOME_COOKIE_CLIENT_TOKEN = "TOKEN-ID-CLIENT-AUTENTICAZIONE";

    /** Nelle richieste HTTP, è il nome dell' header contenente il token di autenticazione.*/
    private static final String NOME_HEADER_AUTHORIZATION = "Authorization";

    /** Nelle risposte HTTP (server verso client), è il nome dell' header
     * che richiede l'autenticazione del client.*/
    private static final String NOME_HEADER_AUTHENTICATE = "WWW-Authenticate";

    /** Tipo di Autenticazione HTTP richiesta.*/
    private static final String TIPO_AUTENTICAZIONE_RICHIESTA = "Bearer";


    /** Restituisce l'attore corrispondente alle credenziali date
     * come parametri se le credenziali sono valide, null altrimenti.
     * Vedere anche {@link #creaResponseAutenticazione(String, String)}.*/
    public static Attore getAttoreDaCredenziali(String username, String password) {

        boolean credenzialiCorrette =
                    AuthenticationDatabaseEntry.verificaCredenziali(username, password);

        if( credenzialiCorrette ) {
            return Attore.getAttoreDaUsername( username );
        } else {
            return null;
        }

    }

    /** Date le credenziali, restituisce una {@link javax.ws.rs.core.Response}
     * per un attore che si sta autenticando.
     * Procedura di autenticazione di un client: si verifica la validità delle
     * credenziali e, se <strong>non</strong> valide, si risponde con esito
     * negativo, altrimenti (se valide) si segue la seguente procedura:
     * <ol>
     *     <li>
     *         Creare un cookie (<i>HttpOnly</i>) contenente un token CSRF di
     *         lunghezza specificata in {@link #LUNGHEZZA_TOKEN_AUTENTICAZIONE}
     *         (questo cookie sarà inviato al client insieme alla response).
     *     </li>
     *     <li>
     *         Calcolare, con la password (hashed e salted) del client, l'hash del
     *         token CSRF del punto precedente.
     *     </li>
     *     <li>
     *         Generare un token JWT (di autenticazione) con le informazioni del
     *         client ed aggiungervi il claims contenente l'hash del token CSRF
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
        return creaResponseAutenticazione(attore);
    }

    /** Come {@link #creaResponseAutenticazione(String, String)}, ma crea
     * la risposta di autenticazione a partire da un {@link Attore} anziché
     * dalle sue credenziali.*/
    public static Response creaResponseAutenticazione(Attore attore) {
        boolean isAttoreAutenticato = attore!=null;
        if( isAttoreAutenticato ) {
            try {
                return creaResponseAutenticazionePerAttore(attore);
            } catch (NotFoundException ignored) { /*viene comunque restituita la risposta unauthorized*/}
        }

        // Autenticazione fallita
        return creaResponseUnauthorized();
    }

    public static Response creaResponseUnauthorized() {
        return ResponseHelper.creaResponseUnauthorized(
                "Credenziali invalide",
                new ResponseHelper.HeaderResponse(NOME_HEADER_AUTHENTICATE, TIPO_AUTENTICAZIONE_RICHIESTA) // invita il client ad autenticarsi
        );
    }

    /** Crea una risposta HTTP grazie alla quale sovrascrive il cookie il cui
     * valore è necessario per validare il token di autenticazione (vedere
     * {@link #creaResponseAutenticazione(String, String)}), rendendo
     * quest'ultimo inutilizzabile dal client.
     * Questo metodo non ha alcun effetto se il browser usato dal client non
     * sovrascrive il cookie come prescritto dall'header <i>SetCookie</i>
     * della risposta creata da questo metodo.*/
    public static Response creaResponseLogout() {

        return ResponseHelper.creaResponseOk("", new Cookie[]{
                new Cookie( NOME_COOKIE_CLIENT_TOKEN,
                            "deleted", 0,
                            "Rimozione cookie di autenticazione."
                )
        });

    }

    /** Metodo da invocare se un attore ha fornito le credenziali corrette.
     * Restituisce una response contenente i cookie ed il token di autenticazione
     * che il client dovrà esibire a questo server per autenticarsi.
     * Questo metodo può essere usato anche per aggiornare il token di
     * autenticazione del client.
     *
     * @param attore che ha fornito le credenziali corrette.
     * @return Response con token e cookie di autenticazione per l'attore.
     * @throws NotFoundException Se le credenziali non sono corrette.
     */
    public static Response creaResponseAutenticazionePerAttore(Attore attore)
            throws NotFoundException {

        try {
            String valoreTokenAutenticazione = generaTokenAlfanumerico(LUNGHEZZA_TOKEN_AUTENTICAZIONE);
            Cookie cookieIdClientPerAutenticazione = new Cookie( NOME_COOKIE_CLIENT_TOKEN,
                                                                 valoreTokenAutenticazione,
                                                                 TIMEOUT_AUTENTICAZIONE_IN_SECONDI,
                                                                 "Cookie per l'autenticazione del client." );

            return ResponseHelper.creaResponseOk(
                    Autenticazione.creaJwtTokenAutenticazionePerAttore(attore, valoreTokenAutenticazione),
                    MediaType.APPLICATION_FORM_URLENCODED_TYPE,  // token non è propriamente application/json
                    new Cookie[]{
                            cookieIdClientPerAutenticazione
                    }
            );

        } catch (NoSuchAlgorithmException|InvalidKeyException e) {
            Logger.scriviEccezioneNelLog(Autenticazione.class,
                    "Impossibile creare il token JWT di autenticazione.", e);

            return ResponseHelper.creaResponseServerError("");
        }

    }

    /** Verifica se il client è autenticato in base agli header della richiesta HTTP.
     * La verifica dell'autenticazione è basata sul Bearer Token che dovrebbe essere
     * presente nell'header <i>Authorization</i> della request.
     * Inoltre, la request deve contenere il cookie il cui hash del valore corrisponde
     * a quello indicato nel claims di nome specificato in
     * {@link JwtClaim#JWT_HASH_TOKEN_AUTENTICAZIONE_IN_COOKIE_CLAIM_NAME} contenuto nel payload
     * del token appena verificato.
     * @return true se il client è autenticato, false altrimenti.*/
    public static boolean isClientAutenticato(HttpServletRequest httpServletRequest) {

        try {
            String tokenAutenticazione = getTokenAutenticazioneBearer(httpServletRequest);
            JwtToken jwtTokenAutenticazione = JwtToken.creaJwtTokenDaStringaCodificata(tokenAutenticazione);

            // Cookie ricevuti in questa richiesta HTTP
            Cookie[] cookiesDaQuestaRichiestaHttp = Cookie.getCookieDaRichiestaHttp(httpServletRequest);

            return JwtToken.isTokenValido(tokenAutenticazione)
                    && isStessoHashCookieIdNelToken(jwtTokenAutenticazione, cookiesDaQuestaRichiestaHttp);

        } catch (Exception e) {
            // Verifica autenticazione può generare eccezioni
            return false;
        }

    }

    /** Data una HttpServletRequest, restituisce il token di autenticazione
     * se presente, stringa vuota altrimenti.*/
    public static String getTokenAutenticazioneBearer(HttpServletRequest httpServletRequest) {
        String tokenAutenticazioneBearer = httpServletRequest.getHeader(NOME_HEADER_AUTHORIZATION);  // prende header con token
        if( tokenAutenticazioneBearer==null )
            return "";

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

        String valoreHash_nelTokenAutenticazione_dalClient = (String) jwtTokenAutenticazione.getValoreClaimByName(JwtClaim.NomeClaim.HASH_TOKEN_AUTENTICAZIONE);
        String valoreCookieId_dalClient;
        try {
            valoreCookieId_dalClient = Cookie.cercaCookiePerNomeERestituiscilo(NOME_COOKIE_CLIENT_TOKEN, cookies)
                                             .getValue();
        } catch ( NoSuchElementException e) {
            // Cookie non trovato
            return false;
        }

        try {

            String usernameAttoreCheStaAutenticandosi = (String) jwtTokenAutenticazione.getValoreClaimByName(JwtClaim.NomeClaim.USERNAME_ATTORE);

            String hashPasswordAttore_daAuthDb =
                    AuthenticationDatabaseEntry.getHashedSaltedPasswordDellAttore(usernameAttoreCheStaAutenticandosi);

            String hashValoreCookieId_ricalcolatoCoiValoriNelServer =
                    GestoreSicurezza.hmacSha256(valoreCookieId_dalClient, hashPasswordAttore_daAuthDb);

            return valoreHash_nelTokenAutenticazione_dalClient.equals(hashValoreCookieId_ricalcolatoCoiValoriNelServer);

        } catch (NoSuchAlgorithmException|InvalidKeyException e) {
            Logger.scriviEccezioneNelLog(Autenticazione.class, e);
            return false;
        } catch (NoSuchElementException|NotFoundException ignored) {
            return false;
        }

    }

    /** Crea un Jwt Token che certifica l'autenticazione dell'attore indicato
     * nel parametro, quindi lo codifica in base64 url-encoded e lo restituisce.
     * @param attore Attore per cui si deve creare il token.
     * @param valoreCookieId è il valore del cookie associato all'attore che si sta autenticando.
     * @throws InvalidKeyException generata da {@link GestoreSicurezza#hmacSha256(String)}.
     * @throws NoSuchAlgorithmException generata da {@link GestoreSicurezza#hmacSha256(String)}.
     * @throws NotFoundException Se l'{@link Attore} passato nel parametro non viene trovato
 *                                nell'AuthDB.*/
    private static String creaJwtTokenAutenticazionePerAttore(Attore attore,
                                                              String valoreCookieId)
            throws InvalidKeyException, NoSuchAlgorithmException, NotFoundException {

        String hashPasswordAttore = AuthenticationDatabaseEntry.getHashedSaltedPasswordDellAttore(attore.getUsername());

        JwtPayload jwtPayload = new JwtPayload();
        jwtPayload.aggiungiClaim(new JwtSubjectClaim(attore.getIdentificativoAttore()));
        jwtPayload.aggiungiClaim(new JwtExpirationTimeClaim(TIMEOUT_AUTENTICAZIONE_IN_SECONDI));
        jwtPayload.aggiungiClaim(new JwtClaim<>(JwtClaim.NomeClaim.HASH_TOKEN_AUTENTICAZIONE.nomeClaim(),
                                                GestoreSicurezza.hmacSha256(valoreCookieId, hashPasswordAttore)) );

        // Aggiunge gli attributi dell'attore
        jwtPayload.aggiungiClaim( new JwtNomeSubjectClaim(attore.getNominativo()) );
        jwtPayload.aggiungiClaim( new JwtUsernameSubjectClaim(attore.getUsername()) );
        jwtPayload.aggiungiClaim( new JwtEmailSubjectClaim(attore.getEmail()) );
        jwtPayload.aggiungiClaim( new JwtTipoAttoreClaim( Attore.TipoAttore.valueOf(attore.getTipoAttore()) ) );

        return new JwtToken(jwtPayload).generaTokenJsonCodificatoBase64UrlEncoded();
        
    }

    /** Ricerca l'attore nel database in base al token di autenticazione
     * della richiesta giunta dal client: se trova l'attore nel database,
     * lo restituisce, altrimenti restituisce null.*/
    public static Attore getAttoreDaDbInBaseATokenAutenticazione(@NotNull String tokenAutenticazione) {

        try {
            JwtToken jwtTokenAutenticazione = JwtToken.creaJwtTokenDaStringaCodificata(tokenAutenticazione);
            Long idAttore = ((Number)jwtTokenAutenticazione.getValoreClaimByName(JwtClaim.NomeClaim.ID_ATTORE)).longValue();

            return Attore.getAttoreDaIdentificativo(idAttore);
        } catch (Exception ignored) {
            // Se il token di autenticazione è mal formato o manca il token di autenticazione
            return null;
        }

    }

    /** Data una HttpServletRequest, restituisce l'attore autenticato
     * per quella HttpServletRequest. Se la richiesta proviene da un client
     * che non si è autenticato oppure se l'autenticazione non è valida,
     * allora restituisce null.
     * Questo metodo utilizza {@link #getAttoreDaDbInBaseATokenAutenticazione(String)}*/
    public static Attore getAttoreDaDatabase(HttpServletRequest httpServletRequest) {

        String tokenAutenticazione = getTokenAutenticazioneBearer(httpServletRequest);
        return getAttoreDaDbInBaseATokenAutenticazione(tokenAutenticazione);

    }

    /** Restituisce il tipo di {@link Attore} in base alle informazioni contenute
     * nel token JWT.
     * @throws NoSuchElementException Se l'identificativo non viene trovato nel token.*/
    public static String getTipoAttoreDaHttpServletRequest(HttpServletRequest httpServletRequest) {
        JwtToken jwtTokenAutenticazione = JwtToken.creaJwtTokenDaStringaCodificata(getTokenAutenticazioneBearer(httpServletRequest));
        return (String) jwtTokenAutenticazione.getValoreClaimByName( JwtClaim.NomeClaim.TIPO_ATTORE );
    }

    /** Restituisce il nome dell'{@link Attore} in base alle informazioni contenute
     * nel token JWT.
     * @throws NoSuchElementException Se l'identificativo non viene trovato nel token.*/
    public static String getNomeAttoreDaHttpServletRequest(HttpServletRequest httpServletRequest) {
        JwtToken jwtTokenAutenticazione = JwtToken.creaJwtTokenDaStringaCodificata(getTokenAutenticazioneBearer(httpServletRequest));
        return (String) jwtTokenAutenticazione.getValoreClaimByName( JwtClaim.NomeClaim.NOME_ATTORE );
    }

    /** Restituisce l'identificativo dell'{@link Attore} in base alle informazioni contenute
     * nel token JWT.
     * @throws NoSuchElementException Se l'identificativo non viene trovato nel token.*/
    public static Long getIdentificativoAttoreDaHttpServletRequest(HttpServletRequest httpServletRequest) {
        JwtToken jwtTokenAutenticazione = JwtToken.creaJwtTokenDaStringaCodificata(getTokenAutenticazioneBearer(httpServletRequest));
        return ((Number)jwtTokenAutenticazione.getValoreClaimByName( JwtClaim.NomeClaim.ID_ATTORE )).longValue();
    }

}