package it.units.progrweb.api.administrator;

import it.units.progrweb.entities.AuthenticationDatabaseEntry;
import it.units.progrweb.entities.attori.Attore;
import it.units.progrweb.entities.attori.FormatoUsernameInvalido;
import it.units.progrweb.entities.attori.administrator.Administrator;
import it.units.progrweb.entities.attori.nonAdministrator.uploader.Uploader;
import it.units.progrweb.persistence.DatabaseHelper;
import it.units.progrweb.utils.EncoderPrevenzioneXSS;
import it.units.progrweb.utils.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Objects;

/**
 * Questa classe espone agli {@link Administrator} i servizi per
 * la gestione degli attori.
 * @author Matteo Ferfoglia
 */
@Path("/administrator")
public class GestioneAttori {

    /** Metodo per l'aggiunta di un {@link Attore}.
     * Se la procedura va a buon fine, restituisce l'identificativo
     * dell'{@link Attore} appena creato, altrimenti l'errore.*/
    @Path("/aggiungiAttore")
    @POST
    @Consumes( MediaType.APPLICATION_JSON )
    public Response aggiungiAttore( CampiFormAggiuntaAttore campiFormAggiuntaAttore,
                                    @Context HttpServletRequest  httpServletRequest) {


        String username   = campiFormAggiuntaAttore.getUsername();
        String password   = campiFormAggiuntaAttore.getPassword();
        String nominativo = campiFormAggiuntaAttore.getNominativo();
        String email      = campiFormAggiuntaAttore.getEmail();
        String tipoAttore = campiFormAggiuntaAttore.getTipoAttore();

        boolean parametriInputValidi = !Arrays.stream(new String[]{username, password, nominativo, email, tipoAttore})
                                              .anyMatch( Objects::isNull );

        if( parametriInputValidi ) {

            // Verifica che NON esista già l'attore nel sistema
            boolean isAttoreGiaPresente = Attore.getAttoreDaUsername(username) != null;
            Attore attoreDaCreare;

            if ( ! isAttoreGiaPresente ) {

                try {

                    switch (Attore.TipoAttore.valueOf(tipoAttore)) {
                        case Uploader:
                            attoreDaCreare = Uploader.creaAttore(username, nominativo, email);
                            break;

                        case Administrator:
                            attoreDaCreare = Administrator.creaAttore(username, nominativo, email);
                            break;

                        default:
                            throw new IllegalArgumentException("Questo non dovrebbe mai succedere");
                    }

                    Long identificativoAttoreCreato = (Long) DatabaseHelper.salvaEntita(attoreDaCreare);
                    AuthenticationDatabaseEntry authenticationDatabaseEntry = new AuthenticationDatabaseEntry(username, password);
                    DatabaseHelper.salvaEntita(authenticationDatabaseEntry);

                    return Response
                            .ok()
                            .entity(identificativoAttoreCreato)
                            .build();


                } catch (IllegalArgumentException e) {
                    // Tipo attore ricevuto non valido
                    return Response.status(Response.Status.BAD_REQUEST)
                            .entity("Valori di input inseriti non validi: \"" + tipoAttore + "\" non è un tipo valido.") // TODO : risposta molto simile in GestioneConsumer (fare un metodo che restituisce BAD_REQUEST)
                            .build();
                } catch( FormatoUsernameInvalido e ) {
                    return Response.status(Response.Status.BAD_REQUEST)
                            .entity("Formato per il campo username non valido.") // TODO : risposta molto simile in GestioneConsumer (fare un metodo che restituisce BAD_REQUEST)
                            .build();
                }catch (NoSuchAlgorithmException | InvalidKeyException e) {
                    Logger.scriviEccezioneNelLog(GestioneAttori.class,
                            "Errore durante la scrittura in AuthDB",
                            e);
                    return Response.serverError().build();
                }

            } else {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("L'attore \"" + username + "\" è già presente nel sistema.")
                        .build();
            }

        } else {
            return Response.status(Response.Status.BAD_REQUEST)     // TODO : refactoring: fare un metodo che prende in input il messaggio d'errore e risponde BAD_REQUEST
                    .entity("Parametri del form non validi.")
                    .build();
        }

    }

}

/** Classe di comodo per la deserializzazione dei dati JSON
 * ricevuti dal client.*/
@SuppressWarnings("unused")
class CampiFormAggiuntaAttore {

    private String username;
    private String password;
    private String nominativo;
    private String email;
    private String tipoAttore;

    CampiFormAggiuntaAttore(){}

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

    public String getNominativo() {
        return nominativo;
    }

    public void setNominativo(String nominativo) {
        this.nominativo = EncoderPrevenzioneXSS.encodeForJava(nominativo);
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = EncoderPrevenzioneXSS.encodeForJava(email);
    }

    public String getTipoAttore() {
        return tipoAttore;
    }

    public void setTipoAttore(String tipoAttore) {
        this.tipoAttore = EncoderPrevenzioneXSS.encodeForJava(tipoAttore);
    }
}
