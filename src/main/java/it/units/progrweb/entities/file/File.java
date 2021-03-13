package it.units.progrweb.entities.file;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Ignore;
import com.googlecode.objectify.annotation.Index;
import it.units.progrweb.entities.RelazioneUploaderConsumer;
import it.units.progrweb.entities.attori.Attore;
import it.units.progrweb.entities.attori.nonAdministrator.consumer.Consumer;
import it.units.progrweb.entities.attori.nonAdministrator.uploader.Uploader;
import it.units.progrweb.persistence.DatabaseHelper;
import it.units.progrweb.persistence.NotFoundException;
import it.units.progrweb.utils.*;
import it.units.progrweb.utils.datetime.DateTime;
import it.units.progrweb.utils.datetime.PeriodoTemporale;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

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
    protected String nomeDocumento;

    /** Data e ora di caricamento del file salvati come Long.*/
    @Index
    private String dataEdOraDiCaricamento;

    /** Indirizzo IP del consumer che ha visualizzato il file. */
    private String indirizzoIpVisualizzatore;

    /** Data e ora di (eventuale) visualizzazione del file salvati come Long.*/
    private String dataEdOraDiVisualizzazione;

    /** Mittente del file.*/
    @Index
    private Long identificativoMittente;

    /** Destinatario del file.*/
    @Index
    private Long identificativoDestinatario;

    /** Dimensione massima per la dimensione di un file, in byte. */
    @Ignore
    public static final long MAX_SIZE_FILE = 800*1024*1024;


    protected File(){}

    protected File(String nomeDocumento, DateTime dataEdOraDiCaricamento, Long identificativoMittente, Long identificativoDestinatario) {
        this.nomeDocumento = nomeDocumento;
        this.dataEdOraDiCaricamento = DateTime.convertiInString( dataEdOraDiCaricamento );
        this.identificativoMittente = identificativoMittente;
        this.identificativoDestinatario = identificativoDestinatario;
    }

    /** Dato l'identificativo di un'istanza di questa classe, restituisce la data
     * e l'ora di visualizzazione.
     * @throws NotFoundException Se il {@link File} non si trova.*/
    public static String getDataOraVisualizzazione(Long identificativoFile)
            throws NotFoundException{

        return DateTime.convertiInString(
                    File.getEntitaDaDbById( identificativoFile )
                        .getDataEdOraDiVisualizzazione()
                );


    }

    /** Restituisce true se il file è stato eliminato, false altrimenti.*/
    abstract public boolean isEliminato();

    public DateTime getDataEdOraDiCaricamento() {
        return DateTime.convertiDaString( dataEdOraDiCaricamento );     // TODo : vedere in DateTime un metodo di conversione che fornisce un output più carino
    }

    protected void setNomeDocumento(String nomeDocumento) {
        this.nomeDocumento = nomeDocumento;
    }

    protected void setDataEdOraDiCaricamento(DateTime dataEdOraDiCaricamento) {
        this.dataEdOraDiCaricamento = DateTime.convertiInString( dataEdOraDiCaricamento );
    }

    public Long getIdentificativoMittente() {
        return identificativoMittente;
    }

    public Long getIdentificativoDestinatario() {
        return identificativoDestinatario;
    }

    public DateTime getDataEdOraDiVisualizzazione() {
        return DateTime.convertiDaString( dataEdOraDiVisualizzazione );
    }

    protected void setDataEdOraDiVisualizzazione(DateTime dataEdOraDiVisualizzazione) {
        this.dataEdOraDiVisualizzazione = DateTime.convertiInString( dataEdOraDiVisualizzazione );
    }

    protected void setIndirizzoIpVisualizzazione(String indirizzoIpVisualizzazione) {
        this.indirizzoIpVisualizzatore = indirizzoIpVisualizzazione;
    }

    protected String getIndirizzoIpVisualizzatore() {
        return indirizzoIpVisualizzatore;
    }

    /** Restituisce il nome dell'attributo che contiene il nome di un file.*/
    public static String getNomeAttributoContenenteNomeFile() {

        final String NOME_ATTRIBUTO_NOME_FILE = "nomeDocumento";
        return getFieldDaNome(NOME_ATTRIBUTO_NOME_FILE).getName();

    }

    /** Restituisce il nome dell'attributo che contiene la lista
     * degli hashtag di un file.*/
    public static String getNomeAttributoContenenteHashtagNeiFile() {

        final String NOME_ATTRIBUTO_LISTA_HASHTAG_IN_FILE = "listaHashtag";
        return getFieldDaNome(NOME_ATTRIBUTO_LISTA_HASHTAG_IN_FILE).getName();

    }

    /** Restituisce il nome dell'attributo che contiene la data di
     * caricamento di un file.*/
    public static String getNomeAttributoContenenteDataCaricamentoFile() {

        final String NOME_ATTRIBUTO_DATA_CARICAMENTO_FILE = "dataEdOraDiCaricamento";
        return getFieldDaNome(NOME_ATTRIBUTO_DATA_CARICAMENTO_FILE).getName();

    }

    /** Restituisce il nome dell'attributo che contiene data di
     * visualizzazione di un file.*/
    public static String getNomeAttributoContenenteDataVisualizzazioneFile() {

        final String NOME_ATTRIBUTO_DATA_VISUALIZZAZIONE_FILE = "dataEdOraDiVisualizzazione";
        return getFieldDaNome(NOME_ATTRIBUTO_DATA_VISUALIZZAZIONE_FILE).getName();

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
    public static File getEntitaDaDbById(Long idFile)
            throws NotFoundException {

        return  (File) DatabaseHelper.getById(idFile, File.class);

    }

    /** Se il file dato nel parametro è un'istanza di {@link FileStorage},
     * allora restituisce il documento associato a quest'istanza ed imposta
     * data ed ora di visualizzazione e l'indirizzo IP di chi ha visualizzato
     * il documento, altrimenti restituisce null.
     * @param salvaDataOraVisualizzazione true se si vuole salvare la data/ora
     *                                    in cui il documento è richiesto (ignorato
     *                                    se tale data/ora è già salvata).*/
    public static byte[] getContenutoFile(File file,
                                               String indirizzoIpVisualizzazione,
                                               boolean salvaDataOraVisualizzazione) {
        if( file instanceof FileStorage)
            return FileStorage.getContenutoFile((FileStorage) file,
                                                indirizzoIpVisualizzazione,
                                                salvaDataOraVisualizzazione );
        else
            return null;
    }

    /** Data una lista di {@link File}, costruisce una mappa avente per chiave
     * l'identificativo per i file e come valore il file rappresentato come
     * oggetto JSON.
     * @param includiMetadati true se si vogliono includere i metadati del file.*/
    public static Map<String, String> getMappa_idFile_propFile(List<File> listaFile, boolean includiMetadati) {
        // TODO : refactoring potrebbe essere meglio avere una classe documenti che risponda alle richieste di documenti, sia di Consumer sia di Uploaders

        return listaFile.stream()
                 .collect(Collectors.toMap(
                         file -> String.valueOf( file.getIdentificativoFile() ),
                         file -> JsonHelper.convertiMappaProprietaToStringaJson(file.toMap_nomeProprieta_valoreProprieta(includiMetadati)), // Ogni elemento dello stream contiene la descrizione JSON di un file
                         (k,v) -> {
                             IllegalStateException e = new IllegalStateException("Chiave duplicata: " + k);
                             Logger.scriviEccezioneNelLog(File.class, "Errore in toMap()", e);
                             throw e;
                         },
                         LinkedHashMap::new
                     )
                 );
    }

    /**
     * Restituisce un array contenente i nomi degli attributi
     * di {@link File questa} classe
     * @param includiMetadati true se si vogliono anche i metadati.
     */
    public static String[] anteprimaNomiProprietaFile(boolean includiMetadati) {
        return Arrays.stream(getAnteprimaProprietaFile(includiMetadati))
                     .map(Field::getName)
                     .toArray(String[]::new);
    }

    /** Restituisce l'array di tutti gli attributi di {@link File questa} classe.
     * @param includiMetadati true se si vogliono anche i metadati.*/
    public static Field[] getAnteprimaProprietaFile( boolean includiMetadati ) {    // TODO : usare FileProxy per evitare di dover escludere manualmente degli attributi
        return Arrays.stream(File.class.getDeclaredFields())
                     .filter( field -> {

                         // Escludi i campi i cui nomi sono presenti in questo array
                         List<String> nomeAttributiInQuestaClasseDaNonMostrare = new ArrayList<>();
                         {
                             nomeAttributiInQuestaClasseDaNonMostrare.add("identificativoDestinatario");    // TODO : sarebbe meglio spostare questi attributi e relativi metodo in FileStorage, cosicchè non vengano mostrati
                             nomeAttributiInQuestaClasseDaNonMostrare.add("identificativoMittente");
                             nomeAttributiInQuestaClasseDaNonMostrare.add("MAX_SIZE_FILE");

                             if (!includiMetadati) {
                                 nomeAttributiInQuestaClasseDaNonMostrare.add("identificativoFile");
                                 nomeAttributiInQuestaClasseDaNonMostrare.add("indirizzoIpVisualizzatore");
                             }
                             nomeAttributiInQuestaClasseDaNonMostrare
                                     .forEach(nomeUnAttributo -> {
                                         if (!UtilitaGenerale.esisteAttributoInClasse(nomeUnAttributo, File.class)) {
                                             // Informa se il field non si trova
                                             Logger.scriviEccezioneNelLog(File.class,
                                                     "L'attributo \"" + nomeUnAttributo + "\"" +
                                                             " non si trova nella classe " + File.class.getName() +
                                                             ": forse è stato rinominato",
                                                     new NoSuchFieldException());
                                         }
                                     });
                         }

                         return ! UtilitaGenerale.isPresenteNellArray( field.getName(), nomeAttributiInQuestaClasseDaNonMostrare.toArray() );

                     }).toArray(Field[]::new);
    }

    /** Restituisce l'identificativo del file.*/
    public Long getIdentificativoFile() {
        return identificativoFile;
    }

    public void setIdentificativoFile(Long identificativoFile) {
        this.identificativoFile = identificativoFile;
    }

    /** Restituisce la mappa nome-valore degli attributi di un file
     * ritenuti rilevanti dalla classe concreta che implementa questo
     * metodo.
     * @param includiMetadati true se si vogliono anche i metadati.*/
    public abstract Map<String, ?> toMap_nomeProprieta_valoreProprieta(boolean includiMetadati);

    public String getNomeDocumento() {
        return nomeDocumento;
    }

    /** Restituisce il {@link File} (cioè il suo contenuto), il cui identificativo
     * è specificato nel parametro, in una {@link Response}
     * (<a href="https://stackoverflow.com/a/12251265">Fonte (restituzione file tramite JAX-RS)</a>).
     * E' richiesta la HttpServletRequest che ha richiesto il {@link File}
     * per venire a conoscenza dell'indirizzo IP del client a cui il
     * file verrà recapitato.
     * Prima di restituire il file, si verifica che l'{@link Attore} che lo ha
     * richiesto sia in relazione con quel file (o {@link Consumer} o
     * {@link Uploader}) (non si possono recuperare i file di altri utenti).
     * Fonte: https://stackoverflow.com/a/12251265
     * @param salvaDataOraVisualizzazione true se si vuole salvare la data/ora
     *                                    in cui il documento è richiesto (ignorato
     *                                    se tale data/ora è già salvata).*/
    public static Response creaResponseConFile(Long identificativoFile,
                                               HttpServletRequest httpServletRequest,
                                               boolean salvaDataOraVisualizzazione   ) {

        Long identificativoAttoreDaHttpServletRequest =
                Autenticazione.getIdentificativoAttoreDaTokenAutenticazione(httpServletRequest);

        try {

            File file = File.getEntitaDaDbById(identificativoFile);
            if(!(file.getIdentificativoDestinatario().equals(identificativoAttoreDaHttpServletRequest) ||
                      file.getIdentificativoMittente().equals(identificativoAttoreDaHttpServletRequest) ))
                return Autenticazione.creaResponseForbidden("Accesso al file vietato.");

            if(file.isEliminato())
                throw new NotFoundException();

            byte[] contenutoFile = File.getContenutoFile(file, httpServletRequest.getRemoteAddr(), salvaDataOraVisualizzazione);
            return Response.ok(contenutoFile, MediaType.APPLICATION_OCTET_STREAM)
                           .header("Content-Disposition", "attachment; filename=\"" + file.getNomeDocumento() + "\"")
                           .build();

        } catch (NotFoundException notFoundException) {
            return NotFoundException.creaResponseNotFound("File non trovato.");
        }

    }


    /** Crea, salva nel database e restituisce un'istanza di questa classe in base ai parametri.*/
    public static File salvaFileInDb( String nomeFile, byte[] contenuto, List<String> listaHashtag,
                                      Long identificativoMittente, Long identificativoDestinatario ) {

        FileStorage fileStorage = new FileStorage(nomeFile, contenuto, listaHashtag,
                                                  identificativoMittente, identificativoDestinatario);
        fileStorage.setDataEdOraDiCaricamento( DateTime.adesso() );
        Long idFile = (Long) DatabaseHelper.salvaEntita( fileStorage );
        fileStorage.setIdentificativoFile( idFile );

        return fileStorage;

    }

    /** Rimuove il contenuto di un file.*/
    public abstract boolean elimina();

    /** Interroga il database e restituisce l'occorrenza di questa entità
     * che relaziona l'{@link Uploader} con il {@link Consumer} i cui
     * identificativi sono specificati come parametri.
     * Se non viene specificato l'Uploader, allora il filtraggio viene fatto
     * solo sul Consumer.
     * I risultati restituiti sono ordinati secondo la data-ora di caricamento.
     * @return La lista dei file caricati dall'{@link Uploader} e destinati
     * al {@link Consumer} specificati. */
    public static List<File>
    getOccorrenzeFiltrataPerUploaderEConsumer(Long identificativoUploader,
                                              @NotNull Long identificativoConsumer ) {

        // Parametri query
        String nomeAttributo1 = "identificativoMittente";
        String nomeAttributo2 = "identificativoDestinatario";

        if( UtilitaGenerale.esisteAttributoInClasse(nomeAttributo1, File.class) ) {
            if (UtilitaGenerale.esisteAttributoInClasse(nomeAttributo2, File.class)) {

                String nomeAttributoOrdinamentoDocumenti = "dataEdOraDiCaricamento";
                if(UtilitaGenerale.esisteAttributoInClasse(nomeAttributoOrdinamentoDocumenti, File.class)) {

                    List<?> risultatoQuery;

                    if(identificativoUploader==null)
                        risultatoQuery =
                                DatabaseHelper.query(File.class,
                                        nomeAttributo2, DatabaseHelper.OperatoreQuery.UGUALE, identificativoConsumer,
                                        nomeAttributoOrdinamentoDocumenti);
                    else
                        risultatoQuery =
                                DatabaseHelper.queryAnd(File.class,
                                        nomeAttributo1, DatabaseHelper.OperatoreQuery.UGUALE, identificativoUploader,
                                        nomeAttributo2, DatabaseHelper.OperatoreQuery.UGUALE, identificativoConsumer,
                                        nomeAttributoOrdinamentoDocumenti);

                    return risultatoQuery
                            .stream()
                            .filter( Objects::nonNull )
                            .map(unFile -> (File)unFile)
                            .filter(unFile -> !unFile.isEliminato())
                            .collect(Collectors.toList());

                } else {
                    Logger.scriviEccezioneNelLog(File.class,
                            "Controllare il nome dell'attributo su cui si esegue l'ordinamento",
                            new NoSuchFieldException());
                }

            } else {
                Logger.scriviEccezioneNelLog(File.class,
                        "Field \"" + nomeAttributo2 + "\" non trovato.",
                        new NoSuchFieldException());
            }
        } else {
            Logger.scriviEccezioneNelLog(File.class,
                    "Field \"" + nomeAttributo1 + "\" non trovato.",
                    new NoSuchFieldException());
        }

        return new ArrayList<>(0);   // nessun risultato dalla query

    }

    /** Restituisce la lista delle occorrenze, filtrate in base
     * all'{@link Uploader} ed al {@link PeriodoTemporale}
     * specificati nei parametri. Nel filtraggio, per il periodo
     * temporale, si considerano gli estremi inclusi (ad esempio,
     * se il periodo temporale ha come data finale il 27 gennaio,
     * allora si considerano tutti i documenti fino all'istante
     * antecedente il 28 gennaio, ultimo istante compreso).*/
    public static List<File>
    getOccorrenzeFiltratePerUploaderEPeriodoTemporale(Long identificativoUploader,
                                                      PeriodoTemporale periodoTemporaleDiRiferimento) {

        String nomeAttributo_identificativoMittente = "identificativoMittente";
        String nomeAttributo_dataEdOraDiCaricamento = "dataEdOraDiCaricamento";

        if (UtilitaGenerale.esisteAttributoInClasse(nomeAttributo_identificativoMittente, File.class) &&
                UtilitaGenerale.esisteAttributoInClasse(nomeAttributo_dataEdOraDiCaricamento, File.class) ) {

            return DatabaseHelper.queryAnd(File.class,
                    nomeAttributo_identificativoMittente, DatabaseHelper.OperatoreQuery.UGUALE, identificativoUploader,
                    nomeAttributo_dataEdOraDiCaricamento, DatabaseHelper.OperatoreQuery.MAGGIOREOUGUALE, DateTime.convertiInString(periodoTemporaleDiRiferimento.getDataIniziale()),
                    nomeAttributo_dataEdOraDiCaricamento, DatabaseHelper.OperatoreQuery.MINOREOUGUALE, DateTime.convertiInString(periodoTemporaleDiRiferimento.getDataFinale()))
                    .stream()
                    .map( unOccorrenza -> (File)unOccorrenza )
                    .collect(Collectors.toList());

        } else {
            Logger.scriviEccezioneNelLog(File.class,
                    "Uno o più field non trovati nella classe " + File.class.getName() + ".",
                    new NoSuchFieldException());
        }

        return new ArrayList<>();   // nessun risultato dalla query

    }

    /** Interroga il database e restituisce un array con gli identificativi degli
     * {@link Uploader} che servono il {@link Consumer} dato come parametro.*/
    public static Long[] getElencoUploaderServentiConsumer(Long identificativoConsumer) {
        String nomeAttributo_identificativoUploader = "identificativoMittente";
        String nomeAttributo_identificativoConsumer = "identificativoDestinatario";
        String nomeAttributo_flagFileEliminato = "eliminato";
        if( UtilitaGenerale.esisteAttributoInClasse(nomeAttributo_identificativoConsumer, File.class) )
            if( UtilitaGenerale.esisteAttributoInClasse(nomeAttributo_identificativoUploader, File.class) )
//                // TODO : perché ofy().load().type(Entity.class).project("field1").distinct(true) non funziona?
//                return  DatabaseHelper.proiezione(
//                            DatabaseHelper.creaERestituisciQueryAnd(File.class,
//                                    nomeAttributo_identificativoConsumer, DatabaseHelper.OperatoreQuery.UGUALE, identificativoConsumer,
//                                    nomeAttributo_flagFileEliminato, DatabaseHelper.OperatoreQuery.UGUALE, false),
//                        nomeAttributo_identificativoUploader
//                        ).stream()
//                         .map(unFile -> ((File)unFile).getIdentificativoMittente())
//                         .toArray(Long[]::new);
                return DatabaseHelper.queryAnd(File.class,
                                    nomeAttributo_identificativoConsumer, DatabaseHelper.OperatoreQuery.UGUALE, identificativoConsumer,
                                    nomeAttributo_flagFileEliminato, DatabaseHelper.OperatoreQuery.UGUALE, false)
                        .stream()
                        .map(unFile -> ((File)unFile).getIdentificativoMittente())
                        .distinct()
                        .toArray(Long[]::new);
            else
                Logger.scriviEccezioneNelLog(File.class,
                        "Field \"" + nomeAttributo_identificativoUploader + "\" non trovato.",
                        new NoSuchFieldException());
        else
            Logger.scriviEccezioneNelLog(File.class,
                    "Field \"" + nomeAttributo_identificativoConsumer + "\" non trovato.",
                    new NoSuchFieldException());
        return new Long[0];
    }

    /** Se l'{@link Uploader} dato nel parametro è associato al
     * {@link File} di cui si sta chiedendo l'eliminazione, il cui
     * identificativo è specificato nel parametro, elimina il file
     * e restituisce true. Altrimenti restituisce false e non elimina
     * il file.
     * Se il file non esiste, restituisce false.*/
    public static boolean eliminaFileDiUploader(Long idFileDaEliminare, Long identificativoUploader) {

        try {

            FileStorage file = (FileStorage) File.getEntitaDaDbById(idFileDaEliminare);

            if( ! file.getIdentificativoMittente().equals(identificativoUploader) )
                return false;       // un uploader non può cancellare un file non caricato da lui

            return file.elimina();

        } catch (NotFoundException notFoundException) {
            return false;
        }

    }

    /** Crea un nuovo {@link File}, caricato dall'{@link Uploader} il
     * cui identificativo è fornito come parametro e destinato al {@link Consumer}
     * il cui identificativo è fornito come parametro.
     * @return L'entità appena salvata.*/
    public static File aggiungiFile(InputStream contenutoFile, String nomeFile, List<String> listaHashtag,
                                    Long identificativoUploader, Long identificativoConsumer ) {

        if( listaHashtag==null )
            listaHashtag = new ArrayList<>();

        listaHashtag = listaHashtag.stream()
                                   .map(EncoderPrevenzioneXSS::encodeForJava)
                                   .distinct()
                                   .collect( Collectors.toList() );

        nomeFile = EncoderPrevenzioneXSS.encodeForJava( nomeFile );

        // Salva file in DB
        byte[] contenuto = UtilitaGenerale.convertiInputStreamInByteArray( contenutoFile );
        return File.salvaFileInDb( nomeFile, contenuto, listaHashtag,
                                   identificativoUploader, identificativoConsumer);

    }

    /** Interroga il database e restituisce le occorrenze di questa entità
     * che risultano destinate al {@link Consumer} il cui identificativo è
     * specificato come parametro. */
    public static List<File> getOccorrenzeFiltratePerConsumer(Long identificativoConsumer) {
        return getOccorrenzeFiltrataPerUploaderEConsumer(null, identificativoConsumer);
    }

    /** Data una lista in cui ogni elemento è un'istanza di
     * questa classe, restituisce una mappa che ha come chiave
     * l'identificativo di un {@link Uploader} e come valore
     * corrispondente l'array degli identificativi dei {@link File}
     * inviati da quell'{@link Uploader} il cui identificativo è
     * specificato nella chiave. */
    public static Map<Long, Long[]> mappa_identificativoUploader_arrayIdFile(List<File> listaRelazioni) {

        String nomeMetodoGetterDaUsarePerRaggruppareOccorrenze = "getIdentificativoMittente";
        return mappa_identificativoAttore_arrayIdFiles(listaRelazioni, nomeMetodoGetterDaUsarePerRaggruppareOccorrenze);

    }

    /** A partire da una lista di occorrenze di questa classe data come parametro,
     * questo metodo crea una mappa in cui ogni entry ha come valore un array
     * di identificativi di {@link File} associati all'{@link Attore} il cui identificativo
     * è specificato nella chiave dell'entry: in questo modo, la lista di occorrenze date
     * viene raggruppata in base all'{@link Attore} specificato dal soprascritto metodo getter.*/
    private static Map<Long, Long[]>
    mappa_identificativoAttore_arrayIdFiles(List<File> listaFile,
                                            String nomeMetodoGetterDaUsarePerRaggruppareOccorrenze) {

        try {

            Method getter_metodoRaggruppamentoOccorrenze =
                    File.class.getDeclaredMethod(nomeMetodoGetterDaUsarePerRaggruppareOccorrenze);

            return listaFile
                    .stream()
                    .collect(Collectors.groupingBy(relazione -> {
                        try {
                            return getter_metodoRaggruppamentoOccorrenze.invoke(relazione);
                        } catch (IllegalAccessException | InvocationTargetException e) {
                            Logger.scriviEccezioneNelLog(RelazioneUploaderConsumer.class, e);
                            return null;
                        }
                    }))   // raggruppa in base all'attore ( quale attore dipende dal metodo usato )

                    // Ora che è raggruppato, crea mappa { attore => arrayFilesAssociatiAdAttore }
                    .entrySet()
                    .stream()
                    .filter(Objects::nonNull)
                    .map(entry -> {
                        Long chiave = (Long) entry.getKey();
                        Long[] arrayIdFiles = entry.getValue()
                                .stream()
                                .map(File::getIdentificativoFile)
                                .filter( Objects::nonNull ) // evita le occorrenze in cui il file è null
                                .toArray(Long[]::new);
                        return new AbstractMap.SimpleEntry<Long,Long[]>(chiave, arrayIdFiles);
                    })
                    .collect(Collectors.toMap(
                            AbstractMap.SimpleEntry::getKey,
                            AbstractMap.SimpleEntry::getValue)
                    );
        } catch (NoSuchMethodException e) {
            Logger.scriviEccezioneNelLog(RelazioneUploaderConsumer.class,
                            "Metodo \"" + nomeMetodoGetterDaUsarePerRaggruppareOccorrenze + "\" non trovato.",
                                            e );
            return new HashMap<>(); // mappa vuota
        }
    }


}