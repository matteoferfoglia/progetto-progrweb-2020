package it.units.progrweb.utils.jwt;

import it.units.progrweb.entities.AuthenticationTokenInvalido;
import it.units.progrweb.utils.GestoreSicurezza;
import it.units.progrweb.utils.jwt.componenti.*;
import it.units.progrweb.utils.jwt.componenti.claims.JwtClaim;
import it.units.progrweb.utils.jwt.componenti.claims.JwtExpirationTimeClaim;
import it.units.progrweb.utils.Base64Helper;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.NoSuchElementException;

/**
 * Classe per la gestione dei token JWT
 * (<a href="https://metamug.com/article/security/jwt-java-tutorial-create-verify.html">Fonte</a>).
 */
public class JwtToken {

    private final JwtHeader header;
    private final JwtPayload payload;
    private final JwtSignature signature;

    /** Numero di componenti di cui è costituito un token JWT:
        3 componenti (header, payload e signature).*/
    private final static short NUMERO_COMPONENTI_JWT = 3;

    /**
     * Crea un token JWT a partire dal suo payload.
     * @throws InvalidKeyException generata da {@link GestoreSicurezza#hmacSha256(String)}
     * @throws NoSuchAlgorithmException generata da {@link GestoreSicurezza#hmacSha256(String)}
     */
    public JwtToken(JwtPayload payload)
            throws NoSuchAlgorithmException, InvalidKeyException {

        this(   new JwtHeader(JwtSignature.getAlgoritmoHash()),
                payload,
                new JwtSignature(new JwtHeader(JwtSignature.getAlgoritmoHash()), payload)   );

    }

    /** Copy constructor. */
    private JwtToken(JwtHeader header, JwtPayload payload, JwtSignature signature) {
        this.header = new JwtHeader(header);
        this.payload = new JwtPayload(payload);
        this.signature = new JwtSignature(signature);
    }

    /** Copy constructor. */
    public JwtToken(JwtToken jwtToken) {
        this(jwtToken.header, jwtToken.payload, jwtToken.signature);
    }

    /** Data una stringa che codifica un token, crea un'istanza di questa classe.
     * @throws IllegalArgumentException Se il token di autenticazione è mal formato.*/
    public static JwtToken creaJwtTokenDaStringaCodificata(String jwtToken_stringDaVerificare)
            throws IllegalArgumentException {

        String[] componentiDelJwt = jwtToken_stringDaVerificare.split("\\.");
        if(componentiDelJwt.length != NUMERO_COMPONENTI_JWT)
            throw new IllegalArgumentException("Un token JWT dovrebbe essere costituito da "
                    + "esattamente tre componenti (header, payload e signature) separate dal punto (.), "
                    + "ma sono state trovate " + componentiDelJwt.length + " componenti.");

        JwtHeader jwtHeader = new JwtHeader(JwtClaimsSet.convertiJSONToClaimsSet(Base64Helper.decodeFromBase64UrlEncodedToString(componentiDelJwt[0])));
        JwtPayload jwtPayload = new JwtPayload(JwtClaimsSet.convertiJSONToClaimsSet(Base64Helper.decodeFromBase64UrlEncodedToString(componentiDelJwt[1])));
        JwtSignature jwtSignature = new JwtSignature(componentiDelJwt[2]);  // signature da non codificare base64

        return new JwtToken(jwtHeader, jwtPayload, jwtSignature);

    }

    public JwtPayload getPayload() {
        return payload;
    }

    /**
     * Restituisce il valore di un claims nel token a partire dal nome del claims (parametro).
     * Tale claims viene cercato sia nell'header sia nel payload.
     * @throws NoSuchElementException se non è presente alcun claims con il nome specificato.
     */
    public Object getValoreClaimByName(JwtClaim.NomeClaim nomeClaim){
        Object valoreClaim;
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

    /** Genera il token JWT, già firmato e nella codifica corretta (Base64 separato da punti).*/
    public String generaTokenJsonCodificatoBase64UrlEncoded() {
        return header.getClaimsSetInFormatJsonBase64UrlEncoded()
                + "." + payload.getClaimsSetInFormatJsonBase64UrlEncoded()
                + "." + signature.getSignature();
    }

    /** Verifica la validità di questa istanza del token JWT.
     * @param tokenJwtBas64UrlEncoded Rappresentazione Base64UrlEncoded di un token JWT.*/
    public static boolean isTokenValido(String tokenJwtBas64UrlEncoded) {

        if( tokenJwtBas64UrlEncoded==null )
            return false;

        JwtToken jwtToken = JwtToken.creaJwtTokenDaStringaCodificata(tokenJwtBas64UrlEncoded);

        boolean tokenScaduto = jwtToken.isTokenScaduto();
        boolean signatureValida = isSignatureValida(tokenJwtBas64UrlEncoded);

        boolean tokenInvalidato = AuthenticationTokenInvalido.isTokenInvalido( tokenJwtBas64UrlEncoded );

        return !tokenScaduto && signatureValida && !tokenInvalidato;

    }

    /**
     * Verifica che questa istanza di token non sia scaduta.
     * Se NON è presente il claims "Expiration Time", allora restituisce false.
     * Se è presenta il claims "Expiration Time" e la data/ora da
     * esso rappresentato è strettamente inferiore ai quella corrente
     * allora restituisce true.
     */
    public boolean isTokenScaduto() {

        try{
            return new JwtExpirationTimeClaim(payload.getClaimByName(JwtClaim.NomeClaim.EXP))
                                                     .isScaduto();
        } catch(NoSuchElementException e) {
            // Se qui, non c'è Expiration Time nel token, quindi non è scaduto
            return false;
        }

    }

    /** Data la rappresentazione Base64UrlEncoded di un token,
     * restituisce true se la firma risulta valida, false altrimenti.*/
    public static boolean isSignatureValida(String jwtTokenBase64) {

        if( jwtTokenBase64==null )
            return false;

        String[] componentiToken = jwtTokenBase64.split("\\.");
        if( componentiToken.length!=NUMERO_COMPONENTI_JWT )
            return false;

        String headerBase64    = componentiToken[0];
        String payloadBase64   = componentiToken[1];
        String signatureBase64 = componentiToken[2];

        try {
            return signatureBase64.equals( new JwtSignature(headerBase64, payloadBase64).getSignature() );
        } catch (InvalidKeyException|NoSuchAlgorithmException e) {
            return false;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        JwtToken jwtToken = (JwtToken) o;

        if (header != null ? !header.equals(jwtToken.header) : jwtToken.header != null) return false;
        if (payload != null ? !payload.equals(jwtToken.payload) : jwtToken.payload != null) return false;
        return signature != null ? signature.equals(jwtToken.signature) : jwtToken.signature == null;
    }
}


