package it.units.progrweb.api.consumer;

import it.units.progrweb.entities.attori.Attore;
import it.units.progrweb.entities.attori.Consumer;
import it.units.progrweb.entities.file.File;
import it.units.progrweb.utils.Autenticazione;
import it.units.progrweb.utils.JsonHelper;

import javax.servlet.http.HttpServletRequest;
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
 * è demandata all'opportuno filtro.    // TODO : creare filtro per richieste destinate a /consumer
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
    public String getNomiProprietaFile() {
        return JsonHelper.convertiOggettoInJson(File.nomiProprietaFile());
    }

    /** Restituisce un array (JSON) in cui ogni elemento è un'astrazione
     * di un documento destinato al Consumer da cui proviene la richiesta.
     * Ogni elemento è un oggetto rappresentato come insieme di proprietà
     * nella forma "nome-valore" (stile JavaScript) e contiene tutte e
     * sole le proprietà date da {@link File#getProprietaFile()}.
     * Risponde con {@link javax.ws.rs.core.Response.Status#FORBIDDEN}
     * se il client che ha effettuato la richiesta non è un Consumer.*/
    @Path("/elencoDocumenti")     // TODO : variabile d'ambiente
    @GET
    public Response getElencoDocumenti(@Context HttpServletRequest httpServletRequest) {
        Attore attore = Autenticazione.getAttoreDaHttpServletRequest(httpServletRequest);
        if(attore instanceof Consumer) {
            Consumer consumer = (Consumer) attore;
            // TODO : da implementare : scaricare la lista di file di questo attore
            List<File> listaFile = consumer.getFilesDestinatiAQuestoConsumer();

            // Ogni elemento dell'array contiene la descrizione JSON di un file:
            String[] arrayJsonDescrizioniFile = listaFile.stream()
                                                         .map(file -> file.toJson())
                                                         .toArray(String[]::new);
            String contenutoResponse = JsonHelper.convertiOggettoInJson(arrayJsonDescrizioniFile);
            return Response.ok()
                           .type(MediaType.APPLICATION_JSON)
                           .entity(contenutoResponse)
                           .build();
        } else {
            return Response.status(Response.Status.FORBIDDEN).build();
        }
    }

}