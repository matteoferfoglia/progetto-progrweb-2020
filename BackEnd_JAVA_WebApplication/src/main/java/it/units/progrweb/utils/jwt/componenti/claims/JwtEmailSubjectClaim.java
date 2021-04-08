package it.units.progrweb.utils.jwt.componenti.claims;

import it.units.progrweb.entities.attori.Attore;

/**
 * Classe per rappresentare il claims "Email Subject".
 * Contiene l'email dell'{@link Attore} che possiede il token.
 */
public class JwtEmailSubjectClaim extends JwtClaim<String> {

    /**
     * Crea un claim per il nome del Subject.
     * @param emailSubject L'email del subject.
     */
    public JwtEmailSubjectClaim(String emailSubject) {
        super(JwtClaim.JWT_EMAIL_SUBJECT_CLAIM_NAME, emailSubject);
    }

}