package it.units.progrweb.entities;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;
import it.units.progrweb.entities.attori.nonAdministrator.consumer.Consumer;
import it.units.progrweb.entities.attori.nonAdministrator.uploader.Uploader;

/**
 * Classe di comodo utilizzata per creare una relazione
 * che coinvolga {@link Uploader},
 * {@link it.units.progrweb.entities.attori.nonAdministrator.consumer.Consumer} e
 * {@link it.units.progrweb.entities.file.File}, con l'obiettivo
 * di minimizzare i costi relativi al database, sia in termini
 * di numero di indici, sia in termini di numero di accessi
 * per l'esecuzione delle query.
 * @author Matteo Ferfoglia
 */
@Entity
class RelazioneUploaderConsumerFile {

    /** Id per un'occorrenza.*/
    @Id
    private Long idRelazione;

    /** Chiave per {@link Uploader}.*/
    @Index
    private Key<Uploader> Uploader;

    /** Chiave per {@link it.units.progrweb.entities.attori.nonAdministrator.consumer.Consumer}.*/
    @Index
    private Key<Consumer> Consumer;

    /** Chiave per {@link it.units.progrweb.entities.file.File}.*/
    private Key<it.units.progrweb.entities.file.File> File;

}