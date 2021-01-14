package it.units.progrweb.entities.attori.nonAdministrator.consumer;

import com.googlecode.objectify.annotation.Entity;
import it.units.progrweb.entities.attori.nonAdministrator.UtenteNonAdministrator;
import it.units.progrweb.entities.attori.nonAdministrator.uploader.Uploader;
import it.units.progrweb.entities.file.File;
import it.units.progrweb.persistence.DatabaseHelper;
import it.units.progrweb.persistence.NotFoundException;

import java.util.List;
import java.util.Map;

/**
 * Rappresentazione di un Consumer.
 * @author Matteo Ferfoglia
 */
@Entity
public abstract class Consumer extends UtenteNonAdministrator {

    // TODO : implementare questa classe

    private Consumer() {
        super();
    }

    public Consumer(String username, String nomeCognome, String email) {
        super(username, nomeCognome, email);
    }

    /** Restituisce l'entità {@link Consumer} cercata nel database
     * in base all'identificativo fornito.
     * @return Il consumer cercato, oppure null in caso di errore.*/
    public static Consumer cercaConsumerById( Long identificativoConsumer ) {
        // TODO : metodo simile anche in Uploader: valutare se creare un metodo direttamente nella classe padre e passrvi Uploader.class / Consumer.class
        try{
            return (Consumer) DatabaseHelper.getById(identificativoConsumer, Consumer.class);
        } catch ( NotFoundException notFoundException) {
            return null;
        }
    }

    /** Restituisce una mappa { "Nome attributo" -> "Valore attributo" }
     * di un'istanza di questa classe.*/
    abstract public Map<String, ?> getMappaAttributi_Nome_Valore(); // TODO : metodo analogo anche in Consumer : si può mettere nella classe padre?

    /** Dato l'identificativo di un {@link Uploader}, carica dal database
     * tutti i {@link File} caricati da quell'{@link Uploader} per questo
     * {@link Consumer} e li restituisce.
     * @param identificativoUploader Identifocativo dell'{@link Uploader}
     *                               dei {@link File} da restituire.
     */
    public List<File> getAnteprimaFiles(Long identificativoUploader) {
        Long identificativoQuestoConsumer = getIdentificativoAttore();
        return ConsumerStorage.getAnteprimaFiles(identificativoQuestoConsumer, identificativoUploader);
    }

}