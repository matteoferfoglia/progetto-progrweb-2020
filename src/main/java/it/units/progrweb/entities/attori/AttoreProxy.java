package it.units.progrweb.entities.attori;

/**
 * Classe proxy: non memorizzabile nel database,
 * ma permette di istanziare oggetti della classe
 * {@link Attore}.
 * @author Matteo Ferfoglia
 */
public class AttoreProxy extends Attore{

    /** Crea un'istanza proxy di {@link Attore}. */
    public AttoreProxy(Attore attore) {
        super(attore);
    }

    /** "zero argument constructor" per accesso da JAX-RS.*/
    @SuppressWarnings("unused") // usato da JAX-RS
    public AttoreProxy() {
        super();
    }

}
