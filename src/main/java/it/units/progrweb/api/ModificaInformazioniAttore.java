package it.units.progrweb.api;

import it.units.progrweb.entities.AuthenticationDatabaseEntry;
import it.units.progrweb.entities.attori.Attore;
import it.units.progrweb.entities.attori.nonAdministrator.uploader.Uploader;
import it.units.progrweb.persistence.DatabaseHelper;
import it.units.progrweb.persistence.NotFoundException;
import it.units.progrweb.utils.Autenticazione;
import it.units.progrweb.utils.Logger;
import it.units.progrweb.utils.UtilitaGenerale;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

/**
 * Questa classe espone i servizi per la modifica
 * delle informazioni dell'{@link Attore} autenticato.
 * @author Matteo Ferfoglia
 */
@Path("/modificaInformazioniAttore")
public class ModificaInformazioniAttore {

    /** Se tutti i controlli vanno a buon fine, apporta le
     * modifiche richieste. Si è preferito utilizzare un
     * unico servizio anziché più servizi separati così da
     * ottimizzare il numero di accessi al database.
     * Se un campo tra i parametri è vuoto, allora <strong>non
     * </strong> viene modificato. 
     * <strong>Attenzione</strong>: se si modificano alcune delle
     * informazioni che sono presenti anche nel token di autenticazione
     * del client, allora quel token diverterà invalido, infatti questo
     * metodo restituisce nell'entity della risposta il nuovo token
     * di autenticazione (questo metodo fa uso di {@link
     * Autenticazione#creaResponseAutenticazionePerAttoreAutenticato(Attore)}).*/
    @POST
    @Consumes( MediaType.MULTIPART_FORM_DATA )
    public Response modificaInformazioni(@Context HttpServletRequest httpServletRequest,
                                         @FormDataParam("nominativo")      String nuovoNominativo,
                                         @FormDataParam("email")           String nuovaEmail,
                                         @FormDataParam("vecchiaPassword") String vecchiaPassword,
                                         @FormDataParam("password")        String nuovaPassword,
                                         @FormDataParam("immagineLogo")    InputStream nuovoLogo,                  // TODO : variabili d'ambiente tutti i campi del form
                                         @FormDataParam("immagineLogo")    FormDataContentDisposition dettagliNuovoLogo) {

        // TODO : verirficare che questo metodo funzioni
        // TODO : duplicazione di codice con GestioneAttori -> modificaAttore

        Attore attoreDaModificare = Autenticazione.getAttoreDaHttpServletRequest(httpServletRequest);   // TODO: salvare info attore nel token di autenticazione per evitare accesso al db

        if (attoreDaModificare != null) {

            String username = attoreDaModificare.getUsername();

            // Per l'aggiornamento del database cerco di raggruppare insieme
            // tutti i campi della stessa entità (solo un accesso al database).
            // Minore leggibilità del codice, ma non spreco accessi nel database.

            Optional<?> optionalModificaAuthDb = Optional.of("qualsiasi valore di inizializzazione, così isPresent() restituirà true");

            // Aggiorno prima AuthDb : se ci sono problemi non accedo altre volte (non necessarie) al database
            //  (tutti gli attori sono registrati nell'AuthDB)
            if (UtilitaGenerale.isStringaNonNullaNonVuota(vecchiaPassword) && UtilitaGenerale.isStringaNonNullaNonVuota(nuovaPassword)) {
                // Se qui, allora la richiesta richiede di modificare la psswd in AuthDb
                optionalModificaAuthDb = AuthenticationDatabaseEntry.modificaPassword(username, vecchiaPassword, nuovaPassword);
            }

            if (optionalModificaAuthDb.isPresent()) {

                if (Autenticazione.getTipoAttoreDaHttpServletRequest(httpServletRequest).equals(Uploader.class.getSimpleName())) {
                    try {
                        modificaInfoUploader((Uploader) attoreDaModificare, nuovoLogo, dettagliNuovoLogo, nuovoNominativo, nuovaEmail);
                    } catch (IOException e) {
                        // Dimensioni logo eccessive
                        return Response.status( Response.Status.REQUEST_ENTITY_TOO_LARGE )  // TODO : codice duplicato dal metodo API di modifica Attori degli Administrator
                                       .entity( e.getMessage() )
                                       .build();
                    }
                } else {
                    modificaInfoAttore(attoreDaModificare, nuovoNominativo, nuovaEmail);   // in generale, gli attori non hanno il logo
                }

                optionalModificaAuthDb.get();

            }

            try {
                return Autenticazione.creaResponseAutenticazionePerAttoreAutenticato(attoreDaModificare);
            } catch (NotFoundException notFoundException) {
                Logger.scriviEccezioneNelLog(ModificaInformazioniAttore.class, notFoundException);
                return Response.serverError().build();
            }

        } else {
            // Se qui significa attore non trovato
            return Response.status( Response.Status.BAD_REQUEST )           // TODO : refactoring: creare un metodo che invia BAD_REQUEST
                           .entity( "Problemi nel recupero delle informazioni dell'autore della richiesta." )
                           .build();
        }

    }

    /** Aggiorna nel database gli attributi di un {@link Uploader}
     * con quelli forniti nei parametri (solo se validi).
     * @throws IOException Se il logo ha una dimensione eccessiva.*/
    private void modificaInfoUploader(@NotNull Uploader uploaderDaModificare,
                                      InputStream nuovoLogo, FormDataContentDisposition dettagliNuovoLogo,
                                      String nuovoNominativo, String nuovaEmail)
            throws IOException {

        if( dettagliNuovoLogo != null ) {
            // TODO : verificare che sia null quando NON c'è il logo e che NON sia null quando c'è il logo
            uploaderDaModificare.setImmagineLogo(UtilitaGenerale.convertiInputStreamInByteArray(nuovoLogo),
                    UtilitaGenerale.getEstensioneDaNomeFile(dettagliNuovoLogo.getFileName()));
        }

        modificaInfoAttore( uploaderDaModificare, nuovoNominativo, nuovaEmail );
    }

    /** Aggiorna nel database gli attributi di un {@link Attore}
     * con quelli forniti nei parametri (solo se validi).*/
    private static void modificaInfoAttore(@NotNull Attore attoreDaModificare,
                                           String nuovoNominativo, String nuovaEmail) {

        if( UtilitaGenerale.isStringaNonNullaNonVuota(nuovoNominativo) ) {
            attoreDaModificare.setNominativo( nuovoNominativo );
        }

        if( UtilitaGenerale.isStringaNonNullaNonVuota(nuovaEmail) ) {
            attoreDaModificare.setEmail( nuovaEmail );
        }
        // TODO controllare che alcune informazioni vengano effettivamnte modificate, evitiamo un accesso in scrittura al DB per non modificare alcunché

        DatabaseHelper.salvaEntita( attoreDaModificare );

    }

}
