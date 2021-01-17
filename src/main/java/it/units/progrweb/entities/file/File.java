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
import it.units.progrweb.utils.Logger;
import it.units.progrweb.utils.datetime.DateTime;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
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
                     .map( field -> field.getName() )
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

    /** Restituisce il {@link File} (cioè il suo contenuto), il cui identificativo
     * è specificato nel parametro, in una {@link Response}
     * (<a href="https://stackoverflow.com/a/12251265">Fonte (restituzione file tramite JAX-RS)</a>).
     * E' richiesta la HttpServletRequest che ha richiesto il {@link File}
     * per venire a conoscenza dell'indirizzo IP del client a cui il
     * file verrà recapitato.
     * Prima di restituire il file, si verifica che l'{@link Attore} che lo ha
     * richiesto sia in relazione con quel file (o {@link Consumer} o
     * {@link Uploader}) (non si possono recuperare i file di altri utenti).*/
    public static Response creaResponseConFile(Long identificativoFile, HttpServletRequest httpServletRequest) {

        // TODO : verifica che questo metodo funzioni

        String usernameAttoreDaHttpServletRequest =
                Autenticazione.getUsernameAttoreDaTokenAutenticazione(httpServletRequest);

        try {

            RelazioneUploaderConsumerFile relazioneFileAttore = RelazioneUploaderConsumerFile
                    .attorePuoAccedereAFile( usernameAttoreDaHttpServletRequest, identificativoFile );

            if( relazioneFileAttore != null ) {

                File file = File.getEntitaFromDbById(identificativoFile);
                InputStream inputStream = File.getContenutoFile(file, httpServletRequest.getRemoteAddr());
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
}