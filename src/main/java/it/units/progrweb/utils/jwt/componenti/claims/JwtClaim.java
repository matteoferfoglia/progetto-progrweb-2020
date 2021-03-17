package it.units.progrweb.utils.jwt.componenti.claims;

import it.units.progrweb.entities.attori.Attore;

/**
 * Rappresentazione di un claims nel payload di un token JWT.
 */
public class JwtClaim<TipoValoreClaim> implements Comparable<JwtClaim<?>> {

    /** Nome del claims corrispondente all' "Expiration Time".*/
    public static final String JWT_EXPIRATION_TIME_CLAIM_NAME = "exp";

    /** Nome del claim corrispondente al "Subject".*/
    public static final String JWT_SUBJECT_CLAIM_NAME = "sub";

    /** Nome del claim corrispondente al nome del subject.*/
    public static final String JWT_NOME_SUBJECT_CLAIM_NAME = "Subject name";

    /** Nome del claim corrispondente allo username del subject.*/
    public static final String JWT_USERNAME_SUBJECT_CLAIM_NAME = "Subject username";

    /** Nome del claim corrispondente al tipo dell'{@link Attore}
     * con cui il possessore del token si sta qualificando.*/
    public static final String JWT_TIPO_ATTORE_CLAIM_NAME = "Tipo attore";

    /** Nome del claim il cui valore è il CSRF-token. */
    public static final String JWT_CSRF_TOKEN_CLAIM_NAME = "CSRF-TOKEN";

    /** Nome del claim il cui valore è l'indirizzo IP
     * del client a cui è stato rilasciato il token CSRF.*/
    public static final String JWT_IP_CLIENT_CLAIM_NAME = "IP-CLIENT";

    /** Nome del claim il cui valore è l'hash del token associato al client
     * il cui token è salvato nel cookie di autenticazione.
     * Vedere il procedimento di autenticazione basato su Cookie e JWT implementato
     * nella classe {@link it.units.progrweb.utils.Autenticazione}.*/
    public static final String JWT_HASH_TOKEN_AUTENTICAZIONE_IN_COOKIE_CLAIM_NAME = "hash-token-autenticazione";

    @Override
    public int compareTo(JwtClaim<?> jwtClaim) {
        return this.getName().compareTo(jwtClaim.getName());
    }

    /** Enum contenente i possibili nomi dei claim. */
    public enum NomeClaim {

        /**Vedere {@link JwtClaim#JWT_EXPIRATION_TIME_CLAIM_NAME}. */
        EXP                       (JWT_EXPIRATION_TIME_CLAIM_NAME),

        /**Vedere {@link JwtClaim#JWT_SUBJECT_CLAIM_NAME}. */
        ID_ATTORE                 (JWT_SUBJECT_CLAIM_NAME),

        /**Vedere {@link JwtClaim#JWT_NOME_SUBJECT_CLAIM_NAME}. */
        NOME_ATTORE               (JWT_NOME_SUBJECT_CLAIM_NAME),

        /**Vedere {@link JwtClaim#JWT_USERNAME_SUBJECT_CLAIM_NAME}. */
        USERNAME_ATTORE           (JWT_USERNAME_SUBJECT_CLAIM_NAME),

        /**Vedere {@link JwtClaim#JWT_TIPO_ATTORE_CLAIM_NAME}. */
        TIPO_ATTORE               (JWT_TIPO_ATTORE_CLAIM_NAME),

        /**Vedere {@link JwtClaim#JWT_CSRF_TOKEN_CLAIM_NAME}. */
        CSRF_TOKEN                (JWT_CSRF_TOKEN_CLAIM_NAME),

        /**Vedere {@link JwtClaim#JWT_IP_CLIENT_CLAIM_NAME}. */
        IP_CLIENT                 (JWT_IP_CLIENT_CLAIM_NAME),

        /**Vedere {@link JwtClaim#JWT_HASH_TOKEN_AUTENTICAZIONE_IN_COOKIE_CLAIM_NAME}. */
        HASH_TOKEN_AUTENTICAZIONE (JWT_HASH_TOKEN_AUTENTICAZIONE_IN_COOKIE_CLAIM_NAME);

        private final String nomeClaim;

        NomeClaim(String nomeClaim) {
            this.nomeClaim = nomeClaim;
        }

        public String nomeClaim() {return nomeClaim;}

    }

    private final String name;
    private TipoValoreClaim value;

    public JwtClaim(String name, TipoValoreClaim value) {
        this.name = name;
        this.value = value;
    }

    /** Copy constructor. */
    public JwtClaim(JwtClaim<TipoValoreClaim> jwtClaim) {
        this( jwtClaim.name, jwtClaim.value);
    }

    public String getName() {
        return name;
    }

    public TipoValoreClaim getValue() {
        return value;
    }

}

