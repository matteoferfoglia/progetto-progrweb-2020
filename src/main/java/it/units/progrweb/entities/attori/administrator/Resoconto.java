package it.units.progrweb.entities.attori.administrator;

import it.units.progrweb.entities.attori.nonAdministrator.uploader.Uploader;
import it.units.progrweb.utils.datetime.DateTime;
import it.units.progrweb.utils.datetime.PeriodoTemporale;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Classe per rappresentare un resoconto, cioè un elenco in cui per ogni
 * uploader è elencato il numero di documenti caricati ed il numero di
 * consumers diversi cui tali documenti afferiscono.
 */
class Resoconto {

    // TODO : classe da implementare e testare

    /**
     * Lista di resoconti, uno per ogni uploader.
     */
    private final List<ResocontoDiUnUploader> listaResocontiDiTuttiGliUploader;

    /**
     * Periodo di riferimento di questo resoconto.
     */
    private final PeriodoTemporale periodoDiRiferimento;


    /**
     * Crea il resoconto per il periodo specificato.
     */
    private Resoconto(PeriodoTemporale periodoDiRiferimento) {
        // TODO
        this.periodoDiRiferimento = periodoDiRiferimento;
        this.listaResocontiDiTuttiGliUploader = listaResocontiDiTuttiGliUploaderNelPeriodo(periodoDiRiferimento);
    }

    /**
     * Crea il resoconto riferito al mese precedente.
     * Con "mese precedente" si intende l'effettivo mese precedente del
     * calendario, cioè, ad esempio, se oggi è il 29 dicembre 2020, il
     * mese precedente è il periodo dal 1 novembre al 30 novembre 2020.
     */
    public Resoconto() {
        // TODO
        this(PeriodoTemporale.mesePrecedente());
    }

    /**
     * Crea il resoconto riferito alle dati specificate nei parametri, estremi inclusi.
     */
    public Resoconto(DateTime dataIniziale, DateTime dataFinale) {
        // TODO
        this(new PeriodoTemporale(dataIniziale, dataFinale));
    }


    /**
     * Crea la lista dei resoconti di tutti gli uploader nel periodo di riferimento specificato.
     */
    private static List<ResocontoDiUnUploader> listaResocontiDiTuttiGliUploaderNelPeriodo(PeriodoTemporale periodoDiRiferimento) {
        // TODO :  calcola la lista dei resoconti
        // todo : testare questo metodo

        List<Uploader> listaUploader = getListaConTuttiGliUploader();

        return listaUploader.stream()
                .map(uploader -> new ResocontoDiUnUploader(uploader, periodoDiRiferimento))
                .collect(Collectors.toList());
    }

    /**
     * Restituisce la lista di tutti gli uploader del sistema.
     */
    private static List<Uploader> getListaConTuttiGliUploader() {
        // TODO : interrograre database per ottenere la lista di tutti gli uplaoders.
        return new ArrayList<>();   // TODO !!!
    }


    public List<ResocontoDiUnUploader> getListaResocontiDiTuttiGliUploader() {
        return listaResocontiDiTuttiGliUploader;
    }

    public PeriodoTemporale getPeriodoDiRiferimento() {
        return periodoDiRiferimento;
    }
}
