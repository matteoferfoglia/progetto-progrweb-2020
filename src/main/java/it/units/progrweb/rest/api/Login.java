package it.units.progrweb.rest.api;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/login")
public class Login {

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String login(){
        // TODO metodo e signature
        return "Benvenuto utente registrato";
    }
}
