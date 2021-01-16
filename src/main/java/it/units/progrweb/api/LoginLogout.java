package it.units.progrweb.api;

import it.units.progrweb.filters.FiltroAutenticazione;
import it.units.progrweb.utils.Autenticazione;
import it.units.progrweb.utils.EncoderPrevenzioneXSS;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Classe per la gestione dell'autenticazione dei client.
 * Vedere anche {@link FiltroAutenticazione}.
 * @author Matteo Ferfoglia
 */
@Path("")
public class LoginLogout {

    /** Risponde alle richieste di login, rilasciando
     * un'opportuna {@link Response} al client.*/
    @Path("/login")     // TODO : path variabile d'ambiente
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    public Response login(CampiFormLogin campiFormLogin){

        // TODO : verificare che CSRF token venga controllato e testare
        return Autenticazione.creaResponseAutenticazione(campiFormLogin.getUsername(), campiFormLogin.getPassword());
    }

    /** Effettua il logout del client che ne fa richiesta. */
    @Path("/logout")     // TODO : path variabile d'ambiente
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public Response logout() {

        // TODO : testare
        return Autenticazione.creaResponseLogout();

    }

    /** Servizio per verificare se l'utente che ha effettuato
     * la richiesta risulta attualmente autenticato: se la
     * verifica ha esito positivo, questo metodo restituisce
     * "true", altrimenti "false".*/
    @Path("/verificaTokenAutenticazione")   // TODO : var d'ambiente
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public boolean isClientAttualmenteAutenticato(@Context HttpServletRequest httpServletRequest) {
        return Autenticazione.isClientAutenticato(httpServletRequest);
    }

    /** Restituisce il nome dell'attore attualmente autenticato.*/
    @Path("/nomeDiQuestoAttore")    // TODO : path variabile d'ambiente
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String nomeDiQuestoAttoreAutenticato(@Context HttpServletRequest httpServletRequest) {
        // TODO : aggiungerlo al token di autenticazione per evitare accessi nel database
        String nomeAttore = Autenticazione.getNomeAttoreDaHttpServletRequest(httpServletRequest);
        return nomeAttore!=null ? EncoderPrevenzioneXSS.encodeForHTMLContent(nomeAttore) : "";     // TODO : aggiungere EncoderPrevenzioneXSS in tutti i metodi che restituiscono qualcosa al client
    }

    /** Restituisce il tipo di utente autenticato. */
    @Path("/getTipoUtenteAutenticato")  // TODO : var d'ambiente
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String getTipoUtenteAutenticato(@Context HttpServletRequest httpServletRequest) {
        String tipoAttore = Autenticazione.getTipoAttoreDaHttpServletRequest(httpServletRequest);
        return EncoderPrevenzioneXSS.encodeForHTMLContent( tipoAttore );

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
        this.username = EncoderPrevenzioneXSS.encodeForJava(username==null ? "" : username);
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = EncoderPrevenzioneXSS.encodeForJava(password==null ? "" : password);
    }

    public String getCsrfToken() {
        return csrfToken;
    }

    public void setCsrfToken(String csrfToken) {
        this.csrfToken = EncoderPrevenzioneXSS.encodeForJava(csrfToken==null ? "" : csrfToken);
    }
}