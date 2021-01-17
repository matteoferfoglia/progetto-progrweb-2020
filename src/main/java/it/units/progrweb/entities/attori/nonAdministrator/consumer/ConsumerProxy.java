package it.units.progrweb.entities.attori.nonAdministrator.consumer;

import java.util.Map;

/**
 * Classe proxy per {@link Consumer}.
 * @author Matteo Ferfoglia
 */
public class ConsumerProxy extends Consumer {

    private Map<String,?> mappaAttributiUploader_nome_valore;

    public ConsumerProxy(ConsumerStorage consumerStorage) {
        super(consumerStorage.getUsername(), consumerStorage.getNominativo(), consumerStorage.getEmail());    // TODO
        this.mappaAttributiUploader_nome_valore = consumerStorage.getMappaAttributi_Nome_Valore();
    }

    public ConsumerProxy(String username, String nominativo, String email) {   // TODO : serve questo metodo ??
        super(username, nominativo, email);
    }

    /** "zero argument constructor" per accesso da JAX-RS.*/
    public ConsumerProxy() {
        super();
    }

    @Override
    public Map<String, ?> getMappaAttributi_Nome_Valore() {
        return mappaAttributiUploader_nome_valore;
    }

}
