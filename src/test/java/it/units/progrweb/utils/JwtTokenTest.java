package it.units.progrweb.utils;

import it.units.progrweb.utils.jwt.JwtToken;
import it.units.progrweb.utils.jwt.componenti.JwtClaimsSet;
import it.units.progrweb.utils.jwt.componenti.JwtHeader;
import it.units.progrweb.utils.jwt.componenti.JwtPayload;
import it.units.progrweb.utils.jwt.componenti.JwtSignature;
import it.units.progrweb.utils.jwt.componenti.claims.JwtClaim;
import it.units.progrweb.utils.jwt.componenti.claims.JwtExpirationTimeClaim;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.lang.reflect.Field;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static it.units.progrweb.UtilsInTest.fallisciTestACausaDiEccezioneNonAttesa;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;

/**
 * Classe di test per {@link it.units.progrweb.utils.jwt.JwtToken}.
 * @author Matteo Ferfoglia
 */
public class JwtTokenTest {

    /** Numero di iterazioni di ogni test di questa classe.
     * I test hanno dei parametri aleatori, quindi, probabilmente,
     * iterazioni successive corrispondono a diversi parametri per i test.*/
    private final static int QUANTI_TEST_PER_OGNI_TIPO_IN_QUESTA_CLASSE = 1000;

    /** Test per {@link it.units.progrweb.utils.jwt.JwtToken#generaTokenJsonCodificatoBase64UrlEncoded()}.*/
    @ParameterizedTest
    @MethodSource("generaTokenECorrispondenteRappresentazioneComeStringaDelToken")
    void generaTokenJsonTest(JwtToken token, String corrispondenteJsonCodificatoBase64UrlEncoded){

        assertEquals(corrispondenteJsonCodificatoBase64UrlEncoded, token.generaTokenJsonCodificatoBase64UrlEncoded());

    }

    /** Genera i parametri per {@link #generaToken()}.*/
    private static Stream<Arguments> generaTokenECorrispondenteRappresentazioneComeStringaDelToken() {

        return IntStream .range(0, QUANTI_TEST_PER_OGNI_TIPO_IN_QUESTA_CLASSE)
                         .mapToObj(contatore -> {

                             JwtClaimsSet jwtClaimsSet;
                             String payloadJson = "{"; // rappresentazione JSON del payload
                             {
                                 // Creazione del JwtClaimsSet per il payload per un JwtToken

                                 final short NUMERO_MASSIMO_CLAIM_PAYLOAD = 10;
                                 final short LUNGHEZZA_MASSIMA_NOME_CLAIM = 1000;
                                 final short LUNGHEZZA_MASSIMA_VALORE_CLAIM = 1000;

                                 Random random = new Random();

                                 final int NUMERO_CLAIM_QUESTO_PAYLOAD = random.nextInt(NUMERO_MASSIMO_CLAIM_PAYLOAD);
                                 Object[] stream_claims_json = IntStream.range(0, NUMERO_CLAIM_QUESTO_PAYLOAD)
                                                 .mapToObj(contatoreClaimPayload -> {
                                                     final int LUNGHEZZA_NOME_CLAIM = random.nextInt(LUNGHEZZA_MASSIMA_NOME_CLAIM);
                                                     final int LUNGHEZZA_VALORE_CLAIM = random.nextInt(LUNGHEZZA_MASSIMA_VALORE_CLAIM);

                                                     final String NOME_CLAIM_GENERATO = GeneratoreTokenCasuali.generaTokenAlfanumerico(LUNGHEZZA_NOME_CLAIM + 1);
                                                     final String VALORE_CLAIM_GENERATO = GeneratoreTokenCasuali.generaTokenAlfanumerico(LUNGHEZZA_VALORE_CLAIM + 1);

                                                     String claimJson = " \"" + NOME_CLAIM_GENERATO + "\":\"" + VALORE_CLAIM_GENERATO + "\" ";

                                                     return new Object[]{new JwtClaim<>(NOME_CLAIM_GENERATO, VALORE_CLAIM_GENERATO), claimJson};
                                                 }).toArray();

                                 jwtClaimsSet = new JwtClaimsSet(Arrays.stream(stream_claims_json)
                                                                     .map(claim_json -> {
                                                                         Object[] object_token_json = (Object[])claim_json;
                                                                         return (JwtClaim<?>) object_token_json[0];
                                                                     }).collect(Collectors.toList()) );
                                 payloadJson += Arrays.stream(stream_claims_json)
                                         .map(claim_json -> {
                                             Object[] object_token_json = (Object[])claim_json;
                                             return (String) object_token_json[1];
                                         }).collect(Collectors.joining(","));

                             }
                             JwtPayload jwtPayload = new JwtPayload(jwtClaimsSet);
                             payloadJson += "}";

                             try {

                                 JwtToken jwtToken;
                                 try {
                                     jwtToken = new JwtToken(jwtPayload);
                                 } catch (IllegalStateException e) {
                                     // Eccezione se ci sono due claims con lo stesso nome nel payload
                                     jwtPayload = new JwtPayload();
                                     jwtToken = new JwtToken(jwtPayload); // token con payload vuoto
                                     payloadJson = "{}";
                                 }

                                 String headerJson = "{ \"alg\":\"" + JwtSignature.getAlgoritmoHash() + "\""
                                         + " , \"typ\":\"JWT\" }";
                                 String signature = new JwtSignature(new JwtHeader(JwtSignature.getAlgoritmoHash()),
                                         jwtPayload).getSignature();

                                 String stringaJsonCorrispondenteAlTokenCodificataBase64UrlEncoded = Base64Helper.encodeToBase64UrlEncoded(headerJson) + "."
                                                                                                     + Base64Helper.encodeToBase64UrlEncoded(payloadJson) + "."
                                                                                                     + signature;

                                 return arguments(jwtToken, stringaJsonCorrispondenteAlTokenCodificataBase64UrlEncoded);
                             } catch (NoSuchAlgorithmException|InvalidKeyException e) {
                                 return (Arguments)fallisciTestACausaDiEccezioneNonAttesa(e);
                             }
                         });
    }

    /** Test per {@link JwtToken#equals(Object)}.*/
    @ParameterizedTest
    @MethodSource("generaToken")
    void equalsTest(JwtToken jwtToken){
        try {
            @SuppressWarnings("EqualsWithItself") boolean equalsFunzionaConLoStessoToken = jwtToken.equals(jwtToken);

            boolean equalsFunzionaConLaCopiaDelloStessoToken = new JwtToken(jwtToken).equals(jwtToken);

            JwtToken jwtTokenDiverso;   // per verificare che .equals() riesca a riconoscere oggetti diversi
            jwtTokenDiverso = creaNuovoTokenUgualeAQuelloDatoEdAggiungiUnClaim(jwtToken, new JwtClaim<>("Claim aggiunto che renderà il token diverso", "qualsiasi valore"));
            boolean equalsRiconosceTokenDiversi = ! jwtTokenDiverso.equals(jwtToken);

            //noinspection ConstantConditions   // test fatto apposta per verificare correttezza
            assertTrue(equalsFunzionaConLoStessoToken &&
                        equalsFunzionaConLaCopiaDelloStessoToken &&
                        equalsRiconosceTokenDiversi);


        } catch ( IllegalAccessException | NoSuchFieldException e) {
            fallisciTestACausaDiEccezioneNonAttesa(e);
        }


    }

    /** Crea un nuovo JwtToken identico a quello dato nel primo parametro,
     * poi aggiunge il claims dato nel secondo parametro e restituisce
     * il token così ottenuto.
     * NB.: non viene ricalcolata la signature del token, quindi il token
     * prodotto da questo metodo risulterà invalido.*/
    private static JwtToken creaNuovoTokenUgualeAQuelloDatoEdAggiungiUnClaim(JwtToken jwtToken, JwtClaim<?> claimDaAggiungere)
            throws NoSuchFieldException, IllegalAccessException {
         
        // Crea un nuovo token identico a quello iniziale, poi lo modifica
        JwtToken jwtTokenDiverso = new JwtToken(jwtToken);
        JwtPayload jwtPayloadModificato = getPayload(jwtTokenDiverso);
        jwtPayloadModificato.aggiungiClaim(claimDaAggiungere);
        
        return jwtTokenDiverso;
    }

    /** Restituisce il payload di un token JWT dato.*/
    private static JwtPayload getPayload(JwtToken jwtToken)
            throws NoSuchFieldException, IllegalAccessException {

        Field jwtPayload_Field = JwtToken.class.getDeclaredField("payload");
        jwtPayload_Field.setAccessible(true);
        return (JwtPayload) jwtPayload_Field.get(jwtToken);

    }

    private static Stream<Arguments> generaToken() {
        Stream<Arguments> streamTokenECorrispondenteJson = generaTokenECorrispondenteRappresentazioneComeStringaDelToken();
        // ogni elemento di questa map è un Arguments, è un array di due oggetti: JwtToken e String
        return streamTokenECorrispondenteJson
                .map( tokenEJson -> arguments(tokenEJson.get()[0]) ) ;   //estraggo solo il token
    }

    /** Crea token JWT con payload casuale ed una scadenza,
     * quindi restituisce il token come primo parametro ed
     * un flag booleano come secondo parametro (true se il
     * token è valido, false se è scaduto).
     * La decisione di generare un flag scaduto oppure valido
     * viene presa in modo aleatorio.
     */
    private static Stream<Arguments> generaTokenConScadenza() {

        final int MINIMA_DIFFERENZA_TEMPORALE_SCADENZA_TOKEN_RISPETTO_CURRENT_TIME_IN_SECONDI = 100;    // potrebbe anche essere zero secondi (serve solo a garantire che il token non scada prima di arrivare al test)

        Stream<Arguments> tokensJwt = generaToken();

        return tokensJwt.map(tokenJwtArgument -> {

            // Generazione casuale del flag di validità (true se token valido)
            boolean isValido = Math.round(Math.random()) == 1;

            JwtToken jwtToken = (JwtToken)tokenJwtArgument.get()[0];
            {
                JwtPayload jwtPayload = new JwtPayload();
                try {
                    jwtPayload = getPayload(jwtToken);
                } catch (NoSuchFieldException|IllegalAccessException e) {
                    // Non dovrebbe mai succedere
                    fallisciTestACausaDiEccezioneNonAttesa(e);
                }

                int differenzaInSecondiTraTempoAttualeEScadenzaDiQuestoToken = ((int)(Math.random()*Short.MAX_VALUE)   // il valore assoluto di questa differenza potrebbe essere qualsiasi (qua generato casualmente)
                                                                                + MINIMA_DIFFERENZA_TEMPORALE_SCADENZA_TOKEN_RISPETTO_CURRENT_TIME_IN_SECONDI)
                                                                                * (isValido ? 1 : -1);  //valore negativo (rispetto ad ora) significa token scaduto

                jwtPayload.aggiungiClaim(new JwtExpirationTimeClaim(differenzaInSecondiTraTempoAttualeEScadenzaDiQuestoToken));

                // Ricalcolo della firma sul token JWT (a seguito dell'aggiunta di un claims, il token è cambiato e la firma non è più valida).
                try {
                    jwtToken = new JwtToken(jwtPayload);
                } catch (NoSuchAlgorithmException|InvalidKeyException e) {
                    // Non dovrebbe mai succedere
                    fallisciTestACausaDiEccezioneNonAttesa(e);
                }
            }

            return arguments(jwtToken, isValido);

        });

    }


    /** Test per {@link it.units.progrweb.utils.jwt.JwtToken#isTokenScaduto()} .*/
    @ParameterizedTest
    @MethodSource("generaTokenConScadenza")
    void isTokenScadutoTest(JwtToken tokenDiCuiVerificareScadenza, boolean isValido_valoreAtteso){
        boolean isValido_valoreCalcolato = ! tokenDiCuiVerificareScadenza.isTokenScaduto();
        assertEquals(isValido_valoreAtteso, isValido_valoreCalcolato);
    }

}