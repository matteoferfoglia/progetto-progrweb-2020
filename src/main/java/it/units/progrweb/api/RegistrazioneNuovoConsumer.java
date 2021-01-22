package it.units.progrweb.api;

import it.units.progrweb.entities.attori.Attore;
import it.units.progrweb.entities.attori.nonAdministrator.consumer.Consumer;
import it.units.progrweb.utils.Autenticazione;
import it.units.progrweb.utils.EncoderPrevenzioneXSS;
import it.units.progrweb.utils.RegexHelper;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * @author Matteo Ferfoglia
 */
@Path("/registrazioneNuovoConsumer")
public class RegistrazioneNuovoConsumer {

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response registrazioneNuovoConsumer(CampiFormRegistrazione campiFormRegistrazione) {

        // TODO : usare @FormDataParam

        String codiceFiscale = campiFormRegistrazione.getCodiceFiscale();
        String email         = campiFormRegistrazione.getEmail();
        String nominativo    = campiFormRegistrazione.getNominativo();
        String password      = campiFormRegistrazione.getPassword();

        if( RegexHelper.isEmailValida( email )                     &&
                RegexHelper.isCodiceFiscaleValido( codiceFiscale ) &&
                nominativo.length() > 0                            &&
                password.length()   > 0 ) {

            if( ! Attore.isAttoreGiaRegistrato( codiceFiscale ) ) {

                Consumer nuovoConsumer = Consumer.creaConsumer( codiceFiscale, nominativo, email );

                boolean registrazioneConclusaConSuccesso = Attore.salvaNuovoAttoreInDatabase( nuovoConsumer, password );
                if( registrazioneConclusaConSuccesso )
                    return Autenticazione.creaResponseAutenticazione(codiceFiscale, password);  // utente risulterà subito autenticato

                else return Response.serverError().build();

            } else {
                return Response.status( Response.Status.CONFLICT )
                               .entity( codiceFiscale + " risulta gia' registrato nella piattaforma." )
                               .build();
            }

        } else {

            return Response.status( Response.Status.BAD_REQUEST )       // todo : REFACTORING per evitare duplicazione
                           .entity( "Valori di input inseriti non validi." )
                           .build();

        }
    }
}


class CampiFormRegistrazione {

    private String codiceFiscale = "";
    private String nominativo = "";
    private String email = "";
    private String password = "";

    public CampiFormRegistrazione() {}

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
