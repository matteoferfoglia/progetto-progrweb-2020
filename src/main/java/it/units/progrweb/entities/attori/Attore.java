package it.units.progrweb.entities.attori;

import com.googlecode.objectify.annotation.Id;
import it.units.progrweb.utils.Autenticazione;
import it.units.progrweb.utils.RegexHelper;

import javax.security.auth.Subject;
import java.nio.file.attribute.UserPrincipal;


/**
 * Rappresentazione di un attore.
 *
 * @author Matteo Ferfoglia
 */
public abstract class Attore implements UserPrincipal {

    // TODO : implementare questa classe

    /** Identificativo univoco per un attore. */
    @Id
    protected Long identificativoAttore;

    /** Username dell'attore.*/
    protected String username;

    /** Nome e cognome dell'attore.*/
    protected String nomeCognome;

    /** Email dell'attore. */
    protected String email;



    protected Attore(String username, String nomeCognome, String email) {
        this.username = username;
        this.nomeCognome = nomeCognome;
        setEmail(email);
        // TODO
    }

    protected Attore() {}

    public long getIdentificativoAttore() {
        return identificativoAttore;
    }

    public String getNomeCognome() {
        return nomeCognome;
    }

    protected void setEmail(String email) {
        if(!RegexHelper.isEmailValida(email))
            throw new IllegalArgumentException("Formato email non valido.");

        this.email = email;
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

    /** Restituisce l'attore corrispondente all'id dato nel parametro,
     * oppure null se non trovato.*/
    public static Attore getAttoreById(Long identificativoAttore) {
        // TODO : accedere al database e cercare l'attore
        return Autenticazione.getAttoreDaCredenziali("Pippo", "pippo"); // TODO : questo metodo è ancora da implementare!!! Quest'istruzione è solo temporanea per far funzionare il resto intanto
        // return null
    }



    @Override
    public boolean equals(Object o) {

        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Attore attore = (Attore) o;

        if (identificativoAttore != attore.identificativoAttore) return false;
        if (username != null ? !username.equals(attore.username) : attore.username != null) return false;
        if (nomeCognome != null ? !nomeCognome.equals(attore.nomeCognome) : attore.nomeCognome != null) return false;

        return email != null ? email.equals(attore.email) : attore.email == null;
    }

    @Override
    public int hashCode() {
        int result = identificativoAttore.hashCode();
        result = 31 * result + username.hashCode();
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
                "identificativoAttore=" + identificativoAttore +
                ", username='" + username + '\'' +
                ", nomeCognome='" + nomeCognome + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}