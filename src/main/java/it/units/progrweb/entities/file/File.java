package it.units.progrweb.entities.file;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;
import it.units.progrweb.entities.RelazioneUploaderConsumerFile;
import it.units.progrweb.entities.attori.Attore;
import it.units.progrweb.entities.attori.nonAdministrator.consumer.Consumer;
import it.units.progrweb.entities.attori.nonAdministrator.uploader.Uploader;
import it.units.progrweb.persistence.DatabaseHelper;
import it.units.progrweb.persistence.NotFoundException;
import it.units.progrweb.utils.Autenticazione;
import it.units.progrweb.utils.JsonHelper;
import it.units.progrweb.utils.Logger;
import it.units.progrweb.utils.UtilitaGenerale;
import it.units.progrweb.utils.datetime.DateTime;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
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
    protected String nomeDocumento;        // TODO : verificare in objectify che questi campi siano presenti nella subclass "@Entity" che estende questa class

    /** Data e ora di caricamento del file salvati come Long.*/
    @Index
    private String dataEdOraDiCaricamento;

    /** Indirizzo IP del consumer che ha visualizzato il file. */
    private String indirizzoIpVisualizzatore; // TODO : cambiare tipo??

    /** Data e ora di (eventuale) visualizzazione del file salvati come Long.*/
    private String dataEdOraDiVisualizzazione;

    protected File(){}

    protected File(String nomeDocumento, DateTime dataEdOraDiCaricamento) {
        this.nomeDocumento = nomeDocumento;
        this.dataEdOraDiCaricamento = DateTime.convertiInString( dataEdOraDiCaricamento );
    }

    public DateTime getDataEdOraDiCaricamento() {
        return DateTime.convertiDaString( dataEdOraDiCaricamento );
    }

    protected void setNomeDocumento(String nomeDocumento) {
        this.nomeDocumento = nomeDocumento;
    }

    protected void setDataEdOraDiCaricamento(DateTime dataEdOraDiCaricamento) {
        this.dataEdOraDiCaricamento = DateTime.convertiInString( dataEdOraDiCaricamento );
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
    public static InputStream getContenutoFile(File file,
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

        Map<String,String> mappa_idFile_propFileInJson =
               listaFile.stream()
                        .collect(Collectors.toMap(
                                file -> String.valueOf( file.getIdentificativoFile() ),
                                file -> JsonHelper.convertiMappaProprietaToStringaJson(file.toMap_nomeProprieta_valoreProprieta(includiMetadati)) // Ogni elemento dello stream contiene la descrizione JSON di un file
                            )
                        );
        return mappa_idFile_propFileInJson;
    }

    /**
     * Restituisce un array contenente i nomi degli attributi
     * di {@link File questa} classe
     * @param includiMetadati true se si vogliono anche i metadati.
     */
    public static String[] anteprimaNomiProprietaFile(boolean includiMetadati) {
        // TODO : testare questa classe
        return Arrays.stream(getAnteprimaProprietaFile(includiMetadati))
                     .map( field -> field.getName() )
                     .toArray(String[]::new);
    }

    /** Restituisce l'array di tutti gli attributi di {@link File questa} classe.
     * @param includiMetadati true se si vogliono anche i metadati.*/
    public static Field[] getAnteprimaProprietaFile( boolean includiMetadati ) {
        return Arrays.stream(File.class.getDeclaredFields())
                     .filter( field -> {

                         // Escludi i campi i cui nomi sono presenti in questo array
                         String[] nomeAttributiInQuestaClasseDaNonMostrare = new String[]{};

                         if( ! includiMetadati ) {

                             nomeAttributiInQuestaClasseDaNonMostrare = new String[]{
                                     "identificativoFile",
                                     "indirizzoIpVisualizzatore"
                             };

                             Arrays.stream(nomeAttributiInQuestaClasseDaNonMostrare)
                                   .forEach( nomeUnAttributo -> {
                                       if( ! UtilitaGenerale.esisteAttributoInClasse(nomeUnAttributo, File.class) ) {
                                           // Informa se il field non si trova
                                           Logger.scriviEccezioneNelLog(File.class,
                                                   "L'attributo \"" + nomeUnAttributo + "\"" +
                                                           " non si trova nella classe " + File.class.getName() +
                                                           ": forse è stato rinominato",
                                                   new NoSuchFieldException());
                                       }
                                   });

                         }

                         return ! UtilitaGenerale.isPresenteNellArray( field.getName(), nomeAttributiInQuestaClasseDaNonMostrare );

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
     * @param salvaDataOraVisualizzazione true se si vuole salvare la data/ora
     *                                    in cui il documento è richiesto (ignorato
     *                                    se tale data/ora è già salvata).*/
    public static Response creaResponseConFile(Long identificativoFile,
                                               HttpServletRequest httpServletRequest,
                                               boolean salvaDataOraVisualizzazione   ) {

        // TODO : verifica che questo metodo funzioni

        Long identificativoAttoreDaHttpServletRequest =
                Autenticazione.getIdentificativoAttoreDaTokenAutenticazione(httpServletRequest);

        try {

            RelazioneUploaderConsumerFile relazioneFileAttore = RelazioneUploaderConsumerFile
                    .attorePuoAccedereAFile( identificativoAttoreDaHttpServletRequest, identificativoFile );

            if( relazioneFileAttore != null ) {

                File file = File.getEntitaDaDbById(identificativoFile);
                InputStream inputStream = File.getContenutoFile(file, httpServletRequest.getRemoteAddr(), salvaDataOraVisualizzazione);
                return Response.ok(inputStream, MediaType.APPLICATION_OCTET_STREAM)
                               .header("Content-Disposition", "attachment; filename=\"" + file.getNomeDocumento() + "\"")
                               .build();

            } else {
                return Autenticazione.creaResponseForbidden("Accesso al file vietato.");
            }

        } catch (NotFoundException notFoundException) {
            return NotFoundException.creaResponseNotFound("File non trovato.");
        }

    }


    /** Crea, salva nel database e restituisce un'istanza di questa classe in base ai parametri.*/
    public static File salvaFileInDb( String nomeFile, byte[] contenuto, List<String> listaHashtag ) {

        FileStorage fileStorage = new FileStorage(nomeFile, contenuto, listaHashtag);
        fileStorage.setDataEdOraDiCaricamento( DateTime.adesso() );
        Long idFile = (Long) DatabaseHelper.salvaEntita( fileStorage );
        fileStorage.setIdentificativoFile( idFile );
        return fileStorage;

    }
}