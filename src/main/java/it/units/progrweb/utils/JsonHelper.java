package it.units.progrweb.utils;

import com.google.appengine.repackaged.com.google.gson.Gson;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


/**
 * Classe di helper per il formato JSON.
 *
 * @author Matteo Ferfoglia
 */
public class JsonHelper {

    // TODO : rivedere questa classe se semplificabile (es. usando Google Gson())

    /** Converte l'oggetto dato in formato JSON.*/
    public static<T> String convertiOggettoInJson(T oggettoDaConvertireInJson) {
        // TODO : testare questo metodo
        return new Gson().toJson(oggettoDaConvertireInJson);
    }

    /**
     * Data una stringa corrispondente ad una rappresentazione JSON
     * di un oggetto, ne restituisce la mappa chiave-valore in cui
     * ogni entry è una property e le entry della mappa restituita
     * hanno lo  <em>stesso ordine</em> di come erano disposte le
     * properties nella stringa JSON data in ingresso.
     * Questo metodo funziona solamente con oggetti JSON le cui proprietà
     * sono di tipo primitivo (cioè non possono esserci altri oggetti JSON
     * annidati).
     * Idea per questo parsing tratta da
     * <a href="https://stackoverflow.com/a/11478699">qui</a>.
     */
    public static Map<String,Object> convertiStringaJsonToMappaProprieta(String stringaJSON) {

        if( stringaJSON == null )
            return null;

        // Parsing della stringaJson per ottenere le properties nella forma:    "nome": "valore"
        Matcher matcherPropertiesJson = Pattern.compile(RegexHelper.REGEX_COPPIA_NOME_VALORE)
                                               .matcher(stringaJSON);   // nomeProperty in Group1, valore property in group3 (vedere definizione della regex)
        Map<String, Object> mappa_NomeProprieta_ValoreProprieta = new LinkedHashMap<>();    // LinkedHashedMap per mantenere l'ordine delle properties
        // TODO : per confrontare due token JWT si va a confrontare la firma. Se cambia l'ordine dei claim, allora cambia la firma del token. Ma questo non va bene, perché le property nel JSON non dovrebbero dipendere dall'ordine.
        while(matcherPropertiesJson.find()) {
            String nomeProperty = matcherPropertiesJson.group(RegexHelper.NUMERO_GRUPPO_CORRISPONDENTE_A_NOME_PROPRIETA_IN_REGEX_COPPIA_NOME_VALORE);
            String valoreProperty = matcherPropertiesJson.group(RegexHelper.NUMERO_GRUPPO_CORRISPONDENTE_A_VALORE_PROPRIETA_IN_REGEX_COPPIA_NOME_VALORE);

            // Individuo il tipo del valore (numero/boolean/null/string)
            if( Pattern.compile(RegexHelper.REGEX_NUMERO).matcher(valoreProperty).find() ) {
                final char SEPARATORE_DECIMALI = '.';
                Double valoreDouble = Double.parseDouble(valoreProperty);
                if (valoreProperty.indexOf(SEPARATORE_DECIMALI)>0)  // true se si tratta di un numero decimale
                    mappa_NomeProprieta_ValoreProprieta.put(nomeProperty, valoreDouble);
                else //altrimenti è un intero
                    mappa_NomeProprieta_ValoreProprieta.put(nomeProperty, valoreDouble.longValue());
            }
            else if( Pattern.compile(RegexHelper.REGEX_BOOLEAN).matcher(valoreProperty).find() )
                mappa_NomeProprieta_ValoreProprieta.put(nomeProperty,
                                                        valoreProperty.equals("true") ? new Boolean(true) : new Boolean(false));
            else if( valoreProperty.equals("null") )
                mappa_NomeProprieta_ValoreProprieta.put(nomeProperty, null);
            else //altrimenti valore considerato una stringa
                mappa_NomeProprieta_ValoreProprieta.put(nomeProperty, valoreProperty.replaceAll("^\"|\"$","")); //elimina virgolette esterne derivanti dalla codifica JSON delle stringhe

        }

         return mappa_NomeProprieta_ValoreProprieta;
    }

    /**
     * Data una mappa, restituisce la stringa corrispondente alla
     * rappresentazione JSON di tale oggetto.
     * Questo metodo funziona solamente con oggetti JSON le cui proprietà
     * sono di tipo primitivo (cioè non possono esserci altri oggetti JSON
     * annidati).
     * @param <K> è la classe per la chiave della mappa data come argomento.
     * @param <V> è la classe per il valore della mappa data come argomento.
     */
    public static<K,V> String convertiMappaProprietaToStringaJson(Map<K,V> mappaProprieta) {

        if ( mappaProprieta == null )
            return "{ }";

        return "{"
                +   mappaProprieta.entrySet().stream()
                                             .map(entry -> "\"" + entry.getKey() + "\": "
                                                                + ( entry.getValue()==null ? "null" :
                                                                  entry.getValue() instanceof String ?
                                                                    "\"" + entry.getValue() + "\"" :
                                                                    String.valueOf(entry.getValue()) ) )
                                             .collect(Collectors.joining(", "))
                + "}";

    }
}
