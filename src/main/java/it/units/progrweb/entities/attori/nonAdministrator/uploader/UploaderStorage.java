package it.units.progrweb.entities.attori.nonAdministrator.uploader;

import com.googlecode.objectify.annotation.Subclass;
import it.units.progrweb.entities.attori.nonAdministrator.consumer.Consumer;
import it.units.progrweb.entities.attori.nonAdministrator.consumer.ConsumerStorage;
import it.units.progrweb.entities.file.File;

import java.util.HashMap;
import java.util.Map;

/**
 * Classe rappresentante un'entità {@link Uploader}
 * da memorizzare nel database.
 * @author Matteo Ferfoglia
 */
// TODO : serve Proxy pattern?
@Subclass(index = false)
class UploaderStorage extends Uploader {

    // TODO : implementare questa classe

    private UploaderStorage() {
        super();    // TODO
    }

    /** Vedere {@link Uploader}.*/
     public UploaderStorage(String username, String nominativo, String email,
                     byte[] immagineLogo, String estensioneFileContenenteImmagineLogo) {
         super(username, nominativo, email, immagineLogo, estensioneFileContenenteImmagineLogo);
     }

    public String getImmagineLogoBase64() {
        return immagineLogoUploader.getLogo_base64();
    }

    @Override
    public String getNomeUploader() {
        return nominativo;
    }

    @Override
    public Map<String, ?> getMappaAttributi_Nome_Valore() {

         Map<String, String> mappa_nomeAttributo_valore = new HashMap<>(2);
         mappa_nomeAttributo_valore.put(Uploader.getNomeFieldNomeUploader(), getNomeUploader());
         mappa_nomeAttributo_valore.put(Uploader.getNomeFieldLogoUploader(), getImmagineLogoBase64());

         return mappa_nomeAttributo_valore;

    }

    public byte[] getImmagineLogoBytes() {
        return immagineLogoUploader.getLogo();
    }

    /** Restituisce true se la modifica del logo va a buon fine, false altrimenti.*/
    public boolean modificalogo(File nuovaImmagineLogo) {
        // TODO
        return true;    // todo
    }

    /** Crea un {@link Consumer} con le proprietà specificate nei parametri.*/
    public Consumer creaConsumer(String username, String nominativo, String email) {
        return new ConsumerStorage(username, nominativo, email);    // TODO
    }

    /** Restituisce true se la modifica del {@link Consumer va a buon fine, false altrimenti.*/
    public boolean modificaConsumer(Consumer consumerModificato) {
        // TODO
        return true;    // todo
    }

    /** Restituisce true se l'eliminazione del {@link Consumer va a buon fine, false altrimenti.*/
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

