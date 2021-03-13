package it.units.progrweb.persistence;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.Ref;
import com.googlecode.objectify.cache.AsyncCacheFilter;
import com.googlecode.objectify.cmd.Query;
import it.units.progrweb.utils.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Classe di utilità per interagire con il database.
 * (<a href="https://github.com/objectify/objectify/wiki">Fonte</a>).
 *
 * @author Matteo Ferfoglia
 */
public abstract class DatabaseHelper {

    // TODO : eliminare i metodi che hanno Key in ingresso o in uscita oppure creare una classe wrapper "Key"

    /** Astrazione del database. */
    private static final Objectify database = ObjectifyService.ofy();

    /** Dimensione massima dei contenuti caricabili in byte. */
    public static final long MAX_SIZE_ENTITY = 1024*1024*1024;


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
        return database.save().entity(entita).now().getId();
    }

    /** Salva più di un'entità nel database. */
    @SafeVarargs
    public static<Entita> void salvaPiuEntita(Entita ...entita) {
        database.save().entities(entita).now();
    }

    /** Restituisce un'entità, cercata per Id. Lancia un'eccezione
     * {@link NotFoundException} se non trova l'entità.*/
    public static Object getById(String identificativoEntita, Class<?> classeEntita)
            throws NotFoundException {

        try {
            // return database.load().key( Key.create( classeEntita, identificativoEntita ) ).now();   // TODO : accertarsi che funzioni sia con id numerici sia stringhe
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

    /** Restituisce la lista delle chiavi di tutte le entità della classe specificata.
     * Può essere utile per le cancellazioni per chiave.*/
    public static List<? extends Key<?>> getTutteLeChiavi(Class<?> classe) {
        return database.load().type(classe).keys().list(); // TODO : testare
    }

    /** Restituisce la lista delle chiavi di tutte le entità nel database.
     * Vedere {@link #getTutteLeChiavi(Class)}.*/
    public static List<Key<Object>> getTutteLeChiavi() {
        return database.load().type(Object.class).keys().list(); // TODO : testare
    }

    /** Elimina tutte le entità corrispondenti alle chiavi date.
     * Il metodo è asincrono, invocare {@link #completaOra()} di
     * seguito per eseguire subito.*/
    public static void cancellaEntitaDaChiavi(List<Key<?>> listaChiaviCorrispondentiAdEntitaDaEliminare) {
        database.delete().keys(listaChiaviCorrispondentiAdEntitaDaEliminare);
    }

    /** Elimina l'entità corrispondente alla chiave data.
     * Il metodo è asincrono, invocare {@link #completaOra()} di
     * seguito per eseguire subito.*/
    public static void cancellaEntitaDaChiave(Key<?> chiaveCorrispondenteAdEntitaDaEliminare) {
        database.delete().key(chiaveCorrispondenteAdEntitaDaEliminare);
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
        return database.load().type(classe).count();    // TODO : testare
    }

    /** Restituisce la lista di entità di una certa classe (specificata nel
     * parametro) salvate nel database.*/
    public static List<?> listaEntitaNelDatabase(Class<?> classe) {
        return database.load().type(classe).list();    // TODO : testare
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
     * corrisponde all'istruzione   // TODO : vedere se funziona
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

    /** Come {@link #query(Class, String, OperatoreQuery, Object)}, ma restituisce
     * la lista delle chiavi anziché delle entità.*/
    public static<Attributo> List<?> queryKey(Class<?> classeEntita,
                                              String nomeAttributoCondizione,
                                              OperatoreQuery operatoreCondizione,
                                              Attributo valoreCondizione) {

        // TODO : metodo da testare
        // TODO : risultato richiede cast?

        Query<?> query = creaERestituisciQuery(classeEntita, nomeAttributoCondizione, operatoreCondizione, valoreCondizione);
        return query!=null ? query.keys().list() : new ArrayList<>(0);

    }

    /**
     * @return null se non esite l'attributo su cui si esegue la query,
     *          altrimenti restituisce la {@link Query} risultante
     *          dall'interrogazione al database.*/
    public static <Attributo> Query<?> creaERestituisciQuery(Class<?> classeEntita,
                                                             String nomeAttributoCondizione,
                                                             OperatoreQuery operatoreCondizione,
                                                             Attributo valoreCondizione) {
        // TODO : testare
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

    /** Come {@link #query(Class, String, OperatoreQuery, Object)}, ma permette
     * di filtrare tramite AND la condizione e specifica il massimo numero di
     * entità da restiture.*/
    public static<Attributo> List<?> queryAnd(Class<?> classeEntita, int maxNumeroEntitaDaRestituire,
                                              String nomeAttributo1,OperatoreQuery operatoreCondizione1, Attributo valoreCondizione1,
                                              String nomeAttributo2,OperatoreQuery operatoreCondizione2, Attributo valoreCondizione2) {

        // TODO : rifare questo metodo ed interfacciarlo meglio con gli altri. Vedi : com.googlecode.objectify.cmd.Loader
        // TODO                                                                Ad un Loader (ottenibile con .type(Class) può essere filtrato con .filter(...) )

        Query<?> query = creaERestituisciQueryAnd(classeEntita,
                nomeAttributo1, operatoreCondizione1, valoreCondizione1,
                nomeAttributo2, operatoreCondizione2, valoreCondizione2).limit(maxNumeroEntitaDaRestituire);


        return query!=null ? query.list() : new ArrayList<>(0);

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

    /** Come {@link #query(Class, String, OperatoreQuery, Object)}, ma senza
     * specificare la classe dell'entità. Il risultato sarà una lista di {@link Object}.*/
    public static<Attributo> List<?> query(String nomeAttributoCondizione,
                                           OperatoreQuery operatoreCondizione,
                                           Attributo valoreCondizione) {
        // TODO : metodo da testare
        return query(Object.class, nomeAttributoCondizione, operatoreCondizione, valoreCondizione);
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

    /** Data una lista di riferimenti ad entità salvate nel database,
     * restituisce la lista di entità.*/
    public static<Entita> List<Entita> getListaEntitaDaListaReference(List<Ref<Entita>> listaRiferimenti) {
        return listaRiferimenti.stream()
                               .map(Ref::get)
                               .collect(Collectors.toList());
    }

    /** Data una lista di entita (salvate nel database), crea e restituisce
     * una lista di riferimenti a tali entità.
     * Utilizzabile nei metodi setter delle entità di che presentano dei
     * riferimenti.*/
    public static<Entita> List<Ref<Entita>> setListaRiferimentiEntitaPerListaEntita(List<Entita> listaEntita) {
        return listaEntita.stream()
                          .map(Ref::create)
                          .collect(Collectors.toList());
    }

    /** Dato il riferimento ad un'entità salvata nel database,
     * restituisce l'entità. */
    public static<Entita> Entita getEntita(Ref<Entita> riferimentoEntita) {
        return riferimentoEntita.get();
    }

    /** Data un'entità (salvata nel database), crea e restituisce
     * un riferimento a tale entità.
     * Utilizzabile nei metodi setter delle entità di che presentano dei
     * riferimenti.*/
    public static<Entita> Ref<Entita> setRiferimentoEntita(Entita entita) {
        return Ref.create(entita);
    }

}