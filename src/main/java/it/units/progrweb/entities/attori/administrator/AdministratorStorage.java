package it.units.progrweb.entities.attori.administrator;

import com.googlecode.objectify.annotation.Subclass;
import it.units.progrweb.entities.attori.Attore;

/**
 * Classe rappresentante un'entità {@link Administrator}
 * da memorizzare nel database.
 * @author Matteo Ferfoglia
 */
@SuppressWarnings("DefaultAnnotationParam")
@Subclass(index = false)
class AdministratorStorage extends Administrator {

    @SuppressWarnings("unused") // necessario per Objectify ("There must be a no-arg constructor")
    private AdministratorStorage() {
        super();
    }

    /** Copy-constructor, usato in {@link Attore#clone()}.*/
    @SuppressWarnings("unused") // Usato tramite reflection dal metodo Attore#clone()
    private AdministratorStorage( AdministratorStorage administrator ) {
        this( administrator.username, administrator.nominativo, administrator.email );
    }

    public AdministratorStorage(String username, String nominativo, String email) {
        super(username, nominativo, email);
    }
}
