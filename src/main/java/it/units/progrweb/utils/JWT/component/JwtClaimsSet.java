package it.units.progrweb.utils.JWT.component;

import it.units.progrweb.utils.Base64Helper;
import it.units.progrweb.utils.JWT.component.claim.JwtClaim;
import it.units.progrweb.utils.JsonHelper;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Classe per la rappresentazione di un insieme di claim JWT,
 * cioè il JWT header o il JWT payload.
 */
public class JwtClaimsSet {

    /** Lista di claim. */
    private final Collection<JwtClaim> claimsSet;

    /**
     * Memorizza il claims set in formato JSON e codificato in
     * Base64 Url-Encoded. Questo attributo permette di evitare
     * calcoli duplicati, visto che il suo contenuto può tornare
     * utile più colte durante la creazione di un token JWT.
     * Idealmente: questo attributo verrà inizializzato appena
     * quando qualche componente lo richiederà.
     */
    private String claimsSetInFormatJsonBase64UrlEncoded;

    public JwtClaimsSet() {
        claimsSet = new HashSet<>();
    }

    public JwtClaimsSet(JwtClaim singoloJwtClaim) {
        this();
        claimsSet.add(singoloJwtClaim);
    }

    public JwtClaimsSet(Collection<JwtClaim> JwtClaimSet) {
        this();
        claimsSet.addAll(JwtClaimSet);
    }

    protected JwtClaimsSet(JwtClaimsSet jwtClaimsSet) {
        this(jwtClaimsSet.claimsSet);
    }

    /**
     * Aggiunge un claim.
     */
    public void addClaim(JwtClaim jwtClaim) {
        claimsSet.add(jwtClaim);
        if(is_claimsSetInFormatJsonBase64UrlEncoded_giaInizializzato())
            calcolaClaimsSetInFormatJsonBase64UrlEncoded();
    }

    /**
     * Aggiunge una collection di claim.
     */
    public void addAllClaims(Collection<? extends JwtClaim> collectionOfClaims) {
        collectionOfClaims.forEach(claim -> claimsSet.add(claim));
    }

    /**
     * Recupera un particolare claim in base al nome dal claim set.
     * Se non c'è un claim con quel nome, restituisce l'eccezione
     * "NoSuchElementException".
     * @throws NoSuchElementException Se il claim cercato non è presente.
     * @param claimName - il nome del claim da cercare.
     * @return Il claim corrispondente al nome cercato.
     */
    public JwtClaim getClaimByName(String claimName) {
        return claimsSet.stream()
                .filter(claim -> claim.getName().equals(claimName))
                .findAny()
                .orElseThrow(NoSuchElementException::new);
    }


    /**
     * Crea la rappresentazione JSON e codificata in Base64
     * Url-Encoded del claims set, quindi la memorizza
     * nell'apposito attributo di questa classe
     * ({@link #claimsSetInFormatJsonBase64UrlEncoded claimsSetInFormatJsonBase64UrlEncoded}).
     */
    private void calcolaClaimsSetInFormatJsonBase64UrlEncoded() {

        Map<String, String> mappaProprietaOggetto;
        mappaProprietaOggetto = claimsSet.stream()
                .map(claim -> new AbstractMap.SimpleEntry<>(claim.getName(),claim.getValue()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        claimsSetInFormatJsonBase64UrlEncoded = Base64Helper.encodeToBase64UrlEncoded(JsonHelper.convertiMappaProprietaToStringaJson(mappaProprietaOggetto));

    }

    /**
     * Restituisce la rappresentazione JSON e codificata in Base64
     * Url-Encoded del claims set.
     */
    public String getClaimsSetInFormatJsonBase64UrlEncoded() {
        if(! is_claimsSetInFormatJsonBase64UrlEncoded_giaInizializzato())
            calcolaClaimsSetInFormatJsonBase64UrlEncoded();

        return claimsSetInFormatJsonBase64UrlEncoded;
    }

    /** Restituisce true se {@link #claimsSetInFormatJsonBase64UrlEncoded claimsSetInFormatJsonBase64UrlEncoded}
     * è già stato inizializzato, false altrimenti.*/
    private boolean is_claimsSetInFormatJsonBase64UrlEncoded_giaInizializzato() {
        return claimsSetInFormatJsonBase64UrlEncoded != null;
    }

    /**
     * Crea un'istanza di questa classe a partire da una stringa
     * che è una rappresentazione JSON di un insieme di claim.
     */
    public static JwtClaimsSet convertiJSONToClaimsSet(String claimSetJSON) {

        JwtClaimsSet claimsSet = new JwtClaimsSet();
        Map<String, String> mappaProprietaOggettoJSON = (Map<String, String>) JsonHelper.convertiStringaJsonToMappaProprieta(claimSetJSON);

        mappaProprietaOggettoJSON.forEach((nomeClaim, valoreClaim) -> claimsSet.addClaim(new JwtClaim(nomeClaim, valoreClaim)));

        return claimsSet;
    }

}