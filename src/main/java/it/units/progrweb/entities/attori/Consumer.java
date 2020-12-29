package it.units.progrweb.entities.attori;

import com.googlecode.objectify.annotation.Subclass;
import it.units.progrweb.entities.File;

import java.util.ArrayList;
import java.util.List;

/**
 * Rappresentazione di un Consumer.
 * @author Matteo Ferfoglia
 */
@Subclass(index=true) // TODO : index?
public class Consumer extends UtenteNonAdministrator {

    // TODO : implementare questa classe

    private Consumer() {
        super();
    }

    public Consumer(String username, String nomeCognome, String email) {
        super(username, nomeCognome, email);
    }

    /** Scarica tutti i file per questo uploader. */
    public List<File> scaricaFile() {
        // TODO : implementare questo metodo: cercare tutti i file destinati a questo uploader e scaricarli
        // TODO : come implementare questo metodo ? Che cosa deve fare ? Che cosa ci si aspetta in outuput??
        return new ArrayList<>();   // TODO : non ancora implementato!!
    }

}