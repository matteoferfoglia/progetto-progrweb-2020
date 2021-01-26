package it.units.progrweb.entities.attori.administrator;

import com.googlecode.objectify.annotation.Subclass;
import it.units.progrweb.entities.attori.Attore;

/**
 * Rappresentazione di un attore con il ruolo di Administrator.
 * @author Matteo Ferfoglia
 */
@Subclass(index = false)    // necessario index=true per distinguere nel DB questa classe dalla superclasse
public abstract class Administrator extends Attore {

    // TODO : implementare questa classe

    protected Administrator() {
        super();    // TODO
    }

    public Administrator(String username, String nominativo, String email) {
        super(username, nominativo, email);  // TODO
        setTipoAttore(Administrator.class.getSimpleName());
    }

    /** Crea un attore di questa classe con le proprietà specificate nei parametri.*/
    public static Administrator creaAttore(String username, String nominativo, String email) {
        return new AdministratorStorage(username, nominativo, email);    // TODO
    }

}

