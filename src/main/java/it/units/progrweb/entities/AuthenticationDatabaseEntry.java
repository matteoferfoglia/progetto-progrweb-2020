package it.units.progrweb.entities;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;
import it.units.progrweb.entities.attori.Attore;
import it.units.progrweb.persistence.NotFoundException;
import it.units.progrweb.utils.GeneratoreTokenCasuali;
import it.units.progrweb.utils.GestoreSicurezza;
import it.units.progrweb.utils.Logger;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * Classe rappresentante un'entità dell'authentication database.
 * Nota: informazioni di autenticazione tenuti separate dalle
 * informazioni degli attori.
 * Ogni entità dell'authentication database ha i seguenti campi:
 * [ User, H(concat(password, salt)), salt ]
 *
 * @author Matteo Ferfoglia
 */
@Entity
public class AuthenticationDatabaseEntry {

    // TODO : refactoring : spostare tutti (o quasi) i metodi statici nella classe di utilita "Autenticazione"

    // TODO : implementare questa @Entity

    private final static int SALT_LENGTH = 15;

    /** Username.*/
    @Id
    @Index
    private String username;

    /** Password, hashed e salted.*/
    private String hashedSaltedPassword;

    /** Salt utilizzato. */
    private String salt;

    /** Crea un' entry dell'authentication database.
     * @throws InvalidKeyException generata da {@link GestoreSicurezza#hmacSha256(String)}
     * @throws NoSuchAlgorithmException generata da {@link GestoreSicurezza#hmacSha256(String)}
     */
    public AuthenticationDatabaseEntry(String username, String passwordAttore)
            throws InvalidKeyException, NoSuchAlgorithmException {

        this.username  = username ;
        this.salt = GeneratoreTokenCasuali.generaTokenAlfanumerico(SALT_LENGTH);
        this.hashedSaltedPassword = calcolaHashedSaltedPassword(passwordAttore, this.salt);
    }

    public AuthenticationDatabaseEntry() {}

    /** Dati la password ed il salt, restituisce l'hash della password (hashed and salted)
     * @throws InvalidKeyException generata da {@link GestoreSicurezza#hmacSha256(String)}
     * @throws NoSuchAlgorithmException generata da {@link GestoreSicurezza#hmacSha256(String)}
     */
    private static String calcolaHashedSaltedPassword(String passwordInChiaro, String salt)
            throws InvalidKeyException, NoSuchAlgorithmException {
        return GestoreSicurezza.hmacSha256(passwordInChiaro.concat(salt));
    }

    /** Dato un attore, restituisce la sua password (hashed e salted).*/
    public static String getHashedSaltedPasswordDellAttore(Attore attore) {
        // TODO : metodo da implementare
        return "PippoHashedSalted";
    }


    /** Dati username  e password, restituisce true se tali
     * credenziali sono valide, false altrimenti. */
    public static boolean verificaCredenziali(String username , String passwordInChiaro) {
        // TODO : implementare questa classe

        boolean credenzialiValide;
        AuthenticationDatabaseEntry authenticationEntry;
                
        try {
            authenticationEntry = cercaAttore(username);

            // Se qui, allora attore trovato
            credenzialiValide = calcolaHashedSaltedPassword(passwordInChiaro, authenticationEntry.salt)
                                    .equals(authenticationEntry.hashedSaltedPassword);

        } catch (NotFoundException e) {
            // attore non trovato
            credenzialiValide = false;
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            Logger.scriviEccezioneNelLog(AuthenticationDatabaseEntry.class, e);    // TODO : gestire quest'eccezione, notificare ai gestori? Stampare su log?
            credenzialiValide = false;  // errore del server durante la verifica delle credenziali
        }

        return credenzialiValide;
    }

    /** Cerca l'attore in base allo username nell'AuthDB.
     * Se lo trova, restituisce l'entry corrispondente,
     * altrimenti lancia un'eccezione.
     * @throws NotFoundException se non trova nell'AuthDB lo username  cercato.
     */
    private static AuthenticationDatabaseEntry cercaAttore(String username )
            throws NotFoundException {
        throw new NotFoundException();  // TODO : metodo da implementare!
    }

}
