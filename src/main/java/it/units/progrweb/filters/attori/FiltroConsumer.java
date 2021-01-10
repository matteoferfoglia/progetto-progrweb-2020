package it.units.progrweb.filters.attori;

import it.units.progrweb.entities.attori.Attore;
import it.units.progrweb.entities.attori.Consumer;
import it.units.progrweb.utils.Autenticazione;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Intercetta le richieste per i Consumer e verifica
 * che siano tali (e non ad esempio Uploader).
 * @author Matteo Ferfoglia
 */
@WebFilter(filterName = "FiltroConsumer", asyncSupported = true)  // TODO : url pattern variabile d'ambiente
public class FiltroConsumer implements Filter {

    public void destroy() {}

    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain)
            throws ServletException, IOException {

        if(req instanceof HttpServletRequest) { // TODO : filtro che intercetta tutte le richieste e verifica se sono HttpServletRequest
            Attore attore = Autenticazione.getAttoreDaHttpServletRequest((HttpServletRequest) req);
            if(attore instanceof Consumer)
                chain.doFilter(req, resp);
        } else {
            Autenticazione.rispondiNonAutorizzato((HttpServletResponse) resp);
        }
    }

    public void init(FilterConfig config) throws ServletException {}

}
