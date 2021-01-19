package it.units.progrweb.utils.jwt.componenti.claim;

/**
 * Classe per rappresentare il claim "Subject".
 * <blockquote cite="https://tools.ietf.org/html/rfc7519#section-4.1.2>
 *    The "sub" (subject) claim identifies the principal that is the
 *    subject of the JWT.  The claims in a JWT are normally statements
 *    about the subject.  The subject value MUST either be scoped to be
 *    locally unique in the context of the issuer or be globally unique.
 *    The processing of this claim is generally application specific.  The
 *    "sub" value is a case-sensitive string containing a StringOrURI
 *    value. Use of this claim is OPTIONAL.
 * </blockquote>
 */
public class JwtSubjectClaim extends JwtClaim {

    /**
     * Crea un Subject claim.
     * @param subject Il subject.
     */
    public JwtSubjectClaim(Object subject) {
        super(JwtClaim.JWT_SUBJECT_CLAIM_NAME, subject);
    }

}