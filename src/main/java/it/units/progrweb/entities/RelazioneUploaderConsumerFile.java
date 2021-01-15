package it.units.progrweb.entities;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;
import it.units.progrweb.entities.attori.Attore;
import it.units.progrweb.entities.attori.nonAdministrator.consumer.Consumer;
import it.units.progrweb.entities.attori.nonAdministrator.uploader.Uploader;
import it.units.progrweb.entities.file.File;
import it.units.progrweb.persistence.DatabaseHelper;
import it.units.progrweb.utils.Logger;
import it.units.progrweb.utils.UtilitaGenerale;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Classe di comodo utilizzata per creare una relazione
 * che coinvolga {@link Uploader}, {@link Consumer} e
 * {@link File}, con l'obiettivo di minimizzare i costi
 * relativi al database, sia in termini di numero di
 * indici, sia in termini di numero di accessi per
 * l'esecuzione delle query.
 * @author Matteo Ferfoglia
 */
@Entity
public class RelazioneUploaderConsumerFile {

    /** Id per un'occorrenza.*/
    @Id
    private Long idRelazione;

    /** Identificativo per {@link Uploader}.*/
    @Index
    private Long idUploader;

    /** Identificativo per {@link Consumer}.*/
    @Index
    private Long idConsumer;

    /** Identificativo per {@link File}.*/
    private Long idFile;

    public Long getIdUploader() {
        return idUploader;
    }

    public Long getIdConsumer() {
        return idConsumer;
    }

    public Long getIdFile() {
        return idFile;
    }

    public RelazioneUploaderConsumerFile() {}

    /** Interroga il database e restituisce le occorrenze di questa entità
     * che risultano caricate dall'{@link Uploader} e destinate al {@link
     * Consumer} i cui identificativi sono specificati come parametri.
     * @return
     */
    public static List<RelazioneUploaderConsumerFile> getOccorrenzeFiltratePerUploaderEConsumer(Long idUploader,
                                                                                                Long idConsumer) {

        // TODO : verificare che funzioni

        // Parametri query
        String nomeAttributo1 = "idUploader";
        String nomeAttributo2 = "idConsumer";

        if( UtilitaGenerale.esisteAttributoInClasse(nomeAttributo1, RelazioneUploaderConsumerFile.class) ) {
            if (UtilitaGenerale.esisteAttributoInClasse(nomeAttributo2, RelazioneUploaderConsumerFile.class)) {

                return (List<RelazioneUploaderConsumerFile>)
                        DatabaseHelper.queryAnd(RelazioneUploaderConsumerFile.class,
                                nomeAttributo1, DatabaseHelper.OperatoreQuery.UGUALE, idUploader,
                                nomeAttributo2, DatabaseHelper.OperatoreQuery.UGUALE, idConsumer);

            } else {
                Logger.scriviEccezioneNelLog(RelazioneUploaderConsumerFile.class,
                        "Field \"" + nomeAttributo2 + "\" non trovato.",
                        new NoSuchFieldException());
            }
        } else {
            Logger.scriviEccezioneNelLog(RelazioneUploaderConsumerFile.class,
                    "Field \"" + nomeAttributo1 + "\" non trovato.",
                    new NoSuchFieldException());
        }

        return new ArrayList<>();   // nessun risultato dalla query

    }

    /** Interroga il database e restituisce le occorrenze di questa entità
     * che risultano destinate al {@link Consumer} il cui identificativo è
     * specificato come parametro.
     */
    public static List<RelazioneUploaderConsumerFile> getOccorrenzeFiltratePerConsumer(Long idConsumer) {

        // TODO : verificare che funzioni
        return queryRelazioneUploaderConsumerFiles("idConsumer", idConsumer);

    }

    /** Interroga il database e restituisce le occorrenze di questa entità
     * che risultano caricate dall'{@link Uploader} il cui identificativo è
     * specificato come parametro.
     */
    public static List<RelazioneUploaderConsumerFile> getOccorrenzeFiltratePerUploader(Long idUploader) {

        // TODO : verificare che funzioni
        return queryRelazioneUploaderConsumerFiles("idUploader", idUploader);

    }

    /** Dati il nome di un attributo di {@link RelazioneUploaderConsumerFile}
     * ed il corrispettivo valore, questo metodo interroga il database e
     * restituisce la lista di tutte le occorrenza in cui l'attributo il
     * cui nome è specificato nel parametro ha il valore specificato nel
     * parametro corrispondente.*/
    private static List<RelazioneUploaderConsumerFile> queryRelazioneUploaderConsumerFiles(String nomeAttributo, Long valoreAttributo) {
        if (UtilitaGenerale.esisteAttributoInClasse(nomeAttributo, RelazioneUploaderConsumerFile.class)) {

            return (List<RelazioneUploaderConsumerFile>)
                    DatabaseHelper.query(RelazioneUploaderConsumerFile.class,
                            nomeAttributo, DatabaseHelper.OperatoreQuery.UGUALE, valoreAttributo );

        } else {
            Logger.scriviEccezioneNelLog(RelazioneUploaderConsumerFile.class,
                    "Field \"" + nomeAttributo + "\" non trovato.",
                    new NoSuchFieldException());
        }

        return new ArrayList<>();   // nessun risultato dalla query
    }

    /** Data una lista in cui ogni elemento è un'istanza di
     * {@link RelazioneUploaderConsumerFile}, restituisce una
     * mappa che ha come chiave l'identificativo di un {@link Uploader}
     * e come valore corrispondente l'array degli identificativi
     * dei {@link File} inviati da quell'{@link Uploader} il cui
     * identificativo è specificato nella chiave.
     */
    public static Map<Long, Long[]> mappa_idUploader_arrayIdFile(List<RelazioneUploaderConsumerFile> listaRelazioni) {

        String nomeMetodoGetterDaUsarePerRaggruppareOccorrenze = "getIdUploader";
        return mappa_idAttore_arrayIdFiles(listaRelazioni, nomeMetodoGetterDaUsarePerRaggruppareOccorrenze);

    }

    /** Data una lista in cui ogni elemento è un'istanza di
     * {@link RelazioneUploaderConsumerFile}, restituisce una
     * mappa che ha come chiave l'identificativo di un {@link Consumer}
     * e come valore corrispondente l'array degli identificativi
     * dei {@link File} inviati al {@link Consumer} il cui
     * identificativo è specificato nella chiave.
     */
    public static Map<Long, Long[]> mappa_idConsumer_arrayIdFile(List<RelazioneUploaderConsumerFile> listaRelazioni) {

        String nomeMetodoGetterDaUsarePerRaggruppareOccorrenze = "getIdConsumer";
        return mappa_idAttore_arrayIdFiles(listaRelazioni, nomeMetodoGetterDaUsarePerRaggruppareOccorrenze);

    }

    /**
     * A partire da una lista di occorrenze di {@link RelazioneUploaderConsumerFile}
     * data come parametro, crea una mappa in cui ogni entry ha come valore un array
     * di identificativi di {@link File} associati all'{@link Attore} il cui identificativo
     * è specificato nella chiave dell'entry: in questo modo, la lista di occorrenze date
     * viene raggruppata in base all'{@link Attore} specificato dal soprascritto metodo getter.*/
    private static Map<Long, Long[]> mappa_idAttore_arrayIdFiles(List<RelazioneUploaderConsumerFile> listaRelazioni,
                                                                 String nomeMetodoGetterDaUsarePerRaggruppareOccorrenze) {

        // TODO : restituire List<Long> anziché Long[]

        try {

            Method getter_metodoRaggruppamentoOccorrenze = RelazioneUploaderConsumerFile
                    .class.getDeclaredMethod(nomeMetodoGetterDaUsarePerRaggruppareOccorrenze);

            return listaRelazioni
                    .stream()
                    .collect(Collectors.groupingBy(relazione -> {
                        try {
                            return getter_metodoRaggruppamentoOccorrenze.invoke(relazione);
                        } catch (IllegalAccessException | InvocationTargetException e) {
                            Logger.scriviEccezioneNelLog(RelazioneUploaderConsumerFile.class, e);
                            return null;
                        }
                    }))   // raggruppa in base all'attore ( quale attore dipende dal metodo usato )

                    // Ora che è raggruppato, crea mappa { uploader => arrayFilesCaricatiDaUploader }
                    .entrySet()
                    .stream()
                    .filter(Objects::nonNull)
                    .map(entry -> {
                        Long chiave = (Long) entry.getKey();
                        Long[] arrayIdFiles = entry.getValue()
                                .stream()
                                .map(RelazioneUploaderConsumerFile::getIdFile)
                                .filter( Objects::nonNull ) // evita le occorrenze in cui il file è null
                                .toArray(Long[]::new);
                        return new AbstractMap.SimpleEntry(chiave, arrayIdFiles);
                    })
                    .collect(Collectors.toMap(
                            entry -> (Long) entry.getKey(),
                            entry -> (Long[]) entry.getValue())
                    );
        } catch (NoSuchMethodException e) {
            Logger.scriviEccezioneNelLog(RelazioneUploaderConsumerFile.class,
                            "Metodo \"" + nomeMetodoGetterDaUsarePerRaggruppareOccorrenze + "\" non trovato.",
                                            e );
            return new HashMap<>(); // mappa vuota
        }
    }

}