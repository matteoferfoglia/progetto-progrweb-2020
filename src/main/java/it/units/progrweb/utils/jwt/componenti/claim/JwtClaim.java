package it.units.progrweb.utils.jwt.componenti.claim;

/**
 * Rappresentazione di un claim nel payload di un token JWT.
 */
public class JwtClaim<TipoValoreClaim> {

    /** Nome del claim corrispondente all' "Expiration Time".*/
    public static final String JWT_EXPIRATION_TIME_CLAIM_NAME = "exp";

    /** Nome del claim corrispondente al "Subject".*/
    public static final String JWT_SUBJECT_CLAIM_NAME = "sub";

    private final String name;
    private TipoValoreClaim value;

    public JwtClaim(String name, TipoValoreClaim value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public TipoValoreClaim getValue() {
        return value;
    }

    public void setValue(TipoValoreClaim value) {
        this.value = value;
    }
}

