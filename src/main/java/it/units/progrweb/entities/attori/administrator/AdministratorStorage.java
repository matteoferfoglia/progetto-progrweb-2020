package it.units.progrweb.entities.attori.administrator;

import com.googlecode.objectify.annotation.Subclass;

import java.util.Map;

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

    @Override
    public Map<String, ?> getMappaAttributi_Nome_Valore() {
        return null;    // TODO : metodo da implementare
    }

    public AdministratorStorage(String username, String nominativo, String email) {
        super(username, nominativo, email);
    }
}
