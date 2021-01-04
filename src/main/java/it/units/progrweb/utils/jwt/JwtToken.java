package it.units.progrweb.utils.jwt;

import it.units.progrweb.utils.GestoreSicurezza;
import it.units.progrweb.utils.jwt.componenti.*;
import it.units.progrweb.utils.jwt.componenti.claim.JwtClaim;
import it.units.progrweb.utils.jwt.componenti.claim.JwtExpirationTimeClaim;
import it.units.progrweb.utils.Base64Helper;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.NoSuchElementException;

/**
 * Classe per la gestione dei token JWT
 * (<a href="https://metamug.com/article/security/jwt-java-tutorial-create-verify.html">Fonte</a>).
 * @param <TipoValoreClaim>: i {@link JwtClaim} possono avere valori
 *                         di tipo diverso: qua viene parametrizzato.
 */
public class JwtToken<TipoValoreClaim> {

    private final JwtHeader header;
    private final JwtPayload payload;
    private final JwtSignature signature;

    /** Numero di componenti di cui è costituito un token JWT:
        3 componenti (header, payload e signature).*/
    private final static short NUMERO_COMPONENTI_JWT = 3;

    /**
     * Crea una rappresentazione come oggetto di un token JWT.
     * @throws InvalidKeyException generata da {@link GestoreSicurezza#hmacSha256(String)}
     * @throws NoSuchAlgorithmException generata da {@link GestoreSicurezza#hmacSha256(String)}
     */
    public JwtToken(JwtPayload payload)
            throws NoSuchAlgorithmException, InvalidKeyException {

        // TODO :  la creazione di un token JWT dovrebbe seguire l'iter descritto dalle specifiche (https://tools.ietf.org/html/rfc7519#section-7.2)

        this(   new JwtHeader(JwtSignature.getAlgoritmoHash()),
                payload,
                new JwtSignature(new JwtHeader(JwtSignature.getAlgoritmoHash()), payload)   );
    }

    /** Restituisce una nuova istanza di questa classe, copiando tutti gli
     * attributi dell'istanza passata come parametro.*/
    private JwtToken(JwtToken jwtTokenDaCopiare) {
        this(new JwtHeader(jwtTokenDaCopiare.header),
                new JwtPayload(jwtTokenDaCopiare.payload),
                new JwtSignature(jwtTokenDaCopiare.signature));
    }

    private JwtToken(JwtHeader header, JwtPayload payload, JwtSignature signature) {
        this.header = header;
        this.payload = payload;
        this.signature = signature;
    }

    /** Data una stringa che codifica un token, crea un'istanza di questa classe. */
    public static JwtToken creaJwtTokenDaStringaCodificata(String jwtToken_stringDaVerificare) {

        // TODO : test

        String[] componentiDelJwt = jwtToken_stringDaVerificare.split("\\.");
        if(componentiDelJwt.length != NUMERO_COMPONENTI_JWT)
            throw new IllegalArgumentException("Un token JWT dovrebbe essere costituito da"
                    + "esattamente tre componenti (header, payload e signature) separate dal punto (.),"
                    + "ma sono state trovate " + componentiDelJwt.length + " componenti.");

        JwtHeader jwtHeader = new JwtHeader(JwtClaimsSet.convertiJSONToClaimsSet(Base64Helper.decodeFromBase64UrlEncodedToString(componentiDelJwt[0])));
        JwtPayload jwtPayload = new JwtPayload(JwtClaimsSet.convertiJSONToClaimsSet(Base64Helper.decodeFromBase64UrlEncodedToString(componentiDelJwt[1])));
        JwtSignature jwtSignature = new JwtSignature(componentiDelJwt[2]);  // signature da non codificare base64

        return new JwtToken(jwtHeader, jwtPayload, jwtSignature);

    }

    /**
     * Restituisce il valore di un claim nel token a partire dal nome del claim (parametro).
     * Tale claim viene cercato sia nell'header sia nel payload.
     * @throws NoSuchElementException se non è presente alcun claim con il nome specificato.
     */
    public TipoValoreClaim getValoreClaimByName(String nomeClaim){
        TipoValoreClaim valoreClaim;
        try{
            valoreClaim = (TipoValoreClaim) header.getClaimByName(nomeClaim).getValue();
        } catch (NoSuchElementException ne) {
            try {
                valoreClaim = (TipoValoreClaim) payload.getClaimByName(nomeClaim).getValue();
            } catch (NoSuchElementException ne2) {
                throw new NoSuchElementException("Claim non trovato.");
            }
        }
        return valoreClaim;
    }

    /**
     * Restituisce il claim "Subject" se presente.
     * @throws NoSuchElementException Se il claim cercato non è presente.
     * @return Il claim corrispondente al nome cercato.
     */
    public TipoValoreClaim getSubjectClaim() {
        return getValoreClaimByName(JwtClaim.JWT_SUBJECT_CLAIM_NAME);
    }

    /** Genera il token JWT, già firmato e nella codifica corretta (Base64 separato da punti).*/
    public String generaTokenJsonCodificatoBase64UrlEncoded() {
        return header.getClaimsSetInFormatJsonBase64UrlEncoded()
                + "." + payload.getClaimsSetInFormatJsonBase64UrlEncoded()
                + "." + signature.getSignature();
    }

    /** Verifica la validità di questa istanza del token JWT.*/
    public boolean isTokenValido() {

        // TODO :  la validazione di un token JWT dovrebbe seguire l'iter descritto dalle specifiche (https://tools.ietf.org/html/rfc7519#section-7.1)

        boolean tokenScaduto = isTokenScaduto();
        boolean signatureValida = isSignatureValida();

        return !tokenScaduto && signatureValida;
    }

    /**
     * Verifica che questa istanza di token non sia scaduta.
     * Se NON è presente il claim "Expiration Time", allora restituisce false.
     * Se è presenta il claim "Expiration Time" e la data/ora da
     * esso rappresentato è strettamente inferiore ai quella corrente
     * allora restituisce true.
     */
    public boolean isTokenScaduto() {

        boolean isTokenScaduto;
        try{
            isTokenScaduto = new JwtExpirationTimeClaim(payload.getClaimByName(JwtClaim.JWT_EXPIRATION_TIME_CLAIM_NAME))
                                            .isScaduto();
        } catch(NoSuchElementException e) {
            // Se qui, non c'è Expiration Time nel token, quindi non è scaduto
            isTokenScaduto = false;
        }

        return isTokenScaduto;

    }

    /**
     * Verifica che la firma su questa istanza di token sia valida: la
     * firma viene ricalcolata e deve coincidere con quella di questa
     * istanza, altrimenti quest'istanza viene considerata invalida.
     */
    public boolean isSignatureValida() {

        JwtSignature signatureCalcolata;
        try {
            signatureCalcolata = new JwtSignature(header, payload);
        } catch (Exception e) {
            return false;
        }

        return signature.equals(signatureCalcolata);
    }

    @Override
    public boolean equals(Object o) {
        // TODO : test
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        JwtToken jwtToken = (JwtToken) o;

        if (header != null ? !header.equals(jwtToken.header) : jwtToken.header != null) return false;
        if (payload != null ? !payload.equals(jwtToken.payload) : jwtToken.payload != null) return false;
        return signature != null ? signature.equals(jwtToken.signature) : jwtToken.signature == null;
    }
}

