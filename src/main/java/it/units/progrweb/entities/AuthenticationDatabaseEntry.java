package it.units.progrweb.entities;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;
import it.units.progrweb.persistence.DatabaseHelper;
import it.units.progrweb.persistence.NotFoundException;
import it.units.progrweb.utils.GeneratoreTokenCasuali;
import it.units.progrweb.utils.GestoreSicurezza;
import it.units.progrweb.utils.Logger;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;
import java.util.function.Supplier;

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
    private final static int PASSWORD_TEMPORANEA_LENGTH = 12;

    /** Username.*/
    @Id
    @Index
    private String username;

    /** Password, hashed e salted.*/
    private String hashedSaltedPassword;

    /** Salt utilizzato. */
    private String salt;

    /** Password temporanea, in chiaro.*/
    private String passwordTemporanea;

    /** Crea un' entry dell'authentication database.
     * @throws InvalidKeyException generata da {@link GestoreSicurezza#hmacSha256(String)}
     * @throws NoSuchAlgorithmException generata da {@link GestoreSicurezza#hmacSha256(String)}
     */
    public AuthenticationDatabaseEntry(String username, String passwordAttore)
            throws InvalidKeyException, NoSuchAlgorithmException {

        this.username  = username ;
        this.salt = generaSalt();
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

    /** Dato lo username di un attore, restituisce la sua password (hashed e salted).
     * @throws NotFoundException Se lo username non viene trovato nell'AuthDB.*/
    public static String getHashedSaltedPasswordDellAttore(String usernameAttore)
            throws NotFoundException {
        return cercaAttoreInAuthDb(usernameAttore).hashedSaltedPassword;
    }

    /** Dato uno username, genera una password casuale temporanea per quell'utente.
     * La vecchia password resta valida. La password appena generata viene salvata
     * in chiaro (senza né hash né salt) e viene restituita in chiaro.
     * @param username Lo username per cui creare una password temporanea.
     * @throws NotFoundException Se lo username dato non viene trovato.*/
    public static String creaPasswordTemporanea(String username)
            throws NotFoundException {

        AuthenticationDatabaseEntry authenticationDatabaseEntry =
                cercaAttoreInAuthDb(username);

        authenticationDatabaseEntry.passwordTemporanea = GeneratoreTokenCasuali.generaTokenAlfanumerico(PASSWORD_TEMPORANEA_LENGTH);
        DatabaseHelper.salvaEntita(authenticationDatabaseEntry);

        return authenticationDatabaseEntry.passwordTemporanea;

    }


    /** Dati username  e password, restituisce true se tali
     * credenziali sono valide, false altrimenti. */
    public static boolean verificaCredenziali(String username , String password) {

        boolean credenzialiValide = false;  // default: false, in caso modificato modificato dal metodo
        AuthenticationDatabaseEntry authenticationEntry;
                
        try {
            authenticationEntry = cercaAttoreInAuthDb(username);

            if( authenticationEntry!= null )
                credenzialiValide = verificaCredenziali(password, authenticationEntry);

        } catch (NotFoundException e) {
            // attore non trovato
            credenzialiValide = false;
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            Logger.scriviEccezioneNelLog(AuthenticationDatabaseEntry.class,
                    "Errore del server durante la verifica delle credenziali",
                    e);
            credenzialiValide = false;
        }

        return credenzialiValide;
    }

    /** Questo metodo <strong>NON</strong> accede al database.
     * @return true se credenziali valide, false altrimenti.*/
    private static boolean verificaCredenziali(String passwordDaVerificare, AuthenticationDatabaseEntry authenticationEntry)
            throws InvalidKeyException, NoSuchAlgorithmException {

        if ( calcolaHashedSaltedPassword(passwordDaVerificare, authenticationEntry.salt)
                .equals(authenticationEntry.hashedSaltedPassword) ) {
            // Credenziali (hashedSalted) valide

            // Elimina eventuali password temporanee
            authenticationEntry.passwordTemporanea = null;

            return true;
        }

        if(authenticationEntry.passwordTemporanea!=null &&
                authenticationEntry.passwordTemporanea.equals(passwordDaVerificare)) {
            // Se si sta accedendo con una password temporanea (salvata in chiaro), allora la si rende "hashedSalted"
            authenticationEntry.salt = generaSalt();
            authenticationEntry.hashedSaltedPassword =
                    calcolaHashedSaltedPassword(passwordDaVerificare, authenticationEntry.salt);
            authenticationEntry.passwordTemporanea = null;

            return true;    // credenziali valide
        }

        // Se qui, credenziali invalide
        return false;
    }

    /** Cerca l'attore in base allo username nell'AuthDB.
     * Se lo trova, restituisce l'entry corrispondente,
     * altrimenti lancia un'eccezione.
     * @throws NotFoundException se non trova nell'AuthDB lo username  cercato.
     */
    private static AuthenticationDatabaseEntry cercaAttoreInAuthDb(String username )
            throws NotFoundException {

        AuthenticationDatabaseEntry authenticationDatabaseEntry =
                (AuthenticationDatabaseEntry) DatabaseHelper.getById(username, AuthenticationDatabaseEntry.class);

        if(authenticationDatabaseEntry==null)
            throw new NotFoundException();

        return authenticationDatabaseEntry;

    }

    /** Se la vecchia password coincide con quella nel database,
     * allora si procede alla modifica con la nuova password e
     * restituisce true, altrimenti restituisce false. In realtà
     * questo metodo predispone il sistema per la modifica e
     * restituisce un {@link Optional}: se {@link Optional#isPresent()}
     * restituisce true, allora si può invocare {@link Optional#get()}
     * per completare l'operazione di aggiornamento nel database,
     * altrimenti significa che ci sono stati problemi (la coppia
     * (username, password) non era presente nel database).
     * L'aggiornamento del database <strong>non</strong> avviene
     * automaticamente, ma bisogna invocare {@link Optional#get()}
     * (può essere invocato solo una volta, le successiva non
     * produce alcun effetto nel database).
     * Motivazione dell'utilizzo di {@link Optional}: il metodo
     * invocante potrebbe richiedere modifiche anche in altre
     * parti del database e prima di confermarle potrebbe dover
     * verificare che non vi siano stati problemi. Se le operazioni
     * sul database invece avessero effetto immediato, doverle
     * poi rimodificare (ad esempio perché qualche controllo su
     * un altro database ha generato un'eccezione) potrebbe
     * comportare un ulteriore costo relativo all'ulteriore accesso
     * in scrittura nel database.
     * @param username
     * @param vecchiaPassword
     * @param nuovaPassword
     * @return {@link Optional} - Vedere descrizione del metodo.
     */
    public static Optional<AuthenticationDatabaseEntry> modificaPassword(String username,
                                                                         String vecchiaPassword,
                                                                         String nuovaPassword   ) {

        // TODO : testare che funzioni come da aspettative

        AuthenticationDatabaseEntry authenticationDatabaseEntry;
        try {
            authenticationDatabaseEntry = cercaAttoreInAuthDb( username );
        } catch (NotFoundException e) {
            return Optional.empty();
        }

        try {
            if( verificaCredenziali( vecchiaPassword, authenticationDatabaseEntry ) ) {

                authenticationDatabaseEntry.salt = generaSalt();
                authenticationDatabaseEntry.hashedSaltedPassword =
                        calcolaHashedSaltedPassword(nuovaPassword, authenticationDatabaseEntry.salt);

                // Creazione del Supplier che modificherà il database
                boolean giaModificatoIlDatabase = false;
                Supplier<AuthenticationDatabaseEntry> modificaDatabase = () -> {
                    if( ! giaModificatoIlDatabase ) // stile closure: questo flag non è modificabile dall'esterno del metodo in cui è definito
                        DatabaseHelper.salvaEntita(authenticationDatabaseEntry);  // importante che le modifiche vengano apportate subito per i cambi di password
                    return authenticationDatabaseEntry;
                };

                return Optional.of(modificaDatabase.get());// TODO: verificare funzionamento e soprattutto che la modifica nel db avvenga solo DOPO aver invoca Optional#get()

            } else {
                return Optional.empty();
            }
        } catch (InvalidKeyException|NoSuchAlgorithmException e) {
            Logger.scriviEccezioneNelLog(AuthenticationDatabaseEntry.class, e);
            return Optional.empty();
        }


    }

    /** Generatore casuale di Salt per questa classe.*/
    private static String generaSalt() {
        return GeneratoreTokenCasuali.generaTokenAlfanumerico(SALT_LENGTH);
    }
}
