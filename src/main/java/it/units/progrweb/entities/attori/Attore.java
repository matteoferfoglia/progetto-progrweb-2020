package it.units.progrweb.entities.attori;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;
import it.units.progrweb.entities.AuthenticationDatabaseEntry;
import it.units.progrweb.entities.RelazioneUploaderConsumer;
import it.units.progrweb.entities.attori.administrator.Administrator;
import it.units.progrweb.entities.attori.nonAdministrator.consumer.Consumer;
import it.units.progrweb.entities.attori.nonAdministrator.uploader.Uploader;
import it.units.progrweb.persistence.DatabaseHelper;
import it.units.progrweb.persistence.NotFoundException;
import it.units.progrweb.utils.*;

import javax.security.auth.Subject;
import javax.validation.constraints.NotNull;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.attribute.UserPrincipal;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;


/**
 * Rappresentazione di un attore.
 *
 * @author Matteo Ferfoglia
 */
@Entity
public abstract class Attore implements UserPrincipal, Cloneable {

    /** Identificativo di un utente.*/
    @Id
    protected Long identificativoAttore;

    /** Username dell'attore.*/
    @Index
    protected String username;

    /** Nome e cognome dell'attore.*/
    protected String nominativo;

    /** Email dell'attore. */
    protected String email;

    /** Tipo di attore (quale sottoclasse di {@link Attore}).*/
    protected TipoAttore tipoAttore;

    /** Questo metodo si occupa di modificare le informazioni di un attore.
     * Funzionamento di questo metodo: clona l'attore ottenuto dal database,
     * una copia la sovrascrive coi nuovi dati mandati dal client (se non
     * nulli) e se risultano delle modifiche (rispetto al clone), le salva nel DB.
     * Le modifiche vanno fatte sull'oggetto ottenuto dal DB, altrimenti poi quando si
     * salva, viene creata una nuova entità anziché sovrascrivere quella esistente.
     * @param attoreDaModificare_conModificheRichiesteDaClient E' la reappresentaazione
     *                              dell'attore con le modifiche richieste dal cliente.
     * @param attore_attualmenteSalvatoInDB E' lo stesso attore, ma con le
     *                       informazioni attualmente salvate nel database.*/
    public static Response modificaAttore(Attore attoreDaModificare_conModificheRichiesteDaClient,
                                          Attore attore_attualmenteSalvatoInDB ) {

        if( attore_attualmenteSalvatoInDB != null &&
                attoreDaModificare_conModificheRichiesteDaClient != null) {

            // Creazione dell'attore con le modifiche richieste dal client (Attore è una classe astratta)

            Attore copia_attore_attualmenteSalvatoInDB = attore_attualmenteSalvatoInDB.clone();

            attore_attualmenteSalvatoInDB.setEmail(attoreDaModificare_conModificheRichiesteDaClient.getUsername());
            attore_attualmenteSalvatoInDB.setNominativo(attoreDaModificare_conModificheRichiesteDaClient.getNominativo());
            // attore_attualmenteSalvatoInDB.setUsername(attoreDaModificare_conModificheRichiesteDaClient.getUsername()); // modifica username non permessa (da requisiti)
            if( attoreDaModificare_conModificheRichiesteDaClient instanceof Uploader ) {
                // SE si sta modificando un Uploader
                Uploader uploader_attualmenteSalvatoInDB = (Uploader) attore_attualmenteSalvatoInDB;
                try {
                    uploader_attualmenteSalvatoInDB
                            .setImmagineLogo( uploader_attualmenteSalvatoInDB.getImmagineLogo(),
                                              uploader_attualmenteSalvatoInDB.getEstensioneImmagineLogo() );
                } catch (IOException e) {
                    return Response.status( Response.Status.REQUEST_ENTITY_TOO_LARGE )
                                   .entity( e.getMessage() )
                                   .build();
                }
                attore_attualmenteSalvatoInDB = uploader_attualmenteSalvatoInDB;
            }

            if( ! attore_attualmenteSalvatoInDB.equals(copia_attore_attualmenteSalvatoInDB) ) {
                // Se non ci sono modifiche, risparmio l'inutile accesso in scrittura al DB
                DatabaseHelper.salvaEntita(attore_attualmenteSalvatoInDB);
            }

            return Response.ok()
                           .type( MediaType.APPLICATION_JSON )
                           .entity( attoreDaModificare_conModificheRichiesteDaClient )
                           .build();

        } else {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Attore da modificare non trovato nel sistema." )
                    .build();
        }

    }

    /** Tipi di attori permessi nel sistema.*/
    public enum TipoAttore {
        Consumer(Consumer.class.getSimpleName()),
        Uploader(Uploader.class.getSimpleName()),
        Administrator(Administrator.class.getSimpleName());

        private final String nomeTipoAttore;
        TipoAttore( String nomeTipoAttore ) {
            this.nomeTipoAttore = nomeTipoAttore;
        }

        public String getTipoAttore() {
            return nomeTipoAttore;
        }
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
        return tipoAttore == null ? "" : tipoAttore.getTipoAttore();
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

    /** Salva un nuovo attore nel database.
     * @return true se la procedura va a buon fine, false altrimenti.*/
    public static boolean salvaNuovoAttoreInDatabase( Attore attore, String password ) {

        try {
            AuthenticationDatabaseEntry authenticationDatabaseEntry =
                    new AuthenticationDatabaseEntry(
                            EncoderPrevenzioneXSS.encodeForJava( attore.getUsername() ),
                            EncoderPrevenzioneXSS.encodeForJava( password ) );

            DatabaseHelper.salvaEntita(attore);
            DatabaseHelper.salvaEntita(authenticationDatabaseEntry);

            return true;

        } catch (InvalidKeyException|NoSuchAlgorithmException e) {
            Logger.scriviEccezioneNelLog( Attore.class, e );
            return false;
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

    /** @throws IllegalArgumentException – Se il parametro non è valido.*/
    public void setTipoAttore(String tipoAttore) {
        this.tipoAttore = TipoAttore.valueOf(tipoAttore);
    }

    /** Restituisce l'attore corrispondente all'identificativo dato nel parametro,
     * oppure null se non trovato.*/
    public static Attore getAttoreDaIdentificativo(Long identificativoAttore) {
        try {
            return  (Attore) DatabaseHelper.getById(identificativoAttore, Attore.class);
        } catch (NotFoundException e) {
            return null;
        }
    }

    /** Elimina l'attore corrispondente all'identificativo dato nel parametro.
     * @return true se l'eliminazione viene completata, false se si verificano errori.*/
    public static boolean eliminaAttoreDaIdentificativo(Long identificativoAttore) {
        try {
            RelazioneUploaderConsumer.eliminaUploader(identificativoAttore);    // se non è un Uploader non fa nulla
            DatabaseHelper.cancellaAdessoEntitaById(identificativoAttore, Attore.class);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /** Restituisce l'attore corrispondente allo username fornito,
     * oppure null se non trovato.*/
    public static Attore getAttoreDaUsername(String username) {

        String nomeAttributoQuery = "username";
        if( UtilitaGenerale.esisteAttributoInClasse( nomeAttributoQuery, Attore.class ) ) {
            List<?> risultatoQuery = DatabaseHelper.query(Attore.class,
                    nomeAttributoQuery, DatabaseHelper.OperatoreQuery.UGUALE, username);
            if( risultatoQuery.size() == 1 ) {
                return (Attore) risultatoQuery.get(0);
            } else {
                return null;
            }
        } else {
            Logger.scriviEccezioneNelLog(Attore.class,
                    "Attributo " + nomeAttributoQuery + " non presente in questa classe.",
                    new NoSuchFieldException());
            return null;
        }

    }

    /** Restituisce la lista degli identificativi di tutti gli Attori della
     * classe data nel parametro registrati nel sistema.*/
    public static List<Long> getListaIdentificativiTuttiGliAttoriNelSistema( Class classeAttore ) {

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



    /** Vedere {@link Principal#getName()}.*/
    @Override
    public String getName() {
        return username;
    }

    /** Restituisce sempre false.*/
    @Override
    public boolean implies(Subject subject) {
        return false;
    }

    @Override
    public String toString() {
        return "Attore{" +
                "identificativo='" + identificativoAttore + '\'' +
                ", username='" + username + '\'' +
                ", nominativo='" + nominativo + '\'' +
                ", email='" + email + '\'' +
                '}';
    }


    /** Restituisce una copia di questo attore
     * (<a href="https://stackoverflow.com/a/18146676">Fonte</a>).
     * @return Il clone dell'istanza data, oppure null se si verificano
     *         dei problemi durante lo'operazione.
     */
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


}


