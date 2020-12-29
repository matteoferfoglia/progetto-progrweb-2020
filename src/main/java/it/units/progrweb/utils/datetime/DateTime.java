package it.units.progrweb.utils.datetime;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.TemporalAdjusters;
import java.util.Date;

/**
 * Classe per rappresentare date e ore, utilizzabile
 * per evitare ambiguità tra formati diversi o, ad
 * esempio, diversi fuso orari.
 * Si utilizza lo standard UTC.
 *
 * @author Matteo Ferfoglia
 */
public class DateTime implements Comparable<DateTime> {

    /** Istante temporale rappresentato da quest'istanza. */
    private final Instant istanteTemporale;

    /** Differenza di fuso orario per UTC. Vedere {@link ZoneOffset#UTC}. */
    public static final ZoneOffset UTC_ZONE_OFFSET = ZoneOffset.UTC;

    /** Restituisce l'istante attuale.*/
    public static DateTime adesso() {
        // TODO : testare questo metodo
        return new DateTime(Instant.now());
    }

    public static long currentTimeInSecondi() {
        return adesso().istanteTemporale.getEpochSecond();
    }

    /** Rappresentazione temporale della giornata odierna. */
    public static DateTime oggi() {
        // TODO : testare questo metodo
        return new DateTime(LocalDate.now().atStartOfDay(UTC_ZONE_OFFSET).toInstant()); // restituisce il primo secondo valido della giornata odierna
        // https://docs.oracle.com/javase/tutorial/datetime/iso/date.html
        // https://docs.oracle.com/javase/8/docs/api/java/time/LocalDate.html#atStartOfDay--
        // https://docs.oracle.com/javase/8/docs/api/java/time/ZoneOffset.html#UTC
    }

    /** Restituisce un'istanza di questa classe a partire
     * da un oggetto di classe Date */
    public static DateTime convertiDaDate(Date dateTime) {
        // TODO : testare questo metodo
        return new DateTime(Instant.ofEpochMilli(dateTime.getTime()));
    }

    /** Restituisce un'istanza della classe Date a partire
     * da un oggetto di questa classe. */
    public Date convertiInDate() {
        // TODO : testare questo metodo
        return Date.from(istanteTemporale);
    }

    private DateTime(Instant istanteTemporale) {
        // TODO : testare
        this.istanteTemporale = istanteTemporale;
    }

    /** Converte quest'istanza in LocalDate. */
    public LocalDate convertiInLocalDate() {
        // TODO : testare
        return istanteTemporale.atZone(UTC_ZONE_OFFSET)
                               .toLocalDate();    // https://stackoverflow.com/a/52264959
    }

    /** Converte quest'istanza in LocalDateTime. */
    public LocalDateTime convertiInLocalDateTime() {
        // TODO : testare
        return LocalDateTime.ofInstant(istanteTemporale, UTC_ZONE_OFFSET);
    }

    /** Restituisce un nuovo DateTime, sottraendo il numero
     * di mesi specificati da questa istanza.*/
    public DateTime sottraiMesi(long numeroMesiDaSottrarre) {
        // TODO : testare
        return new DateTime( convertiInLocalDateTime()
                              .minusMonths(numeroMesiDaSottrarre)
                              .toInstant(UTC_ZONE_OFFSET) );
    }


    /** Restituisce un nuovo DateTime, corrispondente al primo istante del primo giorno del mese.*/
    public DateTime primoGiornoDelMese() {
        // TODO : testare
        return new DateTime( convertiInLocalDate()
                              .with(TemporalAdjusters.firstDayOfMonth()) // https://docs.oracle.com/javase/tutorial/datetime/iso/adjusters.html
                              .atStartOfDay(UTC_ZONE_OFFSET)
                              .toInstant() );
    }

    /** Restituisce un nuovo DateTime, corrispondente all'ultimo istante dell'ultimo giorno del mese.*/
    public DateTime ultimoGiornoDelMese() {
        // TODO : testare
        return new DateTime( convertiInLocalDate()
                .with(TemporalAdjusters.firstDayOfNextMonth()) // https://docs.oracle.com/javase/tutorial/datetime/iso/adjusters.html
                .atStartOfDay(UTC_ZONE_OFFSET)
                .minusNanos(1)  // sottrae un nanosecondo al primo istante del mese successivo, così da ottenere l'ultimo istante del mese precedente
                .toInstant() );
    }

    public boolean isAfter(DateTime dateTime) {
        return this.istanteTemporale.isAfter(dateTime.istanteTemporale);
    }

    public boolean isBefore(DateTime dateTime) {
        return this.istanteTemporale.isBefore(dateTime.istanteTemporale);
    }


    @Override
    public int compareTo(DateTime dateTime) {
        // TODO : test
        return this.istanteTemporale.compareTo(dateTime.istanteTemporale);
    }
}
