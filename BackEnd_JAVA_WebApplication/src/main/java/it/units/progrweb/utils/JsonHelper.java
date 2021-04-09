package it.units.progrweb.utils;

import com.google.appengine.repackaged.com.google.gson.Gson;

import java.util.*;
import java.util.stream.Collectors;


/**
 * Classe di helper per il formato JSON.
 *
 * @author Matteo Ferfoglia
 */
public class JsonHelper {

    /** Istanza di {@link Gson} usata da questa classe. */
    private static final Gson gson = new Gson();

    /**
     * Data una stringa corrispondente ad una rappresentazione JSON
     * di un oggetto, ne restituisce la mappa chiave-valore in cui
     * ogni entry è una property e <strong>non</strong> è garantito che
     * le entry della mappa restituita abbiano lo stesso ordine di come
     * erano disposte le properties nella stringa JSON data in ingresso.
     * Questo metodo funziona solamente con oggetti JSON le cui proprietà
     * sono di tipo primitivo (cioè non possono esserci altri oggetti JSON
     * annidati).
     * Se la stringa data come parametro è nulla, questo metodo resitusce
     * l'oggetto JSON <code>"{}"</code>.
     */
    public static Map<String,Object> convertiStringaJsonInMappaProprieta(String stringaJSON) {

        if( stringaJSON == null )
            return null;

        Map<?,?> mappaProprietaDaJson = gson.fromJson(stringaJSON, Map.class);

        // Cast al tipo restituito corretto
        return mappaProprietaDaJson.entrySet()
                .stream()
                .collect( Collectors.toMap(entry -> (String) entry.getKey(), Map.Entry::getValue) );


    }

    /** Restituisce la rappresentazione JSON dell'oggetto fornito come parametro. */
    public static String convertiOggettoInJSON(Object oggettoDaConvertireInJSON) {
        return gson.toJson(oggettoDaConvertireInJSON);
    }

    /** Restituisce l'oggetto Java corrispondente al valore JSON fornito come parametro.
     * @param valoreJSON La stringa JSON da convertire in oggetto.
     * @param classeOggettoDaRestituire Classe dell'oggetto da restituire.
     * @param <T> Generic rappresentante il tipo da restituire.*/
    public static<T> T convertiOggettoDaJSON(String valoreJSON, Class<T> classeOggettoDaRestituire) {
        return gson.fromJson(valoreJSON,classeOggettoDaRestituire);
    }

    /**
     * Data una mappa, restituisce la stringa corrispondente alla
     * rappresentazione JSON di tale oggetto.
     * Questo metodo funziona solamente con oggetti JSON le cui proprietà
     * sono di tipo primitivo (cioè non possono esserci altri oggetti JSON
     * annidati).
     * L'oggetto nullo viene mappato all'oggetto JSON <code>"{}"</code>.
     * <strong>Non</strong> è garantito che l'ordine delle entries nella
     * mappa venga mantenuto nelle proprietà dell'oggetto JSON risultante.
     * @param <V> è la classe per il valore della mappa data come argomento.
     */
    public static<V> String convertiMappaProprietaToStringaJson(Map<String,V> mappaProprieta) {

        if ( mappaProprieta == null )
            return "{}";

        return gson.toJson(mappaProprieta);

    }

}
