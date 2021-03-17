package it.units.progrweb.utils;

/**
 * Classe di utilità per la gestione delle RegEx.
 * @author Matteo Ferfoglia
 */
public class RegexHelper {

    /** RegEx per email (<a href="https://html.spec.whatwg.org/multipage/input.html">Fonte</a>). */
    // TODO : verificare regex
    public final static String REGEX_EMAIL = "^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*$";

    /** Regex per il codice fiscale
     * (<a href="https://regexlib.com/(A(CAg_Bth78XI7Ry2C7vo_2HR3yuG9GuP1YeCHLd1AH53pIpI-z7JHENTvKnhDjJJhOkjyka4kah-CTZaupEY3MAVWa6qYC256houEBaNXoG01))/UserPatterns.aspx?authorId=10d43491-1297-481f-ae66-db9f2263575c&AspxAutoDetectCookieSupport=1">Fonte</a>).*/
    public static final String REGEX_CODICE_FISCALE = "^([A-Za-z]{6}[0-9lmnpqrstuvLMNPQRSTUV]{2}[abcdehlmprstABCDEHLMPRST]{1}[0-9lmnpqrstuvLMNPQRSTUV]{2}[A-Za-z]{1}[0-9lmnpqrstuvLMNPQRSTUV]{3}[A-Za-z]{1})|([0-9]{11})$";

    /** Valida un email sulla base di una regex. */
    public static boolean isEmailValida(String emailDaValidare) {
        return emailDaValidare.matches(REGEX_EMAIL);    // TODO: testare che funzioni
    }

    /** Valida un codice fiscale sulla base di una regex. */
    public static boolean isCodiceFiscaleValido(String codiceFiscale) {
        return codiceFiscale.matches(REGEX_CODICE_FISCALE);    // TODO: testare che funzioni
    }
}
