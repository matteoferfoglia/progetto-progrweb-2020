package it.units.progrweb.entities.attori.uploader;

/**
 * @author Matteo Ferfoglia
 * Creato il 12 gennaio 2021 (18:01)
 */
public class UploaderProxy extends Uploader {

    private final UploaderStorage uploaderStorage;

    public UploaderProxy(UploaderStorage uploaderStorage) {
        super( uploaderStorage );
        this.uploaderStorage = uploaderStorage;
    }

    @Override
    public byte[] getImmagineLogo() {
        return uploaderStorage.getImmagineLogo();
    }

    @Override
    public String getMediatypeImmagineLogo() {
        return uploaderStorage.getMediatypeImmagineLogo();
    }

}
