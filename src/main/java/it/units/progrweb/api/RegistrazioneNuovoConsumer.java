package it.units.progrweb.api;

import it.units.progrweb.utils.EncoderPrevenzioneXSS;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * @author Matteo Ferfoglia
 */
@Path("/registrazioneNuovoConsumer")
public class RegistrazioneNuovoConsumer {

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    public String registrazioneNuovoConsumer(CampiFormRegistrazione campiFormRegistrazione) {
        // TODO metodo e signature
        return "Registrazione completata per " + campiFormRegistrazione.getCodiceFiscale();
    }
}


@SuppressWarnings("unused")
class CampiFormRegistrazione {

    private String codiceFiscale;
    private String nomeCognome;
    private String email;
    private String password;

    public CampiFormRegistrazione() {}

    public String getCodiceFiscale() {
        return codiceFiscale;
    }

    public String getNomeCognome() {
        return nomeCognome;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public void setCodiceFiscale(String codiceFiscale) {
        this.codiceFiscale =  EncoderPrevenzioneXSS.encodeForJava(codiceFiscale);
    }

    public void setNomeCognome(String nomeCognome) {
        this.nomeCognome = EncoderPrevenzioneXSS.encodeForJava(nomeCognome);
    }

    public void setEmail(String email) {
        this.email = EncoderPrevenzioneXSS.encodeForJava(email);
    }

    public void setPassword(String password) {
        this.password = EncoderPrevenzioneXSS.encodeForJava(password);
    }
}