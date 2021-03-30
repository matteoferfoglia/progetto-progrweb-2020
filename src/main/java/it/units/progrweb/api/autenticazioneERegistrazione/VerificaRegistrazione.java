package it.units.progrweb.api.autenticazioneERegistrazione;

import it.units.progrweb.entities.AuthenticationDatabaseEntry;
import it.units.progrweb.persistence.NotFoundException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import static it.units.progrweb.EnvironmentVariables.API_CONTEXT_ROOT;

/**
 * Classe che espone i servizi per la verifica di registrazione
 * di un utente nel sistema (procedure di verifica account
 * implementate dalla classe {@link it.units.progrweb.entities.AuthenticationDatabaseEntry}).
 * @author Matteo Ferfoglia
 */
@Path("")
public class VerificaRegistrazione {

    /** Path per il servizio che si occuperà della verifica di un account. */
    private static final String PATH_RELATIVO_SERVIZIO_VERIFICA_ACCOUNT = "/verificaAccount";

    /** Path completo (dalla context-root) del servizio che si occupare della verifica di un account. */
    public static final String PATH_SERVIZIO_VERIFICA_ACCCOUNT = API_CONTEXT_ROOT + PATH_RELATIVO_SERVIZIO_VERIFICA_ACCOUNT;

    /** Nome del QueryParam contenente lo username dell'account da verificare. */
    public static final String NOME_QUERY_PARAM_CON_USERNAME_ACCOUNT_DA_VERIFICARE = "username";

    /** Nome del QueryParam contenente il token dell'account da verificare. */
    public static final String NOME_QUERY_PARAM_CON_TOKEN_ACCOUNT_DA_VERIFICARE = "tokenVerifica";

    @Path(PATH_RELATIVO_SERVIZIO_VERIFICA_ACCOUNT)
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String verificaAccount(@QueryParam("username") String usernameAccountDaVerificare,
                                  @QueryParam("tokenVerifica") String tokenVerificaAccount) {

        final String MESSAGGIO_ERRORE_SE_VERIFICA_FALLISCE =
                "Errore nella verifica dell'account. Provare ad usare l'opzione \"Password dimenticaata\".";

        try {
            return AuthenticationDatabaseEntry.verificaAccount(usernameAccountDaVerificare, tokenVerificaAccount) ?
                    "Account verificato, ora utilizzabile regolarmente." :
                    MESSAGGIO_ERRORE_SE_VERIFICA_FALLISCE;
        } catch (NotFoundException ignored) {
            return MESSAGGIO_ERRORE_SE_VERIFICA_FALLISCE;
        }
    }

}
