package it.units.progrweb.entities.attori;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Ignore;
import com.googlecode.objectify.annotation.Index;
import it.units.progrweb.api.autenticazioneERegistrazione.VerificaRegistrazione;
import it.units.progrweb.entities.AuthenticationDatabaseEntry;
import it.units.progrweb.entities.RelazioneUploaderConsumer;
import it.units.progrweb.entities.attori.administrator.Administrator;
import it.units.progrweb.entities.attori.consumer.Consumer;
import it.units.progrweb.entities.attori.uploader.Uploader;
import it.units.progrweb.persistence.DatabaseHelper;
import it.units.progrweb.persistence.NotFoundException;
import it.units.progrweb.utils.*;
import it.units.progrweb.utils.mail.MailSender;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;

import javax.mail.MessagingException;
import javax.security.auth.Subject;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


/**
 * Rappresentazione di un attore.
 *
 * @author Matteo Ferfoglia
 */
@Entity
public abstract class Attore implements Cloneable, Principal {

    /** Tipi di attori permessi nel sistema.*/
    public enum TipoAttore {
        Consumer,
        Uploader,
        Administrator
    }

    /** Identificativo di un utente.*/
    @Id
    protected Long identificativoAttore;

    /** Username dell'attore.*/
    @Index
    protected String username;

    /** Nome e cognome dell'attore.*/
    protected String nominativo;

    /** Email dell'attore. */
    @Index
    protected String email;

    /** Tipo di attore (quale sottoclasse di {@link Attore}).*/
    @Ignore
    protected TipoAttore tipoAttore =
            this instanceof Consumer ? TipoAttore.Consumer :
                this instanceof Uploader ? TipoAttore.Uploader :
                this instanceof Administrator ? TipoAttore.Administrator : null;

    /**
     * Copy-constructor. Copia tutti i {@link Field} <strong>non static e non final</strong>
     * dell'oggetto passato come parametro in un nuovo oggetto di tipo {@link Attore}.
     * <strong>Non vengono copiati i metodi di eventuali classi derivate da questa.</strong>
     * @param attore L'attore da copiare.
     */
    @SuppressWarnings("CopyConstructorMissesField") // tutti i campi di interesse sono copiati tramite reflection
    public Attore(Attore attore) {

        Arrays.stream( Attore.class.getDeclaredFields() )
              .filter( field -> ! Modifier.isStatic( field.getModifiers() ) )   // Check che i campi non siano static, Fonte: https://docs.oracle.com/javase/8/docs/api/java/lang/reflect/Member.html
              .forEach( field -> {

                  try {
                      field.setAccessible(true);
                      field.set(this, field.get(attore));    // modifica del valore
                  } catch (IllegalAccessException exception) {
                      Logger.scriviEccezioneNelLog(
                              this.getClass(),
                              "Errore durante l'accesso ad un attributo dell'oggetto da copiare, con reflection.",
                              exception
                      );
                  }

              });
    }

    /** Metodo per modificare un {@link Attore}.
     * @param identificativoAttoreDaModificare Identificativo dell'{@link Attore} da modificare.
     * @param nuovoNominativo Nuovo nominativo. Può essere null.
     * @param nuovaEmail Nuova email. Può essere null.
     * @param nuovoLogo Nuovo logo dell'{@link Attore}. Può essere null.
     * @param dettagliNuovoLogo Dettagli relativi al logo se lo si vuole modificare. Può essere null.
     * @param modificaRichiestaDaUploader Flag: true se la modifica viene richiesta da un {@link Uploader}.
     * @return Istanza di {@link Response} già pronta per essere inviata al client.
     */
    public static Response modificaAttore(String nuovoNominativo,
                                          String nuovaEmail,
                                          @NotNull Long identificativoAttoreDaModificare,
                                          InputStream nuovoLogo,
                                          FormDataContentDisposition dettagliNuovoLogo,
                                          boolean modificaRichiestaDaUploader) {

        Attore attore_recuperatoDaDB =
                getAttoreDaIdentificativo(identificativoAttoreDaModificare);

        if( attore_recuperatoDaDB != null ) {

            if( modificaRichiestaDaUploader &&
                    ! (attore_recuperatoDaDB instanceof Consumer) ) {

                return ResponseHelper.creaResponseForbidden("Un " + TipoAttore.Uploader + " può modificare solo un " + TipoAttore.Consumer);

            } else {

                // Recupera dal DB l'attore da modificare, ne crea un clone di riferimento,
                // modifica l'entità recuperata dal database e se ci sono modifiche valide
                // allora aggiorna l'entità salvata in database

                Attore clone_attoreRecuperatoDaDB = attore_recuperatoDaDB.clone();
                clone_attoreRecuperatoDaDB.setIdentificativoAttore( attore_recuperatoDaDB.getIdentificativoAttore() );

                if( attore_recuperatoDaDB instanceof Uploader) {    // Solo Uploader ha il logo

                    try {

                        Uploader.modificaInfoUploader(
                                ((Uploader)attore_recuperatoDaDB),
                                nuovoLogo,
                                dettagliNuovoLogo,
                                nuovoNominativo,
                                nuovaEmail
                        );

                    } catch (IOException e) {
                        // Dimensione immagine logo eccessiva
                        return ResponseHelper.creaResponseUnprocessableEntity(e.getMessage());
                    }

                } else {
                    modificaInfoAttore(attore_recuperatoDaDB, nuovoNominativo, nuovaEmail);
                }

                // Salvataggio delle modifiche in DB (se presenti)
                if( ! attore_recuperatoDaDB.equals(clone_attoreRecuperatoDaDB) ) {
                    // Se non ci sono modifiche, risparmio l'inutile accesso in scrittura al DB
                    DatabaseHelper.salvaEntita(attore_recuperatoDaDB);
                }

                return ResponseHelper.creaResponseOk( new AttoreProxy(attore_recuperatoDaDB), MediaType.APPLICATION_JSON_TYPE );
            }


        } else {

            return ResponseHelper.creaResponseBadRequest("Attore da modificare non presente nel sistema.");

        }

    }

    /** Aggiorna <strong>nel database</strong> gli attributi di un {@link Attore}
     * con quelli forniti nei parametri (solo se validi).*/
    public static void modificaInfoAttore(@NotNull Attore attoreDaModificare,
                                          String nuovoNominativo, String nuovaEmail) {

        if( UtilitaGenerale.isStringaNonNullaNonVuota(nuovoNominativo) ) {
            attoreDaModificare.setNominativo( nuovoNominativo );
        }

        if( UtilitaGenerale.isStringaNonNullaNonVuota(nuovaEmail) ) {
            attoreDaModificare.setEmail( nuovaEmail );
        }

        DatabaseHelper.salvaEntita( attoreDaModificare );

    }

    /**
     * @param username Username dell'attore di cui verificare
     *                 la registrazione nella piattaforma.
     * @return true se l'attore associato al parametro fornito è
     *         già registrato nella piattaforma, false altrimenti.
     */
    public static boolean isAttoreGiaRegistrato(String username) {

        String nomeAttributoPerQuery = "username";
        if( UtilitaGenerale.esisteAttributoInClasse( nomeAttributoPerQuery, Attore.class ) ) {
            List<?> risultatoQuery = DatabaseHelper.query( Attore.class,
                    nomeAttributoPerQuery, DatabaseHelper.OperatoreQuery.UGUALE, username );

            return risultatoQuery.size() == 1;
        } else {
            Logger.scriviEccezioneNelLog( Attore.class, new NoSuchFieldException() );
            return false;
        }

    }

    /** Restituisce l'identificativo dell'attore.*/
    public Long getIdentificativoAttore() {
        return identificativoAttore;
    }

    /** Restituisce il tipo di un attore.*/
    public String getTipoAttore() {
        // Per favorire la serializzazione (a cura di JAX-RS) viene restituito un tipo primitivo anziché un enum
        return tipoAttore == null ? "" : tipoAttore.name();
    }

    /** Restituisce il nome del field contenente lo username
     * di un attore, per la ricerca nel database.*/
    public static String getNomeFieldUsernameAttore() {
        return UtilitaGenerale.ricercaFieldPerNomeInQuestaClasse("username", Attore.class);
    }

    /** Restituisce il nome del field contenente lo'email
     * di un attore..*/
    public static String getNomeFieldEmailAttore() {
        return UtilitaGenerale.ricercaFieldPerNomeInQuestaClasse("email", Attore.class);
    }

    /** Restituisce il nome del field contenente il nome
     * di un attore.*/
    public static String getNomeFieldNominativoAttore() {
        return UtilitaGenerale.ricercaFieldPerNomeInQuestaClasse("nominativo", Attore.class);
    }

    /** Salva un nuovo attore nel database e notifica via email l'avvenuta
     * registrazione all'attore stesso.
     * @param attore Attore da salvare nel sistema.
     * @param password Password dell'attore da salvare nel sistema.
     * @param inviarePassword Flag: true se la notifica all'attore deve contenere
     *                        anche la password in chiaro.
     * @param httpServletRequest La richiesta HTTP che ha richiesto la creazione dell'attore.
     * @return l'identificativo dell'attore salvato nel database
     *          se l'operazione va a buon fine, null altrimenti.
     * @throws UnsupportedEncodingException Generata dal tentativo di invio dell'email.
     * @throws MessagingException Generata dal tentativo di invio dell'email.*/
    public static Long salvaNuovoAttoreInDatabase(Attore attore, String password,
                                                  boolean inviarePassword,
                                                  HttpServletRequest httpServletRequest)
            throws UnsupportedEncodingException, MessagingException {

        try {
            AuthenticationDatabaseEntry authenticationDatabaseEntry =
                    new AuthenticationDatabaseEntry(attore.getUsername(), password, false);

            Long identificativoNuovoAttore = (Long) DatabaseHelper.salvaEntita(attore);
            DatabaseHelper.salvaEntita(authenticationDatabaseEntry);

            String urlPerVerificaAccount;
            {
                // Creazione link per la verifica dell'account
                String indirizzoQuestoServer = UtilitaGenerale.getIndirizzoServer(httpServletRequest);
                urlPerVerificaAccount = indirizzoQuestoServer + VerificaRegistrazione.PATH_SERVIZIO_VERIFICA_ACCCOUNT +
                        '?' + VerificaRegistrazione.NOME_QUERY_PARAM_CON_USERNAME_ACCOUNT_DA_VERIFICARE + '=' + authenticationDatabaseEntry.getUsername() + '&' +
                              VerificaRegistrazione.NOME_QUERY_PARAM_CON_TOKEN_ACCOUNT_DA_VERIFICARE    + '=' + authenticationDatabaseEntry.getTokenVerificaAccount();
                        // info per verificare l'account aggiunti come query param
            }

            {
                // Invio email all'attore appena creato

                String oggettoEmail = "Creazione nuovo account";

                // Creazione del corpo del messaggio in formato HTML
                String messaggioHtml =
                        "<!DOCTYPE html>"                                                       +
                        "<html lang=\"it\">"                                                                +
                            "<head>"                                                            +
                                "<title>" + oggettoEmail + "</title>"                           +
                            "</head>"                                                           +
                            "<body>"                                                            +
                                "<h1>" + oggettoEmail + "</h1>"                                 +
                                "<p>"                                                           +
                                    "E' stato creato un nuovo account nella piattaforma. "      +
                                    "Sarà possibile accedervi con le seguenti credenziali:"     +
                                "</p>"                                                          +
                                "<ul>"                                                      +
                                    "<li>username: <strong>" + attore.getUsername() + "</strong></li>"  +
                                    "<li>password: "                                         +
                    ( inviarePassword ? "<strong>" + password + "</strong>"
                                      : "<span style=\"font-style: italic\">utilizzare la password scelta in fase di registrazione.</span>")  +
                                    "</li>"                                                 +
                                "</ul>"                                                     +
                    ( inviarePassword ?
                                "<p>" +
                                    "E' <em>fortemente</em> raccomandata la modifica della "+
                                    "password al primo accesso al sistema."                 +
                                "</p>"
                                      : "")                                                      +
                                "<p>"                                                           +
                                    "Per poter essere utilizzato, l'account deve essere "       +
                                    "confermato: cliccare "                                     +
                                    "<a href=\"" + urlPerVerificaAccount + "\">qui</a>"         +
                                    " per confermare l'account."                                +
                                "</p>"                                                          +
                            "</body>"                                                           +
                        "</html>";

                new MailSender()
                        .inviaEmailMultiPart( attore.getEmail(), attore.getNominativo(),
                                              oggettoEmail, "", messaggioHtml,
                                              null, null, null );

            }

            return identificativoNuovoAttore;

        } catch (InvalidKeyException|NoSuchAlgorithmException e) {
            Logger.scriviEccezioneNelLog( Attore.class, e );
            return null;
        }


    }


    protected Attore(@NotNull String username, String nominativo, String email, TipoAttore tipoAttore) {
        if(username==null)
            throw new NullPointerException("Username non può essere null");

        this.username = username;
        this.nominativo = nominativo;
        setEmail(email);
        this.tipoAttore = tipoAttore;
    }

    protected Attore() {}

    public String getNominativo() {
        return nominativo;
    }

    @NotNull
    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public void setIdentificativoAttore(Long identificativoAttore) {
        if( UtilitaGenerale.isStringaNonNullaNonVuota(username) )
            this.identificativoAttore = identificativoAttore;
    }

    public void setNominativo(String nominativo) {
        if( UtilitaGenerale.isStringaNonNullaNonVuota(nominativo) )
            this.nominativo = EncoderPrevenzioneXSS.encodeForJava(nominativo);
    }

    @SuppressWarnings("unused") // Usato per de/serializzazione JSON dell'oggetto
    public void setUsername(String username) {
        if( UtilitaGenerale.isStringaNonNullaNonVuota(username) )
            this.username = EncoderPrevenzioneXSS.encodeForJava( username );
    }

    /** Modifica l'email solo se il nuovo valore è in un formato valido,
     * altrimenti la mantiene inalterata.*/
    public void setEmail(String email) {

        if( RegexHelper.isEmailValida(email) )
            this.email = EncoderPrevenzioneXSS.encodeForJava( email );

    }

    /** @throws IllegalArgumentException Se il parametro non è valido.*/
    public void setTipoAttore(String tipoAttore) {
        this.tipoAttore = TipoAttore.valueOf(tipoAttore);
    }

    /** Restituisce l'attore corrispondente all'identificativo dato nel parametro,
     * oppure null se non trovato.*/
    public static Attore getAttoreDaIdentificativo(Long identificativoAttore) {
        try {
            if( identificativoAttore==null )
                throw new NullPointerException();
            return  (Attore) DatabaseHelper.getById(identificativoAttore, Attore.class);
        } catch (NotFoundException|NullPointerException ignored) {
            return null;
        }
    }

    /** Elimina dal database l'attore corrispondente all'identificativo dato nel parametro.
     * @return true se l'eliminazione viene completata, false se si verificano errori.*/
    public static boolean eliminaAttoreDaIdentificativo(Long identificativoAttore) {
        try {
            Attore attoreDaEliminare = getAttoreDaIdentificativo(identificativoAttore);

            RelazioneUploaderConsumer.eliminaUploader(identificativoAttore);    // se non è un Uploader non fa nulla
            DatabaseHelper.cancellaAdessoEntitaById(identificativoAttore, Attore.class);

            if( attoreDaEliminare != null )
                AuthenticationDatabaseEntry.eliminaEntry( attoreDaEliminare.getUsername() );

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /** Ricerca nel database e restituisce l'attore corrispondente
     * allo username fornito, oppure null se non trovato.*/
    public static Attore getAttoreDaUsername(String username) {

        String nomeAttributoQuery = "username";
        return ricercaInDatabase(username, nomeAttributoQuery);

    }

    /** Ricerca nel database e restituisce l'attore corrispondente
     * all'email fornita, oppure null se non trovato.
     * @throws IllegalStateException se vengono trovati più istanze
     * di {@link Attore} con la stessa email.
     * Vedere {@link Attore#ricercaInDatabase(String, String)} per
     * il messaggio restituito dall'eccezione. */
    public static Attore getAttoreDaEmail(String email)
            throws IllegalStateException {

        String nomeAttributoQuery = "email";
        return ricercaInDatabase(email, nomeAttributoQuery);

    }

    /** Metodo privato per la ricerca di un {@link Attore} nel database,
     * avente l'attributo specificato nei parametri con il valore specificato.
     * @throws IllegalStateException Se viene trovato più di un attore con il
     * valore indicato nella query, avente come messaggio la rappresentazione
     * JSON della lista degli username degli Attori trovati.*/
    private static Attore ricercaInDatabase(String valoreAttributoDaCercare, String nomeAttributoQuery) {

        if( UtilitaGenerale.esisteAttributoInClasse( nomeAttributoQuery, Attore.class ) ) {

            List<?> risultatoQuery = DatabaseHelper.query(
                    Attore.class,
                    nomeAttributoQuery,
                    DatabaseHelper.OperatoreQuery.UGUALE,
                    valoreAttributoDaCercare
            );

            if( risultatoQuery.size() == 1 ) {
                return (Attore) risultatoQuery.get(0);
            } else if( risultatoQuery.size() > 1 ) {
                List<?> listaUsernameTrovati = risultatoQuery
                        .stream()
                        .map( unAttore -> ((Attore)unAttore).getUsername() )
                        .collect(Collectors.toList());
                throw new IllegalStateException(JsonHelper.convertiOggettoInJSON(listaUsernameTrovati) );
            } else {
                return null;
            }

        } else {

            Logger.scriviEccezioneNelLog(
                    Attore.class,
                    "Attributo " + nomeAttributoQuery + " non presente in questa classe.",
                    new NoSuchFieldException()
            );

            return null;
        }

    }

    /** Ricerca nel database e restituisce la lista degli identificativi di
     * tutti gli Attori della classe data nel parametro registrati nel sistema.*/
    public static List<Long> getListaIdentificativiTuttiGliAttoriNelSistema( Class<?> classeAttore ) {

        return DatabaseHelper.listaEntitaNelDatabase(classeAttore)
                .stream()
                .map( attore -> ((Attore)attore).getIdentificativoAttore() )
                .collect(Collectors.toList());

    }


    /** Due attori sono considerati equivalenti se hanno gli stessi
     * {@link #username}, {@link #nominativo} ed {@link #email}.*/
    @Override
    public boolean equals(Object o) {

        if (this == o) return true;
        if (o == null ) return false;
        if ( ! ( o instanceof Attore )  ) return false;

        Attore attore = (Attore) o;

        if (username != null ? !username.equals(attore.username) : attore.username != null) return false;
        if (nominativo != null ? !nominativo.equals(attore.nominativo) : attore.nominativo != null) return false;

        return email != null ? email.equals(attore.email) : attore.email == null;
    }

    @Override
    public int hashCode() {
        int result = username.hashCode();
        result = 31 * result + nominativo.hashCode();
        result = 31 * result + email.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "\nusername: '" + username + '\'' +
               "\nnominativo: '" + nominativo + '\'' +
               "\nemail: '" + email + '\'';
    }


    /** Restituisce una copia di questo attore
     * (<a href="https://stackoverflow.com/a/18146676">Fonte</a>).
     * @return Il clone dell'istanza data, oppure null se si verificano
     *         dei problemi durante lo'operazione.
     */
    @SuppressWarnings("MethodDoesntCallSuperMethod")    // metodo gestito interamente qui senza chiamata a super
    @Override
    public Attore clone() {

        try {

            Constructor<? extends Attore> costruttoreAttore =
                    this.getClass().getDeclaredConstructor( this.getClass() );

            costruttoreAttore.setAccessible( true );
            return costruttoreAttore.newInstance( this );

        } catch (NoSuchMethodException | InstantiationException |
                IllegalAccessException | InvocationTargetException e) {
            Logger.scriviEccezioneNelLog(Attore.class, "Errore nella creazione del clone di un attore", e);
            return null;
        }

    }


    /**
     * Eccezione generata quando si cerca di assegnare uno username ad
     * un attore, ma quell'attore richiede un particolare formato di
     * username che non è stato rispettato (ad esempio il codice fiscale).
     *
     * @author Matteo Ferfoglia
     */
    public static class FormatoUsernameInvalido extends RuntimeException {

        public FormatoUsernameInvalido(String messaggioErrore) {
            super( messaggioErrore );
        }

    }

    /** Restituisce il nominativo dell'attore. */
    @Override   // definita in Principal
    public String getName() {
        return getNominativo();
    }

    /** Restituisce sempre false. */
    @Override   // definita in Principal
    public boolean implies(Subject subject) {
        return false;
    }
}


