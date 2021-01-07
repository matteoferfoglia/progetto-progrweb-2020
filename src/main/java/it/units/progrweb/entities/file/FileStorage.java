package it.units.progrweb.entities.file;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;
import it.units.progrweb.entities.attori.Consumer;
import it.units.progrweb.entities.attori.Uploader;
import it.units.progrweb.utils.Logger;
import it.units.progrweb.utils.datetime.DateTime;

import java.lang.reflect.Field;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.List;

/**
 * @author Matteo Ferfoglia
 */
@Entity
public class FileStorage extends File {

    // TODO : implementare questa classe!!


    /** Identificativo per il file.*/
    @Id
    private Long identificativoFile;

    /** Uploader che ha caricato il file.*/
    @Index
    private Uploader uploader;  // TODO : questo campo serve qui o meglio altrove?

    /** Consumer a cui è indirizzato il file.*/
    @Index
    private Consumer consumer;  // TODO : questo campo serve qui o meglio altrove?

    /** Indirizzo IP del consumer che ha visualizzato il file. */
    private InetAddress indirizzoIpVisualizzazione; // TODO : cambiare tipo??

    /** Lista di hashtag associati a questo file. */
    @Index
    private List<String> listaHashtag;

    /** Il documento. */
    private java.io.File documento;     // TODO : va bene usare java.io.File? Meglio usare byte[] ? Salvarlo nel db? Oppure salvarlo separatamente? O altrove? e qua mettere solo un link al documento?


    private FileStorage(){}

    /** Crea un nuovo file, con nome ed hashtags specificati. */
    public FileStorage(String nomeFile, String... hashtags) {
        super(nomeFile, DateTime.adesso());
        this.listaHashtag = Arrays.asList(hashtags);
    }


    /** Restituisce il documento associato a quest'istanza ed imposta
     * data ed ora di visualizzazione e l'indirizzo IP di chi ha visualizzato
     * il documento. */
    public java.io.File getFile(InetAddress indirizzoIpVisualizzazione) {
        // TODO : da implementare
        this.dataEdOraDiVisualizzazione = DateTime.adesso();
        this.indirizzoIpVisualizzazione = indirizzoIpVisualizzazione;
        return this.documento;
    }


    /** Elimina questo file dal database ed imposta tutti i suoi campi a null.
     * @return true se la procedura va a buon fine, false altrimenti.*/
    public boolean elimina() {
        // TODO : da implementare e testare (pensare se meglio usare strategie diverse)
        Field[] attributiDiQuestoOggetto = this.getClass().getDeclaredFields();

        Arrays  .stream(attributiDiQuestoOggetto)
                .forEach(attributo -> {
                    attributo.setAccessible(true);
                    try {
                        attributo.set(this, null);
                    } catch (IllegalAccessException exception) {
                        Logger.scriviEccezioneNelLog(this.getClass(), exception);
                    }
                });

        return true;    // todo : metodo da implementare

    }

    @Override
    public Consumer getConsumer() {
        return consumer;
    }

}