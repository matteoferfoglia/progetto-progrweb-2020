package it.units.progrweb.api.consumer;

import it.units.progrweb.entities.attori.nonAdministrator.consumer.Consumer;
import it.units.progrweb.entities.file.File;
import it.units.progrweb.persistence.NotFoundException;
import it.units.progrweb.utils.Autenticazione;
import it.units.progrweb.utils.JsonHelper;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    /** Restituisce un array contenente i nomi delle proprietà
     * comuni a tutti i documenti. Tale array può essere utilizzato
     * dai client per preparare l'interfaccia grafica (es.: intestazione
     * di una tabella che conterrà un documento per riga.*/
    @Path("/arrayNomiProprietaOgniDocumento")  // TODO : variabile d'ambiente
    @GET
    @Produces(MediaType.APPLICATION_JSON)       // TODO : cercare tutti i Produces e Consumes ed usare MediaType
    public String[] getNomiProprietaFileInAnteprima() {
        return File.anteprimaNomiProprietaFile();
    }

    /** Restituisce un oggetto (JSON) in cui ogni property è un'astrazione
     * di un documento destinato al Consumer da cui proviene la richiesta:
     * ogni property ha per nome l'identificativo del documento e per valore
     * l'oggetto che rappresenta il documento (descritto in base ai suoi
     * attributi, tutti e soli quelli dati da {@link File#getAnteprimaProprietaFile()}.
     * @param httpServletRequest La richiesta HTTP.
     * @param identificativoUploader Identificativo dell'Uploader. */
    @Path("/elencoDocumenti/{identificativoUploader}")     // TODO : variabile d'ambiente
    @GET
    // Response costruita senza @Produces per serializzare i dati in modo personalizzato
    public Response getElencoDocumenti(@Context HttpServletRequest httpServletRequest,
                                       @PathParam("identificativoUploader") Long identificativoUploader) {

        // TODO : verificare

        Consumer consumer = (Consumer) Autenticazione.getAttoreDaHttpServletRequest(httpServletRequest);
        List<File> listaFile = consumer.getAnteprimaFiles(identificativoUploader);

        Map<String,String> mappa_idFile_propFileInJson =
               listaFile.stream()
                        .collect(Collectors.toMap(
                                File::getIdentificativoFile,
                                file -> JsonHelper.convertiMappaProprietaToStringaJson(file.toMap_nomeProprieta_valoreProprieta()) // Ogni elemento dello stream contiene la descrizione JSON di un file
                            )
                        );

        // Costruzione della response
        return Response.ok()
                       .type(MediaType.APPLICATION_JSON)
                       .entity(JsonHelper.convertiMappaProprietaToStringaJson(mappa_idFile_propFileInJson))
                       .build();

    }

    /** Restituisce il documento il cui identificativo è
     * nel @PathParam.
     * <a href="https://stackoverflow.com/a/12251265">Fonte (restituzione file tramite JAX-RS)</a>.
     */
    @Path("/downloadDocumento/{identificativoFile}")
    @GET
    // Mediatype indicato nella response
    public Response getFileById(@PathParam("identificativoFile") Long identificativoFile,
                                @Context HttpServletRequest httpServletRequest) {

        // TODO : verifica che questo metod funzioni

        try {

            File file = File.getEntitaFromDbById(identificativoFile);
            InputStream inputStream = File.getContenutoFile(file, httpServletRequest.getRemoteAddr());
            return Response.ok(inputStream, MediaType.APPLICATION_OCTET_STREAM)
                           .header("Content-Disposition", "attachment; filename=\"" + file.getNomeDocumento() + "\"" )
                           .build();

        } catch (NotFoundException notFoundException) {
            return Response.status(Response.Status.NOT_FOUND)
                           .build();
        }

    }


    /** Restituisce il nome dell'attributo di un {@link File} che
     * contiene l'array di hashtag riferiti a quel {@link File}.
     * Vedere anche il {@link #getElencoDocumenti(HttpServletRequest,Long)
     * metodo di invio dei file ai client}.*/
    @Path("/nomePropHashtags")     // TODO : variabile d'ambiente
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String getNomeAttributoContenenteHashtagNeiFile() {
        return File.getNomeAttributoContenenteHashtagNeiFile();
    }

    /** Restituisce il nome dell'attributo di un {@link File} che
     * contiene la data di caricamento di quel {@link File}.
     * Vedere anche il {@link #getElencoDocumenti(HttpServletRequest,Long)
     * metodo di invio dei file ai client}.*/
    @Path("/nomePropDataCaricamento")     // TODO : variabile d'ambiente
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String getNomeAttributoContenenteDataCaricamentoFile() {
        return File.getNomeAttributoContenenteDataCaricamentoFile();
    }

    /** Restituisce il nome dell'attributo di un {@link File} che
     * contiene la data di visualizzazione di quel {@link File}
     * da parte del Consumer.
     * Vedere anche il {@link #getElencoDocumenti(HttpServletRequest,Long)
     * metodo di invio dei file ai client}.*/
    @Path("/nomePropDataVisualizzazione")     // TODO : variabile d'ambiente
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String getNomeAttributoContenenteDataVisualizzazioneFile() {
        return File.getNomeAttributoContenenteDataVisualizzazioneFile();
    }
}