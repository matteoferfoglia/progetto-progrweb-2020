package it.units.progrweb.entities;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import it.units.progrweb.persistence.DatabaseHelper;
import it.units.progrweb.persistence.NotFoundException;

/**
 * Classe per memorizzare informazioni di sistema.
 * Ogni istanza di questa classe rappresenta un parametro di sistema.
 *
 * @author Matteo Ferfoglia
 */

@SuppressWarnings({
        "FieldCanBeLocal", "unused",    // gli attributi devono essere salvati in DB
        "SameParameterValue",           // meglio parametrizzare
})
@Entity
public class ProprietaSistema<ValoreParametro> {

    /** Nome del parametro. */
    @Id     // Va bene finché c'è un solo parametro
    private String nomeParametro;

    /** Valore del parametro. */
    private ValoreParametro valoreParametro;

    /** Parametri possibili. */
    private enum Parametro {
        /** Numero complessivo di istanze avviate dal servlet container.
         * Google App Engine può avviare più istanze per
         * adattarsi al carico.
         * <a href="https://cloud.google.com/appengine/docs/standard/java/an-overview-of-app-engine#instances">Fonte</a>*/
        NUMERO_COMPLESSIVO_ISTANZE_AVVIATE
    }

    private ProprietaSistema() {}

    private ProprietaSistema(Parametro nomeParametro, ValoreParametro valoreParametro) {
        this.nomeParametro = nomeParametro.name();
        this.valoreParametro = valoreParametro;
    }

    /** Dato un parametro, lo cerca nel database e lo restituisce.
     * Se non lo trova, restituisce null. */
    private static ProprietaSistema<?> recuperaProprietaDaDB(Parametro parametro) {

        Object prop = null;
        try {
            prop = DatabaseHelper.getById(parametro.name(), ProprietaSistema.class);
        } catch (NotFoundException ignored) {}

        if( prop instanceof ProprietaSistema<?> )
            return (ProprietaSistema<?>) prop;
        else
            return null;

    }

    /** Restituisce l'istanza di questa classe rappresentante il numero
     * di istanze avviate dal servlet container. Vedere anche
     * {@link Parametro#NUMERO_COMPLESSIVO_ISTANZE_AVVIATE}. */
    private static ProprietaSistema<Long> getProprietaNumeroIstanze() {
        ProprietaSistema<?> proprietaSistema = recuperaProprietaDaDB(Parametro.NUMERO_COMPLESSIVO_ISTANZE_AVVIATE);
        if( proprietaSistema!=null && proprietaSistema.valoreParametro instanceof Long ) {
            // cast verificato
            //noinspection unchecked
            return (ProprietaSistema<Long>) proprietaSistema;
        }
        return new ProprietaSistema<>(Parametro.NUMERO_COMPLESSIVO_ISTANZE_AVVIATE, 0L);
    }

    /** Restitusce il numero di istanze, simile a {@link #getProprietaNumeroIstanze()},
     * ma questo metodo restituisce il valore numerico. */
    public static long getNumeroIstanze() {
        return getProprietaNumeroIstanze().valoreParametro;
    }

    /** Incrementa il numero di istanze avviate. Vedere anche
     * {@link Parametro#NUMERO_COMPLESSIVO_ISTANZE_AVVIATE}. */
    public static void incrementaNumeroIstanze() {
        ProprietaSistema<Long> prop = getProprietaNumeroIstanze();
        prop.valoreParametro ++;
        prop.salva();
    }

    /** Se l'istanza viene modificata, questo metodo la salva nel database. */
    public void salva() {
        DatabaseHelper.salvaEntita(this);
    }

}
