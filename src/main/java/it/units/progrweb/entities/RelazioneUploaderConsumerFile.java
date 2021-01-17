package it.units.progrweb.entities;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;
import it.units.progrweb.entities.attori.Attore;
import it.units.progrweb.entities.attori.nonAdministrator.consumer.Consumer;
import it.units.progrweb.entities.attori.nonAdministrator.uploader.Uploader;
import it.units.progrweb.entities.file.File;
import it.units.progrweb.persistence.DatabaseHelper;
import it.units.progrweb.persistence.NotFoundException;
import it.units.progrweb.utils.Logger;
import it.units.progrweb.utils.UtilitaGenerale;

import java.lang.reflect.Field;
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

    @Id
    /** Identificativo per {@link File}.*/
    private Long idFile;

    /** Identificativo per {@link Uploader}.*/
    @Index
    private String usernameUploader;

    /** Identificativo per {@link Consumer}.*/
    @Index
    private String usernameConsumer;

    /** Dissocia il {@link Consumer} il cui username è dato
     * dall'{link {@link Uploader}} il cui username è dato.*/
    public static void dissociaConsumerDaUploader(String usernameConsumerDaDissociare, String usernameUploader) {
        List<RelazioneUploaderConsumerFile> occorrenzeDaEliminare =
                getOccorrenzeFiltratePerUploaderEConsumer(usernameUploader, usernameConsumerDaDissociare);

        DatabaseHelper.cancellaEntita(occorrenzeDaEliminare);

        // TODO : elimare la relazione tra consumer ed uploader
    }

    /** Se l'attore il cui username è dato come parametro può accedere
     * al file il cui identificativo è dato come parametro, restituisce
     * l'occorrenza di {@link RelazioneUploaderConsumerFile} corrispondente
     * al file cercato. Altrimenti restituisce null.
     * Se il file non esiste, genera un'eccezione {@link NotFoundException}.*/
    public static RelazioneUploaderConsumerFile attorePuoAccedereAFile(String usernameAttoreDaHttpServletRequest,
                                                                       Long identificativoFile)
            throws NotFoundException {

        // TODO : verificare questo metodo

        RelazioneUploaderConsumerFile relazioneUploaderConsumerFile = (RelazioneUploaderConsumerFile)
                    DatabaseHelper.getById(identificativoFile, RelazioneUploaderConsumerFile.class);

        if ( relazioneUploaderConsumerFile.getUsernameConsumer().equals( usernameAttoreDaHttpServletRequest )
                || relazioneUploaderConsumerFile.getUsernameUploader().equals( usernameAttoreDaHttpServletRequest ) )
            return relazioneUploaderConsumerFile;

        else return null;

    }

    /** Se l'{@link Uploader} dato nel parametro è associato al
     * {@link File} di cui si sta chiedendo l'eliminazione, il cui
     * identificativo è specificato nel parametro, elimina il file
     * e restituisce true. Altrimenti restituisce false e non elimina
     * il file.
     * Se il file non esiste, genera un'eccezione {@link NotFoundException}.*/
    public static boolean eliminaFileDiUploader(Long idFileDaEliminare, String usernameUploader)
            throws NotFoundException {

        // TODO : verificare questo metodo

        RelazioneUploaderConsumerFile relazioneUploaderConsumerFile =
                attorePuoAccedereAFile( usernameUploader, idFileDaEliminare );

        if( relazioneUploaderConsumerFile != null ) {

            DatabaseHelper.cancellaEntitaById(idFileDaEliminare, RelazioneUploaderConsumerFile.class);
            DatabaseHelper.cancellaEntitaById(idFileDaEliminare, File.class);   // elimina a cascata anche il file

            return true;
        } else {
            return false;
        }

    }

    public String getUsernameUploader() {
        return usernameUploader;
    }

    public String getUsernameConsumer() {
        return usernameConsumer;
    }

    public Long getIdFile() {
        return idFile;
    }

    public RelazioneUploaderConsumerFile() {}

    private RelazioneUploaderConsumerFile( String usernameConsumer, String usernameUploader, Long idFile ) {
        this.usernameConsumer = usernameConsumer;
        this.usernameUploader = usernameUploader;
        this.idFile = idFile;
    }

    /** Interroga il database e restituisce le occorrenze di questa entità
     * che risultano caricate dall'{@link Uploader} e destinate al {@link
     * Consumer} i cui username sono specificati come parametri.
     * @return
     */
    public static List<RelazioneUploaderConsumerFile> getOccorrenzeFiltratePerUploaderEConsumer(String usernameUploader,
                                                                                                String usernameConsumer) {

        // TODO : verificare che funzioni

        // Parametri query
        String nomeAttributo1 = "usernameUploader";
        String nomeAttributo2 = "usernameConsumer";

        if( UtilitaGenerale.esisteAttributoInClasse(nomeAttributo1, RelazioneUploaderConsumerFile.class) ) {
            if (UtilitaGenerale.esisteAttributoInClasse(nomeAttributo2, RelazioneUploaderConsumerFile.class)) {

                return (List<RelazioneUploaderConsumerFile>)
                        DatabaseHelper.queryAnd(RelazioneUploaderConsumerFile.class,
                                nomeAttributo1, DatabaseHelper.OperatoreQuery.UGUALE, usernameUploader,
                                nomeAttributo2, DatabaseHelper.OperatoreQuery.UGUALE, usernameConsumer);

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
     * che risultano destinate al {@link Consumer} il cui username è
     * specificato come parametro.
     */
    public static List<RelazioneUploaderConsumerFile> getOccorrenzeFiltratePerConsumer(String usernameConsumer) {

        // TODO : verificare che funzioni
        return queryRelazioneUploaderConsumerFiles("usernameConsumer", usernameConsumer);

    }

    /** Interroga il database e restituisce le occorrenze di questa entità
     * che risultano caricate dall'{@link Uploader} il cui username è
     * specificato come parametro.
     */
    public static List<RelazioneUploaderConsumerFile> getOccorrenzeFiltratePerUploader(String usernameUploader) {

        // TODO : verificare che funzioni
        return queryRelazioneUploaderConsumerFiles("usernameUploader", usernameUploader);

    }

    /** Interroga il database e restituisce la lista degli username dei
     * {@link Consumer} associati con l'{@link Uploader} il cui username
     * è specificato come parametro.*/
    public static List<String> getListaConsumerDiUploader( String usernameUploader ) {

        String nomeAttributoUploader = "usernameUploader";
        String nomeAttributoConsumer = "usernameConsumer";
        if( UtilitaGenerale.esisteAttributoInClasse( nomeAttributoUploader, RelazioneUploaderConsumerFile.class ) &&
                UtilitaGenerale.esisteAttributoInClasse( nomeAttributoConsumer, RelazioneUploaderConsumerFile.class ) ) {

            List<String> risultato = DatabaseHelper.proiezione(
                    DatabaseHelper.creaERestituisciQuery( RelazioneUploaderConsumerFile.class,
                            nomeAttributoUploader, DatabaseHelper.OperatoreQuery.UGUALE, usernameUploader ),
                    nomeAttributoConsumer
            )
                    .stream()
                    .map( usernameConsumer -> (String)usernameConsumer )
                    .collect(Collectors.toList());

            return risultato;

        } else {
            Logger.scriviEccezioneNelLog(RelazioneUploaderConsumerFile.class,
                                        "Contrallare esistenza dei Field \"" + nomeAttributoConsumer + "\" e \"" + nomeAttributoUploader + "\".",
                                         new NoSuchFieldException() );
            return new ArrayList<>();   // lista vuota come risultato
        }

    }


    /** Dati il nome di un attributo di {@link RelazioneUploaderConsumerFile}
     * ed il corrispettivo valore, questo metodo interroga il database e
     * restituisce la lista di tutte le occorrenza in cui l'attributo il
     * cui nome è specificato nel parametro ha il valore specificato nel
     * parametro corrispondente.*/
    private static List<RelazioneUploaderConsumerFile> queryRelazioneUploaderConsumerFiles(String nomeAttributo,
                                                                                           String valoreAttributo) {

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
    public static Map<String, Long[]> mappa_usernameUploader_arrayIdFile(List<RelazioneUploaderConsumerFile> listaRelazioni) {

        String nomeMetodoGetterDaUsarePerRaggruppareOccorrenze = "getUsernameUploader";
        return mappa_usernameAttore_arrayIdFiles(listaRelazioni, nomeMetodoGetterDaUsarePerRaggruppareOccorrenze);

    }

    /** Data una lista in cui ogni elemento è un'istanza di
     * {@link RelazioneUploaderConsumerFile}, restituisce una
     * mappa che ha come chiave l'identificativo di un {@link Consumer}
     * e come valore corrispondente l'array degli identificativi
     * dei {@link File} inviati al {@link Consumer} il cui
     * identificativo è specificato nella chiave.
     */
    public static Map<String, Long[]> mappa_usernameConsumer_arrayIdFile(List<RelazioneUploaderConsumerFile> listaRelazioni) {

        String nomeMetodoGetterDaUsarePerRaggruppareOccorrenze = "getUsernameConsumer";
        return mappa_usernameAttore_arrayIdFiles(listaRelazioni, nomeMetodoGetterDaUsarePerRaggruppareOccorrenze);

    }

    /**
     * A partire da una lista di occorrenze di {@link RelazioneUploaderConsumerFile}
     * data come parametro, crea una mappa in cui ogni entry ha come valore un array
     * di identificativi di {@link File} associati all'{@link Attore} il cui identificativo
     * è specificato nella chiave dell'entry: in questo modo, la lista di occorrenze date
     * viene raggruppata in base all'{@link Attore} specificato dal soprascritto metodo getter.*/
    private static Map<String, Long[]> mappa_usernameAttore_arrayIdFiles(List<RelazioneUploaderConsumerFile> listaRelazioni,
                                                                         String nomeMetodoGetterDaUsarePerRaggruppareOccorrenze) {

        // TODO : restituire List<Long> anziché Long[] ??

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
                        String chiave = (String) entry.getKey();
                        Long[] arrayIdFiles = entry.getValue()
                                .stream()
                                .map(RelazioneUploaderConsumerFile::getIdFile)
                                .filter( Objects::nonNull ) // evita le occorrenze in cui il file è null
                                .toArray(Long[]::new);
                        return new AbstractMap.SimpleEntry(chiave, arrayIdFiles);
                    })
                    .collect(Collectors.toMap(
                            entry -> (String) entry.getKey(),
                            entry -> (Long[]) entry.getValue())
                    );
        } catch (NoSuchMethodException e) {
            Logger.scriviEccezioneNelLog(RelazioneUploaderConsumerFile.class,
                            "Metodo \"" + nomeMetodoGetterDaUsarePerRaggruppareOccorrenze + "\" non trovato.",
                                            e );
            return new HashMap<>(); // mappa vuota
        }
    }

    /** Restituisce true se il consumer con username dato risulta attualmente
     * servito dall'uploader con username dato, false altrimenti.*/
    public static boolean isConsumerServitoDaUploader( String usernameUploader, String usernameConsumer ) {
        // TODO : verificare funzioni
        try {
            Field uploader_field = RelazioneUploaderConsumerFile.class.getDeclaredField("usernameUploader");  // eccezione se non esiste questo field
            Field consumer_field = RelazioneUploaderConsumerFile.class.getDeclaredField("usernameConsumer");  // eccezione se non esiste questo field
            
            return DatabaseHelper.esisteNelDatabase(
                    RelazioneUploaderConsumerFile.class,
                    DatabaseHelper.creaERestituisciQueryAnd( RelazioneUploaderConsumerFile.class,
                                     uploader_field.getName(), DatabaseHelper.OperatoreQuery.UGUALE, usernameUploader,
                                     consumer_field.getName(), DatabaseHelper.OperatoreQuery.UGUALE, usernameConsumer ) );

        } catch (NoSuchFieldException e) {
            Logger.scriviEccezioneNelLog(RelazioneUploaderConsumerFile.class,
                                         "Attributo non trovato nella classe, forse è stato modificato il nome.",
                                         e);
            return false;
        }
    }

    /** Associa il consumer il cui username è dato all'Uploader
     * con username dato. Restituisce true se la richiesta va
     * a buon fine, false altrimenti.*/
    public static void aggiungiConsumerAdUploader(String usernameConsumer, String usernameUploader) {

        DatabaseHelper.salvaEntita( new RelazioneUploaderConsumerFile(usernameConsumer, usernameUploader, null) );

    }

}