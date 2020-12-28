package it.units.progrweb.utils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.*;

/**
 * Gestore delle chiavi privata e pubblica per questo server.
 * Fonti: <a href="https://docs.oracle.com/javase/tutorial/security/apisign/step1.html">Fonte</a>,
 * <a href="https://docs.oracle.com/javase/tutorial/security/apisign/step2.html">Fonte</a>.
 *
 * @author Matteo Ferfoglia
 */
public class GestoreSicurezza {

    /** Algoritmo di cifratura da usare.
     * <a href="https://docs.oracle.com/javase/8/docs/technotes/guides/security/StandardNames.html#KeyPairGenerator">Lista degli algoritmi possibili</a>
     * ("DiffieHellman", "DSA", "RSA", "RSASSA-PSS", "EC").
     * Vedere il parametro "algorithm" in {@link java.security.KeyPairGenerator#getInstance(String)} */
    private final static String CIPHERING_ALGORITHM = "RSA";

    /** Lunghezza (numero di bit) delle chiavi.
     * Vedere il parametro "keysize" in {@link java.security.KeyPairGenerator#initialize(int)}.*/
    private final static int KEY_NUMBER_OF_BITS = 1024;

    private PublicKey publicKey;
    private PrivateKey privateKey;

    private final static GestoreSicurezza gestoreSicurezza = new GestoreSicurezza();   // TODO: da creare durante bootstrap del server

    private GestoreSicurezza() {
        try{
            // TODO : rivedere questo metodo (la funzione KeyPairGenerator.getInstance() richiederebbe anche un provider)
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance(CIPHERING_ALGORITHM);
            keyGen.initialize(KEY_NUMBER_OF_BITS); // TODO: cosa fa questo metodo?
            KeyPair keyPair = keyGen.generateKeyPair();
            this.publicKey = keyPair.getPublic();
            this.privateKey = keyPair.getPrivate();
        } catch(NoSuchAlgorithmException e) {
            System.err.println("Eccezione nella creazione della coppia di chiavi pubblica e privata: " + e.toString()); //TODO qua ci vuole il log
        }
    }

    public static PrivateKey getPrivateKey() {
        return gestoreSicurezza.privateKey;
    }
    public static PublicKey getPublicKey() {
        return gestoreSicurezza.publicKey;
    }

    /**
     * Algoritmo di hashing HMAC+SHA256, da usare per firmare i dati.
     * Fonte: https://metamug.com/article/security/jwt-java-tutorial-create-verify.html
     * @param datiDaFirmare - i dati da firmare con la chiave privata.
     * @return stringa ottenuta dal calcolo dell'hash.
     */
    public static String hmacSha256(String datiDaFirmare)
            throws NoSuchAlgorithmException, InvalidKeyException {

        byte[] hash = gestoreSicurezza.privateKey.getEncoded();

        Mac sha256Hmac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKey = new SecretKeySpec(hash, "HmacSHA256");
        sha256Hmac.init(secretKey);

        byte[] signedBytes = sha256Hmac.doFinal(datiDaFirmare.getBytes(StandardCharsets.UTF_8));

        return Base64Helper.encodeToBase64UrlEncoded(signedBytes);

    }

}
