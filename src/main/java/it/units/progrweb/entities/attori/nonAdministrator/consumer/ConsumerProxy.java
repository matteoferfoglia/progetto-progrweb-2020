package it.units.progrweb.entities.attori.nonAdministrator.consumer;

/**
 * Classe proxy per {@link Consumer}.
 * @author Matteo Ferfoglia
 */
public class ConsumerProxy extends Consumer {

    public ConsumerProxy(ConsumerStorage consumerStorage) {
        super(consumerStorage.getUsername(), consumerStorage.getNomeCognome(), consumerStorage.getEmail());    // TODO
    }

    public ConsumerProxy(String username, String nomeCognome, String email) {
        super(username, nomeCognome, email);
    }
}
