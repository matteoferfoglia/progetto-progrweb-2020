package it.units.progrweb.entities.file;

import com.googlecode.objectify.annotation.Index;
import com.googlecode.objectify.annotation.Subclass;
import it.units.progrweb.persistence.DatabaseHelper;
import it.units.progrweb.utils.Logger;
import it.units.progrweb.utils.UtilitaGenerale;
import it.units.progrweb.utils.datetime.DateTime;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

/**
 * @author Matteo Ferfoglia
 */
@Subclass(index = true)
class FileStorage extends File {

    /** Lista di hashtag associati a questo file. */
    @Index
    @SuppressWarnings("FieldCanBeLocal") // attributo necessario affinché venga salvato da Objectify
    private List<String> listaHashtag;

    /** Il file. */
    private byte[] file;   // byte[] automaticamente convertito in Blob nel datastore

    /** Flag: true se il file è stato eliminato.*/
    @Index
    private boolean eliminato;


    private FileStorage(){}

    /** Crea un nuovo file, con nome ed hashtags specificati. */
    public FileStorage(String nomeDocumento, byte[] contenuto, List<String> hashtags,
                       Long identificativoMittente, Long identificativoDestinatario) {
        super(nomeDocumento, DateTime.adesso(), identificativoMittente, identificativoDestinatario);
        this.file         = contenuto;
        this.listaHashtag = hashtags;
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

    @Override
    public Map<String, ?> toMap_nomeProprieta_valoreProprieta(boolean includiMetadati) {
        Map<String, Object> mappaNomeValoreProprieta;

        mappaNomeValoreProprieta = UtilitaGenerale.getMappaNomeValoreProprieta(getAnteprimaProprietaFile(includiMetadati), this);

        // Aggiunge attributi rilevanti da questa classe
        // Accesso con reflection, così se campo non presente (es. se cambia nome) è subito individuato da un'eccezione
        try {
            Field listaHashtag = this.getClass().getDeclaredField("listaHashtag");
            listaHashtag.setAccessible(true);
            mappaNomeValoreProprieta.put(listaHashtag.getName(), listaHashtag.get(this));
        } catch (NoSuchFieldException | IllegalAccessException e) {
            Logger.scriviEccezioneNelLog(this.getClass(), e);
        }

        return mappaNomeValoreProprieta;
    }

}