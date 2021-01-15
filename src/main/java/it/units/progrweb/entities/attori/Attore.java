package it.units.progrweb.entities.attori;

import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;
import it.units.progrweb.persistence.DatabaseHelper;
import it.units.progrweb.persistence.NotFoundException;
import it.units.progrweb.utils.RegexHelper;
import it.units.progrweb.utils.UtilitaGenerale;

import javax.security.auth.Subject;
import java.nio.file.attribute.UserPrincipal;


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
    protected String nomeCognome;

    /** Email dell'attore. */
    protected String email;

    /** Restituisce il nome del field contenente lo username
     * di un attore, per la ricerca nel database.*/
    public static String getNomeFieldUsernameAttore() {
        final String nomeField = "username";
        return UtilitaGenerale.ricercaFieldPerNomeInQuestaClasse(nomeField, Attore.class);
    }

    /** Restituisce il nome del field contenente il nome
     * di un attore.*/
    public static String getNomeFieldNomeAttore() {
        final String nomeField = "nomeCognome";
        return UtilitaGenerale.ricercaFieldPerNomeInQuestaClasse(nomeField, Attore.class);
    }


    protected Attore(String username, String nomeCognome, String email) {
        this.username = username;
        this.nomeCognome = nomeCognome;
        setEmail(email);
        // TODO
    }

    protected Attore() {}

    public String getNomeCognome() {
        return nomeCognome;
    }

    protected void setEmail(String email) {
        if( ! RegexHelper.isEmailValida(email) )
            throw new IllegalArgumentException("Formato email non valido.");

        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public String getUsername() {
        return username;
    }

    /** Restituisce true se la password viene correttamente modificata, false altrimenti.*/
    protected boolean modificaPropriaPassword(String nuovaPassword) {
        // TODO : accedere al database per modificare la password
        return true;
    }

    /** Restituisce true se il campo nomeCognome viene correttamente modificato, false altrimenti.*/
    protected boolean modificaNomeCognome(String nuovoNomeCognome){
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
        if (nomeCognome != null ? !nomeCognome.equals(attore.nomeCognome) : attore.nomeCognome != null) return false;

        return email != null ? email.equals(attore.email) : attore.email == null;
    }

    @Override
    public int hashCode() {
        int result = username.hashCode();
        result = 31 * result + nomeCognome.hashCode();
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
                ", nomeCognome='" + nomeCognome + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}