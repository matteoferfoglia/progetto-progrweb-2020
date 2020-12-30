package it.units.progrweb;

import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Classe di utilità per i test.
 * @author Matteo Ferfoglia
 */
public class UtilsInTest {

    /** Restituisce una stringa con l'intero stacktrace. */
    public static String stringaConStackTrace(Throwable throwable) {
        return Arrays.stream(throwable.getStackTrace())
                     .map(StackTraceElement::toString)
                     .collect(Collectors.joining("\n\t"));
    }

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
    public static void testLog(String daStampare) {
        // TODO : da implementare: dove deve scrivere??
        System.out.println(daStampare); // TODO : temporaneo, da implementare
    }

}
