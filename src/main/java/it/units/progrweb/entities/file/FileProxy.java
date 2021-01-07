package it.units.progrweb.entities.file;

import it.units.progrweb.entities.attori.Consumer;

/**
 * Proxy per {@link FileStorage}.
 * @author Matteo Ferfoglia
 */
public class FileProxy extends File {

    // NOTA : questa classe estende File, quindi ne eredita gli attributi "protected"

    private final Consumer consumerDestinatarioFile;

    /** Data un'istanza di {@link it.units.progrweb.entities.file.FileStorage},
     * restituice un'istanza di questa classe, che funge da proxy per quella data.
     * @param fileStorage Entità di cui creare il proxy.
     */
    public FileProxy(FileStorage fileStorage) {
        this.consumerDestinatarioFile = fileStorage.getConsumer();

    }

    @Override
    public Consumer getConsumer() {
        return consumerDestinatarioFile;
    }
}
