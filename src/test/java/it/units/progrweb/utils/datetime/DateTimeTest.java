package it.units.progrweb.utils.datetime;

import it.units.progrweb.UtilsInTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;

/**
 * Classe di test per {@link DateTimeTest}
 * @author Matteo Ferfoglia
 */
public class DateTimeTest {

    /** Da questo parametro dipende il numero di test che verranno esguiti, vedere {@link #generaParametri_Instant()}. */
    private final static int QUANTI_ISTANTI_GENERARE = 1000;    // valore elevato perché alcuni parametri sono aleatori // TODO : VARIABILE D'AMBIENTE


    /** Test per {@link DateTime#adesso()} ()} , {@link DateTime#toString()},
     * {@link DateTime#oggi()}, {@link DateTime#getData()},
     * {@link DateTime#DateTime(int, int, int, int, int, int)},
     * {@link DateTime#getOraPrecisaAlSecondo()}, {@link DateTime#getDataOra()}.
     * Risultati controllati manualmente (presente un'automazione parziale).*/
    @Test
    void testDaVerificareManualmente(){

        // Test da controllare manualmente

        boolean corretto;

        UtilsInTest.testLog(DateTime.adesso().toString());
        UtilsInTest.testLog(Date.from(Instant.now()).toString());

        UtilsInTest.testLog("---");

        DateTime dateTime = new DateTime(31,8,2020,11,5,0);
        UtilsInTest.testLog(dateTime.toString());
        UtilsInTest.testLog(Date.from(LocalDateTime.of(2020, Month.AUGUST,31,11,5,0).toInstant(ZoneOffset.UTC)).toString());
        UtilsInTest.testLog(String.valueOf(dateTime.getGiornoDelMese()));
        String s = new DateTime("31/08/2020 11:05:00 Z",
                                DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss X")).toString();
        UtilsInTest.testLog(s);
        corretto = dateTime.toString().equals(s);

        UtilsInTest.testLog("---");

        DateTime dateTime2 = new DateTime(31,12,2020,11,5,0);
        UtilsInTest.testLog(dateTime2.toString());
        UtilsInTest.testLog(Date.from(LocalDateTime.of(2020, Month.DECEMBER,31,11,5,0).toInstant(ZoneOffset.UTC)).toString());
        UtilsInTest.testLog(String.valueOf(dateTime2.getGiornoDelMese()));
        String s2 = new DateTime("31/12/2020 11:05:00 Z",
                                DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss X")).toString();
        UtilsInTest.testLog(s2);
        corretto = corretto && dateTime2.toString().equals(s2);

        UtilsInTest.testLog("---");

        DateTime dateTime3 = new DateTime(1,8,2020,11,5,0);
        UtilsInTest.testLog(dateTime3.toString());
        UtilsInTest.testLog(Date.from(LocalDateTime.of(2020, Month.AUGUST,1,11,5,0).toInstant(ZoneOffset.UTC)).toString());
        UtilsInTest.testLog(String.valueOf(dateTime3.getGiornoDelMese()));
        String s3 = new DateTime("01/08/2020 11:05:00 Z",
                                DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss X")).toString();
        UtilsInTest.testLog(s3);
        corretto = corretto && dateTime3.toString().equals(s3);

        UtilsInTest.testLog("---");
        UtilsInTest.testLog(DateTime.oggi().getData());
        UtilsInTest.testLog(DateTime.oggi().getDataOra());

        UtilsInTest.testLog("---");

        String primoIstanteDiOggi = DateTime.oggi().getOraPrecisaAlSecondo();
        UtilsInTest.testLog(primoIstanteDiOggi);
        corretto = corretto && DateTime.oggi().getOraPrecisaAlSecondo().equals("00:00:00 (UTC)");

        UtilsInTest.testLog("------");

        assertTrue(corretto);
    }

    /** Test per {@link it.units.progrweb.utils.datetime.DateTime#currentTimeInSecondi()} .*/
    @Test
    void currentTimeInSecondiTest(){
        assertEquals(System.currentTimeMillis()/1000, DateTime.currentTimeInSecondi());
    }

    /** Test per {@link it.units.progrweb.utils.datetime.DateTime#convertiDaDate(java.util.Date)}
     * e {@link DateTime#DateTime(Instant)}.
     * @param istanteDaCuiCreareLaData è l'{@link Instant istante} da cui creare le date.
     */
    @ParameterizedTest
    @MethodSource("generaParametri_Instant")
    void convertiDaDateTest(Instant istanteDaCuiCreareLaData){
        Date dataDaConvertire = Date.from(istanteDaCuiCreareLaData);
        DateTime dataConvertitaAttesa = new DateTime(istanteDaCuiCreareLaData);

        // Data convertita (è il metodo da testare)
        DateTime dataConvertita = DateTime.convertiDaDate(dataDaConvertire);

        assertEquals(dataConvertita, dataConvertitaAttesa);
    }

    /** Test per {@link it.units.progrweb.utils.datetime.DateTime#convertiInDate()}
     * e {@link DateTime#DateTime(Instant)}.
     * @param istanteDaCuiCreareLaData è l'{@link Instant istante} da cui creare le date.
     */
    @ParameterizedTest
    @MethodSource("generaParametri_Instant")
    void convertiInDateTest(Instant istanteDaCuiCreareLaData){
        Date dataConvertitaAttesa = Date.from(istanteDaCuiCreareLaData);
        DateTime dataDaConvertire = new DateTime(istanteDaCuiCreareLaData);

        // Data convertita (è il metodo da testare)
        Date dataConvertita = dataDaConvertire.convertiInDate();

        assertEquals(dataConvertita, dataConvertitaAttesa);
    }

    /** Test per {@link it.units.progrweb.utils.datetime.DateTime#convertiInLocalDate()}
     * e {@link DateTime#DateTime(Instant)}.
     * @param istanteDaCuiCreareLaData è l'{@link Instant istante} da cui creare le date.
     */
    @ParameterizedTest
    @MethodSource("generaParametri_Instant")
    void convertiInLocalDateTest(Instant istanteDaCuiCreareLaData){
        LocalDate dataConvertitaAttesa = istanteDaCuiCreareLaData.atZone(DateTime.UTC_ZONE_OFFSET).toLocalDate();
        DateTime dataDaConvertire = new DateTime(istanteDaCuiCreareLaData);

        // Data convertita (è il metodo da testare)
        LocalDate dataConvertita = dataDaConvertire.convertiInLocalDate();

        assertEquals(dataConvertita, dataConvertitaAttesa);
    }

    /** Test per {@link it.units.progrweb.utils.datetime.DateTime#convertiInLocalDateTime()}
     * e {@link DateTime#DateTime(Instant)}.
     * @param istanteDaCuiCreareLaData è l'{@link Instant istante} da cui creare le date.
     */
    @ParameterizedTest
    @MethodSource("generaParametri_Instant")
    void convertiInLocalDateTimeTest(Instant istanteDaCuiCreareLaData){
        LocalDateTime dataConvertitaAttesa = istanteDaCuiCreareLaData.atZone(DateTime.UTC_ZONE_OFFSET).toLocalDateTime();
        DateTime dataDaConvertire = new DateTime(istanteDaCuiCreareLaData);

        // Data convertita (metodo da testare)
        LocalDateTime dataConvertita = dataDaConvertire.convertiInLocalDateTime();

        assertEquals(dataConvertita, dataConvertitaAttesa);
    }

    /** Test per {@link it.units.progrweb.utils.datetime.DateTime#sottraiMesi(long)}
     * e {@link DateTime#DateTime(Instant)}.
     * @param istanteDaCuiCreareLaData è l'{@link Instant istante} da cui creare le date.
     * @param quantiMesi è il numero di mesi da sottrarre.
     */
    @ParameterizedTest
    @MethodSource("generaParametri_Instant_int")
    void sottraiMesiTest(Instant istanteDaCuiCreareLaData, int quantiMesi){

        LocalDateTime dataAttesa = istanteDaCuiCreareLaData.atZone(DateTime.UTC_ZONE_OFFSET)
                                                           .toLocalDateTime()
                                                           .minusMonths(quantiMesi);
        LocalDateTime dataCalcolata = new DateTime(istanteDaCuiCreareLaData).sottraiMesi(quantiMesi)
                                                                            .convertiInLocalDateTime();


        assertEquals(dataAttesa, dataCalcolata);
    }

    /** Test per {@link DateTime#primoGiornoDelMese()} e {@link DateTime#DateTime(Instant)}.
     * @param istanteDaCuiCreareLaData è l'{@link Instant istante} da cui creare le date.
     */
    @ParameterizedTest
    @MethodSource("generaParametri_Instant")
    void primoGiornoDelMeseTest(Instant istanteDaCuiCreareLaData){

        LocalDateTime dataAttesa = istanteDaCuiCreareLaData.atZone(DateTime.UTC_ZONE_OFFSET)
                                                       .toLocalDate()
                                                       .with(TemporalAdjusters.firstDayOfMonth())
                                                       .atStartOfDay();
        LocalDateTime dataCalcolata = new DateTime(istanteDaCuiCreareLaData).primoGiornoDelMese()
                                                                            .convertiInLocalDateTime();

        assertEquals(dataAttesa, dataCalcolata);
    }

    /** Test per {@link DateTime#ultimoGiornoDelMese()} e {@link DateTime#DateTime(Instant)}.
     * @param istanteDaCuiCreareLaData è l'{@link Instant istante} da cui creare le date.
     */
    @ParameterizedTest
    @MethodSource("generaParametri_Instant")
    void ultimoGiornoDelMeseTest(Instant istanteDaCuiCreareLaData){

        LocalDateTime dataAttesa = istanteDaCuiCreareLaData.atZone(DateTime.UTC_ZONE_OFFSET)
                .toLocalDate()
                .with(TemporalAdjusters.lastDayOfMonth())
                .atStartOfDay()
                .plusDays(1).minusNanos(1);
        LocalDateTime dataCalcolata = new DateTime(istanteDaCuiCreareLaData).ultimoGiornoDelMese()
                                                                            .convertiInLocalDateTime();

        assertEquals(dataAttesa, dataCalcolata);
    }

    /** Test per {@link DateTime#compareTo(DateTime)} e {@link DateTime#DateTime(Instant)}.
     * Si confrontano i due istanti forniti come parametri.
     */
    @ParameterizedTest
    @MethodSource("generaParametri_Instant_Instant")
    void compareToDateTimeTest(Instant istante1, Instant istante2){

        DateTime date1 = new DateTime(istante1);
        DateTime date2 = new DateTime(istante2);

        int atteso = istante1.compareTo(istante2);
        int calcolato = date1.compareTo(date2);

        assertEquals(atteso, calcolato);
    }

    /** Test per {@link DateTime#isBefore(DateTime)} e {@link DateTime#DateTime(Instant)}.
     * Si confrontano i due istanti forniti come parametri.
     */
    @ParameterizedTest
    @MethodSource("generaParametri_Instant_Instant")
    void isBeforeDateTimeTest(Instant istante1, Instant istante2){

        DateTime date1 = new DateTime(istante1);
        DateTime date2 = new DateTime(istante2);

        boolean atteso = istante1.isBefore(istante2);
        boolean calcolato = date1.isBefore(date2);

        assertEquals(atteso, calcolato);
    }

    /** Test per {@link DateTime#isBefore(DateTime)} e {@link DateTime#DateTime(Instant)}.
     * Si confrontano i due istanti forniti come parametri.
     */
    @ParameterizedTest
    @MethodSource("generaParametri_Instant_Instant")
    void isAfterDateTimeTest(Instant istante1, Instant istante2){

        DateTime date1 = new DateTime(istante1);
        DateTime date2 = new DateTime(istante2);

        boolean atteso = istante1.isAfter(istante2);
        boolean calcolato = date1.isAfter(date2);

        assertEquals(atteso, calcolato);
    }


    /** Genera delle istanze di {@link Instant} da usare come parametri per i test.*/
    static Stream<Arguments> generaParametri_Instant() {
        final int QUANTI_TEST = QUANTI_ISTANTI_GENERARE;    // specifica quanti arguments devono essere generati (un arguments per ogni test)
        // Parametri generati in modo casuale, quindi tenere alto il numero di test.

        return IntStream.range(0,QUANTI_TEST)
                        .mapToObj(val -> {
                            // Crea un istante compreso tra "epoch" ed il doppio della data odierna
                            Instant i = Instant.ofEpochMilli(new Double(Math.random()*2*System.currentTimeMillis()).longValue());
                            return arguments(i);
                        });
    }

    /** Genera delle coppie ( {@link Instant}, int ) da usare come parametri per i test.*/
    static Stream<Arguments> generaParametri_Instant_int() {

        final int MIN = -30;    // valore minimo per l'intero   // TODO : VARIABILE D'AMBIENTE
        final int MAX = +30;    // valore massimo per l'intero  // TODO : VARIABILE D'AMBIENTE

        int[] valoriInteri = IntStream.rangeClosed(MIN,MAX).toArray();
        Stream<Arguments> argumentsStreamInstant = generaParametri_Instant();

        return argumentsStreamInstant.flatMap(argument -> {
            Instant instant = (Instant) argument.get()[0];
            return Arrays.stream(valoriInteri).mapToObj(valoreIntero -> arguments(instant, valoreIntero));
        });
    }

    /** Genera delle coppie ( {@link Instant}, {@link Instant} ) da usare come parametri per i test.*/
    static Stream<Arguments> generaParametri_Instant_Instant() {

        final int LIMIT = QUANTI_ISTANTI_GENERARE;  // numero limite di parametri generati da questo metodo

        final int limiteNellaGenerazione = (int)Math.ceil(Math.sqrt(LIMIT));
        List<Instant> listaIstanti = generaParametri_Instant().limit(limiteNellaGenerazione)
                                                              .map(arg -> (Instant) arg.get()[0])
                                                              .collect(Collectors.toList());

        return generaParametri_Instant().limit(limiteNellaGenerazione)
                                        .flatMap(arguments -> {
            Instant primoIstante = (Instant) arguments.get()[0];
            return listaIstanti.stream().map(secondoIstante -> arguments(primoIstante, secondoIstante));
        }).limit(LIMIT);
    }

}