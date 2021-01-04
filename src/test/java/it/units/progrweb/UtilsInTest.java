package it.units.progrweb;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Classe di utilità per i test.
 * @author Matteo Ferfoglia
 */
public class UtilsInTest {

    /** Restituisce una stringa CSV creata da un array di stringhe.
     * NON esegue escape di tutti i caratteri speciali: le funzionalità
     * di questo metodo sono limitate a quanto richiesto dai test. */
    public static String generaCsvDaArray(String[] arrayDiStringhe) {
        return Arrays.stream(arrayDiStringhe)
                .map( stringa -> "'" + stringa + "'")   // un elemento csv è delimitato da ' ' (https://junit.org/junit5/docs/current/user-guide/#writing-tests-parameterized-tests-sources-CsvSource)
                .collect(Collectors.joining(", "));
    }

    /** Restituisce un nuovo array con gli elementi specificati aggiunti in coda. */
    public static<T> T[] appendiElementi(T[] vecchioArray, T ...elementiDaAggiungereInCoda) {
        // TODO : verificare che funzioni
        return (T[]) Stream.concat(Arrays.stream(vecchioArray), Arrays.stream(elementiDaAggiungereInCoda))
                           .toArray();
    }

    /** Scrive nel log per i test. */
    public static void scriviNelLogDeiTest(String daStampare) {
        PrintStream outputTest = System.out;    // TODO : creare un file di log per i test
        outputTest.println(daStampare);
    }

    /** Genera casualmente un indirizzo IPv4. */
    public static String generaIndirizzoIP() {

        final int NUMERO_BYTES_IPV4 = 4;
        final int VALORE_MASSIMO_UN_BYTE = 255 ;
        return IntStream.range(0,NUMERO_BYTES_IPV4)
                        .mapToObj(contatoreByteIndirizzo -> String.valueOf((int)(Math.random()*VALORE_MASSIMO_UN_BYTE)))
                        .collect(Collectors.joining("."));

    }
}
