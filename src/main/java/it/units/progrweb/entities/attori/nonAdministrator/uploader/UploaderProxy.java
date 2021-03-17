package it.units.progrweb.entities.attori.nonAdministrator.uploader;

/**
 * @author Matteo Ferfoglia
 * Creato il 12 gennaio 2021 (18:01)
 */
public class UploaderProxy extends Uploader {

    public UploaderProxy(UploaderStorage uploaderStorage) {
        super( uploaderStorage );
    }

    @Override
    public byte[] getImmagineLogo() {
        return new byte[0];
    }

    @Override
    public String getEstensioneImmagineLogo() {
        return null;
    }

    @Override
    public String getMediatypeImmagineLogo() { return null; }

}
