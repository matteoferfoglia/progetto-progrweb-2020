package it.units.progrweb.entities.attori.nonAdministrator.uploader;

import com.googlecode.objectify.annotation.Serialize;
import com.googlecode.objectify.annotation.Subclass;
import it.units.progrweb.entities.attori.nonAdministrator.consumer.Consumer;
import it.units.progrweb.entities.attori.nonAdministrator.consumer.ConsumerProxy;
import it.units.progrweb.entities.file.File;

import java.io.IOException;

/**
 * Classe rappresentante un'entità {@link Uploader}
 * da memorizzare nel database.
 * @author Matteo Ferfoglia
 */
// TODO : serve Proxy pattern?
@Subclass(index = false)
class UploaderStorage extends Uploader {

    @Serialize
    private LogoUploader immagineLogoUploader;

    private UploaderStorage() {
        super();    // TODO
    }

    /** Vedere {@link Uploader}.
     * @param immagineLogo  Array di bytes corrispondenti all'immagine logo di questo UploaderStorage.
     * @param estensioneFileContenenteImmagineLogo  Estensione dell'immagine (per sapere come interpretare l'array di bytes).
     * @throws IOException Se l'immagine ha dimensioni eccessive. Vedere {@link LogoUploader#LogoUploader(byte[], String)}.*/
    public UploaderStorage(String username, String nominativo, String email,
                 byte[] immagineLogo, String estensioneFileContenenteImmagineLogo)
            throws IOException {
        super(username, nominativo, email);
        this.immagineLogoUploader = new LogoUploader(immagineLogo, estensioneFileContenenteImmagineLogo) ;
        setTipoAttore( Uploader.class.getSimpleName() );
    }

    /** Copy-constructor. Vedere {@link #UploaderStorage(String, String, String, byte[], String)}*/
    private UploaderStorage( UploaderStorage uploader )
            throws IOException {
        this( uploader.username, uploader.nominativo, uploader.email, uploader.immagineLogoUploader.getLogo(),
                uploader.immagineLogoUploader.getEstensioneFileContenenteLogo() );
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
        return new ConsumerProxy(username, nominativo, email);    // TODO
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

    /** Modifica l'immagine logo dell'Uploader.*/
    public void setImmagineLogo(byte[] immagineLogo_bytes, String estensioneFileContenenteLogo)
            throws IOException {
        this.immagineLogoUploader.setLogo(immagineLogo_bytes, estensioneFileContenenteLogo);
    }

    @Override
    public byte[] getImmagineLogo() {
        return immagineLogoUploader.getLogo();
    }

    @Override
    public String getEstensioneImmagineLogo() {
        return immagineLogoUploader.getEstensioneFileContenenteLogo();
    }

    @Override
    public String getMediatypeImmagineLogo() {
        return immagineLogoUploader.getMediaTypeLogo();
    }

    /** Restituisce l'immagine logo in formato Base64.*/
    public String getLogo_base64() {
        return immagineLogoUploader.getLogo_dataUrl();
    }

    /** Verifica l'equivalenza del logo, oltre a quanto verificato
     * dal metodo della super classe.*/
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        UploaderStorage uploader = (UploaderStorage) o;

        return immagineLogoUploader != null ?
                immagineLogoUploader.equals(uploader.immagineLogoUploader) :
                uploader.immagineLogoUploader == null;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (immagineLogoUploader != null ? immagineLogoUploader.hashCode() : 0);
        return result;
    }

}

