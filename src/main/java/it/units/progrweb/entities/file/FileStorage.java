package it.units.progrweb.entities.file;

import com.googlecode.objectify.Ref;
import com.googlecode.objectify.annotation.*;
import it.units.progrweb.entities.attori.Consumer;
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


    // ATTRIBUTI ENTITA

        /** Identificativo per il file.*/
        @Id
        private Long identificativoFile;

        /** Indirizzo IP del consumer che ha visualizzato il file. */
        private InetAddress indirizzoIpVisualizzatore; // TODO : cambiare tipo??

        /** Lista di hashtag associati a questo file. */
        @Index
        private List<String> listaHashtag;

        /** Il documento. */
        private byte[] documento;   // byte[] automaticamente convertito in Blob nel datastore



    // RELAZIONI (usare getter e setter)
        /** Consumer a cui è indirizzato il file.*/
        @Index
        @Load
        private Ref<Consumer> consumer;



    // METODI (a seguire)


    private FileStorage(){}

    /** Crea un nuovo file, con nome ed hashtags specificati. */
    public FileStorage(String nomeDocumento, String... hashtags) {
        super(nomeDocumento, DateTime.adesso());
        this.listaHashtag = Arrays.asList(hashtags);
    }


    /** Restituisce il documento associato a quest'istanza ed imposta
     * data ed ora di visualizzazione e l'indirizzo IP di chi ha visualizzato
     * il documento. */
    public byte[] getFile(InetAddress indirizzoIpVisualizzazione) {
        // TODO : da implementare
        this.dataEdOraDiVisualizzazione = DateTime.adesso();
        this.indirizzoIpVisualizzatore = indirizzoIpVisualizzazione;
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
        return consumer.get();
    }

    public void setConsumer(Consumer consumer) {
        this.consumer = Ref.create(consumer);
    }

}
