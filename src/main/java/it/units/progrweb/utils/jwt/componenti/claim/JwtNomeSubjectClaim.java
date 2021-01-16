package it.units.progrweb.utils.jwt.componenti.claim;

import it.units.progrweb.entities.attori.Attore;

/**
 * Classe per rappresentare il claim "Nome Subject".
 * Contiene il nome dell'{@link Attore} che
 * possiede il token.
 */
public class JwtNomeSubjectClaim extends JwtClaim {

    /**
     * Crea un username claim.
     * @param nomeSubject Lo username del subject.
     */
    public JwtNomeSubjectClaim(String nomeSubject) {
        super(JwtClaim.JWT_NOME_SUBJECT_CLAIM_NAME, nomeSubject);
    }

}