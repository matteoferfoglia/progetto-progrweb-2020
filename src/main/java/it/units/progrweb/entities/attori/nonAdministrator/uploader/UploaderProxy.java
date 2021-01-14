package it.units.progrweb.entities.attori.nonAdministrator.uploader;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Matteo Ferfoglia
 * Creato il 12 gennaio 2021 (18:01)
 */
public class UploaderProxy extends Uploader {

    /** Nome dell'uploader.*/
    private final String nomeUploader;

    /** Immagine logo dell'uploader codificato in base 64.*/
    private final String logoUploaderBase64;

    /** Vedere {@link Uploader#getMappaAttributi_Nome_Valore()} .*/
    private final Map<String, ?> mappaAttributiUploader_nome_valore;

    public UploaderProxy(UploaderStorage uploaderStorage) {
        this.nomeUploader = uploaderStorage.getNomeCognome();
        this.logoUploaderBase64 = uploaderStorage.getImmagineLogoBase64();
        this.mappaAttributiUploader_nome_valore = uploaderStorage.getMappaAttributi_Nome_Valore();
    }

    public UploaderProxy( String nomeUploader, String emailUploader, String logoBase64 ) {
        // TODO : cancellare questo metodo, utilizzato solo in sviluppo per fare delle prove

        this.nomeUploader = nomeUploader;
        super.email = emailUploader;
        this.logoUploaderBase64 = logoBase64;
        this.mappaAttributiUploader_nome_valore = new HashMap<>();
    }

    @Override
    public String getImmagineLogoBase64() {
        return logoUploaderBase64;
    }

    @Override
    public String getNomeUploader() {
        return nomeUploader;
    }

    @Override
    public Map<String, ?> getMappaAttributi_Nome_Valore() {
        return mappaAttributiUploader_nome_valore;
    }

}
