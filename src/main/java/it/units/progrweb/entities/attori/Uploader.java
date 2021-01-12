package it.units.progrweb.entities.attori;

import com.googlecode.objectify.Ref;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Load;
import it.units.progrweb.entities.file.File;
import it.units.progrweb.persistence.DatabaseHelper;
import it.units.progrweb.utils.datetime.PeriodoTemporale;

import java.util.ArrayList;
import java.util.List;

/**
 * Rappresentazione di un Uploader.
 * @author Matteo Ferfoglia
 */
@Entity
public class Uploader extends UtenteNonAdministrator {

    // TODO : implementare questa classe

    /** Logo dell'uploader.*/
    private byte[] immagineLogo;  // byte[] automaticamente convertito in Blob nel datastore

    /** File caricati da questo Uploader.*/
    @Load
    private List<Ref<File>> filesCaricatiDaQuestoUploader = new ArrayList<>();          // Relazione uno a molti

    /** Consumer serviti da questo Uploader.*/
    @Load
    private List<Ref<Consumer>> consumerServitiDaQuestoUploader = new ArrayList<>();    // Relazione uno a molti


    public List<File> getFilesCaricatiDaQuestoUploader() {
        return DatabaseHelper.getListaEntitaDaListaReference(filesCaricatiDaQuestoUploader);
    }

    public List<Consumer> getConsumerServitiDaQuestoUploader() {
        return DatabaseHelper.getListaEntitaDaListaReference(consumerServitiDaQuestoUploader);
    }


    private Uploader() {
        super();    // TODO
    }

    public Uploader(String username, String nomeCognome, String email, byte[] immagineLogo) {
        // TODO
        super(username, nomeCognome, email);
        this.immagineLogo = immagineLogo;
    }

    /** Restituisce la lista dei documenti caricati da questo uploader nel periodo temporale specificato*/
    public List<File> getDocumentiCaricatiNelPeriodo(PeriodoTemporale periodoTemporale) {
        // TODO : interroga db è restituisci il numero di documenti caricati da questo uploadder nel periodo indicato
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
    public boolean creaFilePerConsumer(byte[] file, Consumer consumer) {
        // TODO : creare il file per il consumer
        return true;   // TODO !!!
    }

    /** Elimina il file specificato.
     * Restituisce true se la procedura va a buon fine, false altrimenti. */
    public boolean eliminaFile(byte[] fileDaEliminare) {
        // TODO : eliminare il file specificato
        // TODO : pensare alla strategia di eliminazione: per i consumer che hanno già ricevuto il file? O già visualizzato?
        return true;   // TODO !!!
    }

}