package it.units.progrweb.entities.attori.nonAdministrator.uploader;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Matteo Ferfoglia
 * Creato il 12 gennaio 2021 (18:01)
 */
public class UploaderProxy extends Uploader {

    /** Immagine logo dell'uploader codificato in base 64.*/
    private final String logoUploaderBase64;

    /** Vedere {@link Uploader#getMappaAttributi_Nome_Valore()} .*/
    private final Map<String, ?> mappaAttributiUploader_nome_valore;

    public UploaderProxy(UploaderStorage uploaderStorage) {
        this.logoUploaderBase64 = uploaderStorage.getImmagineLogoBase64();
        this.mappaAttributiUploader_nome_valore = uploaderStorage.getMappaAttributi_Nome_Valore();
    }

    public UploaderProxy( String username, String nomeUploader, String emailUploader, String logoBase64 ) {
        // TODO : cancellare questo metodo, utilizzato solo in sviluppo per fare delle prove

        super(username,nomeUploader,emailUploader,logoBase64.getBytes(StandardCharsets.UTF_8), "png");
        this.logoUploaderBase64 = logoBase64;
        this.mappaAttributiUploader_nome_valore = new HashMap<>();

        // TODO  : cancellare !!
    }

    @Override
    public String getImmagineLogoBase64() {
        return logoUploaderBase64;
    }

    @Override
    public Map<String, ?> getMappaAttributi_Nome_Valore() {
        return mappaAttributiUploader_nome_valore;
    }

}
