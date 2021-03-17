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

    /** Chiave segreta di questo server usata in {@link #hmacSha256(String)},
     * generata con algoritmo RSA a 1024 bit.*/
    private final static String CHIAVE_SEGRETA_SERVER_HS256 = "MIICeAIBADANBgkqhkiG9w0BAQEFAASCAmIwggJeAgEAAoGBAKwvp_-oaEHmBnpT4eHB-vV10CbeP0rKh4AVSoPWJvJ3_fUxNslHDBeV_G_9rS5SFn57w4TCyvIU6e2-CgcARq-nEL3bN9Oq5cA2tsXzD2YbRUfpK4_KDVexiC2NLp9SFETx_zkubOthkF5Ao3xzN7yI1FklS76q77ZRZ0ScFQrbAgMBAAECgYEApAM5CYOG286aOQeR0BOQUyOHxJ5Kt5k3fL_LHM1uh-PYWigowY0VbZoGvT5sKgUzPAPz94_89J8LPNSahJS0vqerxqIC_Yj1TAT_qFq_ncTkSqkVbz-SHy6DuUKUNTex5QLfIPCD4_VD2M-lEFfgPMTbWWcSFqjTwDBgllaeR6ECQQDfX-mO8bLqqo2RTDGab6n46iKSvMqrX25g23lmu-1KmimNzzzfwTSUv9ni-1R-R84VFDJkb42O5_xbzqZmq9OJAkEAxVXH5dglFOTjIR2ohU6X31K9rcOdhqvh6iIUTkkyEyCGZ3WYIhEC7opu4SOVa9GTA9Z3mI0BY2AL5NhXpPW-QwJBALhVy0UmWpLjam5kZW7gBXGfriZP3CRuXYVauSW5ogn1jKM1STQRmdXDOQjihYissvmcMDXIBRbQhOYydAm4dJkCQQCcc3kLwkUL_rgQjkoIfposJZZaiKeAmQ-AqEo_EwsPXQ8SQYo_IAuaAckM2EBj_gE33rZtDQYXupNTeS5ri6WvAkBfWGvoArqyepLBuWh_jGBuRDUIBJo7x4aRNIVkufRFaBDlBjWNE9eZIfaX9CCgLck5w6oNCkQmLiI092Qucryx";


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
