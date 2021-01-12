package it.units.progrweb.entities.attori.nonAdministrator.consumer;

import com.googlecode.objectify.annotation.Entity;
import it.units.progrweb.entities.attori.nonAdministrator.UtenteNonAdministrator;
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

    /** Carica dal database tutti i {@link File} per questo uploader e li restituisce. */
    public List<File> scaricaFile() {
        return ConsumerStorage.scaricaFile(this);
    }

}