package it.units.progrweb.utils.jwt.componenti.claims;

import it.units.progrweb.entities.attori.Attore;

/**
 * Classe per rappresentare il claims "Nome Subject".
 * Contiene il nome dell'{@link Attore} che
 * possiede il token.
 */
public class JwtNomeSubjectClaim extends JwtClaim {

    /**
     * Crea un username claims.
     * @param nomeSubject Lo username del subject.
     */
    public JwtNomeSubjectClaim(String nomeSubject) {
        super(JwtClaim.JWT_NOME_SUBJECT_CLAIM_NAME, nomeSubject);
    }

}