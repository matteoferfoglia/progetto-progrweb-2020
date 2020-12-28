package it.units.progrweb.utils.JWT;   // TODO rivedere tutte le classi in questo package che fanno uso di Gson: probabilmente bisogna tradurre in JSON a mano!

import it.units.progrweb.utils.JWT.component.*;
import it.units.progrweb.utils.JWT.component.claim.JwtClaim;
import it.units.progrweb.utils.JWT.component.claim.JwtExpirationTimeClaim;
import it.units.progrweb.utils.Base64Helper;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.NoSuchElementException;

/**
 * Classe per la gestione dei token JWT.
 * Fonte: https://metamug.com/article/security/jwt-java-tutorial-create-verify.html
 */
public class JwtToken {

    private final JwtHeader header;
    private final JwtPayload payload;
    private final JwtSignature signature;

    /** Numero di componenti di cui è costituito un token JWT:
        3 componenti (header, payload e signature).*/
    private final static short NUMERO_COMPONENTI_JWT = 3;

    /**
     * Crea una rappresentazione come oggetto di un token JWT.
     * @throws InvalidKeyException generata da {@link it.units.progrweb.utils.SecurityManager#hmacSha256(String)}
     * @throws NoSuchAlgorithmException generata da {@link it.units.progrweb.utils.SecurityManager#hmacSha256(String)}
     */
    public JwtToken(JwtPayload payload)
            throws NoSuchAlgorithmException, InvalidKeyException {

        // TODO :  la creazione di un token JWT dovrebbe seguire l'iter descritto dalle specifiche (https://tools.ietf.org/html/rfc7519#section-7.2)

        this.header = new JwtHeader(JwtSignature.getAlgoritmoHash());
        this.payload = payload;
        this.signature = new JwtSignature(this.header, this.payload);

    }

    private JwtToken(JwtHeader header, JwtPayload payload, JwtSignature signature) {
        this.header = header;
        this.payload = payload;
        this.signature = signature;
    }

    /** Data una stringa che codifica un token, crea un'istanza di questa classe. */
    public static JwtToken creaJwtTokenDaStringaCodificata(String jwtToken_stringDaVerificare) {

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
    public String getValoreClaimByName(String nomeClaim){
        String valoreClaim;
        try{
            valoreClaim = header.getClaimByName(nomeClaim).getValue();
        } catch (NoSuchElementException ne) {
            try {
                valoreClaim = payload.getClaimByName(nomeClaim).getValue();
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
    public String getSubjectClaim() {
        return getValoreClaimByName(JwtClaim.JWT_SUBJECT_CLAIM_NAME);
    }

    /** Genera il token JWT, già firmato e nella codifica corretta (Base64 separato da punti).*/
    public String generaTokenJson() {

        // TODO : la codifica a base 64 viene già fatta quando si deve calcolare la firma: è uno spreco di risorse ricalcolarla qui!

        return header.getClaimsSetInFormatJsonBase64UrlEncoded()
                + "." + payload.getClaimsSetInFormatJsonBase64UrlEncoded()
                + "." + signature.getSignature();
    }

    /** Verifica che un token, in forma di stringa, sia valido. */
    public static boolean isTokenValido(String JWTToken_StringDaVerificare) {
        JwtToken jwtTokenDaVerificare = creaJwtTokenDaStringaCodificata(JWTToken_StringDaVerificare);
        return jwtTokenDaVerificare.isTokenValido();
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
    private boolean isTokenScaduto() {

        boolean isTokenScaduto;
        JwtExpirationTimeClaim expirationTimeClaim;
        try{
            expirationTimeClaim = new JwtExpirationTimeClaim(payload.getClaimByName(JwtClaim.JWT_EXPIRATION_TIME_CLAIM_NAME));
            isTokenScaduto = expirationTimeClaim.isScaduto();
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
    private boolean isSignatureValida() {

        JwtSignature signatureCalcolata;
        try {
            signatureCalcolata = new JwtSignature(header, payload);
        } catch (Exception e) {
            return false;
        }

        return signature.equals(signatureCalcolata);
    }

}


