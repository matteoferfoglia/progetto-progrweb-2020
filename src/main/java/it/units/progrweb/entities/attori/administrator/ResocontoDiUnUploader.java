package it.units.progrweb.entities.attori.administrator;

import it.units.progrweb.entities.attori.nonAdministrator.uploader.Uploader;
import it.units.progrweb.entities.file.File;
import it.units.progrweb.utils.datetime.PeriodoTemporale;

import java.util.List;

/**
 * Classe per rappresentare il resoconto associato ad un singolo uploader.
 */
class ResocontoDiUnUploader {

    // TODO : testare questa classe

    /**
     * uploader a cui questo resoconto si riferisce.
     */
    private final Uploader uploader;

    /**
     * Periodo temporale di riferimento per questo resoconto.
     */
    private final PeriodoTemporale periodoTemporaleDiRiferimento;

    /**
     * Documenti caricati dall' {@link Uploader} nel
     * {@link #periodoTemporaleDiRiferimento periodo temporale}
     * di riferimento per questo resoconto.
     */
    private final List<File> listaDocumentiCaricati;

    /**
     * Numero di documenti caricati dall'{@link Uploader}.
     */
    private final long numeroDocumentiCaricati;

    /**
     * Numero di consumers distinti a cui i documenti
     * caricati dall'{@link Uploader} si riferiscono
     */
    private final long numeroConsumersDistintiCuiDocumentiCaricatiRiferiscono;


    /**
     * Crea il resoconto associato all'uploader specificato, nel periodo specificato.
     */
    public ResocontoDiUnUploader(Uploader uploader, PeriodoTemporale periodoTemporale) {
        this.uploader = uploader;
        this.periodoTemporaleDiRiferimento = periodoTemporale;
        this.listaDocumentiCaricati = Uploader.getDocumentiCaricatiNelPeriodoDallUploader(periodoTemporale, uploader);
        this.numeroDocumentiCaricati = this.listaDocumentiCaricati.size();
        this.numeroConsumersDistintiCuiDocumentiCaricatiRiferiscono =
                this.listaDocumentiCaricati.stream()
                        .map(File::getConsumer)
                        .distinct()
                        .count();   // TODO : rifare ! iterare sui consumer o aggiungere Key<Consumer> consumer in File per avere mapping bidirezionale
    }


    public Uploader getUploader() {
        return uploader;
    }

    public PeriodoTemporale getPeriodoTemporaleDiRiferimento() {
        return periodoTemporaleDiRiferimento;
    }

    public List<File> getListaDocumentiCaricati() {
        return listaDocumentiCaricati;
    }

    public long getNumeroDocumentiCaricati() {
        return numeroDocumentiCaricati;
    }

    public long getNumeroConsumersDistintiCuiDocumentiCaricatiRiferiscono() {
        return numeroConsumersDistintiCuiDocumentiCaricatiRiferiscono;
    }
}
