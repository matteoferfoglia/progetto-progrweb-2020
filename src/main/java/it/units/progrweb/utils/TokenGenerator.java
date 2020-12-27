package it.units.progrweb.utils;

import java.util.Random;
import java.util.stream.Collectors;

/** Classe di utilità per la generazione di token (es.: token CSRF).*/
public class TokenGenerator {
    /**
     * Genera un token alfanumerico con un numero di caratteri
     * specificato nel parametro e presi dall'alfabeto definito
     * all'interno del metodo.
     * @param lunghezzaToken - numero di caratteri che costituiranno il token.
     * @return token alfanumerico di lunghezza data e caratteri arbitrari.
     */
    public static String generaTokenAlfanumerico(int lunghezzaToken) {
        final String ALFABETO = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789+/"; // CSRF-token sarà composta da questi caratteri, presi casualmente
        String token = new Random().ints(lunghezzaToken, 0, ALFABETO.length())
                .mapToObj(indiceCasuale -> String.valueOf(ALFABETO.charAt(indiceCasuale)))
                .collect(Collectors.joining());
        return token;
    }
}
