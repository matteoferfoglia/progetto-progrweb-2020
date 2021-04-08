package it.units.progrweb;

import it.units.progrweb.utils.UtilitaGenerale;

import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.fail;

/**
 * Classe di utilità per i test.
 * @author Matteo Ferfoglia
 */
public class UtilsInTest {

    /** Scrive nel log per i test. */
    public static void scriviNelLogDeiTest(String daStampare) {
        PrintStream outputTest = System.out;
        outputTest.println(daStampare);
    }

    /** Metodo un utilizzabile nei test che devono gestire eccezioni non attese.
     * Se invocato, questo metodo fa fallire il test.
     * @param e L'eccezione non attesa.
     * @return null.
     */
    @SuppressWarnings("SameReturnValue")
    public static Object fallisciTestACausaDiEccezioneNonAttesa(Exception e) {
        UtilsInTest.scriviNelLogDeiTest(UtilitaGenerale.stringaConStackTrace(e));
        fail("Eccezione non attesa: " + UtilitaGenerale.stringaConStackTrace(e));
        return null;
    }
}
