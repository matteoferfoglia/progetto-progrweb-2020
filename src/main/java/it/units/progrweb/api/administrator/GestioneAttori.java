package it.units.progrweb.api.administrator;

import it.units.progrweb.entities.AuthenticationDatabaseEntry;
import it.units.progrweb.entities.attori.Attore;
import it.units.progrweb.entities.attori.FormatoUsernameInvalido;
import it.units.progrweb.entities.attori.administrator.Administrator;
import it.units.progrweb.entities.attori.nonAdministrator.uploader.Uploader;
import it.units.progrweb.persistence.DatabaseHelper;
import it.units.progrweb.utils.EncoderPrevenzioneXSS;
import it.units.progrweb.utils.Logger;
import it.units.progrweb.utils.UtilitaGenerale;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
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
    @Path("/modificaAttore")    // TODO : non si può usare direttamente il metodo in ModficaInformazioniAttore?
    @POST  // TODO : valutare se sostituire con metodo PUT (perché in realtà è idempotente)
    @Consumes( MediaType.MULTIPART_FORM_DATA )
    public Response modificaAttore(@Context HttpServletRequest httpServletRequest,
                                   @FormDataParam("identificativoAttore") Long identificativoAttore,
                                   @FormDataParam("username")             String nuovoUsername,
                                   @FormDataParam("nominativo")           String nuovoNominativo,
                                   @FormDataParam("email")                String nuovaEmail,
                                   @FormDataParam("immagineLogo")         InputStream nuovoLogo,                  // TODO : vedere dove è usato il componente Form*.vue che si occupa di inviare queste informazioni e cercare di creare un unico servizio sul server che gestisca tutto
                                   @FormDataParam("immagineLogo")         FormDataContentDisposition dettagliNuovoLogo) {

        // TODO : in Vue aggiungere l'input[type=file] per modificare l'immagine logo

        Attore attoreDaModificare_attualmenteSalvatoInDB = Attore.getAttoreDaIdentificativo( identificativoAttore );
        if( attoreDaModificare_attualmenteSalvatoInDB != null ) {

            // Creazione dell'attore con le modifiche richieste dal client (Attore è
            // una classe astratta, Jersey non riesce a deserializzare )
            // TODO : trovare modo migliore

            Attore attore_conModificheRichiesteDalClient = attoreDaModificare_attualmenteSalvatoInDB.clone();
            attore_conModificheRichiesteDalClient.setUsername( nuovoUsername );
            attore_conModificheRichiesteDalClient.setNominativo( nuovoNominativo );
            attore_conModificheRichiesteDalClient.setEmail( nuovaEmail );

            if( attoreDaModificare_attualmenteSalvatoInDB instanceof Uploader &&
                nuovoLogo != null && dettagliNuovoLogo != null) {

                try {
                    ((Uploader)attoreDaModificare_attualmenteSalvatoInDB)
                            .setImmagineLogo(UtilitaGenerale.convertiInputStreamInByteArray( nuovoLogo ),
                                             UtilitaGenerale.getEstensioneDaNomeFile(dettagliNuovoLogo.getFileName()));
                } catch (IOException e) {
                    // Dimensione immagine logo eccessiva   // TODO : testare
                    return Response.status( Response.Status.REQUEST_ENTITY_TOO_LARGE )
                                   .entity( e.getMessage() )
                                   .build();
                }

            }

            return modificaAttore_metodoStatico(attore_conModificheRichiesteDalClient, attoreDaModificare_attualmenteSalvatoInDB);

        } else {
            // Attore non trovato nel DB
            return Response.status( Response.Status.NOT_FOUND )
                           .entity( "Attore [" + identificativoAttore + "] non trovato." )
                           .build();
        }

    }

    // Metodo creato per riutilizzo del codice
    /** Questo medio si occupa di modificare le informazioni di un attore.
     * @param attoreDaModificare_conModificheRichiesteDaClient E' l'attore
     *                       con le modifiche richieste dal cliente.
     * @param attore_attualmenteSalvatoInDB E' lo stesso attore, ma con le
     *                       informazioni attualmente salvate nel database.*/
    public static Response modificaAttore_metodoStatico( Attore attoreDaModificare_conModificheRichiesteDaClient,
                                                         Attore attore_attualmenteSalvatoInDB ) {

        // Funzionamento di questo metodo: clona l'attore dal DB, una copia la sovrascrive
        // coi nuovi dati mandati dal client (se non nulli) e se risultano delle modifiche
        // (rispetto al clone), le salva nel DB.
        // Le modifiche vanno fatte sull'oggetto ottenuto dal DB, altrimenti poi quando si
        // salva, viene creata una nuova entità anziché sovrascrivere quella esistente.

        if( attore_attualmenteSalvatoInDB != null &&
                attoreDaModificare_conModificheRichiesteDaClient != null) {

            Attore copia_attore_attualmenteSalvatoInDB = attore_attualmenteSalvatoInDB.clone();

            attore_attualmenteSalvatoInDB.setEmail(attoreDaModificare_conModificheRichiesteDaClient.getUsername());
            attore_attualmenteSalvatoInDB.setNominativo(attoreDaModificare_conModificheRichiesteDaClient.getNominativo());
            // attore_attualmenteSalvatoInDB.setUsername(attoreDaModificare_conModificheRichiesteDaClient.getUsername()); // modifica username non permessa (da requisiti)
            if( attoreDaModificare_conModificheRichiesteDaClient instanceof Uploader ) {
                // SE si sta modificando un Uploader
                Uploader uploader_attualmenteSalvatoInDB = (Uploader) attore_attualmenteSalvatoInDB;
                try {
                    uploader_attualmenteSalvatoInDB
                            .setImmagineLogo( uploader_attualmenteSalvatoInDB.getImmagineLogo(),
                                              uploader_attualmenteSalvatoInDB.getEstensioneImmagineLogo() );
                } catch (IOException e) {
                    return Response.status( Response.Status.REQUEST_ENTITY_TOO_LARGE )
                                   .entity( e.getMessage() )
                                   .build();
                }
                attore_attualmenteSalvatoInDB = uploader_attualmenteSalvatoInDB;
            }

            if( ! attore_attualmenteSalvatoInDB.equals(copia_attore_attualmenteSalvatoInDB) ) {
                // Se non ci sono modifiche, risparmio l'inutile accesso in scrittura al DB
                DatabaseHelper.salvaEntita(attore_attualmenteSalvatoInDB);
            }

            return Response.ok()
                           .type( MediaType.APPLICATION_JSON )
                           .entity( attoreDaModificare_conModificheRichiesteDaClient )
                           .build();
            // TODO : riesce a convertire in JSON se è astratto ?

        } else {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Attore da modificare non trovato nel sistema." )
                    .build();
        }

    }

    /** Restituisce un array con gli identificativi di tutti gli {@link Uploader}.*/
    @Path("/elencoUploader")
    @GET
    @Produces( MediaType.APPLICATION_JSON )
    public long[] getElencoIdentificativiUploader() {

        // TODO : refactoring c'è un metodo molto simile in Uploader
        return Uploader.getListaIdentificativiTuttiGliUploaderNelSistema()
                       .stream().mapToLong( i -> i ).toArray();

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
