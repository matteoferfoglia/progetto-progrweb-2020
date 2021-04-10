package it.units.progrweb.entities.attori.uploader;

import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Matteo Ferfoglia
 * Creato il 12 gennaio 2021 (19:43)
 */
class LogoUploader implements Serializable {

    /** Dimensione massima ammessa per l'imagine logo.
     * Nota: un'entry del Datastore di Google può avere
     * dimensione massima complessiva di 1 MB.*/
    public static final long DIMENSIONE_MASSIMA_LOGO_IN_BYTE = 512*1024;

    /**
     * File contenente il logo, rappresentato come array di byte.
     */
    private byte[] logo;  // byte[] automaticamente convertito in Blob nel datastore

    /**
     * Estensione del file contenente il logo, ad esempio "JPEG".
     */
    private String estensioneFileContenenteLogo;

    /**
     * Costruttore.
     * @param logoInBytes Array di byte rappresentante il logo.
     * @param estensioneFileContenenteLogo Estensione del logo: poiché verrà
     *                                     salvato un array di byte, è necessario
     *                                     salvare anche l'estensione del file per
     *                                     sapere come interpretare quei byte.
     * @throws IOException Vedere {@link #setLogo(byte[], String)}.
     */
    LogoUploader(byte[] logoInBytes, String estensioneFileContenenteLogo)
            throws IOException {
        this.setLogo( logoInBytes, estensioneFileContenenteLogo );
    }

    /**
     * Restituisce il logo.
     */
    public byte[] getLogo() {
        return logo;
    }

    /**
     * Modifica il logo.
     * @throws IOException Se il logo caricato ha una dimensione superiore
     *                     a quanto specificato in {@link #DIMENSIONE_MASSIMA_LOGO_IN_BYTE}.
     */
    public void setLogo(byte[] immagineLogo_bytes, String estensioneFileContenenteLogo)
            throws IOException {

        if( immagineLogo_bytes == null ) {

            this.logo = null;
            this.estensioneFileContenenteLogo = "";

        } else if( immagineLogo_bytes.length > DIMENSIONE_MASSIMA_LOGO_IN_BYTE ) {
            throw new IOException("La dimensione dell'immagine deve essere inferiore a " +
                    Math.ceil(DIMENSIONE_MASSIMA_LOGO_IN_BYTE / 1024.0) + " KB.");
        } else {
            this.logo = immagineLogo_bytes;
            this.estensioneFileContenenteLogo = estensioneFileContenenteLogo;
        }
    }

    /**
     * Restituisce il mediatype del logo (es. "image/jpeg").
     */
    public String getMediaTypeLogo() {

        // Mappa di conversione Estensione -> MediaType
        Map<String,String> mappa_estensioneFile_mediaType = new HashMap<>();

        // Mappa di conversione, Fonte: https://developer.mozilla.org/en-US/docs/Web/HTTP/Basics_of_HTTP/MIME_types#image_types
        mappa_estensioneFile_mediaType.put("apng", "image/apng");
        mappa_estensioneFile_mediaType.put("avif", "image/avif");
        mappa_estensioneFile_mediaType.put("gif",  "image/gif");
        mappa_estensioneFile_mediaType.put("jpg",  "image/jpeg");
        mappa_estensioneFile_mediaType.put("jpeg", "image/jpeg");
        mappa_estensioneFile_mediaType.put("jfif", "image/jpeg");
        mappa_estensioneFile_mediaType.put("pjpeg","image/jpeg");
        mappa_estensioneFile_mediaType.put("pjp",  "image/jpeg");
        mappa_estensioneFile_mediaType.put("png",  "image/png");
        mappa_estensioneFile_mediaType.put("svg",  "image/svg+xml");
        mappa_estensioneFile_mediaType.put("webp", "image/webp");
        mappa_estensioneFile_mediaType.put("ico", "image/x-icon");

        String estensione = estensioneFileContenenteLogo.toLowerCase();
        String mediaType = mappa_estensioneFile_mediaType.get(estensione);

        return mediaType==null ?    // se null, estensione sconosciuta
                "" : mediaType;
    }

    public String getEstensioneFileContenenteLogo() {
        return estensioneFileContenenteLogo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LogoUploader that = (LogoUploader) o;

        if (!Arrays.equals(logo, that.logo)) return false;
        return estensioneFileContenenteLogo != null ?
                estensioneFileContenenteLogo.equals(that.estensioneFileContenenteLogo) :
                that.estensioneFileContenenteLogo == null;
    }

    @Override
    public int hashCode() {
        int result = Arrays.hashCode(logo);
        result = 31 * result + (estensioneFileContenenteLogo != null ? estensioneFileContenenteLogo.hashCode() : 0);
        return result;
    }
}
