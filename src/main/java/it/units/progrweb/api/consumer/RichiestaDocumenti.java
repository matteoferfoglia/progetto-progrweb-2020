package it.units.progrweb.api.consumer;

import it.units.progrweb.entities.attori.Consumer;
import it.units.progrweb.entities.file.File;
import it.units.progrweb.utils.Autenticazione;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

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
    public String[] getNomiProprietaFile() {
        return File.nomiProprietaFile();
    }

    /** Restituisce un array (JSON) in cui ogni elemento è un'astrazione
     * di un documento destinato al Consumer da cui proviene la richiesta.
     * Ogni elemento è un oggetto rappresentato come insieme di proprietà
     * nella forma "nome-valore" e contiene tutte e sole le proprietà
     * date da {@link File#getProprietaFile()}.*/
    @Path("/elencoDocumenti")     // TODO : variabile d'ambiente
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String[] getElencoDocumenti(@Context HttpServletRequest httpServletRequest) {

        Consumer consumer = (Consumer) Autenticazione.getAttoreDaHttpServletRequest(httpServletRequest);
        List<File> listaFile = consumer.getFilesDestinatiAQuestoConsumer();

        // Ogni elemento dell'array contiene la descrizione JSON di un file:
        String[] arrayJsonDescrizioniFile = listaFile.stream()
                                                     .map(file -> file.toJson())
                                                     .toArray(String[]::new);

        return arrayJsonDescrizioniFile;
    }

    /** Restituisce un oggetto JSON in cui ogni property
     * è un hashtag ed il corrispettivo valore è un array
     * contenente tutti i file (rappresentati da un
     * identificativo). In pratica, viene restituito
     * un indice dei file, indicizzati in base all'hashtag.
     * Gli hashtag <strong>non</strong> sono (in generale)
     * ordinati.
     * Esempio (solo per illustrare questo metodo, l'esempio
     * è scorrelato dal o dal linguaggio di programmazione).
     * Si consideri il seguente array di valori proveniente
     * dalla richiesta del client:
     * <code>[25,27,9]</code>.
     * Tali valori corrispono a dei file secondo una mappatura
     * nota al server e siano le seguenti le rappresentazioni
     * dei file associati agli identificatori ottenuti dalla
     * richiesta del client:
     * <ol>
     *     <li><code>{"identificativo": 25, "nomeFile": "Fattura123", "hashtags": ["fattura","pagare","soldi"]}</code></li>
     *     <li><code>{"identificativo": 27, "nomeFile": "PreventivoA26", "hashtags": ["pagare"]}</code></li>
     *     <li><code>{"identificativo": 9, "nomeFile": "Bonifico", "hashtags": ["guadagno","soldi"]}</code></li>
     * </ol>
     * Allora, questo metodo restituirà (a meno dell'ordine
     * delle property, oppure dei valori numerici -
     * <strong>non</strong> necessariamente ordinati) il
     * seguente oggetto JSON:
     * <pre><code>
     *     {
     *         "fattura":[25],
     *         "pagare":[25,27],
     *         "soldi":[25,9],
     *         "guadagno":[9]
     *     }
     * </code></pre>
     *
     * @param identificativiFile Array degli identificativi dei file
     *                           per i quali si vuole l'indicizzazione
     *                           rispetto agli hashtag.
     * @return L'indice dei file, indicizzati in base agli hashtag
     *          che essi contengono.
     */
    @GET
    @Path("/{arrayIdentificativiFile}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getListaHashtags(long[] identificativiFile) {

        // TODO : rivedere questo metodo e la sua descrizione

        // TODO : testare : deserializzazione corretta ??? Meglio usare @PathParam ? @QueryParam ?

        return rispostaOkContenutoJson(""); // TODO
    }

    /** Costruisce una risposta, con contenuto in formato JSON.*/
    private static Response rispostaOkContenutoJson(String contenutoJsonResponse) {
        return Response.ok()
                       .type(MediaType.APPLICATION_JSON)
                       .entity(contenutoJsonResponse)
                       .build();
    }
}