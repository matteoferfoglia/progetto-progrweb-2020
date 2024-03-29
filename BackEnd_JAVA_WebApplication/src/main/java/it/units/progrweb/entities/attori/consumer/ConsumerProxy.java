package it.units.progrweb.entities.attori.consumer;

/**
 * Classe proxy per {@link Consumer}.
 * @author Matteo Ferfoglia
 */
public class ConsumerProxy extends Consumer {


    public ConsumerProxy(ConsumerStorage consumerStorage) {
        super(consumerStorage.getUsername(), consumerStorage.getNominativo(), consumerStorage.getEmail());
    }

    /** "zero argument constructor" per accesso da JAX-RS.*/
    @SuppressWarnings("unused") // usato da JAX-RS
    public ConsumerProxy() {
        super();
    }

}
