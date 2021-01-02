package it.units.progrweb.utils.jwt.componenti;

import it.units.progrweb.utils.GestoreSicurezza;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * Classe per la gestione della signature di un token JWT
 * (<a href="https://metamug.com/article/security/jwt-java-tutorial-create-verify.html">Fonte</a>).
 */
public class JwtSignature {

    private final String signature;
    private final static String algoritmoHash  = "HS256";

    /**
     * Crea la signature per il token JWT a partire dai suoi header e payload.
     * @throws InvalidKeyException generata da {@link GestoreSicurezza#hmacSha256(String)}
     *  @throws NoSuchAlgorithmException generata da {@link GestoreSicurezza#hmacSha256(String)}
     */
    public JwtSignature(JwtHeader jwtHeader, JwtPayload jwtPayload)
            throws InvalidKeyException, NoSuchAlgorithmException {

        this.signature = GestoreSicurezza.hmacSha256(jwtHeader.getClaimsSetInFormatJsonBase64UrlEncoded()
                + "." + jwtPayload.getClaimsSetInFormatJsonBase64UrlEncoded() );

    }

    /**
     * Crea un'istanza di questa classe a partire da una firma
     * fornita come stringa.
     */
    public JwtSignature(String signature) {
        this.signature = signature;
    }

    /** Restituisce una nuova istanza di questa classe, copia di quella data nel parametro.*/
    public JwtSignature(JwtSignature signatureDaCopiare) {
        this.signature = signatureDaCopiare.signature;
    }

    public String getSignature() {
        return signature;
    }

    public static String getAlgoritmoHash() {
        return algoritmoHash;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        return signature.equals(((JwtSignature) o).signature);
    }

}
