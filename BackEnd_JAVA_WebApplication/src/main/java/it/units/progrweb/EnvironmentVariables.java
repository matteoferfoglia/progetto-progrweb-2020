package it.units.progrweb;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * Classe in cui sono definite tutte le variabili d'ambiente del progetto.
 * @author Matteo Ferfoglia
 */
public class EnvironmentVariables {

    /** Standard charset utilizzato nel progetto. */
    public static final Charset STANDARD_CHARSET = StandardCharsets.UTF_8;

    /** Inizio dell'URI delle richieste "api". In caso di modifiche,
     * aggiornare anche il file di configurazione web.xml.*/
    public static final String API_CONTEXT_ROOT = "/api";

    /** Servlet path (seguente al context path) per le richieste
     * "api" che non richiedono autenticazione. */
    public static final String API_NOAUTH_SERVLET_PATH = "/noauth";

    /** Nome dell'applicazione */
    public static final String NOME_APPLICAZIONE = "FileSharing";
}
