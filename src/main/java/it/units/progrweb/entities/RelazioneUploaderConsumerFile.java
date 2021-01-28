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
import it.units.progrweb.utils.EncoderPrevenzioneXSS;
import it.units.progrweb.utils.Logger;
import it.units.progrweb.utils.UtilitaGenerale;
import it.units.progrweb.utils.datetime.PeriodoTemporale;

import java.io.InputStream;
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

    // TODO : DEVE restare un'entità che rappresenti la relazione tra uploader e consumer
    //          ma la maggior parte del codice in questa classe dovrebbe stare in FileStorage (=> fondere)

    @Id
    /** Identificativo per {@link File}.*/
    private Long idFile;

    /** Identificativo per {@link Uploader}.*/
    @Index
    private Long identificativoUploader;

    /** Identificativo per {@link Consumer}.*/
    @Index
    private Long identificativoConsumer;

    /** Flag: true se il file è stato eliminato.*/
    @Index  // TODO : refactoring ! Follia indicizzare un flag che nemmeno dovrebbe essere qui (usato solo in isConsumerServitoDaUploader) => ristrutturare lo schema del DB
    private boolean eliminato;  // TODO : questo c'è anche in FileStorage

    /** Dissocia il {@link Consumer} il cui identificativo è dato
     * dall'{link {@link Uploader}} il cui identificativo è dato.*/
    public static void dissociaConsumerDaUploader(Long identificativoConsumerDaDissociare, Long identificativoUploader) {
        List<RelazioneUploaderConsumerFile> occorrenzeDaEliminare =
                getOccorrenzeFiltratePerUploaderEConsumer(identificativoUploader, identificativoConsumerDaDissociare);

        occorrenzeDaEliminare.stream()
                             .forEach( occorrenzaDaEliminare -> {
                                 // elimina tutte le occorrenze cosicché il consumer risulterà dissociato.
                                 eliminaFileDiUploader(occorrenzaDaEliminare.idFile,
                                                       occorrenzaDaEliminare.identificativoUploader);
                             });

        // TODO : elimare la relazione tra consumer ed uploader
    }

    /** Se l'attore il cui identificativo è dato come parametro può accedere
     * al file il cui identificativo è dato come parametro, restituisce
     * l'occorrenza di {@link RelazioneUploaderConsumerFile} corrispondente
     * al file cercato. Altrimenti restituisce null.
     * Se il file non esiste, genera un'eccezione {@link NotFoundException}.*/
    public static RelazioneUploaderConsumerFile attorePuoAccedereAFile(Long identificativoAttore,
                                                                       Long identificativoFile)
            throws NotFoundException {

        // TODO : verificare questo metodo

        RelazioneUploaderConsumerFile relazioneUploaderConsumerFile = (RelazioneUploaderConsumerFile)
                    DatabaseHelper.getById(identificativoFile, RelazioneUploaderConsumerFile.class);

        if ( relazioneUploaderConsumerFile.getIdentificativoConsumer().equals( identificativoAttore )
                || relazioneUploaderConsumerFile.getIdentificativoUploader().equals( identificativoAttore ) )
            return relazioneUploaderConsumerFile;

        else return null;

    }

    /** Se l'{@link Uploader} dato nel parametro è associato al
     * {@link File} di cui si sta chiedendo l'eliminazione, il cui
     * identificativo è specificato nel parametro, elimina il file
     * e restituisce true. Altrimenti restituisce false e non elimina
     * il file.
     * Se il file non esiste, resituisce false.*/
    public static boolean eliminaFileDiUploader(Long idFileDaEliminare, Long identificativoUploader) {

        // TODO : verificare questo metodo

        RelazioneUploaderConsumerFile relazioneUploaderConsumerFile =
                null;
        try {
            relazioneUploaderConsumerFile = attorePuoAccedereAFile( identificativoUploader, idFileDaEliminare );

            if( relazioneUploaderConsumerFile != null ) {

                RelazioneUploaderConsumerFile relazioneConFileDaEliminare =
                        RelazioneUploaderConsumerFile.getEntitaDaDbByID( idFileDaEliminare );
                File fileDaEliminare = File.getEntitaDaDbById( idFileDaEliminare );

                if( fileDaEliminare.elimina() ) {
                    relazioneConFileDaEliminare.eliminato = true;
                    return true;
                } else {
                    return false;
                }

            } else {
                return false;
            }

        } catch (NotFoundException notFoundException) {
            return false;
        }

    }

    /** Restituisce l'occorrenza di relazione associata al file il cui identificativo
     * è fornito come parametro, oppure null se non viene trovata.*/
    public static RelazioneUploaderConsumerFile getEntitaDaDbByID(Long idFile) {
        try {
            return (RelazioneUploaderConsumerFile)
                    DatabaseHelper.getById( idFile, RelazioneUploaderConsumerFile.class );

        } catch (NotFoundException notFoundException) {
            return null;
        }
    }

    public Long getIdentificativoUploader() {
        return identificativoUploader;
    }

    public Long getIdentificativoConsumer() {
        return identificativoConsumer;
    }

    public Long getIdFile() {
        return idFile;
    }

    public RelazioneUploaderConsumerFile() {}

    private RelazioneUploaderConsumerFile( Long identificativoConsumer, Long identificativoUploader, Long idFile ) {
        this.identificativoConsumer = identificativoConsumer;
        this.identificativoUploader = identificativoUploader;
        this.idFile    = idFile;
        this.eliminato = false;
    }

    /** Interroga il database e restituisce le occorrenze di questa entità
     * che risultano caricate dall'{@link Uploader} e destinate al {@link
     * Consumer} i cui identificativi sono specificati come parametri.
     * @return
     */
    public static List<RelazioneUploaderConsumerFile>
    getOccorrenzeFiltratePerUploaderEConsumer( Long identificativoUploader,
                                               Long identificativoConsumer ) {

        // TODO : verificare che funzioni

        // Parametri query
        String nomeAttributo1 = "identificativoUploader";
        String nomeAttributo2 = "identificativoConsumer";

        if( UtilitaGenerale.esisteAttributoInClasse(nomeAttributo1, RelazioneUploaderConsumerFile.class) ) {
            if (UtilitaGenerale.esisteAttributoInClasse(nomeAttributo2, RelazioneUploaderConsumerFile.class)) {

                return DatabaseHelper.queryAnd(RelazioneUploaderConsumerFile.class,
                                nomeAttributo1, DatabaseHelper.OperatoreQuery.UGUALE, identificativoUploader,
                                nomeAttributo2, DatabaseHelper.OperatoreQuery.UGUALE, identificativoConsumer)
                        .stream()
                        .map( ris -> (RelazioneUploaderConsumerFile)ris )
                        .filter( ris -> ! ris.eliminato )   // TODO : cerca tutti i posti in cui c'è .eliminato => non va bene ! Refactor e pensa ad una struttura migliore
                        .collect( Collectors.toList() );

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


    public static List<File> getListaFileDaUploaderAConsumer(Long identificativoUploader, Long identificativoConsumer) {

        // TODO : refactoring del database (è un po' sprecato andare a cercare per id un'entità che magari nemmeno c'è) visto che fintanto che non viene caricato il primo file da consumer ad uploader il valore è fittizio

        List<RelazioneUploaderConsumerFile> occorrenzeRelazione =
                getOccorrenzeFiltratePerUploaderEConsumer( identificativoUploader, identificativoConsumer );
        List<File> listaFile = occorrenzeRelazione.stream()
                                                  .map( relazione -> {
                                                      try {
                                                          File file = File.getEntitaDaDbById( relazione.getIdFile() );
                                                          return file != null && file.isEliminato() ?
                                                                    null : file; // TODO : deve essere prerogativa della classe File
                                                      } catch (NotFoundException notFoundException) {
                                                          return null;
                                                      }
                                                  })
                                                  .filter( Objects::nonNull )
                                                  .collect(Collectors.toList());

        return listaFile;

    }


    /** Restituisce la lista delle occorrenze, filtrate in base
     * all'{@link Uploader} ed al {@link PeriodoTemporale}
     * specificati nei parametri. Nel filtraggio, per il periodo
     * temporale, si considerano gli estremi inclusi (ad esempio,
     * se il periodo temporale ha come data finale il 27 gennaio,
     * allora si considerano tutti i documenti fino all'istante
     * antecedente il 28 gennaio, ultimo istante compreso).*/
    public static List<RelazioneUploaderConsumerFile>
    getOccorrenzeFiltratePerUploaderEPeriodoTemporale(Long identificativoUploader,
                                                      PeriodoTemporale periodoTemporaleDiRiferimento) {

        // TODO : unire questa classe con File (per avere unica entità nel database) e spostarci tutti i metodi
        //          in tal modo questo metodo eseguirà solo una query che sarà più veloce e restituirà meno
        //          occorrenze dal database (costo minore)

        List<RelazioneUploaderConsumerFile> occorrenzeFiltratePerUploader =
                getOccorrenzeFiltratePerUploader( identificativoUploader );

        List<File> listaFileCaricatiDaUploaderNelPeriodoTemporaleDiRiferimento =
                occorrenzeFiltratePerUploader
                        .stream()
                        .map( occorrenzaRelazione -> {
                            File file;
                            try {
                                file = File.getEntitaDaDbById( occorrenzaRelazione.idFile );
                            } catch (NotFoundException notFoundException) {
                                file = null;
                            }
                            return file;
                        })
                        .filter( Objects::nonNull )
                        .filter( file ->
                                file.getDataEdOraDiCaricamento().isAfter( periodoTemporaleDiRiferimento.getDataIniziale().adInizioGiornata() ) &&
                                    file.getDataEdOraDiCaricamento().isBefore( periodoTemporaleDiRiferimento.getDataFinale().aFineGiornata() )
                        )
                        .collect(Collectors.toList());

        List<Long> identificativiFileFiltrati =
                listaFileCaricatiDaUploaderNelPeriodoTemporaleDiRiferimento
                    .stream()
                    .map( File::getIdentificativoFile )
                    .collect(Collectors.toList());

        return occorrenzeFiltratePerUploader
                .stream()
                .filter( occorrenza -> identificativiFileFiltrati.contains( occorrenza.getIdFile() ) )
                .collect(Collectors.toList());

    }

    /** Interroga il database e restituisce le occorrenze di questa entità
     * che risultano destinate al {@link Consumer} il cui identificativo è
     * specificato come parametro.
     */
    public static List<RelazioneUploaderConsumerFile> getOccorrenzeFiltratePerConsumer(Long identificativoConsumer) {

        // TODO : verificare che funzioni
        return queryRelazioneUploaderConsumerFiles("identificativoConsumer", identificativoConsumer);

    }

    /** Interroga il database e restituisce le occorrenze di questa entità
     * che risultano caricate dall'{@link Uploader} il cui identificativo è
     * specificato come parametro.
     */
    public static List<RelazioneUploaderConsumerFile> getOccorrenzeFiltratePerUploader(Long identificativoUploader) {

        // TODO : verificare che funzioni
        return queryRelazioneUploaderConsumerFiles("identificativoUploader", identificativoUploader);

    }

    /** Interroga il database e restituisce la lista degli identificativi dei
     * {@link Consumer} associati con l'{@link Uploader} il cui identificativo
     * è specificato come parametro.*/
    public static List<Long> getListaConsumerDiUploader( Long identificativoUploader ) {

        String nomeAttributoUploader = "identificativoUploader";
        String nomeAttributoConsumer = "identificativoConsumer";
        if( UtilitaGenerale.esisteAttributoInClasse( nomeAttributoUploader, RelazioneUploaderConsumerFile.class ) &&
                UtilitaGenerale.esisteAttributoInClasse( nomeAttributoConsumer, RelazioneUploaderConsumerFile.class ) ) {

            List<Long> risultato = DatabaseHelper.proiezione(
                    DatabaseHelper.creaERestituisciQuery( RelazioneUploaderConsumerFile.class,
                            nomeAttributoUploader, DatabaseHelper.OperatoreQuery.UGUALE, identificativoUploader ),
                    nomeAttributoConsumer
            )
                    .stream()
                    .filter( occorrenzaRelazione ->
                            ! ((RelazioneUploaderConsumerFile)occorrenzaRelazione).eliminato )      // TODO : rivedere in tutti i metodi di questa classe la gestione del file eliminato
                    .map( occorrenzaRelazione ->
                            ((RelazioneUploaderConsumerFile)occorrenzaRelazione)
                                    .getIdentificativoConsumer() )

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
     * parametro corrispondente. Gli attributi su cui si esegui la query
     * devono essere indicizzati.*/
    private static List<RelazioneUploaderConsumerFile> queryRelazioneUploaderConsumerFiles(String nomeAttributo,
                                                                                           Long valoreAttributo) {

        if (UtilitaGenerale.esisteAttributoInClasse(nomeAttributo, RelazioneUploaderConsumerFile.class)) {

            return DatabaseHelper.query(RelazioneUploaderConsumerFile.class,
                                        nomeAttributo, DatabaseHelper.OperatoreQuery.UGUALE, valoreAttributo )
                    .stream()
                    .map( unOccorrenza -> (RelazioneUploaderConsumerFile)unOccorrenza )
                    .collect(Collectors.toList());

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
    public static Map<Long, Long[]> mappa_identificativoUploader_arrayIdFile(List<RelazioneUploaderConsumerFile> listaRelazioni) {

        String nomeMetodoGetterDaUsarePerRaggruppareOccorrenze = "getIdentificativoUploader";
        return mappa_identificativoAttore_arrayIdFiles(listaRelazioni, nomeMetodoGetterDaUsarePerRaggruppareOccorrenze);

    }

    /** Data una lista in cui ogni elemento è un'istanza di
     * {@link RelazioneUploaderConsumerFile}, restituisce una
     * mappa che ha come chiave l'identificativo di un {@link Consumer}
     * e come valore corrispondente l'array degli identificativi
     * dei {@link File} inviati al {@link Consumer} il cui
     * identificativo è specificato nella chiave.
     */
    public static Map<Long, Long[]> mappa_identificativoConsumer_arrayIdFile(List<RelazioneUploaderConsumerFile> listaRelazioni) {

        String nomeMetodoGetterDaUsarePerRaggruppareOccorrenze = "getIdentificativoConsumer";
        return mappa_identificativoAttore_arrayIdFiles(listaRelazioni, nomeMetodoGetterDaUsarePerRaggruppareOccorrenze);

    }

    /**
     * A partire da una lista di occorrenze di {@link RelazioneUploaderConsumerFile}
     * data come parametro, crea una mappa in cui ogni entry ha come valore un array
     * di identificativi di {@link File} associati all'{@link Attore} il cui identificativo
     * è specificato nella chiave dell'entry: in questo modo, la lista di occorrenze date
     * viene raggruppata in base all'{@link Attore} specificato dal soprascritto metodo getter.*/
    private static Map<Long, Long[]> mappa_identificativoAttore_arrayIdFiles(List<RelazioneUploaderConsumerFile> listaRelazioni,
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

    /** Restituisce true se il consumer con identificativo dato risulta attualmente
     * servito dall'uploader con identificativo dato, false altrimenti.*/
    public static boolean isConsumerServitoDaUploader( Long identificativoUploader, Long identificativoConsumer ) {
        // TODO : verificare funzioni
        try {
            Field uploader_field  = RelazioneUploaderConsumerFile.class.getDeclaredField("identificativoUploader");  // eccezione se non esiste questo field
            Field consumer_field  = RelazioneUploaderConsumerFile.class.getDeclaredField("identificativoConsumer");  // eccezione se non esiste questo field
            Field eliminato_field = RelazioneUploaderConsumerFile.class.getDeclaredField("eliminato");               // eccezione se non esiste questo field
            
            return DatabaseHelper.esisteNelDatabase(
                    RelazioneUploaderConsumerFile.class,
                    DatabaseHelper.creaERestituisciQueryAnd( RelazioneUploaderConsumerFile.class,
                                     uploader_field.getName(),  DatabaseHelper.OperatoreQuery.UGUALE, identificativoUploader,
                                     consumer_field.getName(),  DatabaseHelper.OperatoreQuery.UGUALE, identificativoConsumer,
                                     eliminato_field.getName(), DatabaseHelper.OperatoreQuery.UGUALE, false
                            ) );

        } catch (NoSuchFieldException e) {
            Logger.scriviEccezioneNelLog(RelazioneUploaderConsumerFile.class,
                                         "Attributo non trovato nella classe, forse è stato modificato il nome.",
                                         e);
            return false;
        }
    }

    /** Associa il consumer il cui identificativo è dato all'Uploader
     * con identificativo dato. Restituisce true se la richiesta va
     * a buon fine, false altrimenti.*/
    public static void aggiungiConsumerAdUploader(Long identificativoConsumer, Long identificativoUploader) {

        DatabaseHelper.salvaEntita( new RelazioneUploaderConsumerFile(identificativoConsumer, identificativoUploader, null) );

    }

    /** Crea un nuovo {@link File}, caricato dall'{@link Uploader} il
     * cui identificativo è fornito come parametro e destinato al {@link Consumer}
     * il cui identificativo è fornito come parametro.
     * @return L'entità appena salvata.*/
    public static File aggiungiFile(InputStream contenutoFile, String nomeFile, List<String> listaHashtag,
                                    Long identificativoUploader, Long identificativoConsumer ) {

        if( listaHashtag==null ) listaHashtag = new ArrayList<>();

        listaHashtag = listaHashtag.stream()
                                   .map( unHashtag -> EncoderPrevenzioneXSS.encodeForJava( unHashtag ) )
                                   .collect( Collectors.toList() );

        nomeFile = EncoderPrevenzioneXSS.encodeForJava( nomeFile );

        // Salva file in DB
        byte[] contenuto = UtilitaGenerale.convertiInputStreamInByteArray( contenutoFile );
        File fileSalvato = File.salvaFileInDb( nomeFile, contenuto, listaHashtag );

        // Aggiungi relazione
        RelazioneUploaderConsumerFile relazioneUploaderConsumerFile =
                new RelazioneUploaderConsumerFile(identificativoConsumer, identificativoUploader, fileSalvato.getIdentificativoFile());
        DatabaseHelper.salvaEntita(relazioneUploaderConsumerFile);

        return fileSalvato;

    }

}