package it.units.progrweb.filters;

import it.units.progrweb.utils.Autenticazione;
import it.units.progrweb.utils.Cookie;
import it.units.progrweb.utils.JsonHelper;
import it.units.progrweb.utils.Logger;
import it.units.progrweb.utils.csrf.CsrfToken;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Response;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static it.units.progrweb.utils.UtilitaGenerale.getUrlPattern;
import static it.units.progrweb.utils.UtilitaGenerale.isPresenteNellArray;

/**
 * Filtro per verificare la correttezza del token CSRF.
 * Questo filtro intercetta tutte le richieste ma filtra
 * solo quelle per gli URL specificati in {@link #LISTA_URL_DA_CONTROLLARE_CSRF},
 * verificandone il token CSRF prima di permetter alla
 * richiesta di procedere.
 *
 * @author Matteo Ferfoglia
 */
@WebFilter(filterName = "FiltroCSRF", asyncSupported = true, urlPatterns = {"/*"})
public class FiltroCSRF implements Filter {

    /** Blacklist di uri accessibili solo dopo il controllo CSRF.*/
    private static final String[] LISTA_URL_DA_CONTROLLARE_CSRF = {
            "/api/login",
            "/api/logout"
    };   // TODO : creare variabile d'ambiente con whitelist e creare variabile d'ambiente per ogni url pattern delle varie servlet

    /** Nome del parametro che nella richiesta HTTP (proveniente dal client)
     * contiene il token CSRF. */
    private static final String NOME_PARAMETRO_CSRF_TOKEN_NELLA_REQUEST = "csrfToken";      // TODO : CREARE var d'ambiente comune anche al progetto Vue

    // Ricerca dei parametri in una request dipende dal content-type
    private static final String CONTENT_TYPE_JSON = "application/json";



    public void destroy() {}

    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain)
            throws ServletException, IOException {

        if(req instanceof HttpServletRequest) {     // TODO : creare un filtro per NON istanze di HttpRervletRequest
            HttpServletRequest httpReq = (HttpServletRequest) req;

            if( isPresenteNellArray( getUrlPattern(httpReq), LISTA_URL_DA_CONTROLLARE_CSRF) ) {

                String indirizzoIPClient = httpReq.getRemoteAddr();
                String cookieHeader = httpReq.getHeader(Cookie.NOME_HEADER_COOKIE);

                // Cerca parametro CSRF
                String csrfToken = null;    //inizializzazione
                String contentType = httpReq.getContentType();

                // Copio la httpRequest perché getInputStream() può essere invocato una volta sola (e verrà
                // usato dai servizi REST), altrimenti: java.lang.IllegalStateException: READER .
                // Questo codice esegue parsing del body della request, quindi usa getInputStream()
                HttpServletRequest copiaHttpReq = new HttpRequestWrapper(httpReq);

                if(contentType != null) {   // null se non c'è content-type nella request

                    if( contentType.toLowerCase().contains(CONTENT_TYPE_JSON) ) {

                        // Parsing del body della request per trovare il csrfToken se presente (Fonte: https://stackoverflow.com/a/3831791 e adattato)
                        StringBuffer requestBody = new StringBuffer();

                        try {

                            // TODO : rivedere questa parte e refactoring (estrarre un metodo che faccia solo il parsing della richiesta JSON)

                            ServletInputStream reader = copiaHttpReq.getInputStream();

                            // readLine(): https://docs.oracle.com/javaee/6/api/javax/servlet/ServletInputStream.html#readLine(byte[],%20int,%20int)
                            int byteLetti = 0;                                              // integer specifying the character at which this method begins reading
                            final int DIMENSIONE_ARRAY_LETTURA = 64;                        // in bytes
                            byte[] arrayByteLettura = new byte[DIMENSIONE_ARRAY_LETTURA];   // array of bytes into which data is read
                            while( (byteLetti = reader.readLine(arrayByteLettura,           // array di lettura "buffer"
                                                            0,                          // in lettura, occupiamo l'array (primo parametro) dal primo elemento
                                                                DIMENSIONE_ARRAY_LETTURA)   // massimo spazio disponibile nell'array
                                    ) != -1) {    // TODO : rivedere questa parte
                                // Lettura di una linea alla volta
                                String line = new String(arrayByteLettura, StandardCharsets.UTF_8);    // TODO : cercare tutte le occorrenza di UTF-8 e renderla variabile d'ambiente
                                requestBody.append(line);
                            }

                            Map<String,?> mappaProprietaValori = JsonHelper.convertiStringaJsonToMappaProprieta(requestBody.toString());
                            csrfToken = (String) mappaProprietaValori.get(NOME_PARAMETRO_CSRF_TOKEN_NELLA_REQUEST);

                        } catch (Exception e) {
                            Logger.scriviEccezioneNelLog(this.getClass(), "Errore nella lettura del body della request", e);
                            ((HttpServletResponse)resp).sendError(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                                    Response.Status.INTERNAL_SERVER_ERROR.getReasonPhrase());
                        }

                    }

                } else {
                    // Parametri form / query string leggibili con getParameter
                    csrfToken = httpReq.getParameter(NOME_PARAMETRO_CSRF_TOKEN_NELLA_REQUEST);
                }


                if (csrfToken != null               // se esiste il token csrf
                        && cookieHeader != null     // se esiste l'header coi cookie
                        && CsrfToken.isCsrfTokenValido(csrfToken, cookieHeader, indirizzoIPClient)) {

                    chain.doFilter(copiaHttpReq, resp);

                } else {
                    // Problemi con CSRF
                    Autenticazione.rispondiNonAutorizzato((HttpServletResponse) resp);
                }

            } else {
                // Questo ramo dell' IF se la richiesta non ha CSRF token da controllare
                chain.doFilter(req, resp);
            }
        }

    }

    public void init(FilterConfig config) throws ServletException {}

}


/** Wrapper per HttpServletRequest, fonte:  https://stackoverflow.com/a/25640232 .
 * Motivazione di utilizzo di questo wrapper: getReader() può essere invocato una
 * volta sola (e verrà usato dai servizi REST), altrimenti:
 *  java.lang.IllegalStateException: READER . Questo wrapper serve per poter leggere
 *  il body di una request senza generare IllegalStateException .*/
class HttpRequestWrapper extends HttpServletRequestWrapper {

    // TODO : classe da rivedere

    private final String payload;

    public HttpRequestWrapper(HttpServletRequest request) throws IOException {
        super(request);

        // read the original payload into the payload variable
        StringBuilder stringBuilder = new StringBuilder();
        BufferedReader bufferedReader = null;
        try {
            // read the payload into the StringBuilder
            InputStream inputStream = request.getInputStream();
            if (inputStream != null) {
                bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                char[] charBuffer = new char[128];
                int bytesRead = -1;
                while ((bytesRead = bufferedReader.read(charBuffer)) > 0) {
                    stringBuilder.append(charBuffer, 0, bytesRead);
                }
            } else {
                // make an empty string since there is no payload
                stringBuilder.append("");
            }
        } catch (IOException ex) {
            Logger.scriviEccezioneNelLog(HttpRequestWrapper.class, "Error reading the request payload", ex);
            throw new IOException("Error reading the request payload", ex);
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException iox) {
                    // ignore
                }
            }
        }
        payload = stringBuilder.toString();
    }

    @Override
    public ServletInputStream getInputStream () throws IOException {
        final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(payload.getBytes());
        ServletInputStream inputStream = new ServletInputStream() {
            @Override
            public boolean isFinished() {return false;}

            @Override
            public boolean isReady() {return false;}

            @Override
            public void setReadListener(ReadListener readListener) {}

            public int read ()
                    throws IOException {
                return byteArrayInputStream.read();
            }
        };
        return inputStream;
    }
}
