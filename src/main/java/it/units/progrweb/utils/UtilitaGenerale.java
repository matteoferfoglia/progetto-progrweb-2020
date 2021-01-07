package it.units.progrweb.utils;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Classe di utilità generale, ad esempio per la manipolazione di
 * array.
 * @author Matteo Ferfoglia
 */
public class UtilitaGenerale {

    /** Inizio dell'URI delle richieste "api".*/
    private static final String API_URL_PATTERN = "/api";       // TODO : variabile d'ambiente (questa variabile NON dovrebbe essere in questa classe!!!)

    /** Restituisce true se l'array dato contiene l'oggetto
     * da cercare, false altrimenti. La classe dell'oggetto
     * cercato deve implementare il metodo <code>equals</code>.*/
    public static<T> boolean isPresenteNellArray(T oggettoDaCercare, T[] array) {
        return Arrays.stream(array).anyMatch(oggettoDaCercare::equals);
    }

    /** Restituisc true se la richiesta è di tipo "api" (Ajax), false altrimenti.*/
    public static boolean isRichiestaApi(HttpServletRequest httpReq) {
        return getUrlPattern(httpReq).startsWith(API_URL_PATTERN);
    }

    /** Restituisce una stringa con l'intero stacktrace di un'eccezione. */
    public static String stringaConStackTrace(Throwable throwable) {
        return Arrays.stream(throwable.getStackTrace())
                     .map(StackTraceElement::toString)
                     .collect(Collectors.joining("\n\t"));
    }

    /** Data una HttpServletRequest, restituisce la parte dell'url
     * successiva al {@link HttpServletRequest#getContextPath()
     * contex path} (cioè alla cartella di deploy nel servlet container).
     * Questo metodo può essere utile nell'implementazione di filtri
     * che soddisfino un certo url pattern, indipendentemente dal
     * servlet container. Esempio:
     * <ul>
     *     <li>Input: richiesta http con URI
     *          <code>http://www.example.com/cartellaDeployDelServletContainer/servlet1/testo?param=val</code>
     *     </li>
     *     <li>Output di questo metodo (Stringa):
     *          <code>/servlet1/testo?param=val</code>
     *     </li>
     * </ul>
     */
    public static String getUrlPattern(HttpServletRequest httpRequest) {
        // TODO : testare
        return httpRequest.getRequestURI().substring(httpRequest.getContextPath().length());
    }

    /** Metodo di conversione da una stringa CamelCase ad una
     * in cui le parole sono separate da spazi
     * (<a href="https://stackoverflow.com/a/2560017">Fonte</a>).*/
    public static String splitCamelCase(String s) {

        // TODO : rivedere e testare questo metodo

        return s.replaceAll(
                String.format("%s|%s|%s",
                        "(?<=[A-Z])(?=[A-Z][a-z])",
                        "(?<=[^A-Z])(?=[A-Z])",
                        "(?<=[A-Za-z])(?=[^A-Za-z])"
                ),
                " "
        );

    }

    /** Metodo per rendere maiuscola la prima lettera della
     * stringa passata come parametro.*/
    public static String trasformaPrimaLetteraMaiuscola(@NotNull String stringaDaModificare) {
        return stringaDaModificare.substring(0, 1).toUpperCase()
                + stringaDaModificare.substring(1);
    }

}
