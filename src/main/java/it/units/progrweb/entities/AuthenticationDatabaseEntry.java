package it.units.progrweb.entities;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;
import it.units.progrweb.persistence.NotFoundException;
import it.units.progrweb.utils.GeneratoreTokenCasuali;
import it.units.progrweb.utils.GestoreSicurezza;

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

    // TODO : implementare questa @Entity

    private final static int SALT_LENGTH = 15;

    /** Valore identificativo univoco per l'attore. */
    @Id
    @Index
    private String identificativoAttore;

    /** Password, hashed e salted.*/
    private String hashedSaltedPassword;

    /** Salt utilizzato. */
    private String salt;

    /** Crea un' entry dell'authentication database.
     * @throws InvalidKeyException generata da {@link GestoreSicurezza#hmacSha256(String)}
     * @throws NoSuchAlgorithmException generata da {@link GestoreSicurezza#hmacSha256(String)}
     */
    public AuthenticationDatabaseEntry(String identificativoAttore, String passwordAttore)
            throws InvalidKeyException, NoSuchAlgorithmException {

        this.identificativoAttore = identificativoAttore;
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


    /** Dati identificativoAttore e password, restituisce true se tali
     * credenziali sono valide, false altrimenti. */
    public static boolean verificaCredenziali(String identificativoAttore, String passwordInChiaro) {
        // TODO : implementare questa classe

        boolean credenzialiValide;
        AuthenticationDatabaseEntry authenticationEntry;
                
        try {
            authenticationEntry = cercaAttore(identificativoAttore);

            // Se qui, allora attore trovato
            credenzialiValide = calcolaHashedSaltedPassword(passwordInChiaro, authenticationEntry.salt)
                                    .equals(authenticationEntry.hashedSaltedPassword);

        } catch (NotFoundException e) {
            // attore non trovato
            credenzialiValide = false;
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            e.printStackTrace();    // TODO : gestire quest'eccezione, notificare ai gestori? Stampare su log?
            credenzialiValide = false;  // errore del server durante la verifica delle credenziali
        }

        return credenzialiValide;
    }

    /** Cerca l' identificativoAttore dato nell'AuthDB.
     * Se lo trova, restituisce l'entry corrispondente,
     * altrimenti lancia un'eccezione.
     * @throws NotFoundException se non trova nell'AuthDB l' identificativoAttore cercato.
     */
    private static AuthenticationDatabaseEntry cercaAttore(String identificativoAttore)
            throws NotFoundException {
        throw new NotFoundException();  // TODO : metodo da implementare!
    }

}
