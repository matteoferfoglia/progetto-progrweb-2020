package it.units.progrweb.entities.file;

import it.units.progrweb.entities.attori.Consumer;

/**
 * Proxy per {@link FileStorage}.
 * @author Matteo Ferfoglia
 */
public class FileProxy extends File {

    private final FileStorage fileStorage;

    public FileProxy(String nomeFile, String... hashtags) {
        this.fileStorage = new FileStorage(nomeFile, hashtags);
    }

    @Override
    public Consumer getConsumer() {
        return fileStorage.getConsumer();
    }
}
