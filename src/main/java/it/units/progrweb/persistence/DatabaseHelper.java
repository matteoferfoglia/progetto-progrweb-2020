package it.units.progrweb.persistence;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.cache.AsyncCacheFilter;
import it.units.progrweb.utils.Logger;

import java.util.concurrent.TimeUnit;

/**
 * Classe di utilità per interagire con il database.
 *
 * @author Matteo Ferfoglia
 */
public abstract class DatabaseHelper {

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

    /** Restituisce il numero di entità nel datastore.*/
    public static int contaEntitaNelDatabase() {
        return database.load().count();
    }


    /**
     * Porta a termine <em>adesso</em> tutte le operazioni differite nel database.
     * Nota: in ogni caso il sistema garantisce di portare a termine le
     * transazioni in corso, anche senza usare questo metodo, ma l'invocazione
     * di questo metodo lo fa subito (in modo sincrono).
     *
     * @return true se l'esecuzione va a buon fine.*/
    public static boolean completaOra() {     // TODO : indagare meglio su questo metodo

        final long MILLISECONDI_RITARDO_FORZATO = 10;

        AsyncCacheFilter.complete();    // Fonte: https://groups.google.com/g/objectify-appengine/c/a4CaFbZdqh0/m/Ih_vEaoBRCEJ
        try {
            // Pur seguendo la documentazione di Objectify, dai test si è visto che
            // circa il 10% delle volte la modifica non si propagava in modo sincrono
            // sul database, quindi si aggiunge un breve ritardo (~ millisecondi)
            // tale che al termine di questo metodo la modifica sia propagata nel
            // database.
            TimeUnit.MILLISECONDS.sleep(MILLISECONDI_RITARDO_FORZATO);
        } catch (InterruptedException e) {
            Logger.scriviEccezioneNelLog(DatabaseHelper.class,
                    "Eccezione nel completamento immediato di un'operazione" +
                                    " nel database, eccezione generata dal ritardo imposto nel codice", e);
        }
        return true;
    }

}