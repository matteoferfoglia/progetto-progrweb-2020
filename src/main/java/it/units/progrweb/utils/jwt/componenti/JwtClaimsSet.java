package it.units.progrweb.utils.jwt.componenti;

import it.units.progrweb.utils.Base64Helper;
import it.units.progrweb.utils.JsonHelper;
import it.units.progrweb.utils.Logger;
import it.units.progrweb.utils.jwt.componenti.claims.JwtClaim;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Classe per la rappresentazione di un insieme di claims JWT,
 * cioè il JWT header o il JWT payload.
 * @author Matteo Ferfoglia
 */
public class JwtClaimsSet {

    /** Lista di claims. */
    private final Collection<JwtClaim<?>> claimsSet = new ArrayList<>();

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
    }

    public JwtClaimsSet(Collection<JwtClaim<?>> jwtClaimSet) {
        this();
        claimsSet.addAll(jwtClaimSet);
    }

    /** Crea un nuovo JwtClaimsSet a partire da uno dato (gli elementi
     * della collection vengono copiati).*/
    protected JwtClaimsSet(JwtClaimsSet jwtClaimsSet) {
        this(jwtClaimsSet.copia());
        this.claimsSetInFormatJsonBase64UrlEncoded = jwtClaimsSet.claimsSetInFormatJsonBase64UrlEncoded;
    }

    /** Restituisce una copia della collection di claims in quest'istanza. */
    private Collection<JwtClaim<?>> copia() {
        return claimsSet.stream().map(JwtClaim::new).collect(Collectors.toList());
    }

    /**
     * Aggiunge un claims.
     */
    public void aggiungiClaim(JwtClaim<?> jwtClaimDaAggiungere) {
        claimsSet.add(jwtClaimDaAggiungere);
        if(is_claimsSetInFormatJsonBase64UrlEncoded_giaInizializzato())
            calcolaClaimsSetInFormatJsonBase64UrlEncoded();
    }

    /**
     * Recupera un particolare claims in base al nome dal claims set.
     * Se non c'è un claims con quel nome, restituisce l'eccezione
     * "NoSuchElementException".
     * @throws NoSuchElementException Se il claims cercato non è presente.
     * @param claimName - il nome del claims da cercare.
     * @return Il claims corrispondente al nome cercato.
     */
    public JwtClaim<?> getClaimByName(JwtClaim.NomeClaim claimName) {
        return claimsSet.stream()
                        .filter(claim -> claim.getName().equals(claimName.nomeClaim()))
                        .findAny()
                        .orElseThrow(NoSuchElementException::new);
    }


    /**
     * Crea la rappresentazione JSON e codificata in Base64
     * Url-Encoded del claims set, quindi la memorizza
     * nell'apposito attributo di questa classe
     * ({@link #claimsSetInFormatJsonBase64UrlEncoded claimsSetInFormatJsonBase64UrlEncoded}).
     * @throws IllegalStateException se due claims hanno lo stesso nome.
     */
    private void calcolaClaimsSetInFormatJsonBase64UrlEncoded()
            throws IllegalStateException{

        Map<String, Object> mappaProprietaOggetto = claimsSet.stream()
                .collect(Collectors.toMap(
                        JwtClaim::getName,
                        jwtClaim -> jwtClaim.getValue() == null ? "" : jwtClaim.getValue(),
                        (k,v) -> {
                            throw new IllegalStateException("Errore: due claims non possono avere lo stesso nome.");    // il nome del claims è la chiave di questa mappa
                        },
                        LinkedHashMap::new));  // mantiene l'ordine dei claims come in claimsSet (Fonte: https://stackoverflow.com/a/29090335)*/

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
     * che è una rappresentazione JSON di un insieme di claims.
     */
    public static JwtClaimsSet convertiJSONToClaimsSet(String claimSetJSON) {

        JwtClaimsSet claimsSet = new JwtClaimsSet();

        Map<String, ?> mappaProprietaOggettoJSON =
                JsonHelper.convertiStringaJsonInMappaProprieta(claimSetJSON);

        mappaProprietaOggettoJSON.forEach((nomeClaim, valoreClaim) -> {
            try {
                claimsSet.aggiungiClaim(new JwtClaim<>(nomeClaim, valoreClaim));
            } catch (Exception e) {
                Logger.scriviEccezioneNelLog(JwtClaimsSet.class, "Errore durante il casting con Generics", e);
            }
        });

        claimsSet.calcolaClaimsSetInFormatJsonBase64UrlEncoded();

        return claimsSet;
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        JwtClaimsSet that = (JwtClaimsSet) o;

        if(claimsSet.size()==that.claimsSet.size()){
            // Ordina i claims per valore, li riunisce in una stringa e confronta le due stringhe
            String stringaConTuttiIValoriDIUnClaim = ordinaERiunisciTuttiIValoriDeiClaimInUnaStringa();
            String stringaConTuttiIValoriDellAltroClaim = that.ordinaERiunisciTuttiIValoriDeiClaimInUnaStringa();

            return stringaConTuttiIValoriDIUnClaim.equals(stringaConTuttiIValoriDellAltroClaim);
        }
        return false;

    }

    /** Considera tutti i valori dei claims di quest'istanza, quindi
     * li ordina alfabeticamente e li riunisce in una stringa, che restituisce.*/
    private String ordinaERiunisciTuttiIValoriDeiClaimInUnaStringa() {
        return claimsSet.stream()
                        .map(jwtClaim -> String.valueOf(jwtClaim.getValue()))
                        .sorted()
                        .collect(Collectors.joining("; "));
    }

}