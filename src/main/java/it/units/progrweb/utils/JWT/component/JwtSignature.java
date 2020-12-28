package it.units.progrweb.utils.JWT.component;

import it.units.progrweb.utils.Base64Helper;
import it.units.progrweb.utils.SecurityManager;

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
     * @throws InvalidKeyException generata da {@link it.units.progrweb.utils.SecurityManager#hmacSha256(String)}
     *  @throws NoSuchAlgorithmException generata da {@link it.units.progrweb.utils.SecurityManager#hmacSha256(String)}
     */
    public JwtSignature(JwtHeader jwtHeader, JwtPayload jwtPayload)
            throws InvalidKeyException, NoSuchAlgorithmException {

        this.signature = SecurityManager.hmacSha256(Base64Helper.encodeToBase64UrlEncoded(jwtHeader.convertiClaimsSetToJson())
                + "."
                + Base64Helper.encodeToBase64UrlEncoded(jwtPayload.convertiClaimsSetToJson()));

    }

    /**
     * Crea un'istanza di questa classe a partire da una firma
     * fornita come stringa.
     */
    public JwtSignature(String signature) {
        this.signature = signature;
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

    @Override
    public int hashCode() {
        return signature != null ? signature.hashCode() : 0;
    }
}
