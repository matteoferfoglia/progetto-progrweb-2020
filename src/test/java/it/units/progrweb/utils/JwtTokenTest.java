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
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
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
    // TODO


    /** Numero di iterazioni di ogni test di questa classe.
     * I test hanno dei parametri aleatori, quindi, probabilmente,
     * iterazioni successive corrispondono a diversi parametri per i test.*/
    private final static int QUANTI_TEST_PER_OGNI_TIPO_IN_QUESTA_CLASSE = 1000;   // todo : variabile d'ambiente

    /** Test per {@link it.units.progrweb.utils.jwt.JwtToken#generaTokenJsonCodificatoBase64UrlEncoded()}.*/
    @ParameterizedTest
    @MethodSource("generaTokenECorrispondenteRappresentazioneComeStringaDelToken")
    void generaTokenJsonTest(JwtToken token, String corrispondenteJsonCodificatoBase64UrlEncoded){
         // todo : rivedere questo test ed il metodo di generazione dei parametri

        assertEquals(corrispondenteJsonCodificatoBase64UrlEncoded, token.generaTokenJsonCodificatoBase64UrlEncoded());

    }

    /** Genera i parametri per {@link #generaTokenJsonTest(JwtToken)}.*/
    private static Stream<Arguments> generaTokenECorrispondenteRappresentazioneComeStringaDelToken() {

        // TODO : rivedere questo metodo di generazione parametri per il test

        final int QUANTI_TOKEN = QUANTI_TEST_PER_OGNI_TIPO_IN_QUESTA_CLASSE; // è il numero di elementi nello stream

        return IntStream .range(0, QUANTI_TOKEN)
                         .mapToObj(contatore -> {

                             JwtClaimsSet jwtClaimsSet;
                             String payloadJson = "{"; // rappresentazione JSON del payload
                             {
                                 // Creazione del JwtClaimsSet per il payload per un JwtToken

                                 final short NUMERO_MASSIMO_CLAIM_PAYLOAD = 10;    // todo :  variabile d'ambiente
                                 final short LUNGHEZZA_MASSIMA_NOME_CLAIM = 1000;   // todo :  variabile d'ambiente
                                 final short LUNGHEZZA_MASSIMA_VALORE_CLAIM = 1000; // todo :  variabile d'ambiente

                                 Random random = new Random();

                                 final int NUMERO_CLAIM_QUESTO_PAYLOAD = random.nextInt(NUMERO_MASSIMO_CLAIM_PAYLOAD);
                                 Object[] stream_claims_json = IntStream.range(0, NUMERO_CLAIM_QUESTO_PAYLOAD)
                                                 .mapToObj(contatoreClaimPayload -> {
                                                     final int LUNGHEZZA_NOME_CLAIM = random.nextInt(LUNGHEZZA_MASSIMA_NOME_CLAIM);
                                                     final int LUNGHEZZA_VALORE_CLAIM = random.nextInt(LUNGHEZZA_MASSIMA_VALORE_CLAIM);

                                                     final String NOME_CLAIM_GENERATO = GeneratoreTokenCasuali.generaTokenAlfanumerico(LUNGHEZZA_NOME_CLAIM + 1);
                                                     final String VALORE_CLAIM_GENERATO = GeneratoreTokenCasuali.generaTokenAlfanumerico(LUNGHEZZA_VALORE_CLAIM + 1);

                                                     String claimJson = " \"" + NOME_CLAIM_GENERATO + "\":\"" + VALORE_CLAIM_GENERATO + "\" ";

                                                     Object[] claims_e_json = {new JwtClaim(NOME_CLAIM_GENERATO, VALORE_CLAIM_GENERATO), claimJson};
                                                     return claims_e_json;
                                                 }).toArray(); // todo : collect in list, poi prendi una lista coi soli primi elementi di questa crea la lista token, e coi secondi crei la lista cliam json da collezionare separati da virgolab

                                 jwtClaimsSet = new JwtClaimsSet(Arrays.stream(stream_claims_json)
                                                                     .map(claim_json -> {
                                                                         Object[] object_token_json = (Object[])claim_json;
                                                                         JwtClaim jwtClaim = (JwtClaim) object_token_json[0];
                                                                         return jwtClaim;
                                                                     }).collect(Collectors.toList()) );
                                 payloadJson += Arrays.stream(stream_claims_json)
                                         .map(claim_json -> {
                                             Object[] object_token_json = (Object[])claim_json;
                                             String jwtClaimInFormatoJson = (String) object_token_json[1];
                                             return jwtClaimInFormatoJson;
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

    /** Test per {@link it.units.progrweb.utils.jwt.JwtToken#JwtToken(it.units.progrweb.utils.jwt.componenti.JwtPayload)}.
     * Dati in claims del payload in formato JSON, si costruisce il token JWT,
     * si prende la sua rappresentazione come stringa codificata in base64
     * url-encoded e si verifica che sia uguale alla stringa attesa.*/
    @ParameterizedTest
    @CsvSource({
            "'{\"sub\": \"1234567890\", \"name\": \"John Doe\",\"admin\": true}', ''"   // todo
    })
    void JwtTokenTest(String claimsPayloadInJson) {
        // TODO : creare il token col costruttore, prendere la sua rappresentazione come stringa che sia uguale a correttaRappresentazioneJsonDelJwtToken
    // todo test da fare
    }

    /** Test per {@link JwtToken#equals(Object)}.*/
    @ParameterizedTest
    @MethodSource("generaToken")
    void equalsTest(JwtToken jwtToken){
        try {
            boolean equalsFunzionaConLoStessoToken = jwtToken.equals(jwtToken);

            boolean equalsFunzionaConLaCopiaDelloStessoToken = creaNuovoTokenUgualeAQuelloDato(jwtToken).equals(jwtToken);

            boolean equalsRiconosceTokenNulli = ! jwtToken.equals(null);

            JwtToken jwtTokenDiverso;   // per verificare che .equals() riesca a riconoscere oggetti diversi
            jwtTokenDiverso = creaNuovoTokenUgualeAQuelloDatoEdAggiungiUnClaim(jwtToken, new JwtClaim("Claim aggiunto che renderà il token diverso", "qualsiasi valore"));
            boolean equalsRiconosceTokenDiversi = ! jwtTokenDiverso.equals(jwtToken);

            assertTrue(equalsFunzionaConLoStessoToken && equalsFunzionaConLaCopiaDelloStessoToken
                        && equalsRiconosceTokenNulli && equalsRiconosceTokenDiversi);


        } catch (NoSuchMethodException | IllegalAccessException | InstantiationException
                | InvocationTargetException | NoSuchFieldException e) {
            fallisciTestACausaDiEccezioneNonAttesa(e);
        }


    }

    /** Crea un nuovo JwtToken identico a quello dato nel primo parametro,
     * poi aggiunge il claims dato nel secondo parametro e restituisce
     * il token così ottenuto.
     * NB.: non viene ricalcolata la signature del token, quindi il token
     * prodotto da questo metodo risulterà invalido.*/
    private static JwtToken creaNuovoTokenUgualeAQuelloDatoEdAggiungiUnClaim(JwtToken jwtToken, JwtClaim claimDaAggiungere)
            throws NoSuchFieldException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
         
        // Crea un nuovo token identico a quello iniziale, poi lo modifica
        JwtToken jwtTokenDiverso = creaNuovoTokenUgualeAQuelloDato(jwtToken);
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

    private static JwtToken creaNuovoTokenUgualeAQuelloDato(JwtToken jwtToken)
            throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Constructor<JwtToken> costruttoreCopia = JwtToken.class.getDeclaredConstructor(JwtToken.class);
        costruttoreCopia.setAccessible(true);
        return costruttoreCopia.newInstance(jwtToken);
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
            boolean isValido = Math.round(Math.random()) == 1 ? true : false ;

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


    /** Test per {@link JwtToken#creaJwtTokenDaStringaCodificata(String)}.*/
    // @Test // todo : fare questo test
    void creaJwtTokenDaStringaCodificata(String stringaCodificata) {
        // TODO : creare il token col costruttore, prendere la sua rappresentazione come stringa ed invocare il metodo per la costruzione del Jwt, poi confrontarlo con uno costruito manualmente
        // todo
    }

    /** Test per {@link it.units.progrweb.utils.jwt.JwtToken#isSignatureValida()}.
     * In particolare se si altera il payload, la verifica della signature deve
     * dare esito negativo.*/
    @ParameterizedTest
    @MethodSource("generaToken")
    void isSignatureValidaTest(JwtToken jwtTokenDiCuiVerificareLaFirma){

        try {

            boolean firmaDovrebbeEssereValida = jwtTokenDiCuiVerificareLaFirma.isSignatureValida();

            JwtToken jwtTokenModificato = modificaTokenERestituisciNuovoTokenConFirmaNONValida(jwtTokenDiCuiVerificareLaFirma);
            boolean firmaDovrebbeEssereNONValida = jwtTokenModificato.isSignatureValida();

            assertTrue(firmaDovrebbeEssereValida && !firmaDovrebbeEssereNONValida);

        } catch (NoSuchFieldException|InstantiationException|IllegalAccessException
                |InvocationTargetException|NoSuchMethodException e) {
            fallisciTestACausaDiEccezioneNonAttesa(e);
        }

    }

    /** Copia il token dato in una nuova istanza, poi modifica
     * la copia, che quindi risulterà avere una firma invalida.
     * Infine restituisce il token con la firma invalida.
     * Il token passato come parametro non viene modificato.*/
    private static JwtToken modificaTokenERestituisciNuovoTokenConFirmaNONValida(JwtToken jwtToken)
            throws InvocationTargetException, NoSuchMethodException,
            InstantiationException,IllegalAccessException, NoSuchFieldException {
        return creaNuovoTokenUgualeAQuelloDatoEdAggiungiUnClaim(jwtToken, new JwtClaim("", ""));
    }

    /** Test per {@link it.units.progrweb.utils.jwt.JwtToken#isTokenScaduto()} .*/
    @ParameterizedTest
    @MethodSource("generaTokenConScadenza")
    void isTokenScadutoTest(JwtToken tokenDiCuiVerificareScadenza, boolean isValido_valoreAtteso){
        boolean isValido_valoreCalcolato = ! tokenDiCuiVerificareScadenza.isTokenScaduto();
        assertEquals(isValido_valoreAtteso, isValido_valoreCalcolato);
    }


    /** Test per {@link it.units.progrweb.utils.jwt.JwtToken#isTokenValido()} */
    @ParameterizedTest
    @MethodSource("generaTokenConScadenza")
    void isTokenValidoTest(JwtToken jwtToken, boolean isNONScaduto_valoreAtteso){

        try {
            // Decisione aleatoria se in questo token si falsificherà la firma
            boolean isSignatureValida_valoreAtteso = Math.round(Math.random()) == 1 ? true : false;  // se true, firma sarà valida

            if(!isSignatureValida_valoreAtteso)
                jwtToken = modificaTokenERestituisciNuovoTokenConFirmaNONValida(jwtToken);

            boolean isTokenValido_valoreAtteso = isNONScaduto_valoreAtteso && isSignatureValida_valoreAtteso;
            boolean isTokenValido_valoreCalcolato = jwtToken.isTokenValido();

            assertEquals(isTokenValido_valoreAtteso, isTokenValido_valoreCalcolato);

        } catch (InvocationTargetException|NoSuchMethodException|InstantiationException
                |IllegalAccessException|NoSuchFieldException e) {
            fallisciTestACausaDiEccezioneNonAttesa(e);
        }

    }

}