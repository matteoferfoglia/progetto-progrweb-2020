package it.units.progrweb.api.administrator;

import it.units.progrweb.entities.attori.Attore;
import it.units.progrweb.entities.attori.administrator.Administrator;
import it.units.progrweb.entities.attori.administrator.Resoconto;
import it.units.progrweb.entities.attori.nonAdministrator.uploader.Uploader;
import it.units.progrweb.utils.EncoderPrevenzioneXSS;
import it.units.progrweb.utils.datetime.DateTime;
import it.units.progrweb.utils.datetime.PeriodoTemporale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.io.IOException;

/**
 * Classe che espone i servizi relativi al {@link Resoconto} da mostrare
 * ad un {@link Administrator}, come da specifiche.
 * @author Matteo Ferfoglia
 */
@Path("/administrator/resocontoUploader")
public class RichiestaResocontoAttori {

    /** Restituisce il {@link Resoconto} dei documenti caricati dall'
     * {@link Uploader} specificato nel parametro durante il periodo
     * temporale specificato nei parametri.
     * @param identificativoUploader
     * @param dataInizio
     * @param dataFine
     * @throws IOException A causa di {@link HttpServletResponse#sendError(int)}.
     * @return
     */
    @GET
    @Path("/{identificativoUploader}")
    @Consumes( MediaType.APPLICATION_JSON )
    @Produces( MediaType.APPLICATION_JSON )
    public Resoconto getResocontoNelPeriodo(@PathParam("identificativoUploader") Long identificativoUploader,
                                            @QueryParam("dataInizio")            String dataInizio,
                                            @QueryParam("dataFine")              String dataFine,
                                            @Context HttpServletRequest  httpServletRequest,
                                            @Context HttpServletResponse httpServletResponse                  )
            throws IOException {

        DateTime dataIniziale = DateTime.convertiDaString_htmlInputDate(EncoderPrevenzioneXSS.encodeForJava(dataInizio));
        DateTime dataFinale   = DateTime.convertiDaString_htmlInputDate(EncoderPrevenzioneXSS.encodeForJava(dataFine));
        PeriodoTemporale periodoTemporale = new PeriodoTemporale( dataIniziale, dataFinale );

        Uploader uploaderPerCuiSiRichiedeResoconto = (Uploader)
                Attore.getAttoreDaIdentificativo( identificativoUploader );

        if( uploaderPerCuiSiRichiedeResoconto != null ) {

            Resoconto resoconto = new Resoconto( identificativoUploader, periodoTemporale);
            return resoconto;

        } else {
            // Utilizzo httpServletResponse per rispondere con errore (Fonte: https://stackoverflow.com/a/22869076 )
            httpServletResponse.sendError( HttpServletResponse.SC_NOT_FOUND,
                                           "Uploader [" + identificativoUploader + "] non trovato" );
            return null;
        }
    }



}
