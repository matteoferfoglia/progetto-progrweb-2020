package it.units.progrweb.entities.attori.administrator;

import it.units.progrweb.entities.RelazioneUploaderConsumerFile;
import it.units.progrweb.entities.attori.nonAdministrator.consumer.Consumer;
import it.units.progrweb.entities.attori.nonAdministrator.uploader.Uploader;
import it.units.progrweb.utils.datetime.PeriodoTemporale;

import java.util.List;

/**
 * Classe per rappresentare il resoconto di un {@link Uploader}.
 */
public class Resoconto {

    // TODO : classe da implementare e testare
    // TODO : serve una classe proxy ?


    /** Numero di documenti caricati. */
    private long numeroDiDocumentiCaricati;

    /** Numero di {@link Consumer} diversi cui i documenti
     * caricati dall'{@link Uploader} afferiscono.*/
    private long numeroDiConsumersDiversiCuiTaliDocumentiAfferiscono;

    /**
     * Crea il resoconto per il periodo specificato.
     */
    public Resoconto(Long identificativoUploader, PeriodoTemporale periodoDiRiferimento) {

        List<RelazioneUploaderConsumerFile> occorrenzeDocumentiCaricatiDaUploader =
                RelazioneUploaderConsumerFile
                        .getOccorrenzeFiltratePerUploaderEPeriodoTemporale( identificativoUploader,
                                                                            periodoDiRiferimento   );

        this.numeroDiDocumentiCaricati = occorrenzeDocumentiCaricatiDaUploader.size();
        this.numeroDiConsumersDiversiCuiTaliDocumentiAfferiscono =
                occorrenzeDocumentiCaricatiDaUploader
                    .stream()
                    .map( RelazioneUploaderConsumerFile::getIdentificativoConsumer )
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
