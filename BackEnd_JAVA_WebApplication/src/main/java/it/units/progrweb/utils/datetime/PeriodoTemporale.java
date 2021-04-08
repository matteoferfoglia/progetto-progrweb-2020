package it.units.progrweb.utils.datetime;

/**
 * Classe per rappresentare un periodo temporale.
 * @author Matteo Ferfoglia
 */
public class PeriodoTemporale {

    /**
     * Data iniziale del periodo temporale.
     */
    private final DateTime dataIniziale;

    /**
     * Data finale del periodo temporale.
     */
    private final DateTime dataFinale;

    /**
     * Numero di giorni di questo periodo temporale.
     */
    private final long numeroGiorni;

    /**
     * Crea un periodo temporale, a partire dai parametri dati
     *
     * @throws IllegalArgumentException Se la data iniziale è posteriore
     *                                  a quella finale.
     */
    public PeriodoTemporale(DateTime dataIniziale, DateTime dataFinale) {

        if (dataIniziale.isAfter(dataFinale))
            throw new IllegalArgumentException("La data iniziale non può essere posteriore alla data finale");

        this.dataIniziale = dataIniziale;
        this.dataFinale = dataFinale;
        this.numeroGiorni = DateTime.differenzaInGiorni(dataIniziale, dataFinale);
    }

    /**
     * Restituisce il mese precedente rispetto alla data attuale.
     * Esempio, se oggi è il 29 dicembre 2020 (UTC), il mese precedente è
     * il periodo dal 1 novembre al 30 novembre 2020 (UTC).
     */
    public static PeriodoTemporale mesePrecedente() {

        final DateTime oggi = DateTime.oggi();
        return mesePrecedente(oggi);

    }

    /** Restituisce il mese precedente (inteso come {@link it.units.progrweb.utils.datetime.PeriodoTemporale})
     * rispetto alla data indicata come parametro.*/
    public static PeriodoTemporale mesePrecedente(DateTime dataRiferimento) {
        final DateTime oggiMenoUnMese = dataRiferimento.sottraiMesi(1);

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

    @Override
    public String toString() {
        return "PeriodoTemporale{" +
                "dataIniziale="   + dataIniziale.toString() +
                ", dataFinale="   + dataFinale.toString() +
                ", numeroGiorni=" + numeroGiorni +
                '}';
    }
}
