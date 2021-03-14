package it.units.progrweb.utils;

import it.units.progrweb.EnvironmentVariables;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
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

    /** Chiave segreta di questo server usata in {@link #hmacSha256(String)},
     * generata con algoritmo RSA a 1024 bit, vedere
     * {@link #generaChiave(String, int)}*/
    private final static String CHIAVE_SEGRETA_SERVER_HS256 = "MIICeAIBADANBgkqhkiG9w0BAQEFAASCAmIwggJeAgEAAoGBAKwvp_-oaEHmBnpT4eHB-vV10CbeP0rKh4AVSoPWJvJ3_fUxNslHDBeV_G_9rS5SFn57w4TCyvIU6e2-CgcARq-nEL3bN9Oq5cA2tsXzD2YbRUfpK4_KDVexiC2NLp9SFETx_zkubOthkF5Ao3xzN7yI1FklS76q77ZRZ0ScFQrbAgMBAAECgYEApAM5CYOG286aOQeR0BOQUyOHxJ5Kt5k3fL_LHM1uh-PYWigowY0VbZoGvT5sKgUzPAPz94_89J8LPNSahJS0vqerxqIC_Yj1TAT_qFq_ncTkSqkVbz-SHy6DuUKUNTex5QLfIPCD4_VD2M-lEFfgPMTbWWcSFqjTwDBgllaeR6ECQQDfX-mO8bLqqo2RTDGab6n46iKSvMqrX25g23lmu-1KmimNzzzfwTSUv9ni-1R-R84VFDJkb42O5_xbzqZmq9OJAkEAxVXH5dglFOTjIR2ohU6X31K9rcOdhqvh6iIUTkkyEyCGZ3WYIhEC7opu4SOVa9GTA9Z3mI0BY2AL5NhXpPW-QwJBALhVy0UmWpLjam5kZW7gBXGfriZP3CRuXYVauSW5ogn1jKM1STQRmdXDOQjihYissvmcMDXIBRbQhOYydAm4dJkCQQCcc3kLwkUL_rgQjkoIfposJZZaiKeAmQ-AqEo_EwsPXQ8SQYo_IAuaAckM2EBj_gE33rZtDQYXupNTeS5ri6WvAkBfWGvoArqyepLBuWh_jGBuRDUIBJo7x4aRNIVkufRFaBDlBjWNE9eZIfaX9CCgLck5w6oNCkQmLiI092Qucryx";


    /** Lunghezza (numero di bit) delle chiavi.
     * Vedere il parametro "keysize" in {@link java.security.KeyPairGenerator#initialize(int)}.*/
    private final static int KEY_NUMBER_OF_BITS = 1024;

    private PublicKey publicKey;
    private PrivateKey privateKey;

    private final static GestoreSicurezza gestoreSicurezza = new GestoreSicurezza();

    private GestoreSicurezza() {
        this(CIPHERING_ALGORITHM, KEY_NUMBER_OF_BITS);
    }

    private GestoreSicurezza(String algoritmo, int lunghezzaChiaviInNumeroDiBit) {
        try{
            // TODO : rivedere questo metodo (la funzione KeyPairGenerator.getInstance() richiederebbe anche un provider)
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance(algoritmo);
            keyGen.initialize(lunghezzaChiaviInNumeroDiBit); // TODO: cosa fa questo metodo?
            KeyPair keyPair = keyGen.generateKeyPair();
            this.publicKey = keyPair.getPublic();
            this.privateKey = keyPair.getPrivate();
        } catch(NoSuchAlgorithmException e) {
            Logger.scriviEccezioneNelLog(this.getClass(),
                                "Eccezione nella creazione della coppia di chiavi pubblica e privata: ", e);
        }
    }

    public static PrivateKey getPrivateKey() {
        return gestoreSicurezza.privateKey;
    }
    public static PublicKey getPublicKey() {
        return gestoreSicurezza.publicKey;
    }

    /** Genera una chiave crittografica, con algoritmo e lunghezza specificati nei parametri.*/
    public static String generaChiave(String algoritmoCifratura, int lunghezzaInNumeroDiBit){
        GestoreSicurezza gs = new GestoreSicurezza(algoritmoCifratura, lunghezzaInNumeroDiBit);
        return Base64Helper.encodeToBase64UrlEncoded(gs.privateKey.getEncoded());
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
    public static String hmacSha256(String datiDaFirmare, String hashDaUsarePerFirmare)
            throws NoSuchAlgorithmException, InvalidKeyException {

        byte[] hash = hashDaUsarePerFirmare.getBytes(EnvironmentVariables.STANDARD_CHARSET);

        Mac sha256Hmac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKey = new SecretKeySpec(hash, "HmacSHA256");
        sha256Hmac.init(secretKey);

        byte[] signedBytes = sha256Hmac.doFinal(datiDaFirmare.getBytes(EnvironmentVariables.STANDARD_CHARSET));

        return Base64Helper.encodeToBase64UrlEncoded(signedBytes);

    }

}
