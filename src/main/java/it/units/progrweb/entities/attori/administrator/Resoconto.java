package it.units.progrweb.entities.attori.administrator;

import it.units.progrweb.entities.attori.nonAdministrator.consumer.Consumer;
import it.units.progrweb.entities.attori.nonAdministrator.uploader.Uploader;
import it.units.progrweb.entities.file.File;
import it.units.progrweb.utils.datetime.PeriodoTemporale;

import java.util.List;

/**
 * Classe per rappresentare il resoconto di un {@link Uploader}.
 */
public class Resoconto {

    /** Numero di documenti caricati. */
    private long numeroDiDocumentiCaricati;

    /** Numero di {@link Consumer} diversi cui i documenti
     * caricati dall'{@link Uploader} afferiscono.*/
    private long numeroDiConsumersDiversiCuiTaliDocumentiAfferiscono;

    /**
     * Crea il resoconto per il periodo specificato.
     */
    public Resoconto(Long identificativoUploader, PeriodoTemporale periodoDiRiferimento) {

        List<File> occorrenzeDocumentiCaricatiDaUploader =
                File.getOccorrenzeFiltratePerUploaderEPeriodoTemporale( identificativoUploader,
                                                                            periodoDiRiferimento   );

        this.numeroDiDocumentiCaricati = occorrenzeDocumentiCaricatiDaUploader.size();
        this.numeroDiConsumersDiversiCuiTaliDocumentiAfferiscono =
                occorrenzeDocumentiCaricatiDaUploader
                    .stream()
                    .map( File::getIdentificativoDestinatario )
                    .distinct()
                    .count();
    }

    /** Default constructor. Inizializza i valori degli attributi a zero.*/
    public Resoconto() {
        this.numeroDiDocumentiCaricati = 0;
        this.numeroDiConsumersDiversiCuiTaliDocumentiAfferiscono = 0;
    }

    public long getNumeroDiDocumentiCaricati() {
        return numeroDiDocumentiCaricati;
    }

    public void setNumeroDiDocumentiCaricati(long numeroDiDocumentiCaricati) {
        this.numeroDiDocumentiCaricati = numeroDiDocumentiCaricati > 0  ?
                                           numeroDiDocumentiCaricati : 0;
    }

    public long getNumeroDiConsumersDiversiCuiTaliDocumentiAfferiscono() {
        return numeroDiConsumersDiversiCuiTaliDocumentiAfferiscono;
    }

    public void setNumeroDiConsumersDiversiCuiTaliDocumentiAfferiscono(long numeroDiConsumersDiversiCuiTaliDocumentiAfferiscono) {
        this.numeroDiConsumersDiversiCuiTaliDocumentiAfferiscono = numeroDiConsumersDiversiCuiTaliDocumentiAfferiscono > 0  ?
                                                                    numeroDiConsumersDiversiCuiTaliDocumentiAfferiscono : 0;
    }
}
