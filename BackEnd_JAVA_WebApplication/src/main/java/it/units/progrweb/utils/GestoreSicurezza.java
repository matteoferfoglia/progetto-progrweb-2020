package it.units.progrweb.utils;

import it.units.progrweb.EnvironmentVariables;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.security.*;

/**
 * Gestore di sicurezza per questo server.
 *
 * @author Matteo Ferfoglia
 */
public class GestoreSicurezza {

    /** Percorso del file sul server contenente le chiavi del server. */
    private final static String PERCORSO_CHIAVI_SERVER = "WEB-INF/credenziali/chiaviServer.json";

    /** Chiave segreta di questo server usata in {@link #hmacSha256(String)},
     * generata con algoritmo RSA a 1024 bit.*/
    private static String CHIAVE_SEGRETA_SERVER_HS256;
    static {
        try {
            CHIAVE_SEGRETA_SERVER_HS256 = JsonHelper.convertiOggettoDaJSON(
                    UtilitaGenerale.leggiContenutoFile(PERCORSO_CHIAVI_SERVER),
                    CoppiaChiaviPubblicaPrivata.class
            ).getPrivateKey();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** Classe di comodo per la rappresentazione di una coppia di chiavi
     * pubblica e privata (usato per deserializzazione). */
    @SuppressWarnings("unused") // classe di comodo usata per de/serializzazione
    static class CoppiaChiaviPubblicaPrivata {
        /** Chiave privata */
        private String privateKey;

        /** Chiave pubblica */
        private String publicKey;

        public String getPrivateKey() {
            return privateKey;
        }

        public String getPublicKey() {
            return publicKey;
        }
    }


    /**
     * Algoritmo di hashing HMAC+SHA256, da usare per firmare i dati.
     * Fonte: https://metamug.com/article/security/jwt-java-tutorial-create-verify.html
     * @param datiDaFirmare - i dati da firmare con la chiave privata.
     * @return stringa ottenuta dal calcolo dell'hash.
     */
    public static String hmacSha256(String datiDaFirmare)
            throws NoSuchAlgorithmException, InvalidKeyException {
        return hmacSha256(datiDaFirmare, CHIAVE_SEGRETA_SERVER_HS256);
    }

    /** Come {@link #hmacSha256(String)}, ma qui si fornisce l'hash
     * da utilizzare per firmare i dati. */
    public static String hmacSha256(String datiDaFirmare, String chiaveDaUsarePerFirmare)
            throws NoSuchAlgorithmException, InvalidKeyException {

        byte[] chiave = chiaveDaUsarePerFirmare.getBytes(EnvironmentVariables.STANDARD_CHARSET);

        Mac sha256Hmac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKey = new SecretKeySpec(chiave, "HmacSHA256");
        sha256Hmac.init(secretKey);

        byte[] signedBytes = sha256Hmac.doFinal(datiDaFirmare.getBytes(EnvironmentVariables.STANDARD_CHARSET));

        return Base64Helper.encodeToBase64UrlEncoded(signedBytes);

    }

}
