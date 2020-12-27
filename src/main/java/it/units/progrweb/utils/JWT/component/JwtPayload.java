package it.units.progrweb.utils.JWT.component;

import it.units.progrweb.utils.JWT.component.claim.JwtClaim;

import java.util.List;

/**
 * Classe per la rappresentazione del payload di un token JWT.
 */
public class JwtPayload extends JwtClaimsSet{

    public JwtPayload() {
        super();
    }

    public JwtPayload(JwtClaim singoloJwtClaim) {
        super(singoloJwtClaim);
    }

    public JwtPayload(List<JwtClaim> JwtClaimList) {
        super(JwtClaimList);
    }

    public JwtPayload(JwtClaimsSet jwtClaimsSet) {
        super(jwtClaimsSet);
    }

}
