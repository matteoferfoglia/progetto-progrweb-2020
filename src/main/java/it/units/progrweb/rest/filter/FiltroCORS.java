package it.units.progrweb.rest.filter;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author Matteo Ferfoglia
 */
@WebFilter(filterName = "FiltroCORS", asyncSupported = true, urlPatterns = {"/api/*"})
public class FiltroCORS implements Filter {

    // TODO questo filtro è da rivedere / implementare


    // Parametri:

    /** Array di stringhe contenente le origini permesse dalla CORS policy (whitelist).*/
    private final static String[] CORS_ALLOWED_ORIGINS = {
            "http://localhost:8080" // TODO : creare variabile d'ambiente: durante lo sviluppo, questa è l'origine delle pagine originate dal server Vue
    };

    /** Array di stringhe contente i metodi permessi dalla CORS policy. */
    private final static String[] CORS_ALLOWED_METHOD = {
            "GET",
            "POST"
    };

    /**
     * Max-Age per la CORS policy.
     * <blockquote cite="https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Access-Control-Max-Age">
     *     The Access-Control-Max-Age response header indicates how long
     *     the results of a preflight request (that is the information
     *     contained in the Access-Control-Allow-Methods and
     *     Access-Control-Allow-Headers headers) can be cached.
     * </blockquote>
     */
    private final static int CORS_MAX_AGE = 3600; // in secondi

    /** Array di stringhe contente gli header HTTP che saranno permessi dalla CORS policy.*/
    private final static String[] CORS_ALLOWED_HEADERS = {
            // TODO : servono tutti questi headers?
            "Content-Type", // TODO : non servirebbe perché è già in white list cors (vedi specifiche)
            "Access-Control-Allow-Headers",
            "Authorization",
            "X-Requested-With",
            "Accept-Charset",
            "Origin",
            "Accept",
            "Set-Cookie"
    };

    /**
     * Flag impostato a true per permettere l'invio delle credenziali
     * (<a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Access-Control-Allow-Credentials">Fonte</a>).
     */
    private final static boolean CORS_ALLOW_CREDENTIALS = true;




    public FiltroCORS() {
    }

    public void init(FilterConfig filterConfig) {
    }

    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse)servletResponse;

        response.addHeader("Access-Control-Allow-Origin", String.join(", ", CORS_ALLOWED_ORIGINS));
        response.addHeader("Access-Control-Allow-Methods", String.join(", ", CORS_ALLOWED_METHOD));
        response.addHeader("Access-Control-Max-Age", String.valueOf(CORS_MAX_AGE));
        response.addHeader("Access-Control-Allow-Headers", String.join(", ",CORS_ALLOWED_HEADERS));
        response.addHeader("Access-Control-Allow-Credentials", String.valueOf(CORS_ALLOW_CREDENTIALS));

        if(request.getMethod().equals("OPTIONS")){
            response.setStatus(HttpServletResponse.SC_ACCEPTED);
        }else{
            chain.doFilter(request, response);
        }
    }

    public void destroy() {
    }

}
