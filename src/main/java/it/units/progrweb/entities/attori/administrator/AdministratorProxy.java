package it.units.progrweb.entities.attori.administrator;

/**
 * Classe proxy per {@link Administrator}.
 * @author Matteo Ferfoglia
 */
public class AdministratorProxy extends Administrator {
    public AdministratorProxy(String username, String nominativo, String email) {
        super(username, nominativo, email);
    }

    /** Copy-constructor.*/
    public AdministratorProxy( AdministratorProxy administrator ) {
        this( administrator.username, administrator.nominativo, administrator.email );
    }

}
