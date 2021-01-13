package it.units.progrweb.entities;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;
import it.units.progrweb.entities.attori.nonAdministrator.consumer.Consumer;
import it.units.progrweb.entities.attori.nonAdministrator.uploader.Uploader;
import it.units.progrweb.entities.file.File;
import it.units.progrweb.persistence.DatabaseHelper;
import it.units.progrweb.utils.Logger;
import it.units.progrweb.utils.UtilitaGenerale;

import java.util.ArrayList;
import java.util.List;

/**
 * Classe di comodo utilizzata per creare una relazione
 * che coinvolga {@link Uploader}, {@link Consumer} e
 * {@link File}, con l'obiettivo di minimizzare i costi
 * relativi al database, sia in termini di numero di
 * indici, sia in termini di numero di accessi per
 * l'esecuzione delle query.
 * @author Matteo Ferfoglia
 */
@Entity
public class RelazioneUploaderConsumerFile {

    /** Id per un'occorrenza.*/
    @Id
    private Long idRelazione;

    /** Identificativo per {@link Uploader}.*/
    @Index
    private Long idUploader;

    /** Identificativo per {@link Consumer}.*/
    @Index
    private Long idConsumer;

    /** Identificativo per {@link File}.*/
    private Long idFile;

    public Long getIdUploader() {
        return idUploader;
    }

    public Long getIdConsumer() {
        return idConsumer;
    }

    public Long getIdFile() {
        return idFile;
    }

    public RelazioneUploaderConsumerFile() {}

    /** Interroga il database e restituisce le occorrenze di questa entità
     * che risultano caricate dall'{@link Uploader} e destinate al {@link
     * Consumer} i cui identificativi sono specificati come parametri.
     * @return
     */
    public static List<RelazioneUploaderConsumerFile> getOccorrenzeFiltratePerUploaderEConsumer(Long idUploader,
                                                                                                Long idConsumer) {

        // TODO : verificare che funzioni

        // Parametri query
        String nomeAttributo1 = "idUploader";
        String nomeAttributo2 = "idConsumer";

        if( UtilitaGenerale.esisteAttributoInClasse(nomeAttributo1, RelazioneUploaderConsumerFile.class) ) {
            if (UtilitaGenerale.esisteAttributoInClasse(nomeAttributo2, RelazioneUploaderConsumerFile.class)) {

                return (List<RelazioneUploaderConsumerFile>)
                        DatabaseHelper.queryAnd(RelazioneUploaderConsumerFile.class,
                                nomeAttributo1, DatabaseHelper.OperatoreQuery.UGUALE, idUploader,
                                nomeAttributo2, DatabaseHelper.OperatoreQuery.UGUALE, idConsumer);

            } else {
                Logger.scriviEccezioneNelLog(RelazioneUploaderConsumerFile.class,
                        "Field \"" + nomeAttributo2 + "\" non trovato.",
                        new NoSuchFieldException());
            }
        } else {
            Logger.scriviEccezioneNelLog(RelazioneUploaderConsumerFile.class,
                    "Field \"" + nomeAttributo1 + "\" non trovato.",
                    new NoSuchFieldException());
        }

        return new ArrayList<>();   // nessun risultato dalla query

    }

}