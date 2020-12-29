package it.units.progrweb.utils.datetime;

/**
 * Classe per rappresentare un periodo temporale.
 * @author Matteo Ferfoglia
 */
public class PeriodoTemporale {

    // TODO : testare questa classe

    /**
     * Data iniziale del periodo temporale.
     */
    private final DateTime dataIniziale;

    /**
     * Data finale del periodo temporale.
     */
    private final DateTime dataFinale;

    /**
     * Crea un periodo temporale, a partire dai parametri dati
     *
     * @throws IllegalArgumentException Se la data iniziale è posteriore
     *                                  a quella finale.
     */
    public PeriodoTemporale(DateTime dataIniziale, DateTime dataFinale) {

        // TODO : verificare correttezza

        if (dataIniziale.isAfter(dataFinale))
            throw new IllegalArgumentException("La data iniziale non può essere posteriore alla data finale");

        this.dataIniziale = dataIniziale;
        this.dataFinale = dataFinale;
    }

    /**
     * Restituisce il mese precedente rispetto alla data attuale.
     * Esempio, se oggi è il 29 dicembre 2020 (UTC), il mese precedente è
     * il periodo dal 1 novembre al 30 novembre 2020 (UTC).
     */
    public static PeriodoTemporale mesePrecedente() {
        // TODO : verifcare correttezza di questo metodo.

        final DateTime oggi = DateTime.oggi();
        final DateTime oggiMenoUnMese = oggi.sottraiMesi(1);

        final DateTime primoGiornoMesePrecedente = oggiMenoUnMese.primoGiornoDelMese();
        final DateTime ultimoGiornoMesePrecedente = oggiMenoUnMese.ultimoGiornoDelMese();

        return new PeriodoTemporale(primoGiornoMesePrecedente, ultimoGiornoMesePrecedente);

    }

    public DateTime getDataIniziale() {
        return dataIniziale;
    }

    public DateTime getDataFinale() {
        return dataFinale;
    }
}
