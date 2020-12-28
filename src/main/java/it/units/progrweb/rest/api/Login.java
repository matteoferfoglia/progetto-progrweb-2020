package it.units.progrweb.rest.api;

import it.units.progrweb.entities.Attore;
import it.units.progrweb.utils.Autenticazione;
import it.units.progrweb.utils.csrf.CsrfToken;
import it.units.progrweb.utils.EncoderPrevenzioneXSS;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

/**
 * @author Matteo Ferfoglia
 */
@Path("/login")
public class Login {

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    public String login(CampiFormLogin campiFormLogin, @HeaderParam("Cookie") String cookieHeader){
        // TODO metodo e signature
        if(CsrfToken.isCsrfTokenValido(campiFormLogin.getCsrfToken(), cookieHeader)) {

            Attore attore = Autenticazione.getAttoreDaCredenziali(campiFormLogin.getUsername(), campiFormLogin.getPassword());
            if(Autenticazione.isAttoreAutenticato(attore)) {

                // TODO : se procedura di login va a buon fine => impostare COOKIE-AUTENTICAZIONE
                return "Benvenuto " + campiFormLogin.getUsername();
            }

            return "Credenziali invalide";
        }
        else
            return "CsrfToken invalido";
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