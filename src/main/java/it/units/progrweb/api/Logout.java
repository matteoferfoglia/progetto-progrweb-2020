package it.units.progrweb.api;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * Classe per la gestione del logout dei client.
 * @author Matteo Ferfoglia
 */
@Path("/logout")    // TODO : path deve essere variabile d'ambiente
public class Logout {

    /** Effettua il logout del client che ne fa richiesta. */
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String logout() {

        // TODO : metodo da implementare
        // TODO : verifica CSRF

        return "";

    }
}
