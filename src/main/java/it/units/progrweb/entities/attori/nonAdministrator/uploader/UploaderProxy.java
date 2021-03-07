package it.units.progrweb.entities.attori.nonAdministrator.uploader;

import it.units.progrweb.utils.Logger;

/**
 * @author Matteo Ferfoglia
 * Creato il 12 gennaio 2021 (18:01)
 */
public class UploaderProxy extends Uploader {

    public UploaderProxy(UploaderStorage uploaderStorage) {
        super( uploaderStorage );
    }

    /** Restituisce il nome dell'attributo di questa classe con il logo dell'{@link Uploader}.*/
    public static String getNomeFieldLogoUploader() {
        final String NOME_FIELD_LOGO = "logoUploaderBase64";

        // Verifica che il field esista
        try {
            UploaderProxy.class.getDeclaredField( NOME_FIELD_LOGO );
        } catch (NoSuchFieldException e) {
            // Se viene modificato il nome dell'attributo, ci si accorge subito dal log
            Logger.scriviEccezioneNelLog( UploaderProxy.class,
                                          "Forse è stato modificato il nome dell'attributo contenente il logo",
                                          e);
        }

        return NOME_FIELD_LOGO;
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
