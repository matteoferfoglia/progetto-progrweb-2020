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
class ConsumerStorage extends Consumer {

    private ConsumerStorage(String username, String nominativo, String email) {
        super(username, nominativo, email);
    }

    /** Copy-constructor.*/
    private ConsumerStorage( ConsumerStorage consumer ) {
        this( consumer.username, consumer.nominativo, consumer.email );
    }

    private ConsumerStorage(){}

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
                                    try {
                                        File file = File.getEntitaDaDbById(occorrenza.getIdFile());
                                        return file!=null && file.isEliminato() ? null : file;    // TODO : questa dovrebbe essere prerogativa della classe File di non mostrare ai Consumer quelli eliminati
                                                // TODO : refactor per eliminare codice duplicato da RelazioneUploaderConsumerFile.getListaFileDaUploaderAConsumer
                                    }
                                    catch (NotFoundException notFoundException) { return null; }
                                })
                                .filter( Objects::nonNull )
                                .collect( Collectors.toList() );

    }

}
