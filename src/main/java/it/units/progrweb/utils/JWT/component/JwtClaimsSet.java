package it.units.progrweb.utils.JWT.component;

import it.units.progrweb.utils.JWT.component.claim.JwtClaim;
import it.units.progrweb.utils.JSONHelper;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Classe per la rappresentazione di un insieme di claim JWT,
 * cioè il JWT header o il JWT payload.
 */
public class JwtClaimsSet {

    /** Lista di claim. */
    private final Collection<JwtClaim> claimsSet;

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
    public boolean addClaim(JwtClaim jwtClaim) {
        return claimsSet.add(jwtClaim);
    }

    /**
     * Aggiunge una collection di claim.
     */
    public boolean addAllClaims(Collection<? extends JwtClaim> collectionOfClaims) {
        return claimsSet.addAll(collectionOfClaims);
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
     * Crea la rappresentazione JSON dell'insieme di claim.
     * @return rappresentazione JSON.
     */
    public String convertiClaimsSetToJSON() {

        Map<String, String> mappaProprietaOggetto;
        mappaProprietaOggetto = claimsSet.stream()
                .map(claim -> new AbstractMap.SimpleEntry<>(claim.getName(),claim.getValue()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        return JSONHelper.convertiMappaProprietaToJSONString(mappaProprietaOggetto);

    }

    /**
     * Crea un'istanza di questa classe a partire da una stringa
     * che è una rappresentazione JSON di un insieme di claim.
     */
    public static JwtClaimsSet convertiJSONToClaimsSet(String claimSetJSON) {

        JwtClaimsSet claimsSet = new JwtClaimsSet();
        Map<String, String> mappaProprietaOggettoJSON = (Map<String, String>) JSONHelper.convertiStringaJsonToMap(claimSetJSON);

        mappaProprietaOggettoJSON.forEach((nomeClaim, valoreClaim) -> claimsSet.addClaim(new JwtClaim(nomeClaim, valoreClaim)));

        return claimsSet;
    }

}