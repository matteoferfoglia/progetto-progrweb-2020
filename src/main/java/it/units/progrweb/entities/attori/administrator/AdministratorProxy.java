package it.units.progrweb.entities.attori.administrator;

/**
 * Classe proxy per {@link Administrator}.
 * @author Matteo Ferfoglia
 */
public class AdministratorProxy extends Administrator {
    public AdministratorProxy(String username, String nominativo, String email) {
        super(username, nominativo, email);
    }

    /** Crea un'istanza di questa classe a partire da una di {@link AdministratorStorage}.*/
    public AdministratorProxy(AdministratorStorage administratorStorage) {
        // TODO: serve questo metodo ?
        this( administratorStorage.getUsername(),
              administratorStorage.getNominativo(),
              administratorStorage.getEmail() );
    }
}
