package it.units.progrweb.entities.file;

import com.googlecode.objectify.annotation.Index;
import com.googlecode.objectify.annotation.Subclass;
import it.units.progrweb.persistence.DatabaseHelper;
import it.units.progrweb.utils.Logger;
import it.units.progrweb.utils.UtilitaGenerale;
import it.units.progrweb.utils.datetime.DateTime;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author Matteo Ferfoglia
 */
@Subclass(index = true)
class FileStorage extends File {

    // TODO : implementare questa classe!!

    /** Lista di hashtag associati a questo file. */
    @Index
    private List<String> listaHashtag;

    /** Il file. */
    private byte[] file;   // byte[] automaticamente convertito in Blob nel datastore


    private FileStorage(){}

    /** Crea un nuovo file, con nome ed hashtags specificati. */
    public FileStorage(String nomeDocumento, byte[] contenuto, List<String> hashtags) {
        super(nomeDocumento, DateTime.adesso());
        this.file = contenuto;
        this.listaHashtag = hashtags;
    }

    public byte[] getFile() {
        return file;
    }

    /** Vedere {@link #getContenutoFile(File, String, boolean)}.*/
    static InputStream getContenutoFile(FileStorage file,
                                        String indirizzoIpVisualizzazione,
                                        boolean salvaDatiVisualizzazione) {

        if( salvaDatiVisualizzazione && file.getDataEdOraDiVisualizzazione() == null ) {
            // Si tiene traccia solo del primo accesso al file
            file.setDataEdOraDiVisualizzazione( DateTime.adesso() );
            file.setIndirizzoIpVisualizzazione( indirizzoIpVisualizzazione );
            DatabaseHelper.salvaEntita( file );
        }

        // Conversione da byte[]
        return new ByteArrayInputStream(file.getFile());
    }

    /** Elimina questo file dal database ed imposta tutti i suoi campi a null.
     * @return true se la procedura va a buon fine, false altrimenti.*/
    public boolean elimina() {
        // TODO : da implementare e testare (pensare se meglio usare strategie diverse)
        Field[] attributiDiQuestoOggetto = this.getClass().getDeclaredFields();

        Arrays  .stream(attributiDiQuestoOggetto)
                .forEach(attributo -> {
                    attributo.setAccessible(true);
                    try {
                        attributo.set(this, null);
                    } catch (IllegalAccessException exception) {
                        Logger.scriviEccezioneNelLog(this.getClass(), exception);
                    }
                });

        return true;    // todo : metodo da implementare

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


        return mappaNomeValoreProprieta;        // TODO : testare che funzioni (controllare il tipo del valore)
    }

    public List<String> getListaHashtag() {
        return listaHashtag;
    }
}
