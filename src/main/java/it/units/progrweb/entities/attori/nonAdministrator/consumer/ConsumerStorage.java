package it.units.progrweb.entities.attori.nonAdministrator.consumer;

import com.googlecode.objectify.annotation.Subclass;
import it.units.progrweb.entities.RelazioneUploaderConsumerFile;
import it.units.progrweb.entities.attori.nonAdministrator.uploader.Uploader;
import it.units.progrweb.entities.file.File;
import it.units.progrweb.persistence.NotFoundException;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Classe rappresentante un'entità {@link Consumer}
 * da memorizzare nel database.
 * @author Matteo Ferfoglia
 */
// TODO : serve Proxy pattern?
@Subclass(index = false)
public class ConsumerStorage extends Consumer {
    public ConsumerStorage(String username, String nomeCognome, String email) {
        super(username, nomeCognome, email);
    }

    /** Scarica dal database tutti i {@link File} caricati dall'{@link Uploader}
     * il cui identificativo è dato come parametro e destinati al {@link
     * Consumer} il cui identificativo è dato come parametro.
     * Infine, restituisce i {@link File} scaricati.
     */
    static List<File> getAnteprimaFiles(Long idConsumer, Long idUploader) {
        // TODO     ed a marcarli come letti (a meno che non lo siano già - scritture nel DB costano!)
        // TODO : come implementare questo metodo ? Che cosa deve fare ? Che cosa ci si aspetta in outuput??

        List<RelazioneUploaderConsumerFile> occorrenzeTrovate =
                RelazioneUploaderConsumerFile.getOccorrenzeFiltratePerUploaderEConsumer(idUploader, idConsumer);

        return occorrenzeTrovate.stream()
                                .map( occorrenza -> {
                                    try { return File.getEntitaFromDbById(occorrenza.getIdFile()); }
                                    catch (NotFoundException notFoundException) { return null; }
                                })
                                .filter( Objects::nonNull )
                                .collect( Collectors.toList() );

    }

}
