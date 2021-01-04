package it.units.progrweb.utils;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Classe di utilità per gestione dell'input e dell'output.
 * @author Matteo Ferfoglia
 */
public class UtilitaInputOutput {

    /** Restituisce una stringa con l'intero stacktrace di un'eccezione. */
    public static String stringaConStackTrace(Throwable throwable) {
        return Arrays.stream(throwable.getStackTrace())
                     .map(StackTraceElement::toString)
                     .collect(Collectors.joining("\n\t"));
    }

}
