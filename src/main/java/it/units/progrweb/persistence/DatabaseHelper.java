package it.units.progrweb.persistence;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.Ref;
import com.googlecode.objectify.cache.AsyncCacheFilter;
import com.googlecode.objectify.cmd.Query;
import it.units.progrweb.utils.Logger;

import java.lang.reflect.Field;
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
        private OperatoreQuery(String operatore) {
            this.operatore = operatore;
        }
        public String operatore() { return operatore; }

    }

    /** Salva un'entità nel database. */
    public static<Entita> void salvaEntita(Entita entita) {
        database.save().entity(entita);
    }

    /** Salva subito un'entità nel database. */
    public static<Entita> void salvaEntitaAdesso(Entita entita) {
        database.save().entity(entita).now();
    }

    /** Restituisce un'entità, cercata per Id. Lancia un'eccezione
     * {@link NotFoundException} se non trova l'entità.*/
    public static Object getById(String identificativoEntita, Class classeEntita)
            throws NotFoundException {

        try {
            return database.load().type(classeEntita).id(identificativoEntita).now();
        } catch (Exception e) {
            throw new NotFoundException();
        }

    }

    /** Restituisce un'entità, cercata per Id. Lancia un'eccezione
     * {@link NotFoundException} se non trova l'entità.*/
    public static Object getById(Long identificativoEntita, Class classeEntita)
            throws NotFoundException {

        // TODO : attenzione: molto simile al metodo con lo stesso nome che prende String come parametro ... refactoring?

        try {
            return database.load().type(classeEntita).id(identificativoEntita).now();
        } catch (Exception e) {
            throw new NotFoundException();
        }

    }

    /** Restituisce la lista delle chiavi di tutte le entità della classe specificata.
     * Può essere utile per le cancellazioni per chiave (vedere*/
    public static<Entita> List<Key<Entita>> getTutteLeChiavi(Class classe) {
        return (List<Key<Entita>>) database.load().type(classe).keys(); // TODO : testare
    }

    /** Restituisce la lista delle chiavi di tutte le entità nel database.
     * Vedere {@link #getTutteLeChiavi(Class)}.*/
    public static List<Key<Object>> getTutteLeChiavi() {
        return (List<Key<Object>>) database.load().type(Object.class).keys(); // TODO : testare
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

    /** Elimina tutte le entità passate nella lista come parametro.
     * Il metodo è asincrono, invocare {@link #completaOra()} di
     * seguito per eseguire subito.*/
    public static void cancellaEntita(List<?> listaEntitaDaEliminare) {
        database.delete().entities(listaEntitaDaEliminare);
    }

    /** Elimina l'entità specificata
     * Il metodo è asincrono, invocare {@link #completaOra()} di
     * seguito per eseguire subito.*/
    public static<Entita> void cancellaEntita(Entita entitaDaEliminare) {
        database.delete().entity(entitaDaEliminare);
    }

    /** Elimina l'entità corrispondente all'identificativo specificato
     * come parametro.*/
    public static void cancellaEntitaById(Long idEntitaDaEliminare, Class classeEntita) {
        database.delete().type(classeEntita).id(idEntitaDaEliminare);
    }

    /** Restituisce il numero di entità nel database.*/
    public static int contaEntitaNelDatabase() {
        return database.load().count();
    }

    /** Restituisce il numero di entità di una data classe.*/
    public static int contaEntitaNelDatabase(Class classe) {
        return database.load().type(classe).count();    // TODO : testare
    }

    /** Restituisce true se la query cercata produce almeno
     * un risultato.
     */
    public static boolean esisteNelDatabase(Class classe, Query query) {
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
    public static<Attributo> List<?> query(Class classeEntita,
                                           String nomeAttributoCondizione,
                                           OperatoreQuery operatoreCondizione,
                                           Attributo valoreCondizione) {

        // TODO : metodo da testare
        // TODO : risultato richiede cast?

        Query query = creaERestituisciQuery(classeEntita, nomeAttributoCondizione, operatoreCondizione, valoreCondizione);
        return query!=null ? query.list() : new ArrayList<>(0);

    }

    /** Come {@link #query(Class, String, OperatoreQuery, Object)}, ma restituisce
     * la lista delle chiavi anziché delle entità.*/
    public static<Attributo> List<?> queryKey(Class classeEntita,
                                              String nomeAttributoCondizione,
                                              OperatoreQuery operatoreCondizione,
                                              Attributo valoreCondizione) {

        // TODO : metodo da testare
        // TODO : risultato richiede cast?

        Query query = creaERestituisciQuery(classeEntita, nomeAttributoCondizione, operatoreCondizione, valoreCondizione);
        return query!=null ? query.keys().list() : new ArrayList<>(0);

    }

    /** Classe di supporto per {@link #query(Class, String, OperatoreQuery, Object)}
     * e {@link #query(Class, String, OperatoreQuery, Object)}
     * effettua l'interrogazione al database e restituisce un oggetto {@link Query}.
     * @return null se non esite l'attributo su cui si esegue la query,
     *          altrimenti restituisce la {@link Query} risultante
     *          dall'interrogazione al database.*/
    private static <Attributo, T> Query<T> creaERestituisciQuery(Class classeEntita,
                                                                 String nomeAttributoCondizione,
                                                                 OperatoreQuery operatoreCondizione,
                                                                 Attributo valoreCondizione) {
        // TODO : testare

        try {

            // Verifica che l'attributo esista nella classe indicata (altrimenti genera eccezione)
            Field attributoCondizione = classeEntita.getDeclaredField(nomeAttributoCondizione);

            // Verifica che l'attributo sia dello stesso tipo di quello con  cui deve essere confrontato
            if(attributoCondizione.getType().equals(valoreCondizione.getClass())) {
                // TODO : testare che .getClass() sull'attributo il cui tipo è indovinato non restituisca cose strane

                String condizioneQuery = nomeAttributoCondizione + operatoreCondizione.operatore ;
                return database.load().type(classeEntita).filter(condizioneQuery, valoreCondizione);
            } else {
                throw new NoSuchFieldException("Il field " + nomeAttributoCondizione + " è di tipo "
                                                + attributoCondizione.getType() + " ma il valore" +
                                                " della condizione è di tipo " + valoreCondizione.getClass() );
            }
        } catch (NoSuchFieldException e) {
            Logger.scriviEccezioneNelLog(DatabaseHelper.class,
                    "Il field " + nomeAttributoCondizione + " non risulta nella classe " + classeEntita.getName(),
                    e);
            return null;    // TODO : testare (forse è necessario restituire un'istanza di Query)
        }

    }

    /** Crea e restituisce una Query, date due condizioni poste in AND.*/
    public static<Attributo> Query<?> creaERestituisciQueryAnd(Class classeEntita,
                                                    String nomeAttributo1,OperatoreQuery operatoreCondizione1, Attributo valoreCondizione1,
                                                    String nomeAttributo2,OperatoreQuery operatoreCondizione2, Attributo valoreCondizione2) {
        return creaERestituisciQuery(classeEntita, nomeAttributo1, operatoreCondizione1, valoreCondizione1)
                .filter(nomeAttributo2+operatoreCondizione2.operatore, valoreCondizione2);
    }


    /** Come {@link #query(Class, String, OperatoreQuery, Object)}, ma permette
     * di filtrare tramite AND la condizione.*/
    public static<Attributo> List<?> queryAnd(Class classeEntita,
                                   String nomeAttributo1,OperatoreQuery operatoreCondizione1, Attributo valoreCondizione1,
                                   String nomeAttributo2,OperatoreQuery operatoreCondizione2, Attributo valoreCondizione2) {

        // TODO : rifare questo metodo ed interfacciarlo meglio con gli altri. Vedi : com.googlecode.objectify.cmd.Loader
        // TODO                                                                Ad un Loader (ottenibile con .type(Class) può essere filtrato con .filter(...) )

        Query query = creaERestituisciQueryAnd(classeEntita,
                nomeAttributo1, operatoreCondizione1, valoreCondizione1,
                nomeAttributo2, operatoreCondizione2, valoreCondizione2);


        return query!=null ? query.list() : new ArrayList<>(0);

    }

    /** Come {@link #query(Class, String, OperatoreQuery, Object)}, ma permette
     * di filtrare tramite AND la condizione e specifica il massimo numero di
     * entità da restiture.*/
    public static<Attributo> List<?> queryAnd(Class classeEntita, int maxNumeroEntitaDaRestituire,
                                              String nomeAttributo1,OperatoreQuery operatoreCondizione1, Attributo valoreCondizione1,
                                              String nomeAttributo2,OperatoreQuery operatoreCondizione2, Attributo valoreCondizione2) {

        // TODO : rifare questo metodo ed interfacciarlo meglio con gli altri. Vedi : com.googlecode.objectify.cmd.Loader
        // TODO                                                                Ad un Loader (ottenibile con .type(Class) può essere filtrato con .filter(...) )

        Query query = creaERestituisciQueryAnd(classeEntita,
                nomeAttributo1, operatoreCondizione1, valoreCondizione1,
                nomeAttributo2, operatoreCondizione2, valoreCondizione2).limit(maxNumeroEntitaDaRestituire);


        return query!=null ? query.list() : new ArrayList<>(0);

    }

    /** Come {@link #query(Class, String, OperatoreQuery, Object)}, ma senza
     * specificare la classe dell'entità. Il risultato sarà una lista di {@link Object}.*/
    public static<Attributo> List<Object> query(String nomeAttributoCondizione,
                                           OperatoreQuery operatoreCondizione,
                                           Attributo valoreCondizione) {
        // TODO : metodo da testare
        return (List<Object>) query(Object.class, nomeAttributoCondizione, operatoreCondizione, valoreCondizione);
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
                               .map(entitaRef -> entitaRef.get())
                               .collect(Collectors.toList());
    }

    /** Data una lista di entita (salvate nel database), crea e restituisce
     * una lista di riferimenti a tali entità.
     * Utilizzabile nei metodi setter delle entità di che presentano dei
     * riferimenti.*/
    public static<Entita> List<Ref<Entita>> setListaRiferimentiEntitaPerListaEntita(List<Entita> listaEntita) {
        return listaEntita.stream()
                          .map(entita -> Ref.create(entita))
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