package it.units.progrweb.api;

import it.units.progrweb.entities.AuthenticationDatabaseEntry;
import it.units.progrweb.entities.AuthenticationTokenInvalido;
import it.units.progrweb.entities.attori.Attore;
import it.units.progrweb.entities.attori.uploader.Uploader;
import it.units.progrweb.persistence.NotFoundException;
import it.units.progrweb.utils.Autenticazione;
import it.units.progrweb.utils.Logger;
import it.units.progrweb.utils.ResponseHelper;
import it.units.progrweb.utils.UtilitaGenerale;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.function.Supplier;

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
     * del client, allora quel token diventerà invalido, infatti questo
     * metodo restituisce nell'entity della risposta il nuovo token
     * di autenticazione (questo metodo fa uso di {@link
     * Autenticazione#creaResponseAutenticazionePerAttore(Attore)}).*/
    @POST
    @Consumes( MediaType.MULTIPART_FORM_DATA )
    public Response modificaProprieInformazioni(@Context HttpServletRequest httpServletRequest,
                                                @FormDataParam("nominativo")      String nuovoNominativo,
                                                @FormDataParam("email")           String nuovaEmail,
                                                @FormDataParam("vecchiaPassword") String vecchiaPassword,
                                                @FormDataParam("password")        String nuovaPassword,
                                                @FormDataParam("immagineLogo")    InputStream nuovoLogo,
                                                @FormDataParam("immagineLogo")    FormDataContentDisposition dettagliNuovoLogo) {

        Attore attoreDaModificare = Autenticazione.getAttoreDaDatabase(httpServletRequest);

        if (attoreDaModificare != null) {

            String username = attoreDaModificare.getUsername();

            // Obiettivo: minimizzare il #accessi al db, prevedendo un meccanismo di rollback se qualche dato è invalido
            // Minore leggibilità del codice, ma non spreco accessi al database.

            Optional<?> optionalModificaAuthDb = Optional.of("qualsiasi valore di inizializzazione," +
                                                             "così isPresent() restituirà true se non viene modificato");


            // Aggiorno prima AuthDb : se ci sono problemi non accedo altre volte (non necessarie) al database
            //  (tutti gli attori sono registrati nell'AuthDB)
            if (UtilitaGenerale.isStringaNonNullaNonVuota(vecchiaPassword) && UtilitaGenerale.isStringaNonNullaNonVuota(nuovaPassword)) {
                // Se qui, allora la richiesta richiede di modificare la psswd in AuthDb
                optionalModificaAuthDb = AuthenticationDatabaseEntry.modificaPassword(username, vecchiaPassword, nuovaPassword);
            }

            if ( optionalModificaAuthDb.isPresent() ) {

                if ( attoreDaModificare.getTipoAttore().equals(Attore.TipoAttore.Uploader.name()) ) {
                    try {
                        Uploader.modificaInfoUploader((Uploader) attoreDaModificare, nuovoLogo, dettagliNuovoLogo, nuovoNominativo, nuovaEmail);
                    } catch (IOException e) {
                        // Dimensioni logo eccessive
                        return ResponseHelper.creaResponseRequestEntityTooLarge(e.getMessage());
                    }
                } else {
                    Attore.modificaInfoAttore(attoreDaModificare, nuovoNominativo, nuovaEmail);   // in generale, gli attori non hanno il logo
                }

                if( optionalModificaAuthDb.get() instanceof Supplier )
                    ((Supplier<?>) optionalModificaAuthDb.get()).get(); // esegue il Supplier restituito da AuthenticationDatabaseEntry#modificaPassword

            } else {
                // Errore nella modifica della password
                return ResponseHelper.creaResponseBadRequest( "Password inserite non valide." );
            }

            try {
                // Necessaria nuova response di autenticazione per il client perché ha modificato le sue informazioni
                //  (come se avesse appena fatto il login)
                AuthenticationTokenInvalido.aggiungiATokenJwtInvalidi( Autenticazione.getTokenAutenticazioneBearer(httpServletRequest) );   // vecchio token di autenticazione diventa invalido
                return Autenticazione.creaResponseAutenticazionePerAttore(attoreDaModificare);
            } catch (NotFoundException notFoundException) {
                Logger.scriviEccezioneNelLog(ModificaInformazioniAttore.class, notFoundException);
                return ResponseHelper.creaResponseServerError("");
            }

        } else {
            // Se qui significa attore non trovato
            return ResponseHelper.creaResponseBadRequest( "Problemi nel recupero delle informazioni dell'autore della richiesta." );
        }

    }

}