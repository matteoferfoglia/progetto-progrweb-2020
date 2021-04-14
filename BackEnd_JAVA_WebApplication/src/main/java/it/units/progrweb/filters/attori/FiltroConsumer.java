package it.units.progrweb.filters.attori;

import it.units.progrweb.entities.attori.consumer.Consumer;
import it.units.progrweb.filters.FiltroAutenticazione;
import it.units.progrweb.utils.Autenticazione;
import it.units.progrweb.utils.ResponseHelper;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Intercetta le richieste per i Consumer e verifica
 * che siano tali (e non ad esempio Uploader).
 * La verifica viene fatta in base ai valori del token
 * di autenticazione, senza interrogare il database (ci
 * si fida che ciò che è scritto nel token sia autentico
 * ed integro - verifica demandata a {@link FiltroAutenticazione},
 * che dovrebbe intercettare la richiesta prima di questo
 * filtro.
 * @author Matteo Ferfoglia
 */
@WebFilter(filterName = "FiltroConsumer")
public class FiltroConsumer implements Filter {

    public void destroy() {}

    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain)
            throws ServletException, IOException {

        String tipoAttore = Autenticazione.getTipoAttoreDaHttpServletRequest((HttpServletRequest) req);
        if(tipoAttore.equals(Consumer.class.getSimpleName()))
            chain.doFilter(req, resp);
        else
            ResponseHelper.creaResponseUnauthorized("",(HttpServletResponse) resp);

    }

    public void init(FilterConfig config) /*throws ServletException*/ {}

}
