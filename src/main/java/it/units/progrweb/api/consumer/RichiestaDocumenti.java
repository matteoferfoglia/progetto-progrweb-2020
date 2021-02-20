package it.units.progrweb.api.consumer;

import it.units.progrweb.entities.attori.nonAdministrator.consumer.Consumer;
import it.units.progrweb.entities.file.File;
import it.units.progrweb.persistence.NotFoundException;
import it.units.progrweb.utils.Autenticazione;
import it.units.progrweb.utils.UtilitaGenerale;
import it.units.progrweb.utils.datetime.DateTime;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Map;

/**
 * Classe per rispondere alla richiesta di documenti destinati
 * ai Consumer.
 * La verifica dell'autenticazione di un attore quale Consumer
 * è demandata all'opportuno
 * {@link it.units.progrweb.filters.attori.FiltroConsumer filtro}.
 * @author Matteo Ferfoglia
 */
@Path("/consumer/documenti")      // TODO : variabile d'ambiente
public class RichiestaDocumenti {


    /** Restituisce un oggetto (JSON) in cui ogni property è un'astrazione
     * di un documento destinato al Consumer da cui proviene la richiesta:
     * ogni property ha per nome l'identificativo del documento e per valore
     * l'oggetto che rappresenta il documento (descritto in base ai suoi
     * attributi, tutti e soli quelli dati da {@link File#getAnteprimaProprietaFile(boolean)}.
     * @param httpServletRequest La richiesta HTTP.
     * @param  identificativoUploader Identificativo dell'Uploader. */
    @Path("/elencoDocumenti/{identificativoUploader}")     // TODO : variabile d'ambiente
    @GET
    // Response costruita senza @Produces per serializzare i dati in modo personalizzato
    public static Response getElencoDocumenti(@Context HttpServletRequest httpServletRequest,
                                              @PathParam("identificativoUploader") Long identificativoUploader) {

        // TODO : verificare

        Consumer consumer = (Consumer) Autenticazione.getAttoreDaHttpServletRequest(httpServletRequest);
        List<File> listaFile = consumer.getAnteprimaFiles(identificativoUploader);

        Map<String, String> mappa_idFile_propFileInJson = File.getMappa_idFile_propFile(listaFile, false);

        return UtilitaGenerale.rispostaJsonConMappa(mappa_idFile_propFileInJson);

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

    /** Restituisce la data e l'ora di visualizzazione da parte del // TODO : corretto che restituisca la data ora di visualizzazione? Non di caricamento??
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

        Long identificatoreRichiedente = Autenticazione.getIdentificativoAttoreDaTokenAutenticazione( httpServletRequest );
        try {

            File file = File.getEntitaDaDbById(identificativoFile);

            return file.getIdentificativoDestinatario().equals(identificatoreRichiedente) ?
                    DateTime.convertiInString(file.getDataEdOraDiVisualizzazione()) : "";

        } catch (NotFoundException notFoundException) {
            return "";
        }

    }

}