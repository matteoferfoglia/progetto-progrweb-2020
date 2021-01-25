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
import javax.ws.rs.*;
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

    /** Eliminazione di un {@link Attore} dal sistema. */
    @Path("/eliminaAttore/{identificativoAttoreDaEliminare}")
    @DELETE
    public Response eliminaConsumer(@PathParam("identificativoAttoreDaEliminare") Long identificativoAttoreDaEliminare,
                                    @Context HttpServletRequest httpServletRequest) {

        // TODO : questo metodo è molto simile a quello per l'eliminazione dei Consumer da parte di un Uploader (Refactoring??)

        // Ricerca dell'attore da eliminare
        boolean attoreEliminato = Attore.eliminaAttoreDaIdentificativo( identificativoAttoreDaEliminare );

        if( attoreEliminato ) {

            return Response
                    .status( Response.Status.OK )
                    .entity("Attore eliminato")    // TODO : var ambiente con messaggi errore
                    .build();

        } else {
            return Response
                    .status( Response.Status.BAD_REQUEST )  // TODO : fare un metodo che gestisca le BAD_REQUEST con tutte quelle usate nell'intero progetto, che prenda il messaggio d'errore come parametro
                                                        // TODO : vedere se bisogna modificare dei BAD_REQUEST con dei NOT_FOUND
                    .entity( "Impossibile eliminare l'attore " + identificativoAttoreDaEliminare )
                    .build();
        }


    }

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


    /** Modifica di un {@link Attore} di quelli presenti nel sistema. */
    @Path("/modificaConsumer")
    @POST  // TODO : valutare se sostituire con metodo PUT (perché in realtà è idempotente)
    public Response modificaAttore(Attore attoreDaModificare_ricevutoDaClient) {

        return modificaAttore_metodoStatico(attoreDaModificare_ricevutoDaClient);

    }

    /** {@link #modificaAttore(Attore)}.*/  // Metodo creato per riutilizzo del codice
    public static Response modificaAttore_metodoStatico( Attore attoreDaModificare_ricevutoDaClient ) {

        Long identificativoAttoreDaModificare = attoreDaModificare_ricevutoDaClient.getIdentificativoAttore();  // deve essere specificato nella request
        Attore attoreDaModificare_trovatoInDB = Attore.getAttoreDaIdentificativo( identificativoAttoreDaModificare );

        if( attoreDaModificare_trovatoInDB != null ) {

            attoreDaModificare_trovatoInDB.setEmail(attoreDaModificare_ricevutoDaClient.getEmail());
            attoreDaModificare_trovatoInDB.setNominativo(attoreDaModificare_ricevutoDaClient.getNominativo());
            // attoreDaModificare_trovatoInDB.setUsername(attoreDaModificare_ricevutoDaClient.getUsername()); // modifica username non permessa (da requisiti)
            if( attoreDaModificare_ricevutoDaClient instanceof Uploader ) {
                // SE si sta modificando un Uploader
                // TODO : permettere anche la modifica del logo
            }

            if( ! attoreDaModificare_trovatoInDB.equals(attoreDaModificare_ricevutoDaClient) ) {
                // Se non ci sono modifiche, risparmio l'inutile accesso in scrittura al DB
                DatabaseHelper.salvaEntita(attoreDaModificare_ricevutoDaClient);
            }

            return Response.ok().build();

        } else {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Attore " + identificativoAttoreDaModificare + " non trovato nel sistema." )
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
