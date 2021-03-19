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

    /** Inizio dell'URI delle richieste "api".*/
    public static final String API_URL_PATTERN = "/api";

    /** Nome dell'applicazione */
    public static final String NOME_APPLICAZIONE = "FileSharing";
}
