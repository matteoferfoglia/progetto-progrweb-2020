package it.units.progrweb.entities;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;
import it.units.progrweb.entities.attori.nonAdministrator.consumer.Consumer;
import it.units.progrweb.entities.attori.nonAdministrator.uploader.Uploader;
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

    // TODO : DEVE restare un'entità che rappresenti la relazione tra uploader e consumer
    //          ma la maggior parte del codice in questa classe dovrebbe stare in FileStorage (=> fondere)

    /** Identificativo per un'istanza di questa classe.*/
    @Id
    private Long identificativoRelazione;

    /** Identificativo per {@link Uploader}.*/
    @Index
    private Long identificativoUploader;

    /** Identificativo per {@link Consumer}.*/
    @Index
    private Long identificativoConsumer;

    /** Dissocia il {@link Consumer} il cui identificativo è dato
     * dall'{link {@link Uploader}} il cui identificativo è dato.*/
    public static void dissociaConsumerDaUploader(Long identificativoConsumerDaDissociare,
                                                  Long identificativoUploader) {

        RelazioneUploaderConsumer relazioneDaEliminare;
        try {
            relazioneDaEliminare =
                    getOccorrenzaFiltrataPerUploaderEConsumer(identificativoUploader,
                                                              identificativoConsumerDaDissociare);
            if(relazioneDaEliminare!=null)
                DatabaseHelper.cancellaEntitaById(relazioneDaEliminare.identificativoRelazione,
                                                  RelazioneUploaderConsumer.class);
        } catch (RuntimeException ignored) {}   // non fa nulla se l'occorrenza non è presente nel DB
    }

//    /** Se l'attore il cui identificativo è dato come parametro può accedere
//     * al file il cui identificativo è dato come parametro, restituisce
//     * l'occorrenza di {@link RelazioneUploaderConsumer} corrispondente
//     * al file cercato. Altrimenti restituisce null.
//     * Se il file non esiste, genera un'eccezione {@link NotFoundException}.*/
//    public static RelazioneUploaderConsumer attorePuoAccedereAFile(Long identificativoAttore,
//                                                                   Long identificativoFile)
//            throws NotFoundException {
//    // TODO : metodo da eliminare
//
//        RelazioneUploaderConsumer relazioneUploaderConsumer = (RelazioneUploaderConsumer)
//                    DatabaseHelper.getById(identificativoFile, RelazioneUploaderConsumer.class);
//
//        if ( relazioneUploaderConsumer.getIdentificativoConsumer().equals( identificativoAttore )
//                || relazioneUploaderConsumer.getIdentificativoUploader().equals( identificativoAttore ) )
//            return relazioneUploaderConsumer;
//
//        else return null;
//
//    }



//    /** Restituisce l'occorrenza di relazione associata al file il cui identificativo
//     * è fornito come parametro, oppure null se non viene trovata.*/
//    public static RelazioneUploaderConsumer getEntitaDaDbByID(Long idFile) {
//    // TODO : cancellare questo metodo
//
//        try {
//            return (RelazioneUploaderConsumer)
//                    DatabaseHelper.getById( idFile, RelazioneUploaderConsumer.class );
//
//        } catch (NotFoundException notFoundException) {
//            return null;
//        }
//    }

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
     * che relazione l'{@link Uploader} con il {@link Consumer} i cui
     * identificativi sono specificati come parametri.
     * @return la relazione voluta oppure null se non trovata.
     * @throws RuntimeException Se viene trovata più di un'occorrenza.
     */
    public static RelazioneUploaderConsumer
    getOccorrenzaFiltrataPerUploaderEConsumer(Long identificativoUploader,
                                              Long identificativoConsumer )
            throws RuntimeException {

        // TODO : verificare che funzioni

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


//    public static List<File> getListaFileDaUploaderAConsumer(Long identificativoUploader, Long identificativoConsumer) {
//        // TODO : questo metodo dovrebbe stare in File
//        // TODO : refactoring del database (è un po' sprecato andare a cercare per id un'entità che magari nemmeno c'è) visto che fintanto che non viene caricato il primo file da consumer ad uploader il valore è fittizio
//
//        List<RelazioneUploaderConsumer> occorrenzeRelazione =
//                getOccorrenzaFiltrataPerUploaderEConsumer( identificativoUploader, identificativoConsumer );
//        List<File> listaFile = occorrenzeRelazione.stream()
//                                                  .map( relazione -> {
//                                                      try {
//                                                          File file = File.getEntitaDaDbById( relazione.getIdentificativoRelazione() );
//                                                          return file != null && file.isEliminato() ?
//                                                                    null : file; // TODO : deve essere prerogativa della classe File
//                                                      } catch (NotFoundException notFoundException) {
//                                                          return null;
//                                                      }
//                                                  })
//                                                  .filter( Objects::nonNull )
//                                                  .collect(Collectors.toList());
//
//        return listaFile;
//
//    }


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


//    /** Data una lista in cui ogni elemento è un'istanza di
//     * {@link RelazioneUploaderConsumer}, restituisce una
//     * mappa che ha come chiave l'identificativo di un {@link Consumer}
//     * e come valore corrispondente l'array degli identificativi
//     * dei {@link File} inviati al {@link Consumer} il cui
//     * identificativo è specificato nella chiave.
//     */
//    public static Map<Long, Long[]> mappa_identificativoConsumer_arrayIdFile(List<RelazioneUploaderConsumer> listaRelazioni) {
//
//        // TODO : metodo da spostare nella classe File
//
//        String nomeMetodoGetterDaUsarePerRaggruppareOccorrenze = "getIdentificativoConsumer";
//        return mappa_identificativoAttore_arrayIdFiles(listaRelazioni, nomeMetodoGetterDaUsarePerRaggruppareOccorrenze);
//
//    }



    /** Restituisce true se il consumer con identificativo dato risulta attualmente
     * servito dall'uploader con identificativo dato, false altrimenti.*/
    public static boolean isConsumerServitoDaUploader( Long identificativoUploader, Long identificativoConsumer ) {
        // TODO : verificare funzioni
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