package it.units.progrweb.entities;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Ignore;
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
 * Nel caso in cui un utente dimentichi la propria password, può
 * recuperarla: a tal fine si usa il campo passwordTemporanea.
 * Un account deve essere verificato (invocando il metodo
 * {@link #verificaAccount(String, String)}, passando come parametro un
 * token che viene generato alla creazione di un'istanza di questa
 * classe) prima di poter essere usato: se l'account non è verificato,
 * qualsiasi tentativo di autenticazione verrà rigettato. Se il
 * tentativo di autenticazione avviene usando una password temporanea
 * (ottenibile ad es. se l'utente dimentica la password), allora
 * l'utente (se la password temporanea risultasse corretta) risulterà
 * anche verificato (nel caso in cui non lo fosse).// TODO : da verificare
 * Caso d'uso (esempio):
 * <ol>
 *     <li>utente si registra al sistema;
 *     </li>
 *     <li>utente riceve via mail un link di conferma (invio email non gestito da questa
 *         classe, è un'esempio d'uso), contenente il token per la verifica dell'account;
 *     </li>
 *     <li>scenari possibili:
 *         <ul>
 *             <li>SE l'utente clicca su link di conferma nella mail, ALLORA il servizio
 *             che risponde alla richiesta può invocare {@link #verificaAccount(String,String)}
 *             passandogli il token di verifica, quindi quest'ultimo metodo renderà
 *             l'accounta verificato.</li>
 *             <li>SE l'utente tenta di autenticarsi senza aver prima verificato l'account,
 *             ALLORA la richiesta di autenticazione fallirà.</li>
 *             <li>SE l'utente richiede il reset della password (funzione "password
 *             dimenticata"), quindi questa classe genererà una password temporanea, e poi
 *             l'utente accede con la password temporanea corretta, ALLORA l'account risulterà
 *             verificato. Quest'ultimo caso può essere utilizzato nel caso in cui l'utente
 *             perda l'email contenente il link per la verifica dell'account; si assume che
 *             le modalità di erogazione della password temporanea implicitamente verifichino
 *             l'utente (ad es. invio della password temporanea all'email dell'utente: poiché
 *             anche il token di verifica autenticazione era stato mandato alla mail dell'utente,
 *             se quest'ultimo conosce la password temporanea, implicitamente significa che
 *             ha ricevuto (salvo errori) anche la mail contenente il token di verifica, perciò
 *             si può considerare l'account verificato).
 *             </li>
 *         </ul>
 *     </li>
 * </ol>
 *
 * @author Matteo Ferfoglia
 */
@Entity
public class AuthenticationDatabaseEntry {

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

    /** Token per verificare un account appena creato, per poterlo usare.
     * Un account risulta verificato se questo token è null.*/
    private String tokenVerificaAccount;        // TODO : aggiungere in diagramma ER

    /** Lunghezza di {@link #tokenVerificaAccount}.*/
    @Ignore
    private static final int LUNGHEZZA_TOKEN_VERIFICA_ACCOUNT = 64;

    /** Crea un' entry dell'authentication database. L'account creato con
     * questo metodo risulta già verificato e "pronto all'uso".
     * @throws InvalidKeyException generata da {@link GestoreSicurezza#hmacSha256(String)}
     * @throws NoSuchAlgorithmException generata da {@link GestoreSicurezza#hmacSha256(String)}
     */
    public AuthenticationDatabaseEntry(String username, String passwordAttore)
            throws InvalidKeyException, NoSuchAlgorithmException {

        this(username, passwordAttore, true);

    }

    /** Crea un' entry dell'authentication database.
     * @param username Username dell'attore, per il salvataggio.
     * @param passwordAttore Password in chiaro dell'attore, per il salvataggio.
     * @param isAccountVerificato Flag: se impostato a false, allora l'account creato con
     *         questo metodo risulta da verificare, quindi i tentativi di autenticazione
     *         saranno tutti respinti finché l'account non verrà verificato, altrimenti,
     *         se impostato a true, l'account è subito utilizzabile per l'autenticazione.
     * @throws InvalidKeyException generata da {@link GestoreSicurezza#hmacSha256(String)}
     * @throws NoSuchAlgorithmException generata da {@link GestoreSicurezza#hmacSha256(String)}
     */
    public AuthenticationDatabaseEntry(String username, String passwordAttore, boolean isAccountVerificato)
            throws InvalidKeyException, NoSuchAlgorithmException {

        this.username  = username ;
        this.salt = generaSalt();
        this.hashedSaltedPassword = calcolaHashedSaltedPassword(passwordAttore, this.salt);

        this.tokenVerificaAccount = isAccountVerificato ?
                                    null :  // se account già verificato, allora non serve generare un token da usare per verificarlo
                                    GeneratoreTokenCasuali.generaTokenAlfanumerico(LUNGHEZZA_TOKEN_VERIFICA_ACCOUNT);

    }

    public AuthenticationDatabaseEntry() {}

    /** Se lo username fornito come parametro esiste nell'AuthDB, questo
     * metodo elimina l'entry corrispondente. */
    public static void eliminaEntry(String username) {
        try {
            DatabaseHelper.cancellaAdessoEntita(AuthenticationDatabaseEntry.cercaAttoreInAuthDb(username));
        } catch (NotFoundException ignored) {
            // entry già eliminata o comunque non presente
        }
    }

    /** Restituisce true se questo account è già stato verificato, false altrimenti.*/
    private boolean isAccountVerificato() {
        return tokenVerificaAccount==null;
    }

    /** Rende verificato l'account. */
    private void rendiVerificatoQuestoAccount() {
        tokenVerificaAccount = null;    // quando è null significa che l'account è verificato
    }

    public String getTokenVerificaAccount() {
        return tokenVerificaAccount;
    }

    public String getUsername() {
        return username;
    }

    /** Verifica un account, inteso come istanza di questa classe,
     * già creato in precedenza.
     * @param usernameAccountDaVerificare Username dell'account da verificare.
     * @param tokenVerificaAccount Token per la verifica dell'account.
     * @return true se la verifica va a buon fine, false altrimenti.
     * @throws NotFoundException Se l'utente non viene trovato in AuthDB.
     * @throws NullPointerException Se i parametri passati a questo metodo sono null.*/
    public static boolean verificaAccount( String usernameAccountDaVerificare, String tokenVerificaAccount )
            throws NotFoundException, NullPointerException {

        if( usernameAccountDaVerificare==null || tokenVerificaAccount==null )
            throw new NullPointerException("I parametri non possono essere null.");

        AuthenticationDatabaseEntry authenticationDatabaseEntryDaVerificare =
                cercaAttoreInAuthDb(usernameAccountDaVerificare);

        if( authenticationDatabaseEntryDaVerificare.isAccountVerificato() )
            return true;    // era già stato verificato

        if( tokenVerificaAccount.equals(authenticationDatabaseEntryDaVerificare.tokenVerificaAccount) ) {
            authenticationDatabaseEntryDaVerificare.rendiVerificatoQuestoAccount();
            return true;
        }

        return false;

    }

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

        boolean credenzialiValide;  // default: false, in caso modificato modificato dal metodo
        AuthenticationDatabaseEntry authenticationEntry;
                
        try {

            authenticationEntry = cercaAttoreInAuthDb(username);
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


        // Verifica password
        if ( authenticationEntry.isAccountVerificato() &&
                calcolaHashedSaltedPassword(passwordDaVerificare, authenticationEntry.salt)
                    .equals(authenticationEntry.hashedSaltedPassword) ) {
            // Credenziali (hashedSalted) valide

            // Elimina eventuali password temporanee
            if( authenticationEntry.passwordTemporanea != null ) {
                authenticationEntry.passwordTemporanea = null;
                DatabaseHelper.salvaEntita(authenticationEntry);
            }

            return true;
        }


        // Verifica password temporanea (se la verifica della password fallisce)
        if(authenticationEntry.passwordTemporanea!=null &&
                authenticationEntry.passwordTemporanea.equals(passwordDaVerificare)) {
            // Se si sta accedendo con una password temporanea (salvata in chiaro), allora la si rende "hashedSalted"
            authenticationEntry.salt = generaSalt();
            authenticationEntry.hashedSaltedPassword =
                    calcolaHashedSaltedPassword(passwordDaVerificare, authenticationEntry.salt);
            authenticationEntry.passwordTemporanea = null;
            DatabaseHelper.salvaEntita(authenticationEntry);

            // Se l'account non era verificato, ora lo è implicitamente
            authenticationEntry.rendiVerificatoQuestoAccount();

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
    public static AuthenticationDatabaseEntry cercaAttoreInAuthDb(String username )
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
     * sull'oggetto restituito da questo metodo (può essere invocato solo
     * una volta, le successiva non produce alcun effetto nel database).
     * Motivazione dell'utilizzo di {@link Optional}: il metodo
     * invocante potrebbe richiedere modifiche anche in altre
     * parti del database e prima di confermarle potrebbe dover
     * verificare che non vi siano stati problemi. Se le operazioni
     * sul database invece avessero effetto immediato, doverle
     * poi rimodificare (ad esempio perché qualche controllo su
     * un altro database ha generato un'eccezione) potrebbe
     * comportare un ulteriore costo relativo all'ulteriore accesso
     * in scrittura nel database.
     * @param username Nuovo username.
     * @param vecchiaPassword Vecchia password.
     * @param nuovaPassword Nuova password.
     * @return {@link Optional} - Se non si verificano problemi, l'oggetto
     *                            restituito sarà un {@link Optional} contenente
     *                            un {@link Supplier} che, una volta eseguita con
     *                            {@link Supplier#get()}, apporterà le modifiche
     *                            nella base di dati e restituirà l'identificativo
     *                            dell'occorrenza {@link AuthenticationDatabaseEntry}
     *                            appena modificata.
     */
    public static Optional<?> modificaPassword(String username,
                                                                         String vecchiaPassword,
                                                                         String nuovaPassword   ) {

        AuthenticationDatabaseEntry authenticationDatabaseEntry;
        try {
            authenticationDatabaseEntry = cercaAttoreInAuthDb( username );
        } catch (NotFoundException e) {
            return Optional.empty();
        }

        try {
            if( verificaCredenziali( vecchiaPassword, authenticationDatabaseEntry ) ) {

                // variabili inizializzate fuori dalla lambda function per gestire l'eventuale eccezione con try..catch
                String nuovoSalt = generaSalt();
                String nuovoHashedSaltedPassword =
                        calcolaHashedSaltedPassword(nuovaPassword, nuovoSalt);

                // Creazione del Supplier che modificherà il database
                Supplier<?> modificaDatabase = () -> {
                    authenticationDatabaseEntry.salt = nuovoSalt;
                    authenticationDatabaseEntry.hashedSaltedPassword = nuovoHashedSaltedPassword;
                    return DatabaseHelper.salvaEntita(authenticationDatabaseEntry);
                };

                return Optional.of(modificaDatabase);

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
