package it.units.progrweb.utils.jwt.componenti;

import it.units.progrweb.utils.Base64Helper;
import it.units.progrweb.utils.jwt.componenti.claim.JwtClaim;
import it.units.progrweb.utils.JsonHelper;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Classe per la rappresentazione di un insieme di claim JWT,
 * cioè il JWT header o il JWT payload.
 * @param <TipoValoreClaim>: i {@link JwtClaim} possono avere valori
 *                           di tipo diverso: qua viene parametrizzato.
 * @author Matteo Ferfoglia
 */
public class JwtClaimsSet<TipoValoreClaim> {

    /** Lista di claim. */
    private final Collection<JwtClaim> claimsSet = new ArrayList<>();

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

    public JwtClaimsSet(Collection<JwtClaim> jwtClaimSet) {
        this();
        claimsSet.addAll(jwtClaimSet);
    }

    /** Crea un nuovo JwtClaimsSet a partire da uno dato.*/
    protected JwtClaimsSet(JwtClaimsSet jwtClaimsSet) {
        this(jwtClaimsSet.claimsSet);
        this.claimsSetInFormatJsonBase64UrlEncoded = jwtClaimsSet.claimsSetInFormatJsonBase64UrlEncoded;
    }

    /**
     * Aggiunge un claim.
     */
    public void aggiungiClaim(JwtClaim jwtClaimDaAggiungere) {
        claimsSet.add(jwtClaimDaAggiungere);
        if(is_claimsSetInFormatJsonBase64UrlEncoded_giaInizializzato())
            calcolaClaimsSetInFormatJsonBase64UrlEncoded();
    }

    /**
     * Rimuove, se presente, il claim con il nome specificato.
     * Se rimosso, restituisce true, altrimenti false.
     */
    public boolean rimuoviClaim(String nomeClaimDaRimuovere) {
        try {
            return claimsSet.remove(getClaimByName(nomeClaimDaRimuovere));
        } catch(NoSuchElementException e) {
            return false;
        }
    }

    /**
     * Modifica, se presente, il claim con il nome specificato.
     * Se modificato, restituisce true, altrimenti false.
     */
    public boolean modificaValoreClaim(String nomeClaimDaModificare, TipoValoreClaim nuovoValore) {
        try {
            getClaimByName(nomeClaimDaModificare).setValue(nuovoValore);
            return true;
        } catch(NoSuchElementException e) {
            return false;
        }
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
     * @throws IllegalStateException se due claim hanno lo stesso nome.
     */
    private void calcolaClaimsSetInFormatJsonBase64UrlEncoded()
            throws IllegalStateException{

        Map<String, Object> mappaProprietaOggetto = claimsSet.stream()
                .collect(Collectors.toMap(
                        jwtClaim -> jwtClaim.getName(),
                        jwtClaim -> jwtClaim.getValue() == null ? "" : jwtClaim.getValue(),
                        (k,v) -> {
                            throw new IllegalStateException("Errore: due claim non possono avere lo stesso nome.");    // il nome del claim è la chiave di questa mappa
                        },
                        LinkedHashMap::new));  // mantiene l'ordine dei claim come in claimsSet (Fonte: https://stackoverflow.com/a/29090335)*/

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

        Map<String, Object> mappaProprietaOggettoJSON = JsonHelper.convertiStringaJsonToMappaProprieta(claimSetJSON);
        // TODO : il json supporta anche formati non stringa (numero/null/booolean/array...): trovare il modo di salvare il claim con il tipo corretto, non come stringa

        mappaProprietaOggettoJSON.forEach((nomeClaim, valoreClaim) -> claimsSet.aggiungiClaim(new JwtClaim(nomeClaim, valoreClaim)));
        claimsSet.calcolaClaimsSetInFormatJsonBase64UrlEncoded();

        return claimsSet;
    }

    @Override
    public boolean equals(Object o) {

        //TODO : test

        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        JwtClaimsSet that = (JwtClaimsSet) o;

        if(claimsSet != null) {

            if(claimsSet.size()==that.claimsSet.size()){
                // Ordina i claims per valore, li riunisce in una stringa e confronta le due stringhe
                String stringaConTuttiIValoriDIUnClaim = ordinaERiunisciTuttiIValoriDeiClaimInUnaStringa();
                String stringaConTuttiIValoriDellAltroClaim = that.ordinaERiunisciTuttiIValoriDeiClaimInUnaStringa();

                return stringaConTuttiIValoriDIUnClaim.equals(stringaConTuttiIValoriDellAltroClaim);
            }
            return false;

        } else {
            return that.claimsSet == null;
        }
    }

    /** Considera tutti i valori dei claim di quest'istanza, quindi
     * li ordina alfabeticamente e li riunisce in una stringa, che restituisce.*/
    private String ordinaERiunisciTuttiIValoriDeiClaimInUnaStringa() {
        return claimsSet.stream()
                        .map(jwtClaim -> String.valueOf(jwtClaim.getValue()))
                        .sorted()
                        .collect(Collectors.joining("; "));
    }

}