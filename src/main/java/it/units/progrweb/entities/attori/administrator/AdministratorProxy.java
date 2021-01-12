package it.units.progrweb.entities.attori.administrator;

/**
 * Classe proxy per {@link Administrator}.
 * @author Matteo Ferfoglia
 */
public class AdministratorProxy extends Administrator {
    public AdministratorProxy(String username, String nomeCognome, String email) {
        super(username, nomeCognome, email);
    }
}
