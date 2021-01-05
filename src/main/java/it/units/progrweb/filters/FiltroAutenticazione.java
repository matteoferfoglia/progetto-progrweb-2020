package it.units.progrweb.filters;

import it.units.progrweb.utils.Autenticazione;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

import static it.units.progrweb.utils.UtilitaGenerale.*;

/**
 * Questo filtro verifica l'autenticazione di un client,
 * agendo grazie ad una whitelist: tutti gli url pattern
 * sono bloccati, tranne quelli esplicitamente permessi.
 * In questo filtro si verifica solo l'<em>autenticazione</em>
 * e non l'<em>autorizzazione</em> del client.
 * @author Matteo Ferfoglia
 */
@WebFilter(filterName = "FiltroAutenticazione", asyncSupported = true, urlPatterns = {"/*"})
public class FiltroAutenticazione implements Filter {

    /** Whitelist di uri accessibili senza autenticazione.*/
    private static final String[] WHITE_LIST_URL_PATTERNS_AUTENTICAZIONE_NON_RICHIESTA = {
            "/api/login",
            "/api/logout",
            "/api/registrazioneNuovoConsumer",
            "/api/CSRFToken/generaCSRFToken"
    };   // TODO : creare variabile d'ambiente con whitelist e creare variabile d'ambiente per ogni url pattern delle varie servlet


    public void destroy() {}


    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain)
            throws ServletException, IOException {

        if(req instanceof HttpServletRequest) {
            HttpServletRequest httpReq = (HttpServletRequest) req;

            if( isRisorsaAdAccessoLibero(httpReq)
                || (isRichiestaApi(httpReq) && Autenticazione.isClientAutenticato(httpReq)) )
                chain.doFilter(req, resp);
            // attualmente, solo richieste api possibili

        }
    }



    public void init(FilterConfig config) throws ServletException {}



    /** Restituisce true se la richiesta http passata come parametro
     * fa riferimento ad un url presente nella
     * {@link #WHITE_LIST_URL_PATTERNS_AUTENTICAZIONE_NON_RICHIESTA
     * whitelist}, altrimenti restituisce false.
     * @return true se <strong>non</strong> è richiesta l'autenticazione, false altrimenti.*/
    private static boolean isRisorsaAdAccessoLibero(HttpServletRequest httpReq) {
        return isPresenteNellArray( getUrlPattern(httpReq),
                                    WHITE_LIST_URL_PATTERNS_AUTENTICAZIONE_NON_RICHIESTA );
    }

}
