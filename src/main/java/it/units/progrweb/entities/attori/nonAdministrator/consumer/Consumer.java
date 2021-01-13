package it.units.progrweb.entities.attori.nonAdministrator.consumer;

import com.googlecode.objectify.annotation.Entity;
import it.units.progrweb.entities.attori.nonAdministrator.UtenteNonAdministrator;
import it.units.progrweb.entities.attori.nonAdministrator.uploader.Uploader;
import it.units.progrweb.entities.file.File;

import java.util.List;

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