package it.units.progrweb.entities.attori;

import com.googlecode.objectify.annotation.Subclass;
import it.units.progrweb.utils.datetime.DateTime;
import it.units.progrweb.utils.datetime.PeriodoTemporale;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Rappresentazione di un attore con il ruolo di Administrator.
 * @author Matteo Ferfoglia
 */
@Subclass(index=true) // TODO : index?
public class Administrator extends Attore {

    // TODO : implementare questa classe

    private Administrator() {
        super();    // TODO
    }

    public Administrator(String username, String nomeCognome, String email) {
        super(username, nomeCognome, email);  // TODO
    }


    /** Crea un uploader con le proprietà specificate nei parametri.*/
    public Uploader creaUploader(String username, String nomeCognome, String email, byte[] immagineLogo) {
        return new Uploader(username, nomeCognome, email, immagineLogo);    // TODO
    }

    /** Restituisce true se la modifica dell'uploader va a buon fine, false altrimenti.*/
    public boolean modificaUploader(Uploader uploaderDaModificare, Uploader uploaderModificato) {
        // TODO
        return true;    // todo
    }

    /** Restituisce true se l'eliminazione dell'uploader va a buon fine, false altrimenti.*/
    public boolean eliminaUploader(Uploader uploaderDaEliminare) {
        // TODO
        return true;
    }



    /** Crea un Administrator con le proprietà specificate nei parametri.*/
    public Administrator creaAdministrator(String username, String nomeCognome, String email) {
        return new Administrator(username, nomeCognome, email);    // TODO
    }

    /** Restituisce true se la modifica dell'administrator va a buon fine, false altrimenti.*/
    public boolean modificaAdministrator(Administrator administratorDaModificare, Administrator administratorModificato) {
        // TODO
        return true;    // todo
    }

    /** Restituisce true se l'eliminazione dell'administrator va a buon fine, false altrimenti.*/
    public boolean eliminaAdministrator(Administrator administratorDaEliminare) {
        // TODO
        return true;
    }



    /** Restituisce il {@link Resoconto resoconto} del mese precedente*/
    public Resoconto resocontoUploaders() {
        // TODO : metodo da implementare
        return new Resoconto();
    }

    /** Restituisce il {@link Resoconto resoconto} nel periodo temporale
     * specificato nei parametri (estremi inclusi).*/
    public Resoconto resocontoUploaders(DateTime dataIniziale, DateTime dataFinale) {
        // TODO : metodo da implementare
        return new Resoconto(dataIniziale, dataFinale);
    }
}

/** Classe per rappresentare un resoconto, cioè un elenco in cui per ogni
 * Uploader è elencato il numero di documenti caricati ed il numero di
 * consumers diversi cui tali documenti afferiscono.
 */
class Resoconto {

    // TODO : classe da implementare e testare

    /** Lista di resoconti, uno per ogni uploader.*/
    private final List<ResocontoDiUnUploader> listaResocontiDiTuttiGliUploader;

    /** Periodo di riferimento di questo resoconto. */
    private final PeriodoTemporale periodoDiRiferimento;


    /** Crea il resoconto per il periodo specificato.*/
    private Resoconto(PeriodoTemporale periodoDiRiferimento) {
        // TODO
        this.periodoDiRiferimento = periodoDiRiferimento;
        this.listaResocontiDiTuttiGliUploader = listaResocontiDiTuttiGliUploaderNelPeriodo(periodoDiRiferimento) ;
    }

    /** Crea il resoconto riferito al mese precedente.
     * Con "mese precedente" si intende l'effettivo mese precedente del
     * calendario, cioè, ad esempio, se oggi è il 29 dicembre 2020, il
     * mese precedente è il periodo dal 1 novembre al 30 novembre 2020. */
    public Resoconto() {
        // TODO
        this( PeriodoTemporale.mesePrecedente() );
    }

    /** Crea il resoconto riferito alle dati specificate nei parametri, estremi inclusi.*/
    public Resoconto(DateTime dataIniziale, DateTime dataFinale) {
        // TODO
        this( new PeriodoTemporale(dataIniziale, dataFinale) );
    }


    /** Crea la lista dei resoconti di tutti gli uploader nel periodo di riferimento specificato.*/
    private static List<ResocontoDiUnUploader> listaResocontiDiTuttiGliUploaderNelPeriodo(PeriodoTemporale periodoDiRiferimento) {
        // TODO :  calcola la lista dei resoconti
        // todo : testare questo metodo

        List<Uploader> listaUploader = getListaConTuttiGliUploader();

        return listaUploader.stream()
                            .map( uploader -> new ResocontoDiUnUploader(uploader, periodoDiRiferimento) )
                            .collect(Collectors.toList());
    }

    /** Restituisce la lista di tutti gli Uploader del sistema. */
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

/** Classe per rappresentare il resoconto associato ad un singolo uploader.*/
class ResocontoDiUnUploader {

    // TODO : testare questa classe

    /** Uploader a cui questo resoconto si riferisce.*/
    private final Uploader uploader;

    /** Periodo temporale di riferimento per questo resoconto.*/
    private final PeriodoTemporale periodoTemporaleDiRiferimento;

    /** Documenti caricati dall' {@link #uploader Uploader} nel
     * {@link #periodoTemporaleDiRiferimento periodo temporale}
     * di riferimento per questo resoconto. */
    private final List<it.units.progrweb.entities.file.File> listaDocumentiCaricati;

    /** Numero di documenti caricati dall'uploader.*/
    private final long numeroDocumentiCaricati;

    /** Numero di consumers distinti a cui i documenti
     * caricati dall'uploader si riferiscono*/
    private final long numeroConsumersDistintiCuiDocumentiCaricatiRiferiscono;


    /** Crea il resoconto associato all'uploader specificato, nel periodo specificato.*/
    public ResocontoDiUnUploader(Uploader uploader, PeriodoTemporale periodoTemporale) {
        this.uploader = uploader;
        this.periodoTemporaleDiRiferimento = periodoTemporale;
        this.listaDocumentiCaricati = uploader.getDocumentiCaricatiNelPeriodo(periodoTemporale);
        this.numeroDocumentiCaricati = this.listaDocumentiCaricati.size();
        this.numeroConsumersDistintiCuiDocumentiCaricatiRiferiscono = this.listaDocumentiCaricati.stream()
                .map(it.units.progrweb.entities.file.File::getConsumer)
                .distinct()
                .count();   // TODO : rifare ! iterare sui consumer o aggiungere Key<Consumer> consumer in File per avere mapping bidirezionale
    }


    public Uploader getUploader() {
        return uploader;
    }

    public PeriodoTemporale getPeriodoTemporaleDiRiferimento() {
        return periodoTemporaleDiRiferimento;
    }

    public List<it.units.progrweb.entities.file.File> getListaDocumentiCaricati() {
        return listaDocumentiCaricati;
    }

    public long getNumeroDocumentiCaricati() {
        return numeroDocumentiCaricati;
    }

    public long getNumeroConsumersDistintiCuiDocumentiCaricatiRiferiscono() {
        return numeroConsumersDistintiCuiDocumentiCaricatiRiferiscono;
    }
}