package it.units.progrweb.filters.attori;

import it.units.progrweb.entities.attori.Attore;
import it.units.progrweb.entities.attori.nonAdministrator.uploader.Uploader;
import it.units.progrweb.utils.Autenticazione;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Intercetta le richieste per gli Uploader e verifica
 * che siano tali.
 * @author Matteo Ferfoglia
 */
@WebFilter(filterName = "FiltroUploader", asyncSupported = true)
public class FiltroUploader implements Filter {

    public void destroy() {}

    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain)
            throws ServletException, IOException {

        Attore attore = Autenticazione.getAttoreDaHttpServletRequest((HttpServletRequest) req);
        if(attore instanceof Uploader)
            chain.doFilter(req, resp);
        else
            Autenticazione.rispondiNonAutorizzato((HttpServletResponse) resp);

    }

    public void init(FilterConfig config) throws ServletException {}

}
