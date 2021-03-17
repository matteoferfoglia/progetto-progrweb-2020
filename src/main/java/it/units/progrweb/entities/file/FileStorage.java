package it.units.progrweb.entities.file;

import com.googlecode.objectify.annotation.Index;
import com.googlecode.objectify.annotation.Subclass;
import it.units.progrweb.persistence.DatabaseHelper;
import it.units.progrweb.utils.datetime.DateTime;

import java.util.List;

/**
 * @author Matteo Ferfoglia
 */
@Subclass(index = true)
class FileStorage extends File {

    /** Il file. */
    private byte[] file;   // byte[] automaticamente convertito in Blob nel datastore

    /** Flag: true se il file è stato eliminato.*/
    @Index
    private boolean eliminato;


    private FileStorage(){}

    /** Crea un nuovo file, con nome ed hashtags specificati. */
    public FileStorage(String nomeDocumento, byte[] contenuto, List<String> hashtags,
                       Long identificativoMittente, Long identificativoDestinatario) {
        super(nomeDocumento, DateTime.adesso(), identificativoMittente, identificativoDestinatario, hashtags);
        this.file         = contenuto;
        this.eliminato    = false;
    }

    public byte[] getFile() {
        return file != null ? file : new byte[0];
    }

    /** Vedere {@link #getContenutoFile(File, String, boolean)}.*/
    static byte[] getContenutoFile(FileStorage file,
                                        String indirizzoIpVisualizzazione,
                                        boolean salvaDatiVisualizzazione) {

        if( salvaDatiVisualizzazione && file.getDataEdOraDiVisualizzazione() == null ) {
            // Si tiene traccia di data e ora solo del primo accesso al file

            file.setDataEdOraDiVisualizzazione( DateTime.adesso() );
            file.setIndirizzoIpVisualizzazione( indirizzoIpVisualizzazione );
            DatabaseHelper.salvaEntita( file );
        }

        return file.getFile();
    }

    /** Elimina questo file dal database. Il file (inteso come bytes) viene
     * eliminato, ma tutti gli attributi rimangono nel database.
     * @return true se la procedura va a buon fine, false altrimenti.*/
    @Override
    public boolean elimina() {

        file      = null;
        eliminato = true;

        DatabaseHelper.salvaEntita(this);

        return true;

    }

    @Override
    public boolean isEliminato() {
        return eliminato;
    }

}