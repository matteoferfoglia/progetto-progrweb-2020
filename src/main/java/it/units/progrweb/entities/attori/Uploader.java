package it.units.progrweb.entities.attori;

import com.googlecode.objectify.annotation.Subclass;
import it.units.progrweb.utils.datetime.PeriodoTemporale;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Rappresentazione di un Uploader.
 * @author Matteo Ferfoglia
 */
@Subclass(index=true) // TODO : index?
public class Uploader extends UtenteNonAdministrator {

    /** Logo dell'uploader.*/
    private File immagineLogo;  // TODO : come salvare nel database ? Blob? byte[] ? File ? // TODO : salvare in db!! // TODO : meglio fare una classe a parte? Ad es. specificando i formati permessi??

    // TODO : implementare questa classe



    private Uploader() {
        super();    // TODO
    }

    public Uploader(String username, String nomeCognome, String email, File immagineLogo) {
        // TODO
        super(username, nomeCognome, email);
        this.immagineLogo = immagineLogo;
    }

    /** Restituisce la lista dei documenti caricati da questo uploader nel periodo temporale specificato*/
    public List<it.units.progrweb.entities.file.File> getDocumentiCaricatiNelPeriodo(PeriodoTemporale periodoTemporale) {
        // TODO : interroga db è restituisci il numero di documenti caricati da questo uplaoder nel periodo indicato
        return new ArrayList<>();   // TODO !!!
    }



    /** Restituisce true se la modifica del logo va a buon fine, false altrimenti.*/
    public boolean modificalogo(File nuovaImmagineLogo) {
        // TODO
        return true;    // todo
    }



    /** Crea un consumer con le proprietà specificate nei parametri.*/
    public Consumer creaConsumer(String username, String nomeCognome, String email) {
        return new Consumer(username, nomeCognome, email);    // TODO
    }

    /** Restituisce true se la modifica del consumer va a buon fine, false altrimenti.*/
    public boolean modificaConsumer(Consumer consumerModificato) {
        // TODO
        return true;    // todo
    }

    /** Restituisce true se l'eliminazione del consumer va a buon fine, false altrimenti.*/
    public boolean eliminaConsumer(Consumer consumerDaEliminare) {
        // TODO
        return true;
    }


    /** Crea un file per il consumer specificato.
     * Restituisce true se la procedura va a buon fine, false altrimenti. */
    public boolean creaFilePerConsumer(it.units.progrweb.entities.file.File file, Consumer consumer) {
        // TODO : creare il file per il consumer
        return true;   // TODO !!!
    }

    /** Elimina il file specificato.
     * Restituisce true se la procedura va a buon fine, false altrimenti. */
    public boolean eliminaFile(it.units.progrweb.entities.file.File fileDaEliminare) {
        // TODO : eliminare il file specificato
        // TODO : pensare alla strategia di eliminazione: per i consumer che hanno già ricevuto il file? O già visualizzato?
        return true;   // TODO !!!
    }

}