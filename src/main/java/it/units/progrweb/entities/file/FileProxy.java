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

    /** Vedere {@link FileStorage#toMap_nomeProprieta_valoreProprieta(boolean)}.*/
    private final Map<String,?> rappresentazioneJson;

    /** Vedere {@link FileStorage#getListaHashtag()}.*/
    private final List<String> listaHashtag;


    /** Data un'istanza di {@link it.units.progrweb.entities.file.FileStorage},
     * restituisce un'istanza di questa classe, che funge da proxy per quella data.
     * @param fileStorage Entità di cui creare il proxy.
     * @param includiMetadati true se si vogliono includere i metadati del file.
     */
    public FileProxy(FileStorage fileStorage, boolean includiMetadati) {
        this.identificativoFile = fileStorage.getIdentificativoFile();
        this.rappresentazioneJson = fileStorage.toMap_nomeProprieta_valoreProprieta(includiMetadati);
        this.listaHashtag = fileStorage.getListaHashtag();
    }

    /** L'istanza proxy restituisce sempre false.*/
    @Override
    public boolean isEliminato() {
        return false;
    }

    /** L'istanza proxy restituisce sempre false.*/
    @Override
    public boolean elimina() {
        return false;
    }

    @Override
    public Map<String, ?> toMap_nomeProprieta_valoreProprieta(boolean includiMetadati){     // TODO : testare
        return rappresentazioneJson;
    }

}
