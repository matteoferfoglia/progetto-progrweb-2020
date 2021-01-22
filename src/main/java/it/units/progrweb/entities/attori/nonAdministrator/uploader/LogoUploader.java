package it.units.progrweb.entities.attori.nonAdministrator.uploader;

import it.units.progrweb.utils.Base64Helper;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Matteo Ferfoglia
 * Creato il 12 gennaio 2021 (19:43)
 */
class LogoUploader {

    /**
     * File contenente il logo, rappresentato come array di byte.
     */
    private byte[] logo;  // byte[] automaticamente convertito in Blob nel datastore

    /**
     * Estensione del file contenente il logo, ad esempio "JPEG".
     */
    private String estensioneFileContenenteLogo;

    LogoUploader(byte[] logoInBytes, String estensioneFileContenenteLogo) {
        this.logo = logoInBytes;
        this.estensioneFileContenenteLogo = estensioneFileContenenteLogo;
    }

    /**
     * Restituisce la rappresentazione Base64 dell'immagine, come Data URL
     * (<a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Basics_of_HTTP/Data_URIs">Fonte</a>).
     */
    public String getLogo_base64() {
        return "data:" + getMediaTypeLogo() + ";base64," + Base64Helper.encodeToBase64UrlEncoded(logo); // TODO : verificare che sia corretto usare la versione url encoded del base64
    }

    /**
     * Restituisce il logo.
     */
    public byte[] getLogo() {
        return logo;
    }

    /**
     * Modifica il logo.
     */
    public void setLogo(byte[] immagineLogo_bytes, String estensioneFileContenenteLogo) {
        this.logo = immagineLogo_bytes;
        this.estensioneFileContenenteLogo = estensioneFileContenenteLogo;
    }

    /**
     * Restituisce il mediatype del logo (es. "image/jpeg").
     */
    public String getMediaTypeLogo() {

        // Mappa di conversione Estensione -> MediaType
        Map<String,String> mappa_estensioneFile_mediaType = new HashMap<String, String>() {{
            // Inizializzazione mappa, Fonte: https://stackoverflow.com/a/6802502

            // Tabella di conversione, Fonte: https://developer.mozilla.org/en-US/docs/Web/HTTP/Basics_of_HTTP/MIME_types#image_types
            put("apng", "image/apng");
            put("avif", "image/avif");
            put("gif",  "image/gif");
            put("jpg",  "image/jpeg");
            put("jpeg", "image/jpeg");
            put("jfif", "image/jpeg");
            put("pjpeg","image/jpeg");
            put("pjp",  "image/jpeg");
            put("png",  "image/png");
            put("svg",  "image/svg+xml");
            put("webp", "image/webp");
        }};

        String estensione = estensioneFileContenenteLogo.toLowerCase();
        String mediaType = mappa_estensioneFile_mediaType.get(estensione);

        return mediaType==null ?    // se null, estensione sconosciuta
                "" : mediaType;
    }

}
