package it.units.progrweb.entities.attori.nonAdministrator.consumer;

import com.googlecode.objectify.annotation.Subclass;
import it.units.progrweb.entities.attori.nonAdministrator.uploader.Uploader;
import it.units.progrweb.entities.file.File;

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

    ConsumerStorage(String username, String nominativo, String email) {
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

        return Objects.requireNonNull(File.getOccorrenzeFiltrataPerUploaderEConsumer(idUploader, idConsumer))
                        .stream()
                        .map( unFile -> unFile!=null && !unFile.isEliminato() ? unFile : null )
                        .filter( Objects::nonNull )
                        .collect( Collectors.toList() );

    }

}
