package it.units.progrweb.entities.file;

import java.util.List;
import java.util.Map;

/**
 * Proxy per {@link FileStorage}.
 * @author Matteo Ferfoglia
 */
public class FileProxy extends File {

    /** Vedere {@link FileStorage#getIdentificativoFile()}.*/
    private final Long identificativoFile;

    /** Vedere {@link FileStorage#toMap_nomeProprieta_valoreProprieta()}.*/
    private final Map<String,?> rappresentazioneJson;

    /** Vedere {@link FileStorage#getListaHashtag()}.*/
    private final List<String> listaHashtag;


    /** Data un'istanza di {@link it.units.progrweb.entities.file.FileStorage},
     * restituisce un'istanza di questa classe, che funge da proxy per quella data.
     * @param fileStorage Entità di cui creare il proxy.
     */
    public FileProxy(FileStorage fileStorage) {
        this.identificativoFile = fileStorage.getIdentificativoFile();
        this.rappresentazioneJson = fileStorage.toMap_nomeProprieta_valoreProprieta();
        this.listaHashtag = fileStorage.getListaHashtag();
    }

    @Override
    public Map<String, ?> toMap_nomeProprieta_valoreProprieta(){     // TODO : testare
        return rappresentazioneJson;
    }

}
