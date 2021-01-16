package it.units.progrweb.utils;

/**
 * Classe di utilità per la gestione delle RegEx.
 * @author Matteo Ferfoglia
 */
public class RegexHelper {

    // TODO : verifica che funzioni, implementare questa classe ed aggiungerla a git


    /** RegEx per email (<a href="https://html.spec.whatwg.org/multipage/input.html">Fonte</a>). */
    // TODO : verificare regex
    public final static String REGEX_EMAIL = "^[a-zA-Z0-9.!#$%&'*+\\/=?^_`{|}~-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*$";

    /** Regex per il riconoscimento di un numero, con numeri
     * decimali separati dal punto e senza separatore delle migliaia. */
    public static final String REGEX_NUMERO = "^"                           // inizio riconoscimento "literal"
            + "([1-9][0-9]*|[0-9])"     // prima della virgola: o un numero che ha per prima cifra [1-9] seguito da [0-9] ripetute arbitrariamente, oppure solo una cifra [0-9]
            + "(\\.([0-9]*))?"            // eventualmente, un punto seguito da cifre [0-9] ripetute arbitrariamente
            + "$";                      // fine riconoscimento "literal"

    /** Regex per il riconoscimento di un booleano (true o false)*/
    public static final String REGEX_BOOLEAN = "^true$|^false$";

    /** Regex per il riconoscimento di testo, case insensitive.*/
    public static final String REGEX_TESTO = "^(?i:.*)$";         // fine "literal"


    /** RegEx per identificare una proprietà nella forma
     * <pre>
     *     "nome": valore</pre>
     * in cui:
     * <ul>
     *      <li>il nome deve essere costituito da almeno un
     *          carattere in [a-z0-9\-] (case insensitive)
     *      </li>
     *      <li>il valore deve essere
     *          <ul>
     *            <li>una stringa avvolta da virgolette (") con zero o più caratteri</li>
     *            <li>un numero</li>
     *            <li>un valore booleano (<code>true</code> o <code>false</code>)</li>
     *            <li><code>null</code></li>
     *         </ul>
     *      </li>
     *      <li>il nome è separato dal valore grazie al
     *          carattere due punti (:) seguito da uno o più
     *          spazi.
     *      </li>
     * </ul>
     */
    public static final String REGEX_COPPIA_NOME_VALORE = "\"(?i:([a-z0-9 \\-]+))\""   // primo gruppo: nome proprietà (case insensitive, anche con spazi)

                                                            + ":"// separatore nome-valore proprietà
                                                            + "(\\s*)"                  // secondo gruppo: zero o più spazi

                                                            + "(?i:("                    // terzo gruppo: valore property, una delle seguenti regex
                                                            +   "\"[^\"]*\""    // qualsiasi carattere, eccetto le virgolette ("), (anche ripetuto) circondato da virgolette
                                                            +   "|" + REGEX_NUMERO .replaceAll("^\\^|\\$$","")
                                                            +   "|" + REGEX_BOOLEAN.replaceAll("\\^|\\$","")
                                                            +   "|" + "null"                  // valolore null
                                                            + "))";  // Oss.: replaceAll("^\\^|\\$$","") serve ad eliminare le asserzioni di posizioni iniziale e finale (^ e $) dalle regex già pronte
        // se si modifica la regex, aggiornare  // todo: verificare regex


    /** Con riferimento a questa {@link #REGEX_COPPIA_NOME_VALORE RegEx},
     * questa constante è il numero del gruppo nella RegEx corrispondente
     * al nome della proprietà.*/
    public static final short NUMERO_GRUPPO_CORRISPONDENTE_A_NOME_PROPRIETA_IN_REGEX_COPPIA_NOME_VALORE = 1;

    /** Con riferimento a questa {@link #REGEX_COPPIA_NOME_VALORE RegEx},
     * questa constante è il numero del gruppo nella RegEx corrispondente
     * al valore della proprietà.*/
    public static final short NUMERO_GRUPPO_CORRISPONDENTE_A_VALORE_PROPRIETA_IN_REGEX_COPPIA_NOME_VALORE = 3;

    /** Valida un email sulla base di una regex. */
    public static boolean isEmailValida(String emailDaValidare) {
        return emailDaValidare.matches(REGEX_EMAIL);    // TODO: testare che funzioni
    }
}
