package it.units.progrweb.api.autenticazioneERegistrazione;

import it.units.progrweb.entities.attori.Attore;
import it.units.progrweb.entities.attori.consumer.Consumer;
import it.units.progrweb.utils.Autenticazione;
import it.units.progrweb.utils.EncoderPrevenzioneXSS;
import it.units.progrweb.utils.Logger;
import it.units.progrweb.utils.RegexHelper;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.UnsupportedEncodingException;

/**
 * @author Matteo Ferfoglia
 */
@Path("/registrazioneNuovoConsumer")
public class RegistrazioneNuovoConsumer {

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response registrazioneNuovoConsumer(@Context HttpServletRequest httpServletRequest,
                                               CampiFormRegistrazione campiFormRegistrazione) {

        String codiceFiscale = campiFormRegistrazione.getCodiceFiscale();
        String email         = campiFormRegistrazione.getEmail();
        String nominativo    = campiFormRegistrazione.getNominativo();
        String password      = campiFormRegistrazione.getPassword();

        if( RegexHelper.isEmailValida( email )                     &&
                RegexHelper.isCodiceFiscaleValido( codiceFiscale ) &&
                nominativo.length() > 0                            &&
                password.length()   > 0 ) {

            if( ! Attore.isAttoreGiaRegistrato( codiceFiscale ) ) {

                Consumer nuovoConsumer = Consumer.creaAttore( codiceFiscale, nominativo, email );

                boolean registrazioneConclusaConSuccesso;
                try {
                    registrazioneConclusaConSuccesso =
                            Attore.salvaNuovoAttoreInDatabase( nuovoConsumer, password, false, httpServletRequest ) != null;
                } catch (UnsupportedEncodingException | MessagingException e) {
                    Logger.scriviEccezioneNelLog(RegistrazioneNuovoConsumer.class, e);
                    registrazioneConclusaConSuccesso = false;
                }
                if( registrazioneConclusaConSuccesso )
                    return Autenticazione.creaResponseAutenticazione(codiceFiscale, password);  // utente risulterà subito autenticato

                else return Response.serverError().build();

            } else {
                return Response.status( Response.Status.CONFLICT )
                               .entity( codiceFiscale + " risulta gia' registrato nella piattaforma." )
                               .build();
            }

        } else {

            return Response.status( Response.Status.BAD_REQUEST )
                           .entity( "Valori di input inseriti non validi." )
                           .build();

        }
    }
}


@SuppressWarnings("unused") // getter e setter usati durante la deserializzazione da JAX-RS
class CampiFormRegistrazione {

    private String codiceFiscale = "";
    private String nominativo = "";
    private String email = "";
    private String password = "";

    public String getCodiceFiscale() {
        return codiceFiscale;
    }

    public String getNominativo() {
        return nominativo;
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

    public void setNominativo(String nominativo) {
        this.nominativo = EncoderPrevenzioneXSS.encodeForJava(nominativo);
    }

    public void setEmail(String email) {
        this.email = EncoderPrevenzioneXSS.encodeForJava(email);
    }

    public void setPassword(String password) {
        this.password = EncoderPrevenzioneXSS.encodeForJava(password);
    }
}
