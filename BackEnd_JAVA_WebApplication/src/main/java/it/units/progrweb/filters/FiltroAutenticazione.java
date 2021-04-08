package it.units.progrweb.filters;

import it.units.progrweb.utils.Autenticazione;
import it.units.progrweb.utils.UtilitaGenerale;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static it.units.progrweb.EnvironmentVariables.API_CONTEXT_ROOT;
import static it.units.progrweb.api.autenticazioneERegistrazione.VerificaRegistrazione.PATH_SERVIZIO_VERIFICA_ACCCOUNT;
import static it.units.progrweb.utils.UtilitaGenerale.*;

/**
 * Questo filtro verifica l'autenticazione di un client,
 * agendo grazie ad una whitelist: tutti gli url pattern
 * sono bloccati, tranne quelli esplicitamente permessi.
 * In questo filtro si verifica solo l'<em>autenticazione</em>
 * e non l'<em>autorizzazione</em> del client.
 * @author Matteo Ferfoglia
 */
@WebFilter(filterName = "FiltroAutenticazione")
public class FiltroAutenticazione implements Filter {

    /** Whitelist di percorsi uri accessibili senza autenticazione: se il
     * percorso richiesto dal client inizia con una delle stringhe presenti
     * in questo array, allora quella risorsa è considerata ad accesso libero
     * e non si verifica l'autorizzazione del client.*/
    private static final String[] WHITE_LIST_URL_AUTENTICAZIONE_NON_RICHIESTA = {
            API_CONTEXT_ROOT+"/login",
            API_CONTEXT_ROOT+"/firebaseLogin",
            API_CONTEXT_ROOT+"/logout",
            PATH_SERVIZIO_VERIFICA_ACCCOUNT,
            API_CONTEXT_ROOT+"/resetPassword",
            API_CONTEXT_ROOT+"/webService/login",
            API_CONTEXT_ROOT+"/webService/uploadDocumento",
            API_CONTEXT_ROOT+"/verificaTokenAutenticazione",
            API_CONTEXT_ROOT+"/registrazioneNuovoConsumer",
            API_CONTEXT_ROOT+"/CSRFToken/generaCSRFToken",
            API_CONTEXT_ROOT+"/noauth",
            API_CONTEXT_ROOT+"/openapi",
            "/_ah/admin", "/_ah/admin/datastore", "/_ah/resources" // console di amministrazione del sever di sviluppo
    };


    public void destroy() {}


    /** Verifica che la richiesta in ingresso provenga da un client autenticato
     * oppure che la risorsa richiesta non richieda l'autenticazione. */
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain)
            throws ServletException, IOException {

        HttpServletRequest httpReq = (HttpServletRequest) req;

        if( isRisorsaAdAccessoLibero(httpReq)
            || (isRichiestaApi(httpReq)
                && ( httpReq.getMethod().equals(FiltroCORS.METODO_HTTP_INTERCETTATO)   // "OPTIONS" per richieste "api" gestite dal filtro CORS
                     || Autenticazione.isClientAutenticato(httpReq) ) ) ) {

            chain.doFilter(req, resp);
            // attualmente, solo richieste di tipo "api" possibili
        } else {
            Autenticazione.creaResponseUnauthorized((HttpServletResponse) resp);
        }

    }



    public void init(FilterConfig config) /*throws ServletException*/ {}



    /** Restituisce true se la richiesta http passata come parametro
     * fa riferimento ad un url presente nella
     * {@link #WHITE_LIST_URL_AUTENTICAZIONE_NON_RICHIESTA
     * whitelist}, altrimenti restituisce false.
     * @return true se <strong>non</strong> è richiesta l'autenticazione, false altrimenti.*/
    private static boolean isRisorsaAdAccessoLibero(HttpServletRequest httpReq) {
        return UtilitaGenerale.isStessoPrefissoNellArray( getUrlPattern(httpReq),
                WHITE_LIST_URL_AUTENTICAZIONE_NON_RICHIESTA);
    }

}
