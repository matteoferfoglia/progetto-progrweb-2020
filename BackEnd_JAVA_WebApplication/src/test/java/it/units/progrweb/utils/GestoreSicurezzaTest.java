package it.units.progrweb.utils;

import it.units.progrweb.UtilsInTest;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;

/**
 * @author Matteo Ferfoglia
 */
public class GestoreSicurezzaTest {

    /** Verifica la ripetibilità del calcolo di una stringa da firmare.
     * Questo test, date una stringa da firmare e l'hash calcolato in
     * prececedenza con {@link GestoreSicurezza#hmacSha256(String)} su
     * quella stringa da firmare, ricalcola l'hash sulla medesima stringa
     * e verifica che sia lo stesso di quello dato.
     * Questo test serve a scongiurare problemi di rappresentazione delle
     * stringhe relative alle codifiche (es.: carattere non rappresentabile).
     */
    @ParameterizedTest
    @MethodSource("generaParametri")
    void test_hmacSha256(String stringaDaFirmare, String hashCalcolato) {

        try {
            assertEquals(hashCalcolato, GestoreSicurezza.hmacSha256(stringaDaFirmare));
        } catch (NoSuchAlgorithmException|InvalidKeyException e) {
            UtilsInTest.scriviNelLogDeiTest(UtilitaGenerale.stringaConStackTrace(e));
        }

    }

    /** Genera i parametri per {@link #test_hmacSha256(String, String)} (String, String)}.*/
    private static Stream<Arguments> generaParametri() {
        final int LUNGHEZZA_MAX_STRINGHE_DA_FIRMARE = 2000; // una lunghezza per ogni test, quindi questo è il numero di test che verranno effettuati
                                                            // estremo escluso

        return IntStream.range(0,LUNGHEZZA_MAX_STRINGHE_DA_FIRMARE)
                        .mapToObj(lunghezzaStringa -> {
                            String stringaCasuale = GeneratoreTokenCasuali.generaTokenAlfanumerico(lunghezzaStringa);
                            String hashCalcolato = null;
                            try {
                                hashCalcolato = GestoreSicurezza.hmacSha256(stringaCasuale);
                            } catch (NoSuchAlgorithmException|InvalidKeyException e) {
                                UtilsInTest.scriviNelLogDeiTest(UtilitaGenerale.stringaConStackTrace(e));
                            }
                            return arguments(stringaCasuale, hashCalcolato);
                        });
    }

}
