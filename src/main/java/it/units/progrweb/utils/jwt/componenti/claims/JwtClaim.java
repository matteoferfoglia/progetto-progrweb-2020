package it.units.progrweb.utils.jwt.componenti.claims;

import it.units.progrweb.entities.attori.Attore;

/**
 * Rappresentazione di un claims nel payload di un token JWT.
 */
public class JwtClaim<TipoValoreClaim> {

    /** Nome del claims corrispondente all' "Expiration Time".*/
    public static final String JWT_EXPIRATION_TIME_CLAIM_NAME = "exp";

    /** Nome del claims corrispondente al "Subject".*/
    public static final String JWT_SUBJECT_CLAIM_NAME = "sub";

    /** Nome del claims corrispondente al nome del subject.*/
    public static final String JWT_NOME_SUBJECT_CLAIM_NAME = "Subject name";

    /** Nome del claims corrispondente al tipo dell'{@link Attore}
     * con cui il possessore del token si sta qualificando.*/
    public static final String JWT_TIPO_ATTORE_CLAIM_NAME = "Tipo attore";

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

    @SuppressWarnings("unchecked")
    public void setValue(Object value) {
        if( this.value.getClass().equals(value.getClass()) )
            this.value = (TipoValoreClaim) value;
    }
}

