package it.units.progrweb.filters;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * Filtro per rigettare le richieste non HTTP.
 * @author Matteo Ferfoglia
 */
@WebFilter(filterName = "FiltroHttpServletRequest", asyncSupported = true)
public class FiltroHttpServletRequest implements Filter {
    public void destroy() {
    }

    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain)
            throws ServletException, IOException {
        if( req instanceof HttpServletRequest)
            chain.doFilter(req, resp);
        else {
            resp.getWriter().write( "Si accettano solo richieste HTTP." );
        }
    }

    public void init(FilterConfig config) throws ServletException {

    }

}
