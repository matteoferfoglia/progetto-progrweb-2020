package it.units.progrweb.api;

import it.units.progrweb.utils.Autenticazione;
import it.units.progrweb.utils.EncoderPrevenzioneXSS;
import it.units.progrweb.utils.csrf.CsrfToken;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * @author Matteo Ferfoglia
 */
@Path("/login")
public class Login {

    /** Risponde alle richieste di login, rilasciando
     * un'opportuna {@link Response} al client.
     * Verifica anche la validità del token CSRF nel form
     * di login.*/
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    public Response login(CampiFormLogin campiFormLogin,
                          @HeaderParam("Cookie") String cookieHeader,
                          @Context HttpServletRequest httpServletRequest){

        String indirizzoIPClient = httpServletRequest.getRemoteAddr();

        if(CsrfToken.isCsrfTokenValido(campiFormLogin.getCsrfToken(), cookieHeader, indirizzoIPClient))
            return Autenticazione.creaResponseAutenticazione(campiFormLogin.getUsername(), campiFormLogin.getPassword());
        else
            return CsrfToken.creaResponseCsrfTokenInvalido();
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