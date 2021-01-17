package it.units.progrweb.entities.attori;

import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;
import it.units.progrweb.entities.AuthenticationDatabaseEntry;
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


/**
 * Rappresentazione di un attore.
 *
 * @author Matteo Ferfoglia
 */
public abstract class Attore implements UserPrincipal {

    // TODO : implementare questa classe

    /** Username dell'attore.*/
    @Id
    @Index
    protected String username;

    /** Nome e cognome dell'attore.*/
    protected String nominativo;

    /** Email dell'attore. */
    protected String email;

    /** Tipo di attore (quale sottoclasse di {@link Attore}).*/
    protected String tipoAttore;

    /** Restituisce il tipo di un attore.*/
    public String getTipoAttore() {
        return tipoAttore;
    }

    /** Restituisce il nome del field contenente lo username
     * di un attore, per la ricerca nel database.*/
    public static String getNomeFieldUsernameAttore() {
        final String nomeField = "username";
        return UtilitaGenerale.ricercaFieldPerNomeInQuestaClasse(nomeField, Attore.class);
    }

    /** Restituisce il nome del field contenente il nome
     * di un attore.*/
    public static String getNomeFieldNomeAttore() {
        final String nomeField = "nominativo";
        return UtilitaGenerale.ricercaFieldPerNomeInQuestaClasse(nomeField, Attore.class);
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

    protected void setNominativo(String nominativo) {
        this.nominativo = EncoderPrevenzioneXSS.encodeForJava(nominativo);
    }

    protected void setUsername(String username) {
        this.username = EncoderPrevenzioneXSS.encodeForJava( username );
    }

    /** Modifica l'email solo se il nuovo valore è in un formato valido,
     * altrimenti la mantiene inalterata.*/
    protected void setEmail(String email) {

        if( RegexHelper.isEmailValida(email) )
            this.email = email;

    }

    protected void setTipoAttore(String tipoAttore) {
        this.tipoAttore = tipoAttore;
    }

    /** Restituisce true se la password viene correttamente modificata, false altrimenti.*/
    protected boolean modificaPropriaPassword(String nuovaPassword) {
        // TODO : accedere al database per modificare la password
        return true;
    }

    /** Restituisce true se il campo nominativo viene correttamente modificato, false altrimenti.*/
    protected boolean modificaNominativo(String nominativo){
        // TODO : accedere al database per modificare la password
        return true;
    }

    /** Restituisce l'attore corrispondente allo username dato nel parametro,
     * oppure null se non trovato.*/
    public static Attore getAttoreById(String usernameAttore) {
        try {
            return  (Attore) DatabaseHelper.getById(usernameAttore, Attore.class);
        } catch (NotFoundException e) {
            return null;
        }
    }



    @Override
    public boolean equals(Object o) {

        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

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
                "username='" + username + '\'' +
                ", nominativo='" + nominativo + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}