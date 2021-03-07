package it.units.progrweb.entities.attori.nonAdministrator.uploader;

import com.googlecode.objectify.annotation.Serialize;
import com.googlecode.objectify.annotation.Subclass;
import it.units.progrweb.entities.attori.Attore;

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
        super();
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

    /** Copy-constructor, usato in {@link Attore#clone()}.
     * Vedere {@link #UploaderStorage(String, String, String, byte[], String)}*/
    private UploaderStorage( UploaderStorage uploader )
            throws IOException {
        this( uploader.username, uploader.nominativo, uploader.email, uploader.immagineLogoUploader.getLogo(),
                uploader.immagineLogoUploader.getEstensioneFileContenenteLogo() );
    }

    public byte[] getImmagineLogoBytes() {
        return immagineLogoUploader.getLogo();
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

