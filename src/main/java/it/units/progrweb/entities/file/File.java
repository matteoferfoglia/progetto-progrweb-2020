package it.units.progrweb.entities.file;

import com.googlecode.objectify.annotation.Index;
import it.units.progrweb.entities.attori.Consumer;
import it.units.progrweb.utils.JsonHelper;
import it.units.progrweb.utils.Logger;
import it.units.progrweb.utils.UtilitaGenerale;
import it.units.progrweb.utils.datetime.DateTime;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Rappresentazione di un file, caricato da un
 * {@link it.units.progrweb.entities.attori.Uploader}
 * ed indirizzato ad un
 * {@link it.units.progrweb.entities.attori.Consumer}.
 *
 * @author Matteo Ferfoglia
 */
public abstract class File {

    /** Nome del file.*/
    @Index
    protected String nomeDocumento;        // TODO : verificare in objectify che questi campi siano presenti nella subclass "@Entity" che estende questa class

    /** Data e ora di caricamento del file.*/
    @Index
    protected DateTime dataEdOraDiCaricamento;

    /** Data e ora di (eventuale) visualizzazione del file.*/
    @Index  // TODO : è importante sapere la dataora di visualizzazione o non serve l'index?
    protected DateTime dataEdOraDiVisualizzazione;

    protected File(){}

    protected File(String nomeDocumento, DateTime dataEdOraDiCaricamento) {
        this.nomeDocumento = nomeDocumento;
        this.dataEdOraDiCaricamento = dataEdOraDiCaricamento;
    }

    /** Restituisce il Consumer a cui è destinato questo file.*/
     public abstract Consumer getConsumer();


    /**
     * Restituisce un array contenente i nomi degli attributi
     * di {@link File questa} classe.
     */
    public static String[] nomiProprietaFile() {
        // TODO : testare questa classe
        return Arrays.stream(getProprietaFile())
                     .map(field -> {
                         // Formattazione dei nomi prima di restituirli
                         String nomeAttributo = field.getName();
                         nomeAttributo = UtilitaGenerale.splitCamelCase(nomeAttributo.trim());
                         return UtilitaGenerale.trasformaPrimaLetteraMaiuscola(nomeAttributo.toLowerCase());
                     })
                     .toArray(String[]::new);
    }

    /** Restituisce l'array di tutti gli attributi di {@link File questa} classe.*/
    public static Field[] getProprietaFile() {
        return File.class.getDeclaredFields();
    }

    /** Restituisce la mappa nome-valore degli attributi restituiti
     * dal metodo {@link #getProprietaFile()}, nello stesso ordine,
     * rappresentati in formato JSON.*/
    public String toJson(){     // TODO : testare
        Map<String,?> mappaNomeValoreProprieta;
        try {
            mappaNomeValoreProprieta =Arrays.stream(getProprietaFile())
                                            .collect(Collectors.toMap(
                                                    Field::getName,
                                                    field -> {
                                                        try {
                                                            field.setAccessible(true);
                                                            return field.get(this);
                                                        } catch (IllegalAccessException exception) {
                                                            Logger.scriviEccezioneNelLog(this.getClass(), exception);
                                                            return exception;
                                                        }
                                                    },
                                                    (k,v) -> {
                                                        Exception e = new IllegalStateException("");
                                                        Logger.scriviEccezioneNelLog(this.getClass(), e);
                                                        return e;
                                                    },
                                                    LinkedHashMap::new
                                            ));
        } catch (NullPointerException e) {
            // Se gli attributi in cui si cerca di leggere sono null
            mappaNomeValoreProprieta = new LinkedHashMap<>();
        }

        return JsonHelper.convertiMappaProprietaToStringaJson(mappaNomeValoreProprieta);
    }

}