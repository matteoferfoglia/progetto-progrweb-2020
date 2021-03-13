package it.units.progrweb.utils.jwt.componenti;

import it.units.progrweb.utils.jwt.componenti.claim.JwtClaim;

import java.util.ArrayList;
import java.util.Collection;

/** Classe per la rappresentazione del JWT header. */
public class JwtHeader extends JwtClaimsSet {

    public JwtHeader(String algoritmo) {
        super(new JwtHeaderClaims(algoritmo).toCollection());
    }

    public JwtHeader(JwtClaimsSet jwtClaimsSet) {
        super(jwtClaimsSet);
    }

}

class JwtHeaderClaims {
    private final JwtClaim<?> algoritmoHash;
    private final JwtClaim<?> tipoToken;

    public JwtHeaderClaims(String algoritmoHash) {
        this.algoritmoHash = new JwtClaim<>("alg", algoritmoHash);
        this.tipoToken = new JwtClaim<>("typ", "JWT");
    }

    public Collection<JwtClaim<?>> toCollection() {
        Collection<JwtClaim<?>> collection = new ArrayList<>();
        collection.add(algoritmoHash);
        collection.add(tipoToken);
        return collection;
    }
}
