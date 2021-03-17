package it.units.progrweb.api.consumer;

import it.units.progrweb.entities.attori.nonAdministrator.consumer.Consumer;
import it.units.progrweb.entities.file.File;
import it.units.progrweb.persistence.NotFoundException;
import it.units.progrweb.utils.Autenticazione;
import it.units.progrweb.utils.Logger;
import it.units.progrweb.utils.UtilitaGenerale;
import it.units.progrweb.utils.datetime.DateTime;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

/**
 * Classe per rispondere alla richiesta di documenti destinati
 * ai Consumer.
 * La verifica dell'autenticazione di un attore quale Consumer
 * è demandata all'opportuno
 * {@link it.units.progrweb.filters.attori.FiltroConsumer filtro}.
 * @author Matteo Ferfoglia
 */
@Path("/consumer/documenti")
public class RichiestaDocumenti {


    /** Restituisce un oggetto (JSON) in cui ogni property è un'astrazione
     * di un documento destinato al Consumer da cui proviene la richiesta:
     * ogni property ha per nome l'identificativo del documento e per valore
     * l'oggetto che rappresenta il documento (descritto in base ai suoi
     * attributi, tutti e soli quelli dati da {@link File#getAnteprimaProprietaFile(boolean)}.
     * Nel caso in cui venga specificato il QueryParam numeroElementiAttualmenteNotiAlClient,
     * questo metodo restituisce {@link Response.Status#NOT_MODIFIED} nel caso in cui il numero
     * di documenti noti al client sia lo stesso del numero dei documenti noti al server, altrimenti
     * redirect a questa stessa api, ma senza query string, così da restituire tutti i documenti.
     * @param httpServletRequest La richiesta HTTP.
     * @param identificativoUploader Identificativo dell'Uploader.
     * @param numeroDocumentiAttualmenteNotiAlClient Se non nullo, specifica il numero di
     *                                               documenti attualmente noti al client.
     * */
    @Path("/elencoDocumenti/{identificativoUploader}")
    @GET
    // Response costruita senza @Produces per serializzare i dati in modo personalizzato
    public static Response getElencoDocumenti(@Context HttpServletRequest httpServletRequest,
                                              @PathParam("identificativoUploader") Long identificativoUploader,
                                              @QueryParam("numeroElementiAttualmenteNotiAlClient") Integer numeroDocumentiAttualmenteNotiAlClient) {

        Consumer consumer = (Consumer) Autenticazione.getAttoreDaDatabase(httpServletRequest);
        List<File> listaFile = consumer.getAnteprimaFiles(identificativoUploader);

        return creaResponseElencoDocumenti(numeroDocumentiAttualmenteNotiAlClient,
                                           listaFile,
                                           false,
                                           "consumer/documenti/elencoDocumenti/" + identificativoUploader);

    }

    /** Metodo di comodo per #getElencoDocumenti.
     * @param numeroDocumentiAttualmenteNotiAlClient Numero di documenti attualmente noti al client:
     *                                               se il numero di documenti attualmente noti al client coincide
     *                                               con quelli attualmente memorizzati nel database, si ipotizza
     *                                               che l'elenco dei documenti sia aggiornato (possibile bug: se
     *                                               un uploader elimina un documento e ne inserisce uno nuovo e per
     *                                               completare quest'operazione impiega meno tempo di quanto ne impiega
     *                                               un client per fare due richieste successive circa l'elenco dei
     *                                               documenti, allora questo metodo restituirà NOT_MODIFIED; si
     *                                               consideri però che quest'inconsistenza è solo temporanea (basta
     *                                               che il client ricarica la pagina o che scade la sua sessione per
     *                                               ripristinare la situazione corretta) e permette di minimizzare
     *                                               l'overhead dei dati trasmessi).
     * @param listaFile Lista dei file memorizzati nel database e destinati al client che li ha richiesti.
     * @param includiMetadatiFileNellaResponse Flag: true se l'elenco dei file restituiti deve contenere tutti i
     *                                         metadati.
     * @param URI_redirection URI dell'api a cui richiedere l'elenco dei documenti nel caso in cui venga rilevata una
     *                        differenza tra l'elenco dei documenti noti al client e quelli memorizzati nel database.
     * @return {@link Response}: NOT_MODIFIED se l'elenco dei documenti noti al client appare aggiornato con quelli
     *                           nel database, SEE_OTHER se non appare aggiornato, oppure l'elenco dei documenti se
     *                           il parametro numeroDocumentiAttualmenteNotiAlClient è nullo.
     */
    public static Response creaResponseElencoDocumenti(Integer numeroDocumentiAttualmenteNotiAlClient,
                                                       List<File> listaFile,
                                                       boolean includiMetadatiFileNellaResponse,
                                                       String URI_redirection) {

        if( numeroDocumentiAttualmenteNotiAlClient !=null ) {
            if (numeroDocumentiAttualmenteNotiAlClient.equals(listaFile.size())) {
                return Response.notModified().build();
            } else {
                try {
                    return Response.seeOther(new URI(URI_redirection)).build();    // redirect a questa stessa api
                } catch (URISyntaxException e) {
                    Logger.scriviEccezioneNelLog(RichiestaDocumenti.class, e);
                    return Response.serverError().build();
                }
            }
        }

        return UtilitaGenerale.rispostaJsonConMappaConValoriJSON(File.getMappa_idFile_propFile(listaFile, includiMetadatiFileNellaResponse));

    }

    /** Restituisce il documento il cui identificativo è
     * nel @PathParam.
     * <a href="https://stackoverflow.com/a/12251265">Fonte (restituzione file tramite JAX-RS)</a>.
     */
    @Path("/downloadDocumento/{identificativoFile}")
    @GET
    // Mediatype indicato nella response
    public Response downloadFileById(@PathParam("identificativoFile") Long identificativoFile,
                                @Context HttpServletRequest httpServletRequest) {

        return File.creaResponseConFile(identificativoFile, httpServletRequest, true);

    }

    /** Restituisce la data e l'ora di visualizzazione da parte del
     * {@link Consumer} che ne fa richiesta del {@link File} il cui
     * identificativo è specificato come @PathParam.
     * Se l'attore che ha fatto richiesta non ha l'autorizzazione ad
     * accedere al file, si considera che il file richieso non esista.
     * @return la data e l'ora di visualizzazione richista (come stringa)
     *          oppure una stringa vuota se il file non è stato trovato
     *          o non è stato visualizzato.*/
    @Path("/dataOraVisualizzazione/{identificativoFile}")
    @GET
    @Produces( MediaType.TEXT_PLAIN )
    public String getDataOraVisualizzazioneFile( @PathParam("identificativoFile") Long identificativoFile,
                                                 @Context HttpServletRequest httpServletRequest             ) {

        Long identificatoreRichiedente = Autenticazione.getIdentificativoAttoreDaHttpServletRequest( httpServletRequest );
        try {

            File file = File.getEntitaDaDbById(identificativoFile);

            return file.getIdentificativoDestinatario().equals(identificatoreRichiedente) ?
                    DateTime.convertiInString(file.getDataEdOraDiVisualizzazione()) : "";

        } catch (NotFoundException notFoundException) {
            return "";
        }

    }

}