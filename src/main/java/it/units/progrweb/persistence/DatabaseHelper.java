package it.units.progrweb.persistence;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.cache.AsyncCacheFilter;
import com.googlecode.objectify.cmd.Query;
import it.units.progrweb.utils.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Classe di utilità per interagire con il database.
 * (<a href="https://github.com/objectify/objectify/wiki">Fonte</a>).
 *
 * @author Matteo Ferfoglia
 */
public abstract class DatabaseHelper {

    /** Astrazione del database. */
    private static final Objectify database = ObjectifyService.ofy();

    /** Enum delle operazioni possibili nelle query
     * (<a href="https://github.com/objectify/objectify/wiki/Queries#executing-queries">Fonte</a>).
     */
    public enum OperatoreQuery {
        MAGGIORE        (" >")  ,
        MAGGIOREOUGUALE (" >=") ,
        MINORE          (" <")  ,
        MINOREOUGUALE   (" <=") ,
        UGUALE          ("")    ;
        // Attualmente Objectify 6+ non supporta nè "IN" nè "!="


        private final String operatore;
        OperatoreQuery(String operatore) {
            this.operatore = operatore;
        }
        public String operatore() { return operatore; }

    }

    /** Salva un'entità (subito) nel database e ne restituisce l'identificativo. */
    public static<Entita> Object salvaEntita(Entita entita) {
        Key<?> k = database.save().entity(entita).now();
        return k.getId()==0 ? k.getName() : k.getId();  // getId() restituisce l'id o 0 se la chiave ha un nome
    }

    /** Restituisce un'entità, cercata per Id. Lancia un'eccezione
     * {@link NotFoundException} se non trova l'entità.*/
    public static Object getById(String identificativoEntita, Class<?> classeEntita)
            throws NotFoundException {

        try {
            return database.load().type(classeEntita).id(identificativoEntita).now();
        } catch (Exception e) {
            throw new NotFoundException();
        }

    }

    /** Restituisce un'entità, cercata per Id. Lancia un'eccezione
     * {@link NotFoundException} se non trova l'entità.*/
    public static Object getById(Long identificativoEntita, Class<?> classeEntita)
            throws NotFoundException {

        // TODO : attenzione: molto simile al metodo con lo stesso nome che prende String come parametro ... refactoring?

        // TODO : attenzione a quante volte viene invocato questo metodo (accedere al db costa), di seguito stacktrafce invocazioni
        //        ma riprovare mettendo qua il breakpoint e vedere quante volte viene chiamato in una request
        /* Invocato 4 volte da Filtro Autenticazione sulla stessa richiesta, serve?
            getById:94, DatabaseHelper (it.units.progrweb.persistence)
            getAttoreDaIdentificativo:151, Attore (it.units.progrweb.entities.attori)
            isStessoHashCookieIdNelToken:278, Autenticazione (it.units.progrweb.utils)
            isClientAutenticato:236, Autenticazione (it.units.progrweb.utils)
            isClientAttualmenteAutenticato:52, LoginLogout (it.units.progrweb.api)
            FiltroAutenticazione

            getById:94, DatabaseHelper (it.units.progrweb.persistence)
            getNominativoDaIdentificativo:56, Consumer (it.units.progrweb.entities.attori.nonAdministrator.consumer)
            getNomeUploader:219, GestioneConsumer (it.units.progrweb.api.uploader)
            doFilter:34, FiltroUploader (it.units.progrweb.filters.attori)
       */

        try {
            return database.load().type(classeEntita).id(identificativoEntita).now();
        } catch (Exception e) {
            throw new NotFoundException();
        }

    }

    /** Elimina l'entità corrispondente all'identificativo specificato
     * come parametro. Azione asincrona.*/
    public static void cancellaEntitaById(Long idEntitaDaEliminare, Class<?> classeEntita) {
        database.delete().type(classeEntita).id(idEntitaDaEliminare);
    }

    /** Elimina l'entità corrispondente all'identificativo specificato
     * come parametro. Azione sincrona (attende l'eliminazione e genera
     * un'eccezione se causata dalla computazione richiesta).*/
    public static void cancellaAdessoEntitaById(Long idEntitaDaEliminare, Class<?> classeEntita) {
        database.delete().type(classeEntita).id(idEntitaDaEliminare).now();
    }

    /** Restituisce il numero di entità nel database.*/
    public static int contaEntitaNelDatabase() {
        return database.load().count();
    }

    /** Restituisce il numero di entità di una data classe.*/
    public static int contaEntitaNelDatabase(Class<?> classe) {
        return database.load().type(classe).count();
    }

    /** Restituisce la lista di entità di una certa classe (specificata nel
     * parametro) salvate nel database.*/
    public static List<?> listaEntitaNelDatabase(Class<?> classe) {
        return database.load().type(classe).list();
    }

    /** Restituisce true se la query cercata produce almeno
     * un risultato.
     */
    public static boolean esisteNelDatabase(Query<?> query) {
        return query.limit(1).list().size() == 1;
    }


    /** Esegue la query e restituisce una lista in cui ogni elemento è
     * un'occorrenza del risultato della query.
     * Esempio: l'istruzione SQL <pre><code>SELECT * FROM Car WHERE year>1999</code></pre>
     * corrisponde all'istruzione
     * <pre><code>DatabaseHelper.query(Car.class, "year", OperatoreQuery.MAGGIORE, 1999)</code></pre>
     * Vedere {@link com.googlecode.objectify.cmd.Query#filter(String, Object)}.
     * @param classeEntita La classe dell'entità su cui eseguire la query.
     * @param nomeAttributoCondizione Il nome dell'attributo della classe specificata
     *                                  su cui valutare la condizione della query
     * @param operatoreCondizione L'operatore della condizione della query.
     * @param valoreCondizione Il valore della condizione della query.
     * @param <Attributo> La classe dell'attributo su cui valutare la condizione
     *                      della query
     */
    public static<Attributo> List<?> query(Class<?> classeEntita,
                                           String nomeAttributoCondizione,
                                           OperatoreQuery operatoreCondizione,
                                           Attributo valoreCondizione) {

        Query<?> query = creaERestituisciQuery(classeEntita, nomeAttributoCondizione, operatoreCondizione, valoreCondizione);
        return query!=null ? query.list() : new ArrayList<>(0);

    }

    /** Come {@link #query(Class, String, OperatoreQuery, Object)}, ma restitusce una
     * lista ordinata in modo decrescente in base al parametro specificato.
     * @param nomeAttributoOrdinamento Nome dell'attributo su cui eseguire l'ordinamento.*/
    public static<Attributo> List<?> query(Class<?> classeEntita,
                                           String nomeAttributoCondizione,
                                           OperatoreQuery operatoreCondizione,
                                           Attributo valoreCondizione,
                                           String nomeAttributoOrdinamento) {

        Query<?> query = creaERestituisciQuery(classeEntita, nomeAttributoCondizione, operatoreCondizione, valoreCondizione);
        return query!=null ? query.order("-" + nomeAttributoOrdinamento).list() : new ArrayList<>(0);

    }

    /**
     * @return null se non esite l'attributo su cui si esegue la query,
     *          altrimenti restituisce la {@link Query} risultante
     *          dall'interrogazione al database.*/
    public static <Attributo> Query<?> creaERestituisciQuery(Class<?> classeEntita,
                                                             String nomeAttributoCondizione,
                                                             OperatoreQuery operatoreCondizione,
                                                             Attributo valoreCondizione) {

        String condizioneQuery = nomeAttributoCondizione + operatoreCondizione.operatore ;
        return database.load().type(classeEntita).filter(condizioneQuery, valoreCondizione);

    }

    /** Crea e restituisce una Query, date due condizioni poste in AND.*/   // TODO : da rivedere ed eventuale refactoring
    public static<Attributo> Query<?> creaERestituisciQueryAnd(Class<?> classeEntita,
                                                    String nomeAttributo1,OperatoreQuery operatoreCondizione1, Attributo valoreCondizione1,
                                                    String nomeAttributo2,OperatoreQuery operatoreCondizione2, Attributo valoreCondizione2) {
        return creaERestituisciQuery(classeEntita, nomeAttributo1, operatoreCondizione1, valoreCondizione1)
                .filter(nomeAttributo2+operatoreCondizione2.operatore, valoreCondizione2);
    }

    /** Crea e restituisce una Query, date due condizioni poste in AND.*/
    public static<Attributo> Query<?> creaERestituisciQueryAnd(Class<?> classeEntita,
                                                               String nomeAttributo1,OperatoreQuery operatoreCondizione1, Attributo valoreCondizione1,
                                                               String nomeAttributo2,OperatoreQuery operatoreCondizione2, Attributo valoreCondizione2,
                                                               String nomeAttributo3,OperatoreQuery operatoreCondizione3, Attributo valoreCondizione3) {
        return creaERestituisciQueryAnd( classeEntita,
                                         nomeAttributo1, operatoreCondizione1, valoreCondizione1,
                                         nomeAttributo2, operatoreCondizione2, valoreCondizione2 )
                .filter(nomeAttributo3+operatoreCondizione3.operatore, valoreCondizione3);
    }


    /** Come {@link #query(Class, String, OperatoreQuery, Object)}, ma permette
     * di filtrare tramite AND la condizione.*/
    public static<Attributo> List<?> queryAnd(Class<?> classeEntita,
                                              String nomeAttributo1,OperatoreQuery operatoreCondizione1, Attributo valoreCondizione1,
                                              String nomeAttributo2,OperatoreQuery operatoreCondizione2, Attributo valoreCondizione2) {

        // TODO : rifare questo metodo ed interfacciarlo meglio con gli altri. Vedi : com.googlecode.objectify.cmd.Loader
        // TODO                                                                Ad un Loader (ottenibile con .type(Class) può essere filtrato con .filter(...) )

        Query<?> query = creaERestituisciQueryAnd(classeEntita,
                nomeAttributo1, operatoreCondizione1, valoreCondizione1,
                nomeAttributo2, operatoreCondizione2, valoreCondizione2);


        return query!=null ? query.list() : new ArrayList<>(0);

    }

    /** Come {@link #queryAnd(Class, String, OperatoreQuery, Object, String, OperatoreQuery, Object)},
     * ma restituisce i risultati ordinati in modo decrescente in base all'attributo fornito.*/
    public static<Attributo> List<?> queryAnd(Class<?> classeEntita,
                                              String nomeAttributo1,OperatoreQuery operatoreCondizione1, Attributo valoreCondizione1,
                                              String nomeAttributo2,OperatoreQuery operatoreCondizione2, Attributo valoreCondizione2,
                                              String nomeAttributoOrdinamento) {

        // TODO : rifare questo metodo ed interfacciarlo meglio con gli altri. Vedi : com.googlecode.objectify.cmd.Loader
        // TODO                                                                Ad un Loader (ottenibile con .type(Class) può essere filtrato con .filter(...) )

        Query<?> query = creaERestituisciQueryAnd(classeEntita,
                nomeAttributo1, operatoreCondizione1, valoreCondizione1,
                nomeAttributo2, operatoreCondizione2, valoreCondizione2);


        return query!=null ? query.order("-" + nomeAttributoOrdinamento).list() : new ArrayList<>(0);

    }

    /** Come {@link #queryAnd(Class, String, OperatoreQuery, Object, String, OperatoreQuery, Object)}, ma permette
     * di filtrare tramite fino a tre condizioni.*/
    public static<Attributo> List<?> queryAnd(Class<?> classeEntita,
                                              String nomeAttributo1,OperatoreQuery operatoreCondizione1, Attributo valoreCondizione1,
                                              String nomeAttributo2,OperatoreQuery operatoreCondizione2, Attributo valoreCondizione2,
                                              String nomeAttributo3,OperatoreQuery operatoreCondizione3, Attributo valoreCondizione3) {

        // TODO : rifare questo metodo ed interfacciarlo meglio con gli altri. Vedi : com.googlecode.objectify.cmd.Loader
        // TODO                                                                Ad un Loader (ottenibile con .type(Class) può essere filtrato con .filter(...) )

        Query<?> query = creaERestituisciQueryAnd(classeEntita,
                nomeAttributo1, operatoreCondizione1, valoreCondizione1,
                nomeAttributo2, operatoreCondizione2, valoreCondizione2,
                nomeAttributo3, operatoreCondizione3, valoreCondizione3);


        return query!=null ? query.list() : new ArrayList<>(0);

    }

    /**
     * Porta a termine <em>adesso</em> tutte le operazioni differite nel database.
     * Nota: in ogni caso il sistema garantisce di portare a termine le
     * transazioni in corso, anche senza usare questo metodo, ma l'invocazione
     * di questo metodo lo fa subito (in modo sincrono).
     *
     * @return true se l'esecuzione va a buon fine.*/
    public static boolean completaOra() {     // TODO : indagare meglio su questo metodo

        final long MILLISECONDI_RITARDO_FORZATO = 50;   // TODO : verificare tempi di accesso richiesti dal Datastore reale

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