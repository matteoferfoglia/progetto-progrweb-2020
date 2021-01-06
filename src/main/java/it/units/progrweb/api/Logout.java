package it.units.progrweb.api;

import it.units.progrweb.utils.Autenticazione;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Classe per la gestione del logout dei client.
 * @author Matteo Ferfoglia
 */
@Path("/logout")     // TODO : path variabile d'ambiente
public class Logout {

    /** Effettua il logout del client che ne fa richiesta. */
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public Response logout() {

        // TODO : testare
        return Autenticazione.creaResponseLogout();

    }

}
