package it.units.progrweb.utils;

import it.units.progrweb.entities.attori.Attore;
import it.units.progrweb.entities.attori.Consumer;
import it.units.progrweb.utils.jwt.JwtToken;
import it.units.progrweb.utils.jwt.componenti.JwtPayload;
import it.units.progrweb.utils.jwt.componenti.claim.JwtExpirationTimeClaim;
import it.units.progrweb.utils.jwt.componenti.claim.JwtSubjectClaim;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.lang.reflect.Field;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * Classe di utilità per gestire l'autenticazione degli utenti.
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
     * Il token di autenticazione è trasmesso nel body della response.*/
    public static Response creaResponseAutenticazione(String username, String password) {
        Attore attore = Autenticazione.getAttoreDaCredenziali(username, password);
        if(Autenticazione.isAttoreAutenticato(attore)) {

            try {
                String tokenAutenticazione = Autenticazione.creaJwtTokenAutenticazionePerAttore(attore);
                return Response.ok()
                               .entity(tokenAutenticazione)
                               .type(MediaType.APPLICATION_FORM_URLENCODED) // token non è propriamente application/json
                               .build();
            } catch (NoSuchAlgorithmException|InvalidKeyException e) {
                Logger.scriviEccezioneNelLog(Autenticazione.class,
                                    "Impossibile creare il token JWT di autenticazione.", e);
            }

        }

        return Response.status(Response.Status.UNAUTHORIZED)
                       .header("WWW-Authenticate","Bearer")   // invita il client ad autenticarsi
                       .entity("Credenziali invalide")              // body della response
                       .build();
    }

    /** Verifica se il client è autenticato in base agli header della richiesta HTTP.
     * La verifica dell'autenticazione è basata sul Bearer Token che dovrebbe essere
     * presente nell'header <i>Authorization</i> della request.
     * @return true se il clien è autenticato, false altrimenti.*/
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
    private static String creaJwtTokenAutenticazionePerAttore(Attore attore)
            throws InvalidKeyException, NoSuchAlgorithmException {
        
        JwtPayload jwtPayload = new JwtPayload();
        jwtPayload.aggiungiClaim(new JwtSubjectClaim(String.valueOf(attore.getIdentificativoAttore())));
        jwtPayload.aggiungiClaim(new JwtExpirationTimeClaim(TIMEOUT_AUTENTICAZIONE_IN_SECONDI));
        return new JwtToken(jwtPayload).generaTokenJsonCodificatoBase64UrlEncoded();
        
    }
}
