package it.units.progrweb.persistence;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.ObjectifyService;

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
     * Porta a termine <em>adesso</em> tutte le operazioni differite nel database
     * (cioé porta a termine eventuali transazioni non ancora terminate).
     * Nota: in ogni caso il sistema garantisce di portare a termine le
     * transazioni in corso, anche senza usare questo metodo, ma l'invocazione
     * di questo metodo lo fa subito (in modo sincrono).
     *
     * @return true se l'esecuzione va a buon fine, false altrimenti.*/
    public static boolean flush() {
        database.flush();
        return true;
    }


    /** Da invocare al bootstrap del server, per registrare
     * un'entità che dovrà essere gestita dal database.
     * Per Objectify, vedere {@link ObjectifyService#register(Class)} e
     * {@link com.googlecode.objectify.ObjectifyFactory#register(Class) ObjectifyFactory#register(Class)}.*/
     public static void registraEntitaGestitaDaDatabase(Class<?> enititaGestitaDaDatabase) {
         ObjectifyService.register(enititaGestitaDaDatabase);
     }

}