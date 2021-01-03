package it.units.progrweb.utils;

import it.units.progrweb.UtilsInTest;
import it.units.progrweb.utils.csrf.CsrfCookies;
import it.units.progrweb.utils.csrf.CsrfToken;
import it.units.progrweb.utils.jwt.JwtToken;
import it.units.progrweb.utils.jwt.componenti.JwtPayload;
import it.units.progrweb.utils.jwt.componenti.claim.JwtClaim;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static it.units.progrweb.api.CreazioneCsrfToken.CLIENT_ID_TOKEN_LENGTH;
import static it.units.progrweb.api.CreazioneCsrfToken.CSRF_TOKEN_LENGTH;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;

/**
 * @author Matteo Ferfoglia
 */
public class CsrfCookiesTest {

    private static final int QUANTI_TEST_PER_OGNI_TIPO = 1000;  // numero di "iterazioni" di ogni test (con parametri diversi) // TODO : VARIABILE D'AMBIENTE
        // mantenere elevato il numero di test perché ci sono parametri aleatori (generati casualmente)

    /** Test per {@link it.units.progrweb.utils.csrf.CsrfCookies#isCsrfTokenValido(String, JwtToken, String, String, String, String)}.
     * Viene generato il token CSRF e l'identificativo del client (allo stesso
     * modo di quando un client ne fa richiesta), poi viene creato il token
     * JWT (allo stesso modo di come verrebbe creato per il client), infine
     * viene ricreato un nuovo token JWT nel server, con lo stesso payload
     * e si verifica che i due token (quello creato per il client e quello
     * ricreato nel server) siano uguali ({@link JwtToken#equals(Object)}).
     * Questo test è necessario perché la verifica di un token JWT avviene
     * in modo simile (vedere {@link CsrfCookies#isCsrfTokenValido(String, JwtToken, String, String, String, String)}
     * {@link JwtToken#isTokenValido()}, {@link JwtToken#isSignatureValida()})*/
    @RepeatedTest(QUANTI_TEST_PER_OGNI_TIPO) // questo test fa uso di valori casuali, quindi ripeterlo potrebbe portare a risultati diversi
    void test_verificaValiditaTokenCsrf() {

        // TODO : rivedere questo metodo (la descrizione è coerente con ciò che fa? Serve?)

        try {

            String indirizzoIPClient = UtilsInTest.generaIndirizzoIP();

            CsrfToken csrfToken = new CsrfToken(CSRF_TOKEN_LENGTH, CLIENT_ID_TOKEN_LENGTH, indirizzoIPClient);
            String valoreCsrfTokenDaVerificare = csrfToken.getValoreCsrfToken();
            String valoreIdentificativoClientRicevuto =csrfToken.getValoreIdentificativoClient();

            JwtToken jwtTokenDelClient;
            {
                // Creazione del token JWT, come verrebbe creato per un client
                Method metodoCreaJwtToken = CsrfToken.class.getDeclaredMethod("creaJwtToken");
                metodoCreaJwtToken.setAccessible(true);
                jwtTokenDelClient = (JwtToken) metodoCreaJwtToken.invoke(csrfToken);
            }


            // Per validare il token JWT del client, si crea un altro token JWT
            // con gli stessi parametri e si verifica che i due token abbiano la stessa firma
            JwtToken jwtTokenRicreatoNelServer;
            {
                Method metodoCreaJwtPayload = CsrfToken.class.getDeclaredMethod("creaJwtPayload",
                        String.class, String.class, String.class);
                metodoCreaJwtPayload.setAccessible(true);
                JwtPayload jwtPayloadRicreato = (JwtPayload) metodoCreaJwtPayload.invoke(null,
                        valoreIdentificativoClientRicevuto, valoreCsrfTokenDaVerificare, indirizzoIPClient);

                {
                    // Imposta lo stesso ExpirationTimeClaim impostato nel token del client

                    Field jwtPayloadDelClientField = JwtToken.class.getDeclaredField("payload");
                    jwtPayloadDelClientField.setAccessible(true);
                    JwtPayload jwtPayloadDelClient = (JwtPayload) jwtPayloadDelClientField.get(jwtTokenDelClient);
                    JwtClaim expirationTimeClaimClient = jwtPayloadDelClient.getClaimByName(JwtClaim.JWT_EXPIRATION_TIME_CLAIM_NAME);

                    jwtPayloadRicreato.modificaValoreClaim(JwtClaim.JWT_EXPIRATION_TIME_CLAIM_NAME, expirationTimeClaimClient.getValue());
                }

                jwtTokenRicreatoNelServer = new JwtToken(jwtPayloadRicreato);
            }

            assertTrue(jwtTokenDelClient.equals(jwtTokenRicreatoNelServer));


        } catch (NoSuchAlgorithmException | InvalidKeyException | NoSuchMethodException
                | IllegalAccessException | InvocationTargetException | NoSuchFieldException e) {
            UtilsInTest.testLog(UtilsInTest.stringaConStackTrace(e));
            fail("Eccezione non attesa: " + UtilsInTest.stringaConStackTrace(e));
        }

    }


    /** Test per {@link it.units.progrweb.utils.csrf.CsrfCookies#isCsrfTokenValido(String, JwtToken, String, String, String, String)}.
     * @param valoreCsrfTokenDaVerificare è il token CSRF da verificare (ottenuto dal
     *                                    form in cui è stato usato)
     * @param cookieHeader è il cookie header (testuale) ricevuto dalla richiesta HTTP.
     * @param isCsrfCookieValido_expected è il risultato atteso: true se il cookie con
     *                                    il token csrf è valido.
     */
    @ParameterizedTest
    @MethodSource("generaParametri")
    void test_verificaValiditaCookieCsrf(String valoreCsrfTokenDaVerificare,
                                         String indirizzoIPClient,
                                         String cookieHeader,
                                         boolean isCsrfCookieValido_expected ) {

        try {
            Method metodoProtettoDaTestare = CsrfCookies.class.getDeclaredMethod("isCsrfTokenValido",
                    String.class, String.class, String.class, String.class, String.class);
            metodoProtettoDaTestare.setAccessible(true);

            boolean risultatoOttenuto = (boolean) metodoProtettoDaTestare.invoke(null,
                    valoreCsrfTokenDaVerificare, cookieHeader, indirizzoIPClient,
                    CsrfToken.NOME_CLAIM_CSRF_TOKEN, CsrfToken.NOME_CLAIM_IP_CLIENT );

            if(risultatoOttenuto!=isCsrfCookieValido_expected) {
                UtilsInTest.testLog( "----- TEST FALLITO -----\n"
                        + "VALORE CSRF TOKEN DA VERIFICARE:\t" + valoreCsrfTokenDaVerificare + "\n"
                        + "COOKIE HEADER:\t" + cookieHeader + "\n"
                        + "NOME CLAIM CSRF TOKEN:\t" + CsrfToken.NOME_CLAIM_CSRF_TOKEN + "\n"
                        + "NOME CLAIM IP CLIENT:\t" + CsrfToken.NOME_CLAIM_IP_CLIENT + "\n"
                        + "** RISULTATO ATTESO:\t" + isCsrfCookieValido_expected + "\n"
                        + "## RISULTATO OTTENUTO:\t" + risultatoOttenuto + "\n\n");
            }



            assertEquals(isCsrfCookieValido_expected, risultatoOttenuto);

        } catch (IllegalAccessException| InvocationTargetException |NoSuchMethodException e) {
            fail("Eccezione non attesa: " + UtilsInTest.stringaConStackTrace(e));
        }

    }

    /** Genera i parametri per {@link #test_verificaValiditaCookieCsrf(String, String, String, boolean)}.*/
    private static Stream<Arguments> generaParametri() {
        final int QUANTI_TEST_POSITIVI = QUANTI_TEST_PER_OGNI_TIPO;    // specifica quanti arguments devono essere generati (un arguments per ogni test)
        final int QUANTI_TEST_NEGATIVI = QUANTI_TEST_PER_OGNI_TIPO;

        return Stream.concat(
            IntStream.range(0,QUANTI_TEST_POSITIVI)
                    .mapToObj(val -> {
                        String[] args = generaCsrfTokenValido();
                        return arguments(args[0], args[1], args[2], true);
                    }),
            IntStream.range(0,QUANTI_TEST_NEGATIVI)
                    .mapToObj(val -> {
                        String[] args = generaCsrfTokenInvalido();
                        return arguments(args[0], args[1], args[2], false);
                    })
        );
    }



    /** Genera un CSRF token valido e restituisce un array di tre stringhe:
     * <ol>
     *     <li>token CSRF generato</li>
     *     <li>indirizzo IP del client generato</li>
     *     <li>cookie header generato, contenente:
     *      <ul>
     *          <li>identificativo del client</li>
     *          <li>
     *              token JWT generato con (nel payload) l'identificativo del
     *              ed client ed il csrf token e l'indirizzo IP del client
     *          </li>
     *      </ul>
     *     </li>
     * </ol>
     */
    private static String[] generaCsrfTokenValido() {

        final String[] generato = new String[3];

        try {

            final String indirizzoIPClient = UtilsInTest.generaIndirizzoIP();
            final CsrfToken csrfToken = new CsrfToken(CSRF_TOKEN_LENGTH, CSRF_TOKEN_LENGTH, indirizzoIPClient);
            generato[0] = csrfToken.getValoreCsrfToken();
            generato[1] = csrfToken.getValoreIPClient();

            final String cookieHeader;
            {
                final CsrfCookies cookiesJwtEIdClient = csrfToken.creaCookiesPerCsrf();
                final Cookie cookieJwt = cookiesJwtEIdClient.getCookieCSRF();
                final Cookie cookieIdClient = cookiesJwtEIdClient.getCookieVerificaIdentitaClient();

                final String cookieHeaderInventato = "nomeCookieFinto=valoreCookieFinto";

                cookieHeader = cookieHeaderInventato + "; "
                                + cookieJwt.getName() + "=" + cookieJwt.getValue() + "; "
                                + cookieIdClient.getName() + "=" + cookieIdClient.getValue() + "; "
                                + cookieHeaderInventato;
            }

            generato[2] = cookieHeader;
        } catch (NoSuchAlgorithmException|InvalidKeyException e) {
            fail("Eccezione: " + UtilsInTest.stringaConStackTrace(e));
        }

        return generato;
    }

    /** Come {@link #generaCsrfTokenValido()}, ma questo metodo genera un token invalido*/
    private static String[] generaCsrfTokenInvalido() {
        String[] generato;
        // Genera parametri validi, poi li altera
        generato = generaCsrfTokenValido();
        final int posizioneDiUnCarattereACasoDaAlterare = 10;
        final String stringaSostitutiva = "=";
        generato[0] = generato[0].replaceAll(String.valueOf(generato[0].charAt(posizioneDiUnCarattereACasoDaAlterare)),
                                             stringaSostitutiva);
        return generato;
    }
}
