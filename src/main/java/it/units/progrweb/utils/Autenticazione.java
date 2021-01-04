package it.units.progrweb.utils;

import it.units.progrweb.entities.attori.Attore;
import it.units.progrweb.utils.jwt.JwtToken;
import it.units.progrweb.utils.jwt.componenti.JwtPayload;
import it.units.progrweb.utils.jwt.componenti.claim.JwtExpirationTimeClaim;
import it.units.progrweb.utils.jwt.componenti.claim.JwtSubjectClaim;

import javax.ws.rs.core.Response;
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
        return ATTORE_NON_AUTENTICATO;
    }

    /** Restituisce true se l'attore è autenticato, false altrimenti.*/
    public static boolean isAttoreAutenticato(Attore attore) {
        return attore != ATTORE_NON_AUTENTICATO;    // TODO : verificare corretto funzionamento di questo metodo
    }

    /** Date le credenziali, restituisce una {@link javax.ws.rs.core.Response}
     * per un attore che si sta autenticando*/
    public static Response creaResponseAutenticazione(String username, String password) {
        Attore attore = Autenticazione.getAttoreDaCredenziali(username, password);
        if(Autenticazione.isAttoreAutenticato(attore)) {

            try {
                String tokenAutenticazione = Autenticazione.creaJwtTokenAutenticazionePerAttore(attore);
                return Response.ok()
                        .header("Authorization", tokenAutenticazione)
                        .entity("Benvenuto " + attore.getNomeCognome())
                        .build();
            } catch (NoSuchAlgorithmException|InvalidKeyException e) {
                Logger.scriviEccezioneNelLog(Autenticazione.class,
                                    "Impossibile creare il token JWT di autenticazione.", e);
            }
        }

        return Response.status(401, "Unauthorized: credenziali invalide.")
                .entity("Credenziali invalide")
                .build();
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
