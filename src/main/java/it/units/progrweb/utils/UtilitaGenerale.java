package it.units.progrweb.utils;

import it.units.progrweb.EnvironmentVariables;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Classe di utilità generale, ad esempio per la manipolazione di
 * array.
 * @author Matteo Ferfoglia
 */
public class UtilitaGenerale {

    /** Restituisce true se l'array dato contiene l'oggetto
     * da cercare, false altrimenti. La classe dell'oggetto
     * cercato deve implementare il metodo <code>equals</code>.*/
    public static<T> boolean isPresenteNellArray(T oggettoDaCercare, T[] array) {
        return Arrays.asList(array).contains(oggettoDaCercare);
    }

    /** Restituisce true se l'array di stringhe dato contiene un
     * oggetto che inizia con la stringa da cercare. Altrimenti
     * restituisce false.*/
    public static boolean isStessoPrefissoNellArray(String oggettoDaCercare, String[] array) {
        return Arrays.stream(array).anyMatch(oggettoDaCercare::startsWith);
    }

    /** Restituisce true se la richiesta è di tipo "api" (Ajax), false altrimenti.*/
    public static boolean isRichiestaApi(HttpServletRequest httpReq) {
        return getUrlPattern(httpReq).startsWith(EnvironmentVariables.API_URL_PATTERN);
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
    public static String getUrlPattern(@NotNull HttpServletRequest httpRequest) {
        // TODO : testare
        return httpRequest.getRequestURI().substring(httpRequest.getContextPath().length());
    }

    /** Restituiscel'indirizzo del server, inteso come ciò precede quanto
     * restituito da {@link HttpServletRequest#getRequestURI()}.
     * Esempio: se il request URL è  http://nome.server.com:8080/app/file
     * allora questo metodo restituirà http://nome.server.com:8080 .*/
    public static String getIndirizzoServer(@NotNull HttpServletRequest httpServletRequest) {

        // TODO : testare

        String requestUrl = httpServletRequest.getRequestURL().toString();
        String requestUri = httpServletRequest.getRequestURI();

        return requestUrl.substring(0, requestUrl.indexOf(requestUri));

    }

    /** Crea una mappa (nome->valore) con i Field dati nel parametro.
     * Se un attributo (tra i field dati) dell'oggetto dato è null,
     * allora il corrispettivo valore della mappa sarà una stringa vuota.
     * @param campiDaMostrareNellaMappa Entries che devono essere presenti nella mappa prodotta.
     * @param oggetto Oggetto da cui estrarre i valori.
     * @param <T> Classe dell'oggetto.
     * @return Mappa { nomeField[in formato HumanReadable] -> Valore field per l'oggetto dato }.
     */
    public static<T> Map<String, Object> getMappaNomeValoreProprieta(Field[] campiDaMostrareNellaMappa, T oggetto) {

        return Arrays.stream(campiDaMostrareNellaMappa)
                .collect(
                        Collectors.toMap(
                                nomeField -> nomeField.getName(),
                                field -> {
                                    try {
                                        field.setAccessible(true);
                                        Object valore = field.get(oggetto);
                                        return valore == null ? "" : valore;    // stringa vuota se attributo nullo
                                    } catch (IllegalAccessException exception) {
                                        Logger.scriviEccezioneNelLog(oggetto.getClass(), exception);
                                        return exception;
                                    }
                                }

                        )
                );

    }

    /** Verifica l'esistenza dell'attributo il cui nome è dato nel parametro
     * nella classe data.
     * @return true se l'attributo esiste, false altrimenti.
     */
    public static boolean esisteAttributoInClasse(String nomeAttributo, Class classe) {

        try {
            classe.getDeclaredField(nomeAttributo);
            return true;
        } catch (NoSuchFieldException e) {
            return false;
        }

    }

    /** Verifica l'esistenza di un field nella classe data, dato il suo nome
     * come parametro. Se il field non si trova, lo scrive nel {@link Logger}.
     * @return Il nome stesso se il field esiste, stringa vuota altrimenti.
     */
    public static String ricercaFieldPerNomeInQuestaClasse( String nomeField,
                                                            Class classeInCuiCercareField ) {

        if ( esisteAttributoInClasse(nomeField, classeInCuiCercareField) ) {
            return nomeField;
        } else {
            Logger.scriviEccezioneNelLog(classeInCuiCercareField,
                    "Field di nome " + nomeField + " non trovato nella classe.",
                    new NoSuchFieldException(""));
            return "";
        }

    }

    /** Data una mappa, crea la stringa JSON corrispondente e la
     * inserisce nella entity di una {@link Response} di tipo "ok",
     * che restituisce dopo aver costruito.
     * @param mappa
     * @return {@link Response} costruita.
     */
    public static Response rispostaJsonConMappa(Map<String,?> mappa) {
        // Costruzione della response
        return Response.ok()
                       .type(MediaType.APPLICATION_JSON)
                       .entity(JsonHelper.convertiMappaProprietaToStringaJson(mappa))
                       .build();
    }

    /** Effettua la conversione da Input Stream a byte[]
     * (<a href="https://stackoverflow.com/a/1264737">Fonte</a>).*/
    public static byte[] convertiInputStreamInByteArray( InputStream is ) {

        final int CAPACITA_BUFFER_ARRAY = 16384;  // 16 KB
        byte[] bytes = new byte[CAPACITA_BUFFER_ARRAY];

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        int nRead;
        try {
            while ((nRead = is.read(bytes)) != -1)
                buffer.write(bytes, 0, nRead);
        } catch (IOException e) {
            Logger.scriviEccezioneNelLog(UtilitaGenerale.class, e);
        }


        byte[] daRestituire = buffer.toByteArray();
        try {
            buffer.close();
        } catch (IOException e) {
            Logger.scriviEccezioneNelLog(UtilitaGenerale.class, e);
        }
        return daRestituire;

    }

    /** Dato il nome di un file, ne restituisce l'estensione.*/
    public static String getEstensioneDaNomeFile(String nomeFile) {

        if(nomeFile==null)
            return "";

        int indiceSeparatoreEstensioneNelNome = nomeFile.lastIndexOf(".");
        indiceSeparatoreEstensioneNelNome = indiceSeparatoreEstensioneNelNome == -1 ?  // true se manca l'estensione
                                                nomeFile.length() :
                                                indiceSeparatoreEstensioneNelNome;
        return nomeFile.substring(indiceSeparatoreEstensioneNelNome+1);                // +1 per escludere il punto
    }

    /** Verifica che una stringa non sia nè nulla nè vuota.*/
    public static boolean isStringaNonNullaNonVuota(String stringa) {

        if( stringa == null ) return false;
        if( stringa.trim().isEmpty() ) return false;

        return true;

    }
}
