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

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Classe di comodo utilizzata per creare una relazione
 * che coinvolga {@link Uploader} e {@link Consumer}.
 * @author Matteo Ferfoglia
 */
@Entity
public class RelazioneUploaderConsumer {

    /** Identificativo per un'istanza di questa classe.*/
    @Id
    private Long identificativoRelazione;

    /** Identificativo per {@link Uploader}.*/
    @Index
    private Long identificativoUploader;

    /** Identificativo per {@link Consumer}.*/
    @Index
    private Long identificativoConsumer;

    /** Dato l'identificativo di un {@link Uploader}, se presente
     * nella relazione {@link RelazioneUploaderConsumer} nel database
     * lo elimina ed elimina (con il metodo {@link File#elimina()})
     * tutti i file che ha caricato.
     * @param identificativoUploaderDaEliminare Identificativo dell'{@link Uploader}
     *                                          Se nullo, questo metodo non fa nulla.
     * @return true se l'eliminazione va a buon fine, false altrimenti.*/
    public static void eliminaUploader(Long identificativoUploaderDaEliminare) {

        if( identificativoUploaderDaEliminare==null )
            return;

        RelazioneUploaderConsumer
                .getOccorrenzaFiltrataPerUploader(identificativoUploaderDaEliminare)
                .forEach( unOccorrenza -> {

                    // Elimina tutti i file caricati da questo Uploader, per qualsiasi Consumer
                    File.getOccorrenzeFiltrataPerUploaderEConsumer(unOccorrenza.getIdentificativoUploader(),
                                                                   unOccorrenza.getIdentificativoConsumer())
                        .forEach( File::elimina );

                    // Elimina la relazione
                    DatabaseHelper.cancellaAdessoEntitaById(unOccorrenza.getIdentificativoRelazione(),
                                                            RelazioneUploaderConsumer.class);

                });

    }

    /** Dissocia il {@link Consumer} il cui identificativo è dato
     * dall'{link {@link Uploader}} il cui identificativo è dato.
     * Tutti i file caricati da quell'{@link Uploader} per quel
     * {@link Consumer} vengono eliminati (vede
     * {@link File#elimina()}.*/
    public static void dissociaConsumerDaUploader(Long identificativoConsumerDaDissociare,
                                                  Long identificativoUploader) {

        RelazioneUploaderConsumer relazioneDaEliminare;
        try {

            {
                // Eliminazione dei file caricati dall'Uploader il cui identificativo è specificato
                //  e destinati al Consumer da dissociare.
                File.getOccorrenzeFiltrataPerUploaderEConsumer(identificativoUploader,identificativoConsumerDaDissociare)
                    .forEach(File::elimina);
            }

            relazioneDaEliminare =
                    getOccorrenzaFiltrataPerUploaderEConsumer(identificativoUploader,
                                                              identificativoConsumerDaDissociare);
            if(relazioneDaEliminare!=null)
                DatabaseHelper.cancellaEntitaById(relazioneDaEliminare.identificativoRelazione,
                                                  RelazioneUploaderConsumer.class);
        } catch (RuntimeException ignored) {}   // non fa nulla se l'occorrenza non è presente nel DB
    }

    public Long getIdentificativoUploader() {
        return identificativoUploader;
    }

    public Long getIdentificativoConsumer() {
        return identificativoConsumer;
    }

    public Long getIdentificativoRelazione() {
        return identificativoRelazione;
    }

    public RelazioneUploaderConsumer() {}   // TODO : può essere privato?

    private RelazioneUploaderConsumer(Long identificativoConsumer, Long identificativoUploader, Long identificativoRelazione) {
        this.identificativoConsumer = identificativoConsumer;
        this.identificativoUploader = identificativoUploader;
        this.identificativoRelazione = identificativoRelazione;
    }

    /** Interroga il database e restituisce l'occorrenza di questa entità
     * che relaziona l'{@link Uploader} con il {@link Consumer} i cui
     * identificativi sono specificati come parametri.
     * @return la relazione voluta oppure null se non trovata.
     * @throws RuntimeException Se viene trovata più di un'occorrenza.
     */
    public static RelazioneUploaderConsumer
    getOccorrenzaFiltrataPerUploaderEConsumer(Long identificativoUploader,
                                              Long identificativoConsumer )
            throws RuntimeException {

        // Parametri query
        String nomeAttributo1 = "identificativoUploader";
        String nomeAttributo2 = "identificativoConsumer";

        if( UtilitaGenerale.esisteAttributoInClasse(nomeAttributo1, RelazioneUploaderConsumer.class) ) {
            if (UtilitaGenerale.esisteAttributoInClasse(nomeAttributo2, RelazioneUploaderConsumer.class)) {

                List<?> risultatoQuery =
                        DatabaseHelper.queryAnd(RelazioneUploaderConsumer.class,
                                nomeAttributo1, DatabaseHelper.OperatoreQuery.UGUALE, identificativoUploader,
                                nomeAttributo2, DatabaseHelper.OperatoreQuery.UGUALE, identificativoConsumer);

                if( risultatoQuery.size()==1)   // se tutto ok, dovrebbe essere restituita una sola occorrenza
                    return (RelazioneUploaderConsumer) risultatoQuery.get(0);
                else throw new RuntimeException("Nel database dovrebbe esserci esattamente un'occorrenza," +
                        "ma ne sono state trovate " + risultatoQuery.size() );

            } else {
                Logger.scriviEccezioneNelLog(RelazioneUploaderConsumer.class,
                        "Field \"" + nomeAttributo2 + "\" non trovato.",
                        new NoSuchFieldException());
            }
        } else {
            Logger.scriviEccezioneNelLog(RelazioneUploaderConsumer.class,
                    "Field \"" + nomeAttributo1 + "\" non trovato.",
                    new NoSuchFieldException());
        }

        return null;   // nessun risultato dalla query

    }

    /** Interroga il database e restituisce la lista di occorrenze di questa entità
     * in cui è presente l'{@link Uploader} il cui identificativo è quello specificato
     * nel parametro.
     * @param identificativoUploader Identificativo dell'{@link Uploader}.
     * @return La lista di relazioni.
     */
    public static List<RelazioneUploaderConsumer>
    getOccorrenzaFiltrataPerUploader( Long identificativoUploader )
            throws RuntimeException {

        // Parametri query
        String nomeAttributo1 = "identificativoUploader";

        if( UtilitaGenerale.esisteAttributoInClasse(nomeAttributo1, RelazioneUploaderConsumer.class) ) {
            return DatabaseHelper
                    .query(RelazioneUploaderConsumer.class,
                            nomeAttributo1, DatabaseHelper.OperatoreQuery.UGUALE, identificativoUploader)
                    .stream()
                    .map(unOccorrenza -> (RelazioneUploaderConsumer)unOccorrenza)
                    .collect(Collectors.toList());
        } else {
            Logger.scriviEccezioneNelLog(RelazioneUploaderConsumer.class,
                    "Field \"" + nomeAttributo1 + "\" non trovato.",
                    new NoSuchFieldException());
        }

        return new ArrayList<>(0);

    }

    /** Interroga il database e restituisce la lista degli identificativi dei
     * {@link Consumer} associati con l'{@link Uploader} il cui identificativo
     * è specificato come parametro.*/
    public static List<Long> getListaConsumerDiUploader( Long identificativoUploader ) {

        String nomeAttributoUploader = "identificativoUploader";
        String nomeAttributoConsumer = "identificativoConsumer";
        if( UtilitaGenerale.esisteAttributoInClasse( nomeAttributoUploader, RelazioneUploaderConsumer.class ) &&
                UtilitaGenerale.esisteAttributoInClasse( nomeAttributoConsumer, RelazioneUploaderConsumer.class ) ) {

            return DatabaseHelper.query( RelazioneUploaderConsumer.class,
                                    nomeAttributoUploader, DatabaseHelper.OperatoreQuery.UGUALE, identificativoUploader )
                    .stream()
                    .map( occorrenzaRelazione ->
                            ((RelazioneUploaderConsumer)occorrenzaRelazione).getIdentificativoConsumer() )
                    .collect(Collectors.toList());

        } else {
            Logger.scriviEccezioneNelLog(RelazioneUploaderConsumer.class,
                                        "Controllare esistenza dei Field \"" + nomeAttributoConsumer + "\" e \"" + nomeAttributoUploader + "\".",
                                         new NoSuchFieldException() );
            return new ArrayList<>();   // lista vuota come risultato
        }

    }

    /** Restituisce true se il consumer con identificativo dato risulta attualmente
     * servito dall'uploader con identificativo dato, false altrimenti.*/
    public static boolean isConsumerServitoDaUploader( Long identificativoUploader, Long identificativoConsumer ) {

        try {
            Field uploader_field  = RelazioneUploaderConsumer.class.getDeclaredField("identificativoUploader");  // eccezione se non esiste questo field
            Field consumer_field  = RelazioneUploaderConsumer.class.getDeclaredField("identificativoConsumer");  // eccezione se non esiste questo field
            
            return DatabaseHelper.esisteNelDatabase(
                    DatabaseHelper.creaERestituisciQueryAnd( RelazioneUploaderConsumer.class,
                                     uploader_field.getName(),  DatabaseHelper.OperatoreQuery.UGUALE, identificativoUploader,
                                     consumer_field.getName(),  DatabaseHelper.OperatoreQuery.UGUALE, identificativoConsumer
                            ) );

        } catch (NoSuchFieldException e) {
            Logger.scriviEccezioneNelLog(RelazioneUploaderConsumer.class,
                                         "Attributo non trovato nella classe, forse è stato modificato il nome.",
                                         e);
            return false;
        }
    }

    /** Associa il consumer il cui identificativo è dato all'Uploader
     * con identificativo dato. Restituisce true se la richiesta va
     * a buon fine, false altrimenti.*/
    public static void aggiungiConsumerAdUploader(Long identificativoConsumer, Long identificativoUploader) {
        DatabaseHelper.salvaEntita( new RelazioneUploaderConsumer(identificativoConsumer, identificativoUploader, null) );
    }

}