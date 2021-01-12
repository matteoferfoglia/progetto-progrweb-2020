package it.units.progrweb.entities.attori.nonAdministrator.uploader;

import it.units.progrweb.utils.Base64Helper;

/**
 * @author Matteo Ferfoglia
 * Creato il 12 gennaio 2021 (19:43)
 */
class LogoUploader {

    /**
     * File contenente il logo, rappresentato come array di byte.
     */
    private final byte[] logo;  // byte[] automaticamente convertito in Blob nel datastore

    /**
     * Estensione del file contenente il logo, ad esempio "JPEG".
     */
    private final String estensioneFileContenenteLogo;

    LogoUploader(byte[] logoInBytes, String estensioneFileContenenteLogo) {
        this.logo = logoInBytes;
        this.estensioneFileContenenteLogo = estensioneFileContenenteLogo;
    }

    /**
     * Restituisce la rappresentazione Base64 dell'immagine, come stringa.
     */
    public String getLogo_base64() {
        return "data:" + getMediaTypeLogo() + ";base64," + Base64Helper.encodeToBase64UrlEncoded(logo);
    }

    /**
     * Restituisce il logo.
     */
    public byte[] getLogo() {
        return logo;
    }

    /**
     * Restituisce il mediatype del logo (es. "image/jpeg").
     */
    public String getMediaTypeLogo() {
        return "image/" + estensioneFileContenenteLogo.toLowerCase();
    }

}
