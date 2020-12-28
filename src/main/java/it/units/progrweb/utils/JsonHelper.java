package it.units.progrweb.utils;

import org.json.JSONObject;

import java.util.Map;
import java.util.stream.Collectors;

public class JsonHelper {

    /**
     * Data una stringa corrispondente ad una rappresentazione JSON
     * di un oggetto, ne restituisce la mappa chiave-valore in cui
     * ogni entry è una property.
     * Questo metodo funziona solamente con oggetti JSON le cui proprietà
     * sono di tipo primitivo (cioè non possono esserci altri oggetti JSON
     * annidati).
     */
    public static Map<String,?> convertiStringaJsonToMappaProprieta(String stringaJSON) {
        JSONObject oggettoJSON = new JSONObject(stringaJSON);
        return oggettoJSON.keySet().stream()
                .collect(Collectors.toMap(key -> key, key -> oggettoJSON.get(key)));
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

        return "{"
                +   mappaProprieta.entrySet().stream()
                                             .map(entry -> "\"" + entry.getKey() + "\": " + "\"" + entry.getValue() + "\"")
                                             .collect(Collectors.joining(", "))
                + "}";

    }
}
