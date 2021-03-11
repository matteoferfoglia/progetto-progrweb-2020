package it.units.progrweb.entities.attori.nonAdministrator;

import it.units.progrweb.entities.attori.Attore;

/**
 * Rappresentazione di un utente non Administrator.
 * @author Matteo Ferfoglia
 */
public abstract class UtenteNonAdministrator extends Attore {

    protected UtenteNonAdministrator() {
        super();
    }

    protected UtenteNonAdministrator(String username, String nominativo, String email, TipoAttore tipoAttore) {
        super(username, nominativo, email, tipoAttore);
    }

    /** Restituisce true se il campo email viene correttamente modificato, false altrimenti.*/
    protected boolean modificaEmail(String nuovaEmail) {
        setEmail(nuovaEmail);
        return true;
    }
}