package it.units.progrweb.utils.jwt.componenti.claims;

import it.units.progrweb.utils.datetime.DateTime;

/**
 * Classe per rappresentare il claims "Expiration Time".
 * <blockquote cite="https://tools.ietf.org/html/rfc7519#section-4.1.4>
 *    The "exp" (expiration time) claims identifies the expiration time on
 *    or after which the JWT MUST NOT be accepted for processing.  The
 *    processing of the "exp" claims requires that the current date-time
 *    MUST be before the expiration date-time listed in the "exp" claims.
 *    Implementers MAY provide for some small leeway, usually no more than
 *    a few minutes, to account for clock skew.  Its value MUST be a number
 *    containing a NumericDate value.  Use of this claims is OPTIONAL.
 * </blockquote>
 */
public class JwtExpirationTimeClaim extends JwtClaim<Long> {

    /**
     * Crea un Expiration Time claims la cui scadenza è impostata
     * tra il numero di secondi specificato come parametro.
     * @param scadenzaInSecondiDaAdesso Numero di secondi dopo i quali
     *                                  il token a cui questo claims si riferisce
     *                                  verrà considerato scaduto.
     */
    public JwtExpirationTimeClaim(int scadenzaInSecondiDaAdesso) {
        super(JwtClaim.JWT_EXPIRATION_TIME_CLAIM_NAME,
                scadenzaInSecondiDaAdesso + DateTime.currentTimeInSecondi());
    }

    /** Dato un {@link JwtClaim}, questo costruttore prova a creare un'istanza di
     * questa classe se il claim fornito è compatibile. */
    public JwtExpirationTimeClaim(JwtClaim<?> jwtClaim) {
        super(JwtClaim.JWT_EXPIRATION_TIME_CLAIM_NAME,
              jwtClaim.getValue() instanceof Long ? (Long)jwtClaim.getValue() : 0);
        if(! ( jwtClaim.getName().equals(JwtClaim.JWT_EXPIRATION_TIME_CLAIM_NAME) &&
               jwtClaim.getValue() instanceof Long ) )
            throw new IllegalArgumentException("Impossibile creare un " + JwtExpirationTimeClaim.class.getName()
                    + ", perché è stato fornito un claims con nome " + jwtClaim.getName());
    }

    /**
     * Verifica se il claims Expiration Time fa riferimento ad una data passata
     * e di conseguenza il token risulta scaduto.
     * In base alle <a href="https://tools.ietf.org/html/rfc7519#section-4.1.4">specifiche</a>,
     * il valore dell' "Expiration Claim" è il numero di secondi trascorsi dal
     * 1970-01-01T00: 00: 00Z UTC fino alla data/ora UTC di scadenza, ignorando
     * i secondi intercalari.
     * @return true se il token a cui questo claims si riferisce è scaduto.
     */
    public boolean isScaduto() {
        long tempoCorrenteInSecondi = DateTime.currentTimeInSecondi();
        long tempoScadenzaQuestoTokenInSecondi = this.getValue();

        return tempoCorrenteInSecondi > tempoScadenzaQuestoTokenInSecondi;
    }

}