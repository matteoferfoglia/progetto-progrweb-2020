package it.units.progrweb.api;

import it.units.progrweb.utils.Autenticazione;
import it.units.progrweb.utils.EncoderPrevenzioneXSS;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Classe per la gestione del login dei client.
 * @author Matteo Ferfoglia
 */
@Path("/login")     // TODO : path variabile d'ambiente
public class Login {

    /** Risponde alle richieste di login, rilasciando
     * un'opportuna {@link Response} al client.*/
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    public Response login(CampiFormLogin campiFormLogin){

        // TODO : verificare che CSRF token venga controllato e testare
        return Autenticazione.creaResponseAutenticazione(campiFormLogin.getUsername(), campiFormLogin.getPassword());
    }

}

@SuppressWarnings("unused")
class CampiFormLogin {
    private String username;
    private String password;
    private String csrfToken;

    CampiFormLogin(){}

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = EncoderPrevenzioneXSS.encodeForJava(username);
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = EncoderPrevenzioneXSS.encodeForJava(password);
    }

    public String getCsrfToken() {
        return csrfToken;
    }

    public void setCsrfToken(String csrfToken) {
        this.csrfToken = EncoderPrevenzioneXSS.encodeForJava(csrfToken);
    }
}