package it.units.progrweb.filters;

import it.units.progrweb.utils.*;
import it.units.progrweb.utils.csrf.CsrfToken;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static it.units.progrweb.utils.UtilitaGenerale.getUrlPattern;

/**
 * Filtro per verificare la correttezza del token CSRF.
 * Questo filtro intercetta tutte le richieste ma filtra
 * solo quelle per gli URL specificati in {@link #LISTA_URL_DA_CONTROLLARE_CSRF},
 * verificandone il token CSRF prima di permetter alla
 * richiesta di procedere.
 *
 * @author Matteo Ferfoglia
 */
@WebFilter(filterName = "FiltroCSRF")
public class FiltroCSRF implements Filter {

    /** Blacklist di uri accessibili solo dopo il controllo CSRF.*/
    private static final String[] LISTA_URL_DA_CONTROLLARE_CSRF = {
            "/api/login",
            "/api/logout",
            "/api/uploader/cancellaConsumerPerQuestoUploader",
            "/api/modificaInformazioniAttore",
            "/api/uploader/aggiungiConsumerPerQuestoUploader",
            "/api/uploader/cancellaConsumerPerQuestoUploader",
            "/api/uploader/documenti/eliminaDocumento",
            "/api/uploader/documenti/uploadDocumento",
            "/api//administrator/aggiungiAttore",
            "/api/administrator/modificaAttore",
            "/api/uploader/modificaConsumer"
    };

    /** Nome del parametro che nella richiesta HTTP (proveniente dal client)
     * contiene il token CSRF. */
    private static final String NOME_PARAMETRO_CSRF_TOKEN_NELLA_REQUEST = "csrfToken";

    // Ricerca dei parametri in una request dipende dal content-type
    private static final String CONTENT_TYPE_JSON = "application/json";
    private static final String COTENT_TYPE_MULTIPART_FORM = "multipart/form-data";



    public void destroy() {}

    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain)
            throws ServletException, IOException {

        HttpServletRequest httpReq = (HttpServletRequest) req;

        if( UtilitaGenerale.isStessoPrefissoNellArray( getUrlPattern(httpReq), LISTA_URL_DA_CONTROLLARE_CSRF) ) {

            String indirizzoIPClient = httpReq.getRemoteAddr();
            String cookieHeader = httpReq.getHeader(Cookie.NOME_HEADER_COOKIE);

            // Cerca parametro CSRF
            String csrfToken = null;    //inizializzazione
            String contentType = httpReq.getContentType();

            // Copio la httpRequest perché getInputStream() può essere invocato una volta sola (e verrà
            // usato dai servizi REST), altrimenti: java.lang.IllegalStateException: READER .
            // Questo codice esegue parsing del body della request, quindi usa getInputStream(), per
            // cercare CSRF token nel body della request.
            HttpServletRequest copiaHttpReq = new HttpRequestWrapper(httpReq);

            if(contentType != null) {   // null se non c'è content-type nella request
                // Cerca CSRF nel body della request

                if( contentType.toLowerCase().contains(CONTENT_TYPE_JSON) ||
                        contentType.toLowerCase().contains(COTENT_TYPE_MULTIPART_FORM) ) {

                    // Parsing del body della request per trovare il csrfToken se presente
                    StringBuilder requestBody = new StringBuilder();

                    try {

                        getRequestBody(copiaHttpReq, requestBody);

                        if( contentType.toLowerCase().contains(CONTENT_TYPE_JSON) ) {
                            Map<String,?> mappaProprietaValori = JsonHelper.convertiStringaJsonInMappaProprieta(requestBody.toString());
                            csrfToken = (String) mappaProprietaValori.get(NOME_PARAMETRO_CSRF_TOKEN_NELLA_REQUEST);
                        } else if ( contentType.toLowerCase().contains(COTENT_TYPE_MULTIPART_FORM) ) {
                            // Necessario parsing (semplificato, solo per individuare il token CSRF)
                            csrfToken = estraiCsrfTokenDaMultiPart(indirizzoIPClient, cookieHeader, contentType, requestBody, NOME_PARAMETRO_CSRF_TOKEN_NELLA_REQUEST);
                        }


                    } catch (Exception e) {
                        Logger.scriviEccezioneNelLog(this.getClass(), "Errore nella lettura del body della request", e);
                        ResponseHelper.creaResponseServerError("",(HttpServletResponse)resp);
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
                // Problemi con CSRF, quindi redirection login
                HttpServletResponse response = (HttpServletResponse)resp;
                response.setStatus( HttpStatusCode_CsrfTokenInvalido.getStatusCode() );
                response.setContentType(MediaType.TEXT_PLAIN);
                response.getWriter().write(HttpStatusCode_CsrfTokenInvalido.getReasonPhrase());
                response.addHeader("Location", httpReq.getRequestURI());// chiede al client di ricaricare la pagina (così aggiorna i token)

            }

        } else {
            // Questo ramo dell' IF se la richiesta non ha CSRF token da controllare
            chain.doFilter(req, resp);
        }

    }

    /** Dato il body di una request di tipo Multipart, restituisce il valore
     * del CSRF token trovato, se presente e valido, o stringa vuota altrimenti.
     * Fonte: https://www.w3.org/Protocols/rfc1341/7_2_Multipart.html */
    @SuppressWarnings("SameParameterValue")
    private String estraiCsrfTokenDaMultiPart(String indirizzoIPClient, String cookieHeader, String contentType,
                                              StringBuilder requestBody, String nomeParametroCsrfTokenNellaRequest) {

        // Estrazione del boundary dal content-type
        String boundary_nomeAttributo="boundary=";
        String boundary = contentType.substring( contentType.indexOf(boundary_nomeAttributo)+boundary_nomeAttributo.length());

        List<String> listaCsrfTokenTorovatiNellaRichiesta =
              Arrays.stream(requestBody.toString().split(boundary))
                    .filter( encapsulatedMultipart -> Pattern.compile("(name=\"" + nomeParametroCsrfTokenNellaRequest + "\")")
                                                             .matcher(encapsulatedMultipart)
                                                             .find() )
                    // dopo filter() restano solo i campi che contengono csrfToken
                    .flatMap( encapsulatedMultipart -> {
                        Matcher matcher = Pattern.compile("(?:\\r\\n)(.*)")   // prendi tutte le righe
                                .matcher(encapsulatedMultipart);

                        List<String> valoriCsrfToken = new ArrayList<>();
                        while(matcher.find())
                            valoriCsrfToken.add(matcher.group(1).trim());  // prendo il gruppo col valore cercato

                        return valoriCsrfToken.stream();
                    })
                    .collect( Collectors.toList() );

        return listaCsrfTokenTorovatiNellaRichiesta.stream()
                        .filter( csrfTokenTrovato -> CsrfToken.isCsrfTokenValido(csrfTokenTrovato, cookieHeader, indirizzoIPClient) )
                        .findAny()
                        .orElse("");   // restituisce il primo (non in ordine) token valido trovato, oppure stringa vuota
    }


    /** Dati una {@link HttpServletRequest} ed un {@link StringBuilder},
     * modifica lo {@link StringBuilder} dato appendendoci linea per linea
     * il body della request data.
     * Fonte: https://docs.oracle.com/javaee/6/api/javax/servlet/ServletInputStream.html#readLine
     * @param httpServletRequest Istanza della {@link HttpServletRequest}.
     * @param requestBody Istanza della classe {@link StringBuilder} a cui appendere tutte le
     *                    linee della {@link HttpServletRequest}.
     * @throws IOException - Eventualmente generata dai metodi {@link HttpServletRequest#getInputStream()}
     *                          oppure {@link ServletInputStream#readLine(byte[], int, int)}.
     */
    private void getRequestBody(HttpServletRequest httpServletRequest, StringBuilder requestBody) throws IOException {
        ServletInputStream inputStream = httpServletRequest.getInputStream();

        UtilitaGenerale.leggiDaInputStream(inputStream, requestBody);
    }

    public void init(FilterConfig config) /*throws ServletException*/ {}

}


/** Wrapper per HttpServletRequest.
 * Motivazione di utilizzo di questo wrapper: getReader() può essere invocato una
 * volta sola (e verrà usato dai servizi REST), altrimenti:
 *  java.lang.IllegalStateException: READER . Questo wrapper serve per poter leggere
 *  il body di una request senza generare IllegalStateException .
 *  Fonte:  https://stackoverflow.com/a/25640232 .*/
class HttpRequestWrapper extends HttpServletRequestWrapper {

    private final byte[] payload;

    public HttpRequestWrapper(HttpServletRequest request) throws IOException {

        super(request);

        // read the original payload into the payload variable
        ByteArrayOutputStream builder = new ByteArrayOutputStream();
        try (InputStream inputStream = request.getInputStream()) {  // try-with-resources, Fonte: https://docs.oracle.com/javase/tutorial/essential/exceptions/tryResourceClose.html
            if (inputStream != null) {
                byte[] buffer = new byte[128];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer, 0, buffer.length)) != -1) {
                    builder.write(buffer, 0, bytesRead);
                }
            } else {
                // make an empty string since there is no payload
                builder.write(Byte.parseByte(""));
            }
        } catch (IOException ex) {
            Logger.scriviEccezioneNelLog(HttpRequestWrapper.class, "Error reading the request payload", ex);
            throw new IOException("Error reading the request payload", ex);
        }

        payload = builder.toByteArray();

    }

    @Override
    public ServletInputStream getInputStream () {
        final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(payload);
        return new ServletInputStream() {
            @Override
            public boolean isFinished() {return false;}

            @Override
            public boolean isReady() {return false;}

            @Override
            public void setReadListener(ReadListener readListener) {}

            public int read () {
                return byteArrayInputStream.read();
            }
        };
    }
}

/** Status code personalizzato per indicare al client che il token
 * CSRF non è più valido.*/
class HttpStatusCode_CsrfTokenInvalido {

    /** Codice errore (personalizzato).*/
    private static final int statusCode = 499;
    private static final String reasonPhrase = "CSRF token invalido";

    public static int getStatusCode() {
        return statusCode;
    }

    public static String getReasonPhrase() {
        return reasonPhrase;
    }

}