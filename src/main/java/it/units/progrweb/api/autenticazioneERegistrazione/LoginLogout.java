package it.units.progrweb.api.autenticazioneERegistrazione;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import it.units.progrweb.entities.AuthenticationDatabaseEntry;
import it.units.progrweb.entities.AuthenticationTokenInvalido;
import it.units.progrweb.entities.attori.Attore;
import it.units.progrweb.filters.FiltroAutenticazione;
import it.units.progrweb.persistence.NotFoundException;
import it.units.progrweb.utils.Autenticazione;
import it.units.progrweb.utils.EncoderPrevenzioneXSS;
import it.units.progrweb.utils.Logger;
import it.units.progrweb.utils.mail.MailSender;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * Classe per la gestione dell'autenticazione dei client.
 * Vedere anche {@link FiltroAutenticazione}.
 * @author Matteo Ferfoglia
 */
@Path("")
public class LoginLogout {

    /** Risponde alle richieste di login, rilasciando
     * un'opportuna {@link Response} al client.*/
    @Path("/login")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response login(CampiFormLogin campiFormLogin){
        return Autenticazione.creaResponseAutenticazione(campiFormLogin.getUsername(), campiFormLogin.getPassword());
    }

    /** Risponde alle richieste di login effettuate tramite Firebase:
     * questo metodo attende il token JWT rilasciato da Firebase dopo
     * aver autenticato l'utente, contenente le informazioni dell'utente.*/
    @Path("/firebaseLogin")
    @POST
    @Consumes(MediaType.TEXT_PLAIN)
    public Response loginConFirebase(String tokenJwtAutenticazioneUtenteRilasciatoDaFirebase) {

        if( tokenJwtAutenticazioneUtenteRilasciatoDaFirebase!=null ) {

            try {

                FirebaseToken decodedToken =
                        FirebaseAuth.getInstance().verifyIdToken(tokenJwtAutenticazioneUtenteRilasciatoDaFirebase);

                String emailAttoreDaToken = decodedToken.getEmail();

                Attore attoreInDb = Attore.getAttoreDaEmail(emailAttoreDaToken);

                return Autenticazione.creaResponseAutenticazione(attoreInDb);

            } catch (Exception e) {
                return Autenticazione.creaResponseUnauthorized();
            }

        } else {
            return Response.status(Response.Status.BAD_REQUEST)
                           .entity("Errore: ricevuto token nullo.")
                           .build();
        }

    }


    /** Effettua il logout del client che ne fa richiesta. */
    @Path("/logout")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public Response logout(@Context HttpServletRequest httpServletRequest) {
        String tokenAutenticazione = Autenticazione.getTokenAutenticazioneBearer(httpServletRequest);
        AuthenticationTokenInvalido.aggiungiATokenJwtInvalidi(tokenAutenticazione);
        return Autenticazione.creaResponseLogout();
    }

    /** Servizio per verificare se l'utente che ha effettuato
     * la richiesta risulta attualmente autenticato: se la
     * verifica ha esito positivo, questo metodo restituisce
     * "true", altrimenti "false".*/
    @Path("/verificaTokenAutenticazione")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public boolean isClientAttualmenteAutenticato(@Context HttpServletRequest httpServletRequest) {
        return Autenticazione.isClientAutenticato(httpServletRequest);
    }

    /** Restituisce il nome dell'attore attualmente autenticato.*/
    @Path("/nomeDiQuestoAttore")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String nomeDiQuestoAttoreAutenticato(@Context HttpServletRequest httpServletRequest) {
        String nomeAttore = Autenticazione.getNomeAttoreDaHttpServletRequest(httpServletRequest);
        return nomeAttore!=null ? EncoderPrevenzioneXSS.encodeForHTMLContent(nomeAttore) : "";
    }

    /** Restituisce l'identificativo dell'attore attualmente autenticato.*/
    @Path("/identificativoDiQuestoAttore")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public Long identificativoDiQuestoAttoreAutenticato(@Context HttpServletRequest httpServletRequest) {
        return Autenticazione.getIdentificativoAttoreDaHttpServletRequest(httpServletRequest);
    }

    /** Restituisce il tipo di utente autenticato. */
    @Path("/getTipoUtenteAutenticato")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String getTipoUtenteAutenticato(@Context HttpServletRequest httpServletRequest) {
        String tipoAttore = Autenticazione.getTipoAttoreDaHttpServletRequest(httpServletRequest);
        return EncoderPrevenzioneXSS.encodeForHTMLContent( tipoAttore );

    }

    /** Reset della password dell'account che lo richiede. Viene creata una nuova
     * password temporanea che viene inviata per mail all'account che ne ha fatto richiesta.*/
    @Path("/resetPassword")
    @POST
    @Produces(MediaType.TEXT_PLAIN)
    public String resetPassword( CampiFormLogin campiFormLogin ) {

        // TODO : testare questo metodo che funzioni correttamente con le mail

        String username = campiFormLogin.getUsername();

        final String MSG_ERRORE_INTERNO = "Errore interno, riprovare più tardi.";

        if(username == null)
            return "Specificare lo username.";

        try {
            String passwordTemporanea = AuthenticationDatabaseEntry.creaPasswordTemporanea(username);

            {
                // Invio password temporanea via mail
                Attore attore = Attore.getAttoreDaUsername(username);
                if ( attore != null ) {
                    MailSender mailSender = new MailSender();
                    try {
                        mailSender.inviaEmail(attore.getEmail(), attore.getNominativo(), "Reset password",
                                "E' stato richiesto il reset della password per il Suo account." +
                                        " La nuova password è: " + passwordTemporanea + ". Si consiglia di modificare" +
                                        " al primo accesso tale password. Se non è stata richiesta la modifica, ignorare" +
                                        " questa email.");
                    } catch (MessagingException | UnsupportedEncodingException e) {
                        Logger.scriviEccezioneNelLog(
                                LoginLogout.class,
                                "Errore nell'invio della mail per il reset della password",
                                e
                        );
                        return MSG_ERRORE_INTERNO;
                    }
                } else {
                    throw new NotFoundException();
                }
            }

            return "E' stata inviata tramite email la nuova password per accedere al sistema.";
        } catch (NotFoundException e) {
            return "Username non trovato nel sistema";
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            Logger.scriviEccezioneNelLog(
                    LoginLogout.class,
                    "Errore nel calcolo dell'hash della password temporanea.",
                    e
            );
            return MSG_ERRORE_INTERNO;
        }

    }

    /** Rilascia un nuovo token di autenticazione per l'attore associato al client che lo richiede. */
    @Path("/nuovoTokenAutenticazione")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public Response creaNuovoTokenAutenticazione(@Context HttpServletRequest httpServletRequest){
        try {
            return Autenticazione.creaResponseAutenticazionePerAttore(Autenticazione.getAttoreDaDatabase(httpServletRequest));
        } catch (NotFoundException notFoundException) {
            Logger.scriviEccezioneNelLog(LoginLogout.class, "Attore non trovato nel sistema", notFoundException);
            return Response.serverError().build();
        }
    }

}

