package it.units.progrweb.api.administrator;

import it.units.progrweb.entities.attori.Attore;
import it.units.progrweb.entities.attori.administrator.Administrator;
import it.units.progrweb.entities.attori.administrator.Resoconto;
import it.units.progrweb.entities.attori.uploader.Uploader;
import it.units.progrweb.utils.EncoderPrevenzioneXSS;
import it.units.progrweb.utils.ResponseHelper;
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
 * Nel {@link Resoconto} vengono conteggiati anche i documenti eliminati
 * dagli {@link Uploader}.
 * @author Matteo Ferfoglia
 */
@Path("/administrator/resocontoUploader")
public class RichiestaResocontoUploader {

    /** Restituisce il {@link Resoconto} dei documenti caricati dall'
     * {@link Uploader} specificato nel parametro durante il periodo
     * temporale specificato nei parametri.
     * @param identificativoUploader L'identificativo dell'{@link Uploader}.
     * @param dataInizio Data di inizio (inclusa) del periodo di interesse.
     * @param dataFine Data di fine (inclusa) del periodo di interesse.
     * @return L'istanza di {@link Resoconto} richiesta.
     * @throws IOException A causa di {@link HttpServletResponse#sendError(int)}.
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
        PeriodoTemporale periodoTemporale = new PeriodoTemporale( dataIniziale.adInizioGiornata(), dataFinale.aFineGiornata() );

        Uploader uploaderPerCuiSiRichiedeResoconto = (Uploader)
                Attore.getAttoreDaIdentificativo( identificativoUploader );

        if( uploaderPerCuiSiRichiedeResoconto != null ) {

            return new Resoconto( identificativoUploader, periodoTemporale);

        } else {
            ResponseHelper.creaResponseNotFound("Uploader [" + identificativoUploader + "] non trovato", httpServletResponse);
            return null;
        }
    }



}
