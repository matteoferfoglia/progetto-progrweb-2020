package it.units.progrweb.utils.jwt.componenti.claims;

import it.units.progrweb.entities.attori.Attore;

/**
 * Classe per rappresentare il claims "Nome Subject".
 * Contiene il nome dell'{@link Attore} che
 * possiede il token.
 */
public class JwtNomeSubjectClaim extends JwtClaim<String> {

    /**
     * Crea un claim per il nome del Subject.
     * @param nomeSubject Il nome del subject.
     */
    public JwtNomeSubjectClaim(String nomeSubject) {
        super(JwtClaim.JWT_NOME_SUBJECT_CLAIM_NAME, nomeSubject);
    }

}