package it.units.progrweb.utils;

import com.google.appengine.api.utils.SystemProperty;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.NewCookie;
import java.util.Arrays;
import java.util.NoSuchElementException;

/**
 * Classe helper per la gestione dei cookie.
 *
 * @author Matteo Ferfoglia
 */
public class Cookie extends NewCookie {

    // Parametri comuni e tipici per tutti i cookie creati da questo server
    // Fonte: https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Set-Cookie

    /**
     * Attributo <i>Path</i> usato di default per il cookie, quando non specificato. E' il
     * percorso nell'url richiesto dal client per il quale verrà inviato il cookie.
     * <blockquote cite="https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Set-Cookie">
     * A path that must exist in the requested URL, or the browser won't send the Cookie header.
     * The forward slash (/) character is interpreted as a directory separator, and
     * subdirectories will be matched as well: for Path=/docs, /docs, /docs/Web/,
     * and /docs/Web/HTTP will all match.
     * </blockquote>.
     */
    public final static String PERCORSO_DEFAULT = "/";

    /**
     * Attributo <i>Host</i> usato di default per il cookie,
     * è l'host a cui verrà inviato il cookie.
     * Se omesso, è l'host del documento corrente.
     */
    public final static String HOST_DOMAIN_DEFAULT = "";

    /** Attributo <i>Max-Age</i> usato di default per il cookie, in secondi.*/
    public final static int MAX_AGE_DEFAULT_IN_SECONDI = 3600;

    /**  Flag <i>Secure</i> usato di default per il cookie. */
    public final static boolean SECURE_DEFAULT = SystemProperty.environment.value() == SystemProperty.Environment.Value.Production; // server di sviluppo non supporta HTTPS

    /** Flag <i>HttpOnly</i> usato di default per il cookie.*/
    public final static boolean HTTP_ONLY_DEFAULT = true;

    /** Valore <i>SameSite</i> usato di default per il cookie.*/
    public final static String SAME_SITE_VALUE = "Lax";


    // ------------------------------ FINE PARAMETRI CONFIGURABILI ------------------------------




    /** Nelle richieste HTTP (client verso server), è il nome dell' header contenente i cookie.*/
    public static final String NOME_HEADER_COOKIE = "Cookie";


    /** Vedere {@link NewCookie#NewCookie(String, String, String, String, String, int, boolean, boolean) NewCookie}.*/
    private Cookie(String nomeCookie, String valoreCookie,
                  String percorsoCookie, String hostDomainCookie,
                  String descrizioneCookie, int maxAge,
                  boolean secureCookie, boolean httpOnly) {

        super(nomeCookie, valoreCookie + ";SameSite="+SAME_SITE_VALUE, percorsoCookie, hostDomainCookie,
                descrizioneCookie, maxAge, secureCookie, httpOnly);
        // Aggiunge SameSite (non disponibile nella super classe utilizzata, quindi aggiunto manualmente in coda al valore del cookie)
    }

    public Cookie(String nomeCookie, String valoreCookie, String descrizioneCookie) {
        this(nomeCookie, valoreCookie,
                PERCORSO_DEFAULT, HOST_DOMAIN_DEFAULT,
                descrizioneCookie, MAX_AGE_DEFAULT_IN_SECONDI,
                SECURE_DEFAULT, HTTP_ONLY_DEFAULT);
    }

    public Cookie(String nomeCookie, String valoreCookie, int maxAge, String descrizioneCookie) {
        this(nomeCookie, valoreCookie,
                PERCORSO_DEFAULT, HOST_DOMAIN_DEFAULT,
                descrizioneCookie, maxAge,
                SECURE_DEFAULT, HTTP_ONLY_DEFAULT);
    }

    /**
     * Crea un nuovo cookie a partire da quello dato (ereditandone le
     * proprietà), impostando gli attributi <i>Max-Age</i> e <i>HttpOnly</i>
     * in base ai parametri ricevuti.
     */
    public Cookie(Cookie cookie, int maxAge, boolean httpOnly){
        this(cookie.getName(), cookie.getValue(),
                cookie.getPath(), cookie.getDomain(),
                cookie.getComment(), maxAge,
                cookie.isSecure(), httpOnly);
    }

    /** Crea un'istanza di questa classe copiando le informazioni dalla super classe.*/
    public Cookie(NewCookie newCookie) {
        super(newCookie);
    }


    /**
     * Dato l'headere di una richiesta HTTP in formato di stringa, ne
     * esegue il parsing e restituisce l'array di cookie trovati.
     * @param cookieHeader (String) Valore dell'header "Cookie" della richiesta HTTP di cui eseguire il parsing.
     * @return array di cookie
     */
    public static Cookie[] trovaCookiesDaHeader(String cookieHeader) {

        Cookie[] cookies;

        if( cookieHeader != null ) {

            try {
                // cookieHeader è una stringa del tipo: nomeCookie1=valoreCookie1; nomeCookie2=valoreCookie2; ...
                cookies = Arrays.stream(cookieHeader.split("; "))  // Stream<String>, ogni elemento è un cookie (nome=valore)
                        .map(Cookie::valueOf)                      // mappa la stringa "nome=valore" ad un Cookie
                        .toArray(Cookie[]::new);
            } catch (IllegalArgumentException exception) {
                //  impossibile eseguire il parsing della stringa data come parametro
                cookies = new Cookie[0];    //array vuoto
            }

        } else {
            cookies = new Cookie[0];
        }

        return cookies;

    }

    /** Data una richiesta HTTP, restituisce il'array dei cookie presenti in quella richiesta.*/
    public static Cookie[] getCookieDaRichiestaHttp(HttpServletRequest httpServletRequest) {
        return trovaCookiesDaHeader(httpServletRequest.getHeader(NOME_HEADER_COOKIE));
    }

    /** Hiding di {@link NewCookie#valueOf(String)}.*/
    public static Cookie valueOf(String cookieAsString_nomeValore)
            throws IllegalArgumentException {
        return new Cookie(NewCookie.valueOf(cookieAsString_nomeValore));
    }

    /**
     * Dato un array di cookie, cerca un cookie in base al nome dato come parametro.
     * @throws NoSuchElementException se il cookie cercato non è presente.
     */
    public static Cookie cercaCookiePerNomeERestituiscilo(String nomeCookieDaCercare, Cookie[] cookies)
            throws NoSuchElementException{

        return Arrays.stream(cookies)
                .filter(cookie -> cookie.getName().equals(nomeCookieDaCercare))
                .findAny()
                .orElseThrow(NoSuchElementException::new);
    }

}
