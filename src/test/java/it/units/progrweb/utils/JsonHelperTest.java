package it.units.progrweb.utils;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Classe di test per la classe {@link JsonHelper}.
 * Nota: mantenere elevato il numero di test (~migliaia)
 * perché i parametri sono generati in modo aleatorio, quindi
 * potrebbero fornire risultati diversi.
 * In ogni caso, si è cercato di coprire ogni eventualità.
 * @author Matteo Ferfoglia
 */
public class JsonHelperTest {

    private final static int QUANTI_TEST = 1000;    // todo : variabile d'ambiente

    /** Tipi primitivi negli oggetti JSON.*/
    private enum TipiPrimitiviInJson {
        STRING, NUMBER, BOOLEAN, NULL
    }

    /** Crea degli oggetti JSON (stringhe) ad un solo livello,
     * cioè il valore di tutte le properties è un valore primitivo,
     * e li restituisce come primo argomento, mentre la mappa
     * nome-valore viene restituita come secondo argomento.
     * @see TipiPrimitiviInJson TipiPrimitiviInJson.*/
    private static Stream<Arguments> generaOggettiJsonAdUnSoloLivello() {

        // todo : refactoring di questo metodo

        final int NUMERO_MASSIMO_PROPERTIES_UN_OGGETTO_JSON = 200;  // todo : variabile d'ambiente

        return  IntStream.range(0,QUANTI_TEST)
                         .mapToObj(contatoreTest -> {
                             // creazione di un oggetto JSON

                             final int QUANTE_PROPERTIES_IN_QUESTO_OGGETTO = (int)(Math.random()*NUMERO_MASSIMO_PROPERTIES_UN_OGGETTO_JSON);

                             try {
                                 List<Object> streamPropertyJson = IntStream.range(0, QUANTE_PROPERTIES_IN_QUESTO_OGGETTO)
                                         .mapToObj(contatoreProperties -> {
                                             // creazione di una property

                                             final int LUNGHEZZA_MASSIMA_NOME_PROPERTY   = 200; // todo : variabile d'ambiente
                                             final int LUNGHEZZA_MASSIMA_VALORE_PROPERTY = 1000; // todo : variabile d'ambiente
                                             final int lunghezzaNomeProperty   = (int)(Math.random()*LUNGHEZZA_MASSIMA_NOME_PROPERTY)+1;    // lunghezza minima 1 per il nome della property
                                             final int lunghezzaValoreProperty = (int)(Math.random()*LUNGHEZZA_MASSIMA_VALORE_PROPERTY+1);  // lunghezza minima 0 per il valore della property

                                             String nomeProperty   = GeneratoreTokenCasuali.generaTokenAlfanumerico(lunghezzaNomeProperty);
                                             String valoreProperty;

                                             // Tipo property generato casualmente
                                             final int NUMERO_TIPI_GENERABILI = TipiPrimitiviInJson.values().length;
                                             final TipiPrimitiviInJson tipoPrimitivoDiQuestoValore= TipiPrimitiviInJson.values()[(int)(Math.random()*NUMERO_TIPI_GENERABILI)];
                                             Object[] rappresentazione_stringa_entryMappaCorrispondente = new Object[2];    // prodotto da questo metodo
                                             switch(tipoPrimitivoDiQuestoValore) {

                                                 case NUMBER: //genera  un numero
                                                     boolean ilNumeroSaraIntero = Math.round(Math.random()) != 0;
                                                     Double numeroCasuale = Math.random()*((double) (10 ^ lunghezzaValoreProperty));
                                                     if(ilNumeroSaraIntero){
                                                         valoreProperty = String.valueOf(numeroCasuale.longValue()); // todo: rivedere come si comporta quest'istruzione
                                                         rappresentazione_stringa_entryMappaCorrispondente[1] = new AbstractMap.SimpleEntry<>(nomeProperty, numeroCasuale.longValue());
                                                     }
                                                     else {
                                                         valoreProperty = String.valueOf(numeroCasuale);
                                                         rappresentazione_stringa_entryMappaCorrispondente[1] = new AbstractMap.SimpleEntry<>(nomeProperty, numeroCasuale);
                                                     }
                                                     break;

                                                 case BOOLEAN:
                                                     Boolean valoreBooleano = Math.round(Math.random()) != 0;
                                                     rappresentazione_stringa_entryMappaCorrispondente[1] = new AbstractMap.SimpleEntry<>(nomeProperty, valoreBooleano);
                                                     valoreProperty = String.valueOf(valoreBooleano);
                                                     break;

                                                 case NULL:
                                                     rappresentazione_stringa_entryMappaCorrispondente[1] = new AbstractMap.SimpleEntry<>(nomeProperty, null);
                                                     valoreProperty = "null";
                                                     break;

                                                 case STRING:
                                                 default:
                                                     valoreProperty = GeneratoreTokenCasuali.generaTokenAlfanumerico(lunghezzaValoreProperty);
                                                     rappresentazione_stringa_entryMappaCorrispondente[1] = new AbstractMap.SimpleEntry<>(nomeProperty, valoreProperty);
                                                     valoreProperty = "\"" + valoreProperty + "\""; // stringa ha le virgolette nella rappresentazione json
                                                     break;

                                             }

                                             rappresentazione_stringa_entryMappaCorrispondente[0] = "\"" + nomeProperty + "\": " + valoreProperty;    // restituisce la stringa   "nomeProp"=valoreProp


                                             return rappresentazione_stringa_entryMappaCorrispondente;  // array (Object[]) con proprietà ("nome":valore) nel primo elemento e l'entry (key-value) nel secondo elemento
                                         }).collect(Collectors.toList());

                                 Supplier<Stream> streamSupplier = () -> streamPropertyJson.stream();   // Fonte: https://stackoverflow.com/a/30922045


                                 // Creazione dello stream di arguments da resituire: primo argomento è la stringa JSON dell'oggetto
                                 String rappresentazioneJson = "{\n\t"
                                         +   streamSupplier.get().map(rappresentazione_stringa_entryMappaCorrispondente -> {
                                     Object[] rappresentazione_stringa_entryMappaCorrispondente_array = (Object[]) rappresentazione_stringa_entryMappaCorrispondente;
                                     return rappresentazione_stringa_entryMappaCorrispondente_array[0];
                                 }).collect(Collectors.joining(", \n\t"))
                                         + "\n}";  // oggetto JSON (String) con tutte le properties

                                 // Creazione dello stream di arguments da resituire: secondo argomento è la mappa chiave-valore con le properties dell'oggetto
                                 Map<String,Object> rappresentazioneMappa_nomeProp_valoreProp = new LinkedHashMap<>();
                                 streamSupplier.get().map(rappresentazione_stringa_entryMappaCorrispondente -> {
                                     Object[] rappresentazione_stringa_entryMappaCorrispondente_array = (Object[]) rappresentazione_stringa_entryMappaCorrispondente;
                                     return  rappresentazione_stringa_entryMappaCorrispondente_array[1];
                                 }).forEach(entry -> {
                                     Map.Entry entryCast = (Map.Entry)entry;
                                     if(rappresentazioneMappa_nomeProp_valoreProp.containsKey(entryCast.getKey()))
                                         throw new IllegalStateException("Chiave già presente nella mappa.");
                                     rappresentazioneMappa_nomeProp_valoreProp.put((String) entryCast.getKey(), entryCast.getValue());
                                 });

                                 return Arguments.arguments(rappresentazioneJson, rappresentazioneMappa_nomeProp_valoreProp);

                             } catch (IllegalStateException e) {
                                 // se vengono generate due proprietà con lo stesso nome
                                 return Arguments.arguments("{ }", new LinkedHashMap<>());   // oggetto vuoto
                             }

                         });
    }


    @ParameterizedTest
    @MethodSource("generaOggettiJsonAdUnSoloLivello")
    void convertiStringaJsonToMappaProprietaTest(String stringaJson, Map<String,Object> mappaProprieta_nome_valore_attesa) {
        Map<String, Object> mappaProprieta_calcolata = JsonHelper.convertiStringaJsonInMappaProprieta(stringaJson);
        assertEquals(mappaProprieta_nome_valore_attesa, mappaProprieta_calcolata);
    }


    @ParameterizedTest
    @MethodSource("generaOggettiJsonAdUnSoloLivello")
    void convertiMappaProprietaToStringaJson(String stringaJson_attesa, Map<String,Object> mappaProprieta_nome_valore) {
        String stringaJson_calcolata = JsonHelper.convertiMappaProprietaToStringaJson(mappaProprieta_nome_valore);

        // Nei test non siamo interessati agli spazi

        Function rimuoviSpaziTranneQuelliTraVirgolette = stringaInput -> {
            final String[] caratteriRegexDaRimuovere = {"\t","\n", " "};
            String stringaOutput = (String)stringaInput;
            for (String carattereRegexDaRimuovere : caratteriRegexDaRimuovere) {
                stringaOutput = stringaOutput.replaceAll(carattereRegexDaRimuovere, "");
            }
            return stringaOutput;
        };

        assertEquals(rimuoviSpaziTranneQuelliTraVirgolette.apply(stringaJson_attesa),
                     rimuoviSpaziTranneQuelliTraVirgolette.apply(stringaJson_calcolata));

    }

}
