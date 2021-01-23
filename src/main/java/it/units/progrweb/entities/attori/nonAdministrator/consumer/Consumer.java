package it.units.progrweb.entities.attori.nonAdministrator.consumer;

import com.googlecode.objectify.annotation.Subclass;
import it.units.progrweb.entities.attori.Attore;
import it.units.progrweb.entities.attori.nonAdministrator.UtenteNonAdministrator;
import it.units.progrweb.entities.attori.nonAdministrator.uploader.Uploader;
import it.units.progrweb.entities.file.File;

import java.util.List;

/**
 * Rappresentazione di un Consumer.
 * @author Matteo Ferfoglia
 */
@Subclass
public abstract class Consumer extends UtenteNonAdministrator {

    // TODO : implementare questa classe

    public Consumer() {
        super();
        setTipoAttore(Consumer.class.getSimpleName());
    }

    public Consumer(String username, String nominativo, String email) {
        super(username, nominativo, email);
        setTipoAttore(Consumer.class.getSimpleName());
    }

    /** Restituisce l'entità {@link Consumer} cercata nel database
     * in base all'identificativo fornito.
     * @return Il consumer cercato, oppure null in caso di errore.*/
    public static Consumer getAttoreDaIdentificativo(Long identificativoConsumer ) {

        Attore attoreTrovatoInDb = Attore.getAttoreDaIdentificativo(identificativoConsumer);
        return attoreTrovatoInDb instanceof Consumer ? (Consumer) attoreTrovatoInDb : null;

    }

    /** Restituisce l'entità {@link Consumer} cercata nel database
     * in base allo username fornito.
     * @return Il consumer cercato, oppure null in caso di errore.*/
    public static Consumer getAttoreDaUsername( String username ) {

        Attore attoreTrovatoInDb = Attore.getAttoreDaUsername(username);
        return attoreTrovatoInDb instanceof Consumer ? (Consumer) attoreTrovatoInDb : null;

    }

    /** Crea e restituisce un {@link Consumer}.*/
    public static Consumer creaConsumer( String username, String nominativo, String email ) {
        return new ConsumerProxy( username, nominativo, email );
    }

    /** Dato l'identificativo di un {@link Uploader}, carica dal database
     * tutti i {@link File} caricati da quell'{@link Uploader} per questo
     * {@link Consumer} e li restituisce.
     * @param identificativoUploader Username dell'{@link Uploader} associato
     *                         ai {@link File} da restituire.
     */
    public List<File> getAnteprimaFiles(Long identificativoUploader) {
        Long identificativoQuestoConsumer = getIdentificativoAttore();
        return ConsumerStorage.getAnteprimaFiles(identificativoQuestoConsumer, identificativoUploader);
    }

}