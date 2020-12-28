package it.units.progrweb.utils.jwt.componenti;

/**
 * Classe per la rappresentazione del payload di un token JWT.
 */
public class JwtPayload extends JwtClaimsSet{

    public JwtPayload() {
        super();
    }

    public JwtPayload(JwtClaimsSet jwtClaimsSet) {
        super(jwtClaimsSet);
    }

}
