package it.units.progrweb.utils.datetime;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAdjusters;
import java.util.Date;
import java.util.Objects;

/**
 * Classe per rappresentare date e ore, utilizzabile
 * per evitare ambiguità tra formati diversi o, ad
 * esempio, diversi fuso orari.
 * Si utilizza lo standard UTC.
 *
 * @author Matteo Ferfoglia
 */
public class DateTime implements Comparable<DateTime> {

    /** Pattern da utilizzare per la conversione da un'istanza di
     * questa classe in una {@link String} e viceversa.*/
    private static final String PATTERN_CONVERSIONE_IN_STRING = "yyyy-MM-dd'T'HH:mm:ssZ"; // ISO 8601 (compatibile con Date() in JS)

    /** Pattern utilizzato per il formato "date" nei gli elementi
     * di input[type=date] in HTML.*/
    private static final String PATTERN_HTML_INPUT_DATA = "yyyy-MM-dd";

    /** Istante temporale rappresentato da quest'istanza. */
    private final Instant istanteTemporale;

    /** Differenza di fuso orario per UTC. Vedere {@link ZoneOffset#UTC}. */
    public static final ZoneOffset UTC_ZONE_OFFSET = ZoneOffset.UTC;


    /** Crea un'istanza di questa classe a partire dall'istante temporale dato.*/
    public DateTime(Instant istanteTemporale) {
        this.istanteTemporale = istanteTemporale;
    }

    /** Crea un'istanza di questa classe a partire da una stringa ed un
     * {@link DateTimeFormatter}, come ad esempio:
     * <ul>
     *     <li>Data: <code>"31/12/2020 13:52:01 Z"</code> ('Z' significa "zero offset" temporale,vedere {@link DateTimeFormatter})</li>
     *     <li>Formatter: <code>DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss X")</code></li>
     * </ul>
     */
    public DateTime(String stringaCorrispondenteAdUnDateTime, DateTimeFormatter formatter) {

        LocalDateTime localDateTime;
        try{
            localDateTime = LocalDateTime.parse(stringaCorrispondenteAdUnDateTime, formatter);
        } catch (DateTimeParseException e) {
            // Se manca l'ora nella stringa rappresentante il DateTime, si considera ad inizio giornata
            localDateTime = LocalDate.parse(stringaCorrispondenteAdUnDateTime, formatter).atStartOfDay();
        }

        this.istanteTemporale = localDateTime.toInstant(UTC_ZONE_OFFSET);

    }

    /** Crea un'istanza di questa classe in base alla data e l'ora (UTC) specificata.
     * @param giornoDelMese da 1 a 31
     * @param meseDellAnno da 1 a 12
     * @param anno Anno
     * @param oraUTC da 0 a 23
     * @param minutoDellOra da 0 a 59
     * @param secondoDelMinuto da 0 a 59
     * @throws DateTimeException se uno dei parametri è invalido.
     */
    public DateTime(int giornoDelMese, int meseDellAnno, int anno, int oraUTC, int minutoDellOra, int secondoDelMinuto)
            throws DateTimeException{
        this(LocalDateTime.of(
                                anno,
                                Month.of(meseDellAnno),
                                giornoDelMese,
                                oraUTC,
                                minutoDellOra,
                                secondoDelMinuto
                             ).toInstant(UTC_ZONE_OFFSET)
        );
    }

    /** Restituisce l'istante attuale, rappresentato come istanza di questa classe.*/
    public static DateTime adesso() {
        return new DateTime(Instant.now());
    }

    /** Restituisce il numero di secondi trascorsi dal 1970-01-01T00:00:00Z*/
    public static long currentTimeInSecondi() {
        return adesso().istanteTemporale.getEpochSecond();
    }

    /** Rappresentazione temporale della giornata odierna. */
    public static DateTime oggi() {
        return new DateTime(LocalDate.now().atStartOfDay(UTC_ZONE_OFFSET).toInstant());
        // restituisce il primo secondo valido della giornata odierna. Fonte:
        // https://docs.oracle.com/javase/tutorial/datetime/iso/date.html
        // https://docs.oracle.com/javase/8/docs/api/java/time/LocalDate.html#atStartOfDay--
        // https://docs.oracle.com/javase/8/docs/api/java/time/ZoneOffset.html#UTC
    }

    /** Restituisce un'istanza di questa classe a partire
     * da un oggetto di classe Date */
    public static DateTime convertiDaDate(Date date) {
        return new DateTime(Instant.ofEpochMilli(date.getTime()));
    }

    /** Metodo per convertire un'istanza di questa classe
     * al tipo {@link String}, che può risultare più semplice
     * da gestire. Restituisce null se il parametro è null.*/
    public static String convertiInString(DateTime dataEdOraDiCaricamento) {
        return dataEdOraDiCaricamento==null ? null :
                dataEdOraDiCaricamento.toString( DateTimeFormatter.ofPattern(PATTERN_CONVERSIONE_IN_STRING) );
    }

    /** Metodo per ottenere un'istanza di questa classe
     * a partire da un valore di tipo {@link String} ottenuto
     * col metodo {@link #convertiInString(DateTime)}.
     * Restituisce null se il parametro è null.*/
    public static DateTime convertiDaString(String dataEdOraDiCaricamento) {
        return dataEdOraDiCaricamento==null ? null :
                new DateTime( dataEdOraDiCaricamento,
                        DateTimeFormatter.ofPattern(PATTERN_CONVERSIONE_IN_STRING) );
    }

    /** Metodo per ottenere un'istanza di questa classe
     * a partire da un valore di tipo {@link String} ottenuto
     * da un campo di input html di tipo "date".*/
    public static DateTime convertiDaString_htmlInputDate(String dataEdOraDiCaricamento) {
        return dataEdOraDiCaricamento==null ? null :
                new DateTime( dataEdOraDiCaricamento,
                        DateTimeFormatter.ofPattern(PATTERN_HTML_INPUT_DATA) );
    }


    /** Restituisce un'istanza della classe Date a partire
     * da un oggetto di questa classe. */
    public Date convertiInDate() {
        return Date.from(istanteTemporale);
    }

    /** Converte quest'istanza in LocalDate. */
    public LocalDate convertiInLocalDate() {
        return istanteTemporale.atZone(UTC_ZONE_OFFSET)
                               .toLocalDate();    // https://stackoverflow.com/a/52264959
    }

    /** Converte quest'istanza in LocalDateTime. */
    public LocalDateTime convertiInLocalDateTime() {
        return LocalDateTime.ofInstant(istanteTemporale, UTC_ZONE_OFFSET);
    }

    /** Restituisce un nuovo DateTime, sottraendo il numero
     * di mesi specificati da questa istanza.*/
    public DateTime sottraiMesi(long numeroMesiDaSottrarre) {
        return new DateTime( convertiInLocalDateTime()
                              .minusMonths(numeroMesiDaSottrarre)
                              .toInstant(UTC_ZONE_OFFSET) );
    }


    /** Restituisce un nuovo DateTime, corrispondente al primo istante del primo giorno del mese.*/
    public DateTime primoGiornoDelMese() {
        return new DateTime( convertiInLocalDateTime()
                              .with(TemporalAdjusters.firstDayOfMonth())    // https://docs.oracle.com/javase/tutorial/datetime/iso/adjusters.html
                              .toInstant(UTC_ZONE_OFFSET) )
                .adInizioGiornata();
    }

    /** Restituisce un nuovo DateTime, corrispondente all'ultimo istante dell'ultimo giorno del mese.*/
    public DateTime ultimoGiornoDelMese() {
        return new DateTime( convertiInLocalDateTime()
                               .with(TemporalAdjusters.lastDayOfMonth()) // https://docs.oracle.com/javase/tutorial/datetime/iso/adjusters.html
                               .toInstant(UTC_ZONE_OFFSET) )
                .aFineGiornata();
    }

    public boolean isAfter(DateTime dateTime) {
        return this.istanteTemporale.isAfter(dateTime.istanteTemporale);
    }

    public boolean isBefore(DateTime dateTime) {
        return this.istanteTemporale.isBefore(dateTime.istanteTemporale);
    }

    /** Converte quest'istanza in una di classe {@link Instant}.*/
    public Instant toInstant() {
        return istanteTemporale;
    }

    /** Restituisce una nuova istanza di questa classe, corrispondente all'inizio giornata.*/
    public DateTime adInizioGiornata() {
        return new DateTime(convertiInLocalDate().atStartOfDay(UTC_ZONE_OFFSET).toInstant());
    }

    /** Restituisce una nuova istanza di questa classe, corrispondente alla fine giornata.*/
    public DateTime aFineGiornata() {
        return new DateTime(adInizioGiornata().convertiInLocalDateTime()    // retrocediamo all'inizio della giornata
                                                 .plusDays(1)               // inizio della giornata successiva
                                                 .minusNanos(1)             // istante precedente (è l'ultimo della giornata precedente)
                                                 .toInstant(ZoneOffset.UTC));
    }

    /** Restituisce il giorno del mese rappresentato da questa istanza.
     * @return numero da 1 a 31 corrispondente al giorno del mese*/
    public int getGiornoDelMese(){
        return Integer.parseInt( convertiInLocalDate().format(DateTimeFormatter.ofPattern("d")) );
    }

    /** Restituisce il numero di giorni di differenza tra le due date.
     * Le approssimazioni (nel caso in cui le due date facciano riferimento
     * a diversi istanti temporali nella giornata) sono fatte sempre per eccesso.
     * Se la data finale è minore di quella iniziale, viene restituito un valore negativo.*/
    public static long differenzaInGiorni(DateTime dataIniziale, DateTime dataFinale) {
        final int secondiInUnMinuto=60, minutiInUnOra=60, oreInUnGiorno=24;
        final double numeroSecondiInUnaGiornata = secondiInUnMinuto*minutiInUnOra*oreInUnGiorno;
        final long differenzaInSecondiTraLeDate = dataFinale.toInstant().getEpochSecond()-dataIniziale.toInstant().getEpochSecond();
        return (long)Math.ceil( differenzaInSecondiTraLeDate / numeroSecondiInUnaGiornata) ;
    }


    @Override
    public int compareTo(DateTime dateTime) {
        return this.istanteTemporale.compareTo(dateTime.istanteTemporale);
    }

    /** Restituisce la data e l'ora attuale in formato UTC
     * <a href="https://en.wikipedia.org/wiki/ISO_8601">ISO 8601</a>.
     */
    @Override
    public String toString() {
        return istanteTemporale.toString();
    }

    /** Crea una rappresentazione come stringa nel {@link DateTimeFormatter} fornito.*/
    private String toString(DateTimeFormatter dateTimeFormatter) {
        return dateTimeFormatter.withZone(UTC_ZONE_OFFSET).format( istanteTemporale );
    }

    /** Restituisce la data rappresentata da quest'istanza,
     * in formato dd/MM/yyyy.*/
    public String getData(){
        return convertiInLocalDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")); // https://docs.oracle.com/javase/8/docs/api/java/time/format/DateTimeFormatter.html
    }

    /** Restituisce l'ora (UTC) rappresentata da quest'istanza,
     * con precisione del secondo, in formato HH:mm:ss. */
    public String getOraPrecisaAlSecondo(){
        return convertiInLocalDateTime().atZone(UTC_ZONE_OFFSET)
                .format(DateTimeFormatter.ofPattern("HH:mm:ss")) + " (UTC)"; // https://docs.oracle.com/javase/8/docs/api/java/time/format/DateTimeFormatter.html
    }

    /** Restituisce data e ora UTC nel seguente formato (ad esempio):
     * 'Tue, 3 Jun 2008 11:05:30 (UTC)'. */
    public String getDataOra() {
        return getDataOraConFusoOrario(UTC_ZONE_OFFSET);
    }

    /** Restituisce data e ora, col fuso orario specificato.*/
    private String getDataOraConFusoOrario(@SuppressWarnings("SameParameterValue") ZoneOffset zoneOffset) {
        String stringa =  convertiInLocalDateTime().atZone(zoneOffset)
                                                   .format(DateTimeFormatter.ofPattern("cccc, d LLLL yyyy,  HH:mm:ss '(" + (zoneOffset.equals(UTC_ZONE_OFFSET) ? "UTC" : "'O'") +")'"));
        return stringa.substring(0,1).toUpperCase() + stringa.substring(1);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DateTime dateTime = (DateTime) o;

        return Objects.equals(istanteTemporale, dateTime.istanteTemporale);
    }

}
