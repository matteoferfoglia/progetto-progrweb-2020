package it.units.progrweb.utils.jwt.componenti.claims;

import it.units.progrweb.entities.attori.Attore;

/**
 * Classe per rappresentare il claims "Tipo Attore".
 * Contiene il tipo di {@link Attore} con cui il
 * possessore del token si sta qualificando.
 */
public class JwtTipoAttoreClaim extends JwtClaim<Attore.TipoAttore> {

    /**
     * Crea un "tipo attore" claims.
     * @param tipoAttore Il {@link Attore.TipoAttore} di {@link Attore} che
     *                   possiede il token.
     */
    public JwtTipoAttoreClaim(Attore.TipoAttore tipoAttore) {
        super(JwtClaim.JWT_TIPO_ATTORE_CLAIM_NAME, tipoAttore);
    }

}