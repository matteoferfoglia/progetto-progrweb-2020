package it.units.progrweb.api.consumer;

import it.units.progrweb.entities.file.File;
import it.units.progrweb.utils.JsonHelper;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * Classe per rispondere alla richiesta di documenti destinati
 * ai Consumer.
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
    @Produces(MediaType.APPLICATION_JSON)
    public String getNomiProprietaFile() {
        return JsonHelper.convertiOggettoInJson(File.nomiProprietaFile());
    }

}
