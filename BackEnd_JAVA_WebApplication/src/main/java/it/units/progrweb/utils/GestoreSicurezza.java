package it.units.progrweb.utils;

import it.units.progrweb.EnvironmentVariables;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.security.*;

/**
 * Gestore di sicurezza per questo server.
 *
 * @author Matteo Ferfoglia
 */
public class GestoreSicurezza {

    /** Percorso del file sul server contenente le chiavi del server. */
    private final static String PERCORSO_CHIAVI_SERVER = "WEB-INF/credenziali/chiaviServer.json";   // nella cartella target

    /** Coppia di chiavi pubblica/privata di questo server.*/
    private static CoppiaChiaviPubblicaPrivata COPPIA_CHIAVI_SERVER;
    static {
        try {
            COPPIA_CHIAVI_SERVER = JsonHelper.convertiOggettoDaJSON(
                    UtilitaGenerale.leggiContenutoFile(PERCORSO_CHIAVI_SERVER),
                    CoppiaChiaviPubblicaPrivata.class
            );
        } catch (IOException e) {
            Logger.scriviEccezioneNelLog(
                    GestoreSicurezza.class,
                    "Errore nella lettura delle chiavi pubblica/privata.",
                    e
            );
        }
    }

    /** Classe di comodo per la rappresentazione di una coppia di chiavi
     * pubblica e privata.
     * Nota: vedere i metodi setter.*/
    static class CoppiaChiaviPubblicaPrivata {

        // Stringhe usate per de/serializzare l'oggetto
        // Array di bytes costituiscono la chiave "vera"

        /** Chiave privata (bytes)*/
        transient private byte[] privateKey_bytes;  // non serializzata

        /** Chiave pubblica (bytes) */
        transient private byte[] publicKey_bytes;   // non serializzata

        /** Chiave privata rappresentata come {@link String}. */
        private String privateKey;

        /** Chiave pubblica rappresentata come {@link String}. */
        private String publicKey;

        @SuppressWarnings("unused") // usato per de/serializzazione dell'oggetto
        private CoppiaChiaviPubblicaPrivata() {}

        public CoppiaChiaviPubblicaPrivata(byte[] privateKey, byte[] publicKey) {
            this.privateKey_bytes = privateKey;
            this.publicKey_bytes  = publicKey;
            this.privateKey       = getPrivateKey();
            this.publicKey        = getPublicKey();
        }

        /** Restituisce l'array di byte costituente la chiave privata. */
        public byte[] getPrivateKey_getBytes() {
            if( privateKey_bytes==null ) {
                privateKey_bytes = Base64Helper.decodeFromBase64UrlEncodedToString(privateKey)
                                               .getBytes(EnvironmentVariables.STANDARD_CHARSET);
            }
            return privateKey_bytes;
        }

        /** Restituisce l'array di byte costituente la chiave pubblica. */
        public byte[] getPublicKey_getBytes() {
            if( publicKey_bytes==null ) {
                publicKey_bytes = Base64Helper.decodeFromBase64UrlEncodedToString(publicKey)
                                              .getBytes(EnvironmentVariables.STANDARD_CHARSET);
            }
            return publicKey_bytes;
        }

        /** Restituisce la chiave privata codificata in Base64UrlEncoded. */
        public String getPrivateKey() {
            return Base64Helper.encodeToBase64UrlEncoded(privateKey_bytes);
        }

        public String getPublicKey() {
            return Base64Helper.encodeToBase64UrlEncoded(publicKey_bytes);
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
        return hmacSha256(datiDaFirmare, COPPIA_CHIAVI_SERVER.getPrivateKey_getBytes());
    }

    /** Come {@link #hmacSha256(String, byte[])}, ma qui si fornisce la chiave
     * da utilizzare per firmare i dati in formato {@link String}. */
    public static String hmacSha256(String datiDaFirmare, String chiaveDaUsarePerFirmare)
            throws NoSuchAlgorithmException, InvalidKeyException {
        return hmacSha256(datiDaFirmare, chiaveDaUsarePerFirmare.getBytes(EnvironmentVariables.STANDARD_CHARSET));
    }

    /** Come {@link #hmacSha256(String)}, ma qui si fornisce la chiave
     * da utilizzare per firmare i dati. */
    public static String hmacSha256(String datiDaFirmare, byte[] chiaveDaUsarePerFirmare)
            throws NoSuchAlgorithmException, InvalidKeyException {

        Mac sha256Hmac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKey = new SecretKeySpec(chiaveDaUsarePerFirmare, "HmacSHA256");
        sha256Hmac.init(secretKey);

        byte[] signedBytes = sha256Hmac.doFinal(datiDaFirmare.getBytes(EnvironmentVariables.STANDARD_CHARSET));

        return Base64Helper.encodeToBase64UrlEncoded(signedBytes);

    }

    /** Metodo per la generazione di una coppia di chiavi pubblica/privata,
     * usando i parametri di default.
     * Vedere {@link #generaCoppiaChiaviPubblicaPrivata(String, int)} per i dettagli.*/
    @SuppressWarnings("unused") // metodo mantenuto per utilità (potrebbe essere necessario generare le chiavi per questo server)
    public static CoppiaChiaviPubblicaPrivata generaCoppiaChiaviPubblicaPrivata()
            throws NoSuchAlgorithmException {

        final String ALGORITMO_GENERAZIONE_CHIAVI_DEFAULT = "RSA";
        final int LUNGHEZZA_CHIAVI_DEFAULT = 1024;  // numero di bit che compongono la chiave

        return generaCoppiaChiaviPubblicaPrivata(ALGORITMO_GENERAZIONE_CHIAVI_DEFAULT, LUNGHEZZA_CHIAVI_DEFAULT);

    }

    /** Metodo per la generazione di una coppia di chiavi pubblica/privata.
     * I seguenti algoritmi sono validi:
     * <ul>
     *     <li>DiffieHellman (1024)</li>
     *     <li>DSA (1024)</li>
     *     <li>RSA (1024, 2048)</li>
     * </ul>
     * @param algoritmoGenerazioneChiavi Algoritmo da utilizzare per la generazione delle chiavi.
     * @param lunghezzaChiavi Lunghezza (numero di bit) delle chiavi da generare.
     * @throws NoSuchAlgorithmException In caso di errore nell'algoritmo scelto.*/
    public static CoppiaChiaviPubblicaPrivata generaCoppiaChiaviPubblicaPrivata(String algoritmoGenerazioneChiavi, int lunghezzaChiavi)
            throws NoSuchAlgorithmException {

        KeyPairGenerator keyGen = KeyPairGenerator.getInstance(algoritmoGenerazioneChiavi);
        keyGen.initialize(lunghezzaChiavi);
        KeyPair keyPair = keyGen.generateKeyPair();

        return new CoppiaChiaviPubblicaPrivata(keyPair.getPrivate().getEncoded(), keyPair.getPublic().getEncoded());

    }

    /** Crea (o sovrascrive se già esistente) un file al percorso indicato e vi
     * salva all'interno una coppia di chiavi pubblica/privata generata.
     * <strong>Importante</strong>: Il file <strong>non</strong> può trovarsi nella
     * cartella della webapp.
     * @param percorsoFile Percorso del file di destinazione.
     * @return La stessa stringa scritta su file.
     * @throws IOException in caso di errori di I/O con il file di destinazione.
     * @throws NoSuchAlgorithmException in caso di errori nella generazione della coppia di chiavi.
     */
    @SuppressWarnings("unused") // metodo di utilità (invocabile es. da debugger per creare una nuova coppia di chiavi pubblica/privata per questo server)
    private static String salvaCoppiaChiaviPubblicaPrivataSuFile(String percorsoFile)
            throws NoSuchAlgorithmException, IOException {

        File fileCredenziali = new File(percorsoFile);

        //noinspection ResultOfMethodCallIgnored    // createNewFile() returns true if the named file does not exist and was successfully created; false if the named file already exists
        fileCredenziali.createNewFile();            // non fa nulla se il file esiste già

        String contenutoFile = JsonHelper.convertiOggettoInJSON(generaCoppiaChiaviPubblicaPrivata());

        PrintWriter printWriter = new PrintWriter(fileCredenziali);
        printWriter.write( contenutoFile );
        printWriter.close();

        return contenutoFile;

    }

}