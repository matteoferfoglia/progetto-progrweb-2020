package it.units.progrweb.utils.datetime;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.Instant;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Classe di test per {@link PeriodoTemporale}.
 * Presuppone il corretto funzionamento di {@link DateTime}.
 * @author Matteo Ferfoglia
 */
public class PeriodoTemporaleTest {

    /** Test per {@link it.units.progrweb.utils.datetime.PeriodoTemporale#PeriodoTemporale(it.units.progrweb.utils.datetime.DateTime, it.units.progrweb.utils.datetime.DateTime)}.
     * Crea un periodo a partire da istanti temporali.*/
    @ParameterizedTest
    @MethodSource("it.units.progrweb.utils.datetime.DateTimeTest#generaParametri_Instant_Instant")
    void costruttoreTest(Instant istanteInizio, Instant istanteFine) {
        DateTime dataInizio = new DateTime(istanteInizio);
        DateTime dataFine = new DateTime(istanteFine);

        boolean atteso = istanteInizio.isBefore(istanteFine);
        boolean ottenuto;
        try {
            //noinspection unused   // anche se non usato, istruzione usata per verificare corretto funzionamento del costruttore
            PeriodoTemporale periodoTemporale = new PeriodoTemporale(dataInizio, dataFine);
            ottenuto = true;
        } catch (IllegalArgumentException e) {
            ottenuto = false;
        }

        assertEquals(atteso, ottenuto);
    }

    /** Test per {@link PeriodoTemporale#mesePrecedente()}.*/
    @Test
    void mesePrecedenteTest() {
        PeriodoTemporale periodoTemporale = PeriodoTemporale.mesePrecedente();

        DateTime dataInizio = periodoTemporale.getDataIniziale();
        DateTime dataFine = periodoTemporale.getDataFinale();

        boolean esito = dataInizio.isBefore(dataFine)
                            && dataInizio.getGiornoDelMese()==1
                            && ( dataFine.getGiornoDelMese()==28 || dataFine.getGiornoDelMese()==29
                                 || dataFine.getGiornoDelMese()==30 || dataFine.getGiornoDelMese()==31 )   ;

        assertTrue(esito);
    }

    /** Test per il metodo {@link it.units.progrweb.utils.datetime.PeriodoTemporale#mesePrecedente(it.units.progrweb.utils.datetime.DateTime)}.
     * Se si inseriscono parametri formattati in modo non corretto, viene generata un'eccezione
     * (ma non è compito di questo metodo creare una data, che qui è stata fornita in modo
     * testuale solo per avere maggiore controllo sui parametri, ma in realtà il metodo
     * sottoposto a test lavora già su date corrette).*/
    @ParameterizedTest
    @CsvSource({
            "01/08/2020 11:05:00 Z, dd/MM/yyyy HH:mm:ss X, 01/07/2020 00:00:00 Z, 31/07/2020 23:59:59 Z",
            "05/08/2020 11:05:00 Z, dd/MM/yyyy HH:mm:ss X, 01/07/2020 00:00:00 Z, 31/07/2020 23:59:59 Z",
            "31/08/2020 11:05:00 Z, dd/MM/yyyy HH:mm:ss X, 01/07/2020 00:00:00 Z, 31/07/2020 23:59:59 Z",
            "31/12/2020 11:05:00 Z, dd/MM/yyyy HH:mm:ss X, 01/11/2020 00:00:00 Z, 30/11/2020 23:59:59 Z",
            "31/01/2020 11:05:00 Z, dd/MM/yyyy HH:mm:ss X, 01/12/2019 00:00:00 Z, 31/12/2019 23:59:59 Z",
            "29/02/2020 11:05:00 Z, dd/MM/yyyy HH:mm:ss X, 01/01/2020 00:00:00 Z, 31/01/2020 23:59:59 Z",
            "29/03/2020 11:05:00 Z, dd/MM/yyyy HH:mm:ss X, 01/02/2020 00:00:00 Z, 29/02/2020 23:59:59 Z",
            "01/08/1996 11:05:00 Z, dd/MM/yyyy HH:mm:ss X, 01/07/1996 00:00:00 Z, 31/07/1996 23:59:59 Z",
            "05/08/1996 11:05:00 Z, dd/MM/yyyy HH:mm:ss X, 01/07/1996 00:00:00 Z, 31/07/1996 23:59:59 Z",
            "31/08/1996 11:05:00 Z, dd/MM/yyyy HH:mm:ss X, 01/07/1996 00:00:00 Z, 31/07/1996 23:59:59 Z",
            "31/12/1996 11:05:00 Z, dd/MM/yyyy HH:mm:ss X, 01/11/1996 00:00:00 Z, 30/11/1996 23:59:59 Z",
            "31/01/1996 11:05:00 Z, dd/MM/yyyy HH:mm:ss X, 01/12/1995 00:00:00 Z, 31/12/1995 23:59:59 Z",
            "29/02/1996 11:05:00 Z, dd/MM/yyyy HH:mm:ss X, 01/01/1996 00:00:00 Z, 31/01/1996 23:59:59 Z",
            "29/03/1996 11:05:00 Z, dd/MM/yyyy HH:mm:ss X, 01/02/1996 00:00:00 Z, 29/02/1996 23:59:59 Z"
    })
    void mesePrecedenteTest(String dataDiRiferimentoComeStringa, String patternDateTime,
                            String primoGiornoDelPeriodo_atteso, String ultimoGiornoDelPeriodo_atteso) {

        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(patternDateTime);

        DateTime dataRiferimento                   = new DateTime(dataDiRiferimentoComeStringa, dateTimeFormatter);
        DateTime dataPrimoGiornoDelPeriodo_atteso  = new DateTime(primoGiornoDelPeriodo_atteso, dateTimeFormatter);
        DateTime dataUltimoGiornoDelPeriodo_atteso = new DateTime(ultimoGiornoDelPeriodo_atteso, dateTimeFormatter);

        PeriodoTemporale periodoTemporaleCalcolato = PeriodoTemporale.mesePrecedente(dataRiferimento);
        DateTime dataInizio = periodoTemporaleCalcolato.getDataIniziale();
        DateTime dataFine = periodoTemporaleCalcolato.getDataFinale();

        boolean esito = dataInizio.isBefore(dataFine)
                && dataInizio.getGiornoDelMese()==1
                && ( dataFine.getGiornoDelMese()==28 || dataFine.getGiornoDelMese()==29
                        || dataFine.getGiornoDelMese()==30 || dataFine.getGiornoDelMese()==31 )
                && dataPrimoGiornoDelPeriodo_atteso.getDataOra().equals(dataInizio.getDataOra())
                && dataUltimoGiornoDelPeriodo_atteso.getDataOra().equals(dataFine.getDataOra());

        assertTrue(esito);
    }

}
