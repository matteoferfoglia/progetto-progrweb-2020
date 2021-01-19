package it.units.progrweb.entities.attori.nonAdministrator.consumer;

import com.googlecode.objectify.annotation.Entity;
import it.units.progrweb.entities.attori.Attore;
import it.units.progrweb.entities.attori.nonAdministrator.UtenteNonAdministrator;
import it.units.progrweb.entities.attori.nonAdministrator.uploader.Uploader;
import it.units.progrweb.entities.file.File;
import it.units.progrweb.persistence.DatabaseHelper;
import it.units.progrweb.persistence.NotFoundException;
import it.units.progrweb.utils.UtilitaGenerale;

import java.util.List;
import java.util.Map;

/**
 * Rappresentazione di un Consumer.
 * @author Matteo Ferfoglia
 */
@Entity
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
    public static Consumer cercaConsumerDaIdentificativo(Long identificativoConsumer ) {
        // TODO : metodo simile anche in Uploader: valutare se creare un metodo direttamente nella classe padre e passrvi Uploader.class / Consumer.class
        try{
            return (Consumer) DatabaseHelper.getById(identificativoConsumer, Consumer.class);
        } catch ( NotFoundException notFoundException) {
            return null;
        }
    }

    /** Crea e restituisce un {@link Consumer}.*/
    public static Consumer creaConsumer( String username, String nominativo, String email ) {
        return new ConsumerStorage( username, nominativo, email );
    }

    /** Ricerca nel database il {@link Consumer} corrispondente
     * all'identificativo fornito e lo restituisce se presente,
     * altrimenti restituisce null.*/
    public static String getNominativoDaIdentificativo(Long identificativoConsumer) {
        try {
            return ((Consumer)DatabaseHelper.getById( identificativoConsumer, Consumer.class )).getNominativo();
        } catch (NotFoundException notFoundException) {
            return null;
        }
    }

    /** Restituisce una mappa { "Nome attributo" -> "Valore attributo" }
     * di un'istanza di questa classe. Devono essere presenti le proprietà
     * i cui nomi sono restituiti dai metodi {@link #getNomeFieldNominativoConsumer()},
     * {@link #getNomeFieldEmailConsumer()} e {@link #getNomeFieldUsernameConsumer()}.*/
    abstract public Map<String, ?> getMappaAttributi_Nome_Valore(); // TODO : metodo analogo anche in Consumer : si può mettere nella classe padre?

    /** Nome della proprietà contenente il nome del {@link Consumer},
     * come richiesto dal metodo {@link #getMappaAttributi_Nome_Valore()}.*/
    public static String getNomeFieldNominativoConsumer() {
        return UtilitaGenerale.ricercaFieldPerNomeInQuestaClasse("nominativo", Attore.class);
    }

    /** Nome della proprietà contenente l'email del {@link Consumer},
     * come richiesto dal metodo {@link #getMappaAttributi_Nome_Valore()}.*/
    public static String getNomeFieldEmailConsumer() {
        return UtilitaGenerale.ricercaFieldPerNomeInQuestaClasse("email", Attore.class);
    }

    /** Nome della proprietà contenente lo username del {@link Consumer},
     * come richiesto dal metodo {@link #getMappaAttributi_Nome_Valore()}.*/
    public static String getNomeFieldUsernameConsumer() {
        return UtilitaGenerale.ricercaFieldPerNomeInQuestaClasse("username", Attore.class);
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