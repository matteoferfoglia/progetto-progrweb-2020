package it.units.progrweb.entities;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;
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

    private final static int SALT_LENGTH = 11;

    /** Valore identificativo univoco per l'attore. */
    @Id
    @Index
    private String identificativoAttore;

    /** Password, hashed e salted.*/
    private String hashedSaltedPassword;

    /** Salt utilizzato. */
    private String salt;

    /** Crea un' entry dell'authentication database. */
    public AuthenticationDatabaseEntry(String identificativoAttore, String passwordAttore)
            throws InvalidKeyException, NoSuchAlgorithmException {

        this.identificativoAttore = identificativoAttore;
        this.salt = GeneratoreTokenCasuali.generaTokenAlfanumerico(SALT_LENGTH);
        this.hashedSaltedPassword = GestoreSicurezza.hmacSha256(passwordAttore.concat(salt));
    }

    public AuthenticationDatabaseEntry() {

    }

    // TODO : implementare questa @Entity

}
