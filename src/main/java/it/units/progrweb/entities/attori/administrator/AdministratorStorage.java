package it.units.progrweb.entities.attori.administrator;

import com.googlecode.objectify.annotation.Subclass;
import it.units.progrweb.entities.attori.Attore;

/**
 * Classe rappresentante un'entità {@link Administrator}
 * da memorizzare nel database.
 * @author Matteo Ferfoglia
 */
// TODO : serve proxy pattern?
@Subclass(index = false)
class AdministratorStorage extends Administrator {

    private AdministratorStorage() {
        super();
    }

    /** Copy-constructor, usato in {@link Attore#clone()}.*/
    private AdministratorStorage( AdministratorStorage administrator ) {
        this( administrator.username, administrator.nominativo, administrator.email );
    }

    public AdministratorStorage(String username, String nominativo, String email) {
        super(username, nominativo, email);
    }
}
