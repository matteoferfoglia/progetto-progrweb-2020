package it.units.progrweb.entities.attori.consumer;

import com.googlecode.objectify.annotation.Subclass;
import it.units.progrweb.entities.attori.Attore;
import it.units.progrweb.entities.attori.uploader.Uploader;
import it.units.progrweb.entities.file.File;
import it.units.progrweb.utils.RegexHelper;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Rappresentazione di un Consumer.
 * @author Matteo Ferfoglia
 */
@Subclass(index = true)
public abstract class Consumer extends Attore {

    /** RegEx per validare lo username di un {@link Uploader}.*/
    private static final String REGEX_VALIDITA_USERNAME_CONSUMER = RegexHelper.REGEX_CODICE_FISCALE;

    public Consumer() {
        super();
        setTipoAttore(Consumer.class.getSimpleName());
    }

    public Consumer(String username, String nominativo, String email) {
        super(username, nominativo, email, TipoAttore.Consumer);
        if( ! Pattern.matches(REGEX_VALIDITA_USERNAME_CONSUMER, username) ) {
            throw new IllegalArgumentException("Lo username deve essere il codice fiscale del Consumer da inserire.");
        }
        setTipoAttore(Consumer.class.getSimpleName());
    }

    /** Restituisce l'entità {@link Consumer} cercata nel database
     * in base all'identificativo fornito.
     * @return Il {@link Consumer} cercato, oppure null in caso di errore.*/
    public static Consumer getAttoreDaIdentificativo(Long identificativoConsumer ) {

        Attore attoreTrovatoInDb = Attore.getAttoreDaIdentificativo(identificativoConsumer);
        return attoreTrovatoInDb instanceof ConsumerStorage ?
                new ConsumerProxy((ConsumerStorage) attoreTrovatoInDb) : null;

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
        return new ConsumerStorage( username, nominativo, email );
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