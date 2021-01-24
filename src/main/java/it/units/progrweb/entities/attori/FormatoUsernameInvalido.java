package it.units.progrweb.entities.attori;

/**
 * Eccezione generata quando si cerca di assegnare uno username ad
 * un attore, ma quell'attore richiede un particolare formato di
 * username che non è stato rispettato (ad esempio il codice fiscale).
 *
 * @author Matteo Ferfoglia
 */
public class FormatoUsernameInvalido extends RuntimeException {

    public FormatoUsernameInvalido(String messaggioErrore) {
        super( messaggioErrore );
    }

}
