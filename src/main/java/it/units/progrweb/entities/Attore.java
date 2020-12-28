package it.units.progrweb.entities;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;
import it.units.progrweb.utils.RegexHelper;

import javax.persistence.GeneratedValue;

/**
 * Rappresentazione di un attore.
 *
 * @author Matteo Ferfoglia
 */
@Entity
public class Attore {

    // TODO : implementare questa classe

    /** Identificativo univoco per un attore. */
    @Id
    @GeneratedValue
    protected long identificativoAttore;

    /** Username dell'attore.*/
    @Index  // TODO : non si può usare javax.* per evitare di essere vendor specific?
            // TODO : quando usare index?
    protected String username;

    /** Nome e cognome dell'attore.*/
    protected String nomeCognome;

    /** Email dell'attore. */
    protected String email;

    public long getIdentificativoAttore() {
        return identificativoAttore;
    }

    public void setIdentificativoAttore(long identificativoAttore) {
        this.identificativoAttore = identificativoAttore;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getNomeCognome() {
        return nomeCognome;
    }

    public void setNomeCognome(String nomeCognome) {
        this.nomeCognome = nomeCognome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        if(!RegexHelper.isEmailValida(email))
            throw new IllegalArgumentException("Formato email non valido.");

        this.email = email;
    }
}