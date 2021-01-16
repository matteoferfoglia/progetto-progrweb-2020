package it.units.progrweb.utils.jwt.componenti.claim;

import it.units.progrweb.entities.attori.Attore;

/**
 * Classe per rappresentare il claim "Tipo Attore".
 * Contiene il tipo di {@link Attore} con cui il
 * possessore del token si sta qualificando.
 */
public class JwtTipoAttoreClaim extends JwtClaim {

    /**
     * Crea un "tipo attore" claim.
     * @param tipoAttore Il tipo di {@link Attore} che
     *                   possiede il token.
     */
    public JwtTipoAttoreClaim(String tipoAttore) {
        super(JwtClaim.JWT_TIPO_ATTORE_CLAIM_NAME, tipoAttore);
    }

}