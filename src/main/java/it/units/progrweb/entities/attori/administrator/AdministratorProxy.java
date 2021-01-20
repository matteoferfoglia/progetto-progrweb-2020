package it.units.progrweb.entities.attori.administrator;

import java.util.Map;

/**
 * Classe proxy per {@link Administrator}.
 * @author Matteo Ferfoglia
 */
public class AdministratorProxy extends Administrator {
    public AdministratorProxy(String username, String nominativo, String email) {
        super(username, nominativo, email);
    }

    @Override
    public Map<String, ?> getMappaAttributi_Nome_Valore() {
        return null;    // TODO : metodo da implementare
    }
}
