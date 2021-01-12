package it.units.progrweb.entities.attori.nonAdministrator.consumer;

import com.googlecode.objectify.annotation.Subclass;
import it.units.progrweb.entities.file.File;

import java.util.ArrayList;
import java.util.List;

/**
 * Classe rappresentante un'entità {@link Consumer}
 * da memorizzare nel database.
 * @author Matteo Ferfoglia
 */
// TODO : serve Proxy pattern?
@Subclass(index = false)
public class ConsumerStorage extends Consumer {
    public ConsumerStorage(String username, String nomeCognome, String email) {
        super(username, nomeCognome, email);
    }

    /** Scarica i file per il {@link Consumer} dato.*/
    static List<File> scaricaFile(Consumer consumer) {
        // TODO : implementare questo metodo: cercare tutti i file destinati a questo uploader e caricarli
        // TODO : devi invocare qualche metodo in File che provveda a caricare tutti i file dal DB
        // TODO     ed a marcarli come letti (a meno che non lo siano già - scritture nel DB costano!)
        // TODO : come implementare questo metodo ? Che cosa deve fare ? Che cosa ci si aspetta in outuput??
        return new ArrayList<>();   // TODO : non ancora implementato!!
    }
}
