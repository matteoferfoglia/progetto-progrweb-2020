package it.units.progrweb.filters.attori;

import it.units.progrweb.entities.attori.Attore;
import it.units.progrweb.entities.attori.administrator.Administrator;
import it.units.progrweb.utils.Autenticazione;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Intercetta le richieste per gli Administrator e verifica
 * che siano tali.
 * @author Matteo Ferfoglia
 */
@WebFilter(filterName = "FiltroAdministrator", asyncSupported = true)
public class FiltroAdministrator implements Filter {

    public void destroy() {}

    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain)
            throws ServletException, IOException {

        Attore attore = Autenticazione.getAttoreDaHttpServletRequest((HttpServletRequest) req);
        if(attore instanceof Administrator)
            chain.doFilter(req, resp);
        else
            Autenticazione.rispondiNonAutorizzato((HttpServletResponse) resp);

    }

    public void init(FilterConfig config) throws ServletException {}

}
