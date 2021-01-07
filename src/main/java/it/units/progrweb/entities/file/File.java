package it.units.progrweb.entities.file;

import com.googlecode.objectify.annotation.Index;
import it.units.progrweb.entities.attori.Consumer;
import it.units.progrweb.utils.UtilitaGenerale;
import it.units.progrweb.utils.datetime.DateTime;

import java.util.Arrays;

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
    protected String nomeFile;        // TODO : verificare in objectify che questi campi siano presenti nella subclass "@Entity" che estende questa class

    /** Data e ora di caricamento del file.*/
    @Index
    protected DateTime dataEdOraDiCaricamento;

    /** Data e ora di (eventuale) visualizzazione del file.*/
    @Index  // TODO : è importante sapere la dataora di visualizzazione o non serve l'index?
    protected DateTime dataEdOraDiVisualizzazione;

    protected File(){}

    protected File(String nomeFile, DateTime dataEdOraDiCaricamento) {
        this.nomeFile = nomeFile;
        this.dataEdOraDiCaricamento = dataEdOraDiCaricamento;
    }

    /**
     * Restituisce un array contenente i nomi delle proprietà
     * di un File.
     */
    public static String[] nomiProprietaFile() {
        // TODO : testare questa classe
        return Arrays.stream(File.class.getDeclaredFields())
                     .map(field -> {
                         // Formattazione dei nomi prima di restituirli
                         String nomeAttributo = field.getName();
                         nomeAttributo = UtilitaGenerale.splitCamelCase(nomeAttributo.trim());
                         return UtilitaGenerale.trasformaPrimaLetteraMaiuscola(nomeAttributo.toLowerCase());
                     })
                     .toArray(String[]::new);
    }

    abstract public Consumer getConsumer();

}