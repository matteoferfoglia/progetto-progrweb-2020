package it.units.progrweb.entities.attori.nonAdministrator;

import it.units.progrweb.entities.attori.Attore;

/**
 * Rappresentazione di un utente non Administrator.
 * @author Matteo Ferfoglia
 */
public abstract class UtenteNonAdministrator extends Attore {

    // TODO : implementare questa classe


    protected UtenteNonAdministrator() {
        super();
    }

    protected UtenteNonAdministrator(String username, String nomeCognome, String email) {
        super(username, nomeCognome, email);
    }



    /** Restituisce true se il campo email viene correttamente modificato, false altrimenti.*/
    protected boolean modificaEmail(String nuovaEmail) {
        setEmail(nuovaEmail);
        return true;    // TODO return true se modifica va a buon fine, false altrimenti
    }
}