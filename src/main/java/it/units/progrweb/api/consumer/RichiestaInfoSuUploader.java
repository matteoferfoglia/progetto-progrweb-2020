package it.units.progrweb.api.consumer;

import it.units.progrweb.entities.attori.nonAdministrator.consumer.Consumer;
import it.units.progrweb.entities.attori.nonAdministrator.uploader.Uploader;
import it.units.progrweb.entities.file.File;
import it.units.progrweb.utils.Autenticazione;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

/**
 * Servizi per richiedere le informazioni sugli Uploader.
 * @author Matteo Ferfoglia
 */
@Path("/consumer")
public class RichiestaInfoSuUploader {

    /** Restituisce un array con gli identificativi di tutti gli {@link Uploader}
     * che hanno caricato dei {@link File} per questo {@link Consumer}.*/
    @Path("/elencoUploader")
    @GET
    @Produces( MediaType.APPLICATION_JSON )
    public Long[] getElencoIdentificativiUploaderCheHannoCaricatoFilePerQuestoConsumer(@Context HttpServletRequest httpServletRequest) {

        Long identificativoQuestoConsumer =
                Autenticazione.getIdentificativoAttoreDaTokenAutenticazione(httpServletRequest);

        return File.getElencoUploaderServentiConsumer(identificativoQuestoConsumer);

    }

}
