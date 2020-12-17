package it.units.progrweb.rest.api;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/api/login")
public class Login {

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public String login(){
        // TODO metodo e signature
        return "Benvenuto utente registrato";
    }
}
