package it.units.progrweb.persistence;

import it.units.progrweb.UtilsInTest;
import it.units.progrweb.entities.RelazioneUploaderConsumerFile;
import it.units.progrweb.utils.UtilitaGenerale;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Classe di test per {@link RelazioneUploaderConsumerFileTest}.
 * @author Matteo Ferfoglia
 */
public class RelazioneUploaderConsumerFileTest {

    /** Test per "visualizzare" se i metodi si comportano come da aspettative
     * Output visibile e verificabile. Test per
     * {@link RelazioneUploaderConsumerFile#mappa_idConsumer_arrayIdFile(List)},
     * fatto su alcuni campioni di interesse.*/
    @Test
    void testMappa_idAttore_arrayIdFiles(){

        // Creazione (simulazione) del risultato di una query
        // SE si sta simulando la query eseguita da un Uploader, allora fare finta che (in tutte le entry) nella prima colonna ci sia sempre l'id dell'Uploader che ha effettuato la query (cioè NON considerare l'idUploader)
        // SE si sta simulando la query eseguita da un Consumer, allora fare finta che (in tutte le entry) nella seconda colonna ci sia sempre l'id del Consumer che ha effettuato la query (cioè NON considerare l'idConsumer)

        List<RelazioneUploaderConsumerFile> risultatoQuerySimulato = new ArrayList<>();

        //       idUploader          idConsumer         idFile
        risultatoQuerySimulato.add( creaRelazione(Long.valueOf(123456), Long.valueOf(789), Long.valueOf(8975460)) );
        risultatoQuerySimulato.add( creaRelazione(Long.valueOf(123456), Long.valueOf(789), Long.valueOf(123)) );
        risultatoQuerySimulato.add( creaRelazione(Long.valueOf(123456), Long.valueOf(789), Long.valueOf(19853)) );
        risultatoQuerySimulato.add( creaRelazione(Long.valueOf(9874), Long.valueOf(789), Long.valueOf(4560)) );
        risultatoQuerySimulato.add( creaRelazione(Long.valueOf(9874), Long.valueOf(789), Long.valueOf(19)) );
        risultatoQuerySimulato.add( creaRelazione(Long.valueOf(79874), Long.valueOf(789), Long.valueOf(21)) );
        risultatoQuerySimulato.add( creaRelazione(Long.valueOf(1234), Long.valueOf(10), null) );
        risultatoQuerySimulato.add( creaRelazione(Long.valueOf(1234), Long.valueOf(11), null) );
        risultatoQuerySimulato.add( creaRelazione(Long.valueOf(1234), Long.valueOf(11), Long.valueOf(220)) );
        risultatoQuerySimulato.add( creaRelazione(Long.valueOf(1234), Long.valueOf(11), Long.valueOf(199)) );
        risultatoQuerySimulato.add( creaRelazione(Long.valueOf(12345), Long.valueOf(11), null) );


        // Creazione delle mappa attese

        Map<Long,Long[]> mappaAttesa_IdConsumer_IdFile = new HashMap<>();   // Simula il risultato di query fatta da un Uploader ("Sono l'uploader yyy, dammi tutti i miei Consumer ed i rispettivi file a loro destinati")
        mappaAttesa_IdConsumer_IdFile.put( Long.valueOf(789), new Long[]{Long.valueOf(8975460), Long.valueOf(123), Long.valueOf(19853), Long.valueOf(4560), Long.valueOf(19), Long.valueOf(21)});
        mappaAttesa_IdConsumer_IdFile.put( Long.valueOf(10), new Long[]{});
        mappaAttesa_IdConsumer_IdFile.put( Long.valueOf(11), new Long[]{Long.valueOf(220), Long.valueOf(199)});

        Map<Long,Long[]> mappaAttesa_IdUploader_IdFile = new HashMap<>();   // Simula il risultato di query fatta da un Uploader ("Sono il consumer xxx, dammi tutti i miei Uploader ed i rispettivi file da loro caricati")
        mappaAttesa_IdUploader_IdFile.put( Long.valueOf(123456), new Long[]{Long.valueOf(8975460), Long.valueOf(123), Long.valueOf(19853)});
        mappaAttesa_IdUploader_IdFile.put( Long.valueOf(9874), new Long[]{Long.valueOf(4560), Long.valueOf(19)});
        mappaAttesa_IdUploader_IdFile.put( Long.valueOf(79874), new Long[]{Long.valueOf(21)});
        mappaAttesa_IdUploader_IdFile.put( Long.valueOf(1234), new Long[]{Long.valueOf(220), Long.valueOf(199)});
        mappaAttesa_IdUploader_IdFile.put( Long.valueOf(12345), new Long[]{});


        // Calcola e stampa mappe

        UtilsInTest.scriviNelLogDeiTest("=================================================");
        UtilsInTest.scriviNelLogDeiTest("La chiave in un entry è l'id di un Consumer gestito dall'Uploader che ha eseguito la query\n");
        UtilsInTest.scriviNelLogDeiTest("\tMAPPA ID_CONSUMER CALCOLATA - [ ID_FILE caricati per quel consumer ] :");
        Map<Long,Long[]> mappaCalcolata_IdConsumer_IdFile = RelazioneUploaderConsumerFile.mappa_identificativoConsumer_arrayIdFile(risultatoQuerySimulato);
        UtilsInTest.scriviNelLogDeiTest(stampaMappa(mappaCalcolata_IdConsumer_IdFile));
        UtilsInTest.scriviNelLogDeiTest("\tMAPPA ID_CONSUMER ATTESA - [ ID_FILE caricati per quel consumer ] :");
        UtilsInTest.scriviNelLogDeiTest(stampaMappa(mappaAttesa_IdConsumer_IdFile));

        UtilsInTest.scriviNelLogDeiTest("=================================================");
        UtilsInTest.scriviNelLogDeiTest("La chiave in un entry è l'id di un Uploader servente il Consumer che ha eseguito la query\n");
        UtilsInTest.scriviNelLogDeiTest("\tMAPPA ID_UPLOADER - [ ID_FILE ricevuti da quell'Uploader ] :");
        Map<Long,Long[]> mappaCalcolata_IdUploader_IdFile = RelazioneUploaderConsumerFile.mappa_identificativoUploader_arrayIdFile(risultatoQuerySimulato);
        UtilsInTest.scriviNelLogDeiTest(stampaMappa(mappaCalcolata_IdUploader_IdFile));
        UtilsInTest.scriviNelLogDeiTest("\tMAPPA ID_UPLOADER ATTESA - [ ID_FILE caricati per quel consumer ] :");
        UtilsInTest.scriviNelLogDeiTest(stampaMappa(mappaAttesa_IdUploader_IdFile));


        // Asserzioni
        assertTrue( mappeUguali( mappaAttesa_IdConsumer_IdFile, mappaCalcolata_IdConsumer_IdFile) );
        assertTrue( mappeUguali( mappaAttesa_IdUploader_IdFile, mappaCalcolata_IdUploader_IdFile) );

    }

    /** Metodo per verificare che due mappe siano equivalenti.*/
    private static boolean mappeUguali( Map<Long,Long[]> mappa1, Map<Long,Long[]> mappa2 ) {

        boolean sonoUguali = mappa1.size() == mappa2.size();

        for( Long chiave : mappa1.keySet() ) {

            sonoUguali = sonoUguali && mappa2.containsKey(chiave);
            if( sonoUguali ) {
                Long[] arrayValoriMappa1 = mappa1.get(chiave);
                Long[] arrayValoriMappa2 = mappa2.get(chiave);

                sonoUguali = sonoUguali && arrayValoriMappa1.length == arrayValoriMappa2.length;

                if( sonoUguali ) {
                    for (Long valore : arrayValoriMappa1) {
                        sonoUguali = sonoUguali && UtilitaGenerale.isPresenteNellArray(valore, arrayValoriMappa2);
                        if( ! sonoUguali )
                            break;
                    }
                }

            }
            else break;

        }

        return sonoUguali;

    }

    /** Metodo per stampare una mappa prodotta da
     * {@link RelazioneUploaderConsumerFile#mappa_identificativoConsumer_arrayIdFile(List)} (List)}
     * oppure da {@link RelazioneUploaderConsumerFile#mappa_identificativoUploader_arrayIdFile(List)} (List)}.*/
    private static String stampaMappa(Map<Long, Long[]> mappa) {

        return mappa.entrySet().stream().map( entry -> {

            String s = Arrays.stream(entry.getValue())
                    .map(idFile -> String.valueOf(idFile))
                    .collect( Collectors.joining(",") );

            return entry.getKey() + " => [" + s + "]";

        }).collect( Collectors.joining("\n") );

    }

    /** Contatore incrementabile per il metodo {@link #creaRelazione(Long, Long, Long)}.*/
    private static Long contatoreRelazioni = Long.valueOf(0);

    /** Crea un'istanza di {@link RelazioneUploaderConsumerFile}.*/
    private static RelazioneUploaderConsumerFile creaRelazione(Long idUploader, Long idConsumer, Long idFile) {

        RelazioneUploaderConsumerFile r = new RelazioneUploaderConsumerFile();

        Field[] fieldsRelazione = RelazioneUploaderConsumerFile.class.getDeclaredFields();
        Arrays.stream(fieldsRelazione).forEach( field -> {

            // Rendi accessibili i field
            field.setAccessible(true);

            // Inizializza i valori
            try {
                switch( field.getName() ) {
                    case "idRelazione"             : field.set(r, contatoreRelazioni++); break;
                    case "identificativoUploader"  : field.set(r, idUploader); break;
                    case "identificativoConsumer"  : field.set(r, idConsumer); break;
                    case "idFile"                  : field.set(r, idFile); break;
                    case "eliminato"               : field.set(r, Math.round(Math.random())==0); break;
                    default:
                        fail("Il field \"" + field.getName() + "\" non è stato trovato: potrebbe essere necessario modificare " +
                                "QUESTO METODO DI TEST (es. se il field è stato rinominato nella classe).");

                }
            } catch (IllegalAccessException exception) {
                UtilsInTest.fallisciTestACausaDiEccezioneNonAttesa(exception);
            }

        });

        return r;

    }

}
