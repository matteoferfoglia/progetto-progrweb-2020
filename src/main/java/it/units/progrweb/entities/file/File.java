package it.units.progrweb.entities.file;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;
import it.units.progrweb.entities.attori.nonAdministrator.consumer.Consumer;
import it.units.progrweb.entities.attori.nonAdministrator.uploader.Uploader;
import it.units.progrweb.persistence.DatabaseHelper;
import it.units.progrweb.persistence.NotFoundException;
import it.units.progrweb.utils.Logger;
import it.units.progrweb.utils.UtilitaGenerale;
import it.units.progrweb.utils.datetime.DateTime;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Map;

/**
 * Rappresentazione di un file, caricato da un
 * {@link Uploader}
 * ed indirizzato ad un
 * {@link Consumer}.
 *
 * @author Matteo Ferfoglia
 */
@Entity
public abstract class File {

    /** Identificativo per il file.*/
    @Id
    @Index
    protected Long identificativoFile;

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

    /** Restituisce il nome dell'attributo che contiene la lista
     * degli hashtag di un file in formato Human-Readable.*/
    public static String getNomeAttributoContenenteHashtagNeiFile() {

        final String NOME_ATTRIBUTO_LISTA_HASHTAG_IN_FILE = "listaHashtag";
        return UtilitaGenerale.getNomeAttributoInFormatoHumanReadable(getFieldDaNome(NOME_ATTRIBUTO_LISTA_HASHTAG_IN_FILE));
    }

    /** Restituisce il nome dell'attributo che contiene data di
     * caricamento di un file in formato Human-Readable.*/
    public static String getNomeAttributoContenenteDataCaricamentoFile() {

        final String NOME_ATTRIBUTO_DATA_CARICAMENTO_FILE = "dataEdOraDiCaricamento";
        return UtilitaGenerale.getNomeAttributoInFormatoHumanReadable(getFieldDaNome(NOME_ATTRIBUTO_DATA_CARICAMENTO_FILE));

    }

    /** Restituisce il nome dell'attributo che contiene data di
     * visualizzazione di un file in formato Human-Readable.*/
    public static String getNomeAttributoContenenteDataVisualizzazioneFile() {

        final String NOME_ATTRIBUTO_DATA_VISUALIZZAZIONE_FILE = "dataEdOraDiVisualizzazione";
        return UtilitaGenerale.getNomeAttributoInFormatoHumanReadable(getFieldDaNome(NOME_ATTRIBUTO_DATA_VISUALIZZAZIONE_FILE));

    }

    /** Controlla che dato un nome esista un attributo con quel nome
     * in {@link File questa classe} oppure nella classe {@link FileStorage}.
     * Se non trova tale attributo, viene riportata un'eccezione, in questo
     * modo, se venisse modificato il nome dell'attributo nella classe,
     * questo metodo se ne accorgerebbe nel caso in cui venisse invocato
     * con parametro il vecchio dell'attributo.
     * @return Il field corrispondente al nome dato, se trovato, altrimenti null.*/
    private static Field getFieldDaNome(String nomeAttributoDiCuiVerificareEsistenza) {
        try {
            return File.class.getDeclaredField(nomeAttributoDiCuiVerificareEsistenza);
        } catch (NoSuchFieldException ne) {
            try {   // Ritenta la ricerca nell'altra classe
                return FileStorage.class.getDeclaredField(nomeAttributoDiCuiVerificareEsistenza);
            } catch (NoSuchFieldException e) {
                Logger.scriviEccezioneNelLog(File.class, "Attributo di nome " +
                        nomeAttributoDiCuiVerificareEsistenza + " non trovato.", e);
                return null;
            }
        }
    }

    /** Dato l'identificativo di un {@link File}, scarica
     * dal database il {@link File} corrispondente.
     * @throws NotFoundException se l'entità non si trova.*/
    public static File getEntitaFromDbById(Long idFile)
            throws NotFoundException {

        return  (File) DatabaseHelper.getById(idFile, File.class);

    }

    /** Se il file dato nel parametro è un'istanza di {@link FileStorage},
     * allora restituisce il documento associato a quest'istanza ed imposta
     * data ed ora di visualizzazione e l'indirizzo IP di chi ha visualizzato
     * il documento, altrimenti restituisce null.*/
    public static InputStream getContenutoFile(File file, String indirizzoIpVisualizzazione) {
        if( file instanceof FileStorage)
            return FileStorage.getContenutoFile((FileStorage) file, indirizzoIpVisualizzazione);
        else
            return null;
    }

    /** Restituisce il Consumer a cui è destinato questo file.*/
    public abstract Consumer getConsumer();


    /**
     * Restituisce un array contenente i nomi degli attributi
     * di {@link File questa} classe.
     */
    public static String[] anteprimaNomiProprietaFile() {
        // TODO : testare questa classe
        return Arrays.stream(getAnteprimaProprietaFile())
                     .map(UtilitaGenerale::getNomeAttributoInFormatoHumanReadable)
                     .toArray(String[]::new);
    }

    /** Restituisce l'array di tutti gli attributi di {@link File questa} classe.*/
    public static Field[] getAnteprimaProprietaFile() {
        return File.class.getDeclaredFields();
    }

    /** Restituisce l'identificativo del file.*/
    public abstract String getIdentificativoFile();

    /** Restituisce la mappa nome-valore degli attributi di un file
     * ritenuti rilevanti dalla classe concreta che implementa questo
     * metodo.*/
    public abstract Map<String, ?> toMap_nomeProprieta_valoreProprieta();

    public String getNomeDocumento() {
        return nomeDocumento;
    }
}