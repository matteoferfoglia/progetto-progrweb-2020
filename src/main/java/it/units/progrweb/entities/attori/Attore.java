package it.units.progrweb.entities.attori;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;
import it.units.progrweb.entities.AuthenticationDatabaseEntry;
import it.units.progrweb.entities.attori.administrator.Administrator;
import it.units.progrweb.entities.attori.nonAdministrator.consumer.Consumer;
import it.units.progrweb.entities.attori.nonAdministrator.uploader.Uploader;
import it.units.progrweb.persistence.DatabaseHelper;
import it.units.progrweb.persistence.NotFoundException;
import it.units.progrweb.utils.EncoderPrevenzioneXSS;
import it.units.progrweb.utils.Logger;
import it.units.progrweb.utils.RegexHelper;
import it.units.progrweb.utils.UtilitaGenerale;

import javax.security.auth.Subject;
import java.nio.file.attribute.UserPrincipal;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.util.List;
import java.util.Map;


/**
 * Rappresentazione di un attore.
 *
 * @author Matteo Ferfoglia
 */
@Entity
public abstract class Attore implements UserPrincipal {

    // TODO : implementare questa classe

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
                    nomeAttributoPerQuery, DatabaseHelper.OperatoreQuery.UGUALE, username );    // TODO : che succede se non trova nulla ?

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
        return tipoAttore.getTipoAttore();
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


    protected Attore(String username, String nominativo, String email) {
        this.username = username;
        this.nominativo = nominativo;
        setEmail(email);
        // TODO
    }

    protected Attore() {}

    public String getNominativo() {
        return nominativo;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
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


    /** Ricerca nel database l {@link Attore} corrispondente
     * all'identificativo fornito e restituisce il suo nominativo
     * se presente, altrimenti restituisce null.*/
    public static String getNominativoDaIdentificativo(Long identificativoAttore) {
        try {
            return ((Attore)DatabaseHelper.getById( identificativoAttore, Consumer.class )).getNominativo();
        } catch (NotFoundException notFoundException) {
            return null;
        }
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
                "username='" + username + '\'' +
                ", nominativo='" + nominativo + '\'' +
                ", email='" + email + '\'' +
                '}';
    }


    /** Restituisce una mappa { "Nome attributo" -> "Valore attributo" }
     * di un'istanza di questa classe. Devono essere presenti le proprietà
     * i cui nomi sono restituiti dai metodi {@link #getNomeFieldNominativoAttore()},
     * {@link #getNomeFieldEmailAttore()} e {@link #getNomeFieldUsernameAttore()}.*/
    abstract public Map<String, ?> getMappaAttributi_Nome_Valore(); // TODO : metodo analogo anche in Consumer : si può mettere nella classe padre?

}


