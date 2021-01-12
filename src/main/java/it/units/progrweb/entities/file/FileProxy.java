package it.units.progrweb.entities.file;

import it.units.progrweb.entities.attori.nonAdministrator.consumer.Consumer;

import java.util.List;
import java.util.Map;

/**
 * Proxy per {@link FileStorage}.
 * @author Matteo Ferfoglia
 */
public class FileProxy extends File {

    // NOTA : questa classe estende File, quindi ne eredita gli attributi "protected"

    /** Vedere {@link File#getConsumer()}.*/
    private final Consumer consumerDestinatarioFile;

    /** Vedere {@link FileStorage#getIdentificativoFile()}.*/
    private final String identificativoFile;

    /** Vedere {@link FileStorage#toMap_nomeProprieta_valoreProprieta()}.*/
    private final Map<String,?> rappresentazioneJson;

    /** Vedere {@link FileStorage#getListaHashtag()}.*/
    private final List<String> listaHashtag;


    /** Data un'istanza di {@link it.units.progrweb.entities.file.FileStorage},
     * restituice un'istanza di questa classe, che funge da proxy per quella data.
     * @param fileStorage Entità di cui creare il proxy.
     */
    public FileProxy(FileStorage fileStorage) {
        this.consumerDestinatarioFile = fileStorage.getConsumer();
        this.identificativoFile = fileStorage.getIdentificativoFile();
        this.rappresentazioneJson = fileStorage.toMap_nomeProprieta_valoreProprieta();
        this.listaHashtag = fileStorage.getListaHashtag();
    }

    @Override
    public Consumer getConsumer() {
        return consumerDestinatarioFile;
    }

    @Override
    public String getIdentificativoFile() {
        return identificativoFile;
    }

    @Override
    public Map<String, ?> toMap_nomeProprieta_valoreProprieta(){     // TODO : testare
        return rappresentazioneJson;
    }

}
