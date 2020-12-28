package it.units.progrweb.persistence;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.ObjectifyService;

/**
 * Classe di utilità per interrogare il database.
 *
 * @author Matteo Ferfoglia
 */
public class DatabaseHelper {

    /** Astrazione del database. */
    private static final Objectify database = ObjectifyService.ofy();

    /** Salva un'entità nel database. */
    public static<Entita> void salvaEntita(Entita entita) {
        database.save().entity(entita);
    }

    /** Salva subito un'entità nel database. */
    public static<Entita> void salvaEntitaAdesso(Entita entita) {
        database.save().entity(entita).now();
    }

    /** Restituisce un'entità, cercata per chiave. */
    public static<Entita> Entita getByKey(String identificativoEntita) {
        Key<Entita> key = Key.create(identificativoEntita);
        return database.load().key(key).now();
    }

}