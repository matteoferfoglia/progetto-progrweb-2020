package it.units.progrweb.entities.attori.consumer;

import com.googlecode.objectify.annotation.Subclass;
import it.units.progrweb.entities.attori.Attore;
import it.units.progrweb.entities.attori.uploader.Uploader;
import it.units.progrweb.entities.file.File;

import java.util.List;

/**
 * Classe rappresentante un'entità {@link Consumer}
 * da memorizzare nel database.
 * @author Matteo Ferfoglia
 */
@SuppressWarnings("DefaultAnnotationParam")
@Subclass(index = false)
class ConsumerStorage extends Consumer {

    ConsumerStorage(String username, String nominativo, String email) {
        super(username, nominativo, email);
    }

    /** Copy-constructor, usato in {@link Attore#clone()}.*/
    @SuppressWarnings("unused") // accesso tramite reflection da Attore.clone()
    private ConsumerStorage( ConsumerStorage consumer ) {
        this( consumer.username, consumer.nominativo, consumer.email );
    }

    /** No-arg constructor necessario per Objectify. */
    @SuppressWarnings("unused")
    private ConsumerStorage(){}

    /** Scarica dal database tutti i {@link File} caricati dall'{@link Uploader}
     * il cui identificativo è dato come parametro e destinati al {@link
     * Consumer} il cui identificativo è dato come parametro.
     * Infine, restituisce i {@link File} scaricati.
     */
    static List<File> getAnteprimaFiles(Long idConsumer, Long idUploader) {

        return File.getOccorrenzeFiltrataPerUploaderEConsumer(idUploader, idConsumer);

    }

}
