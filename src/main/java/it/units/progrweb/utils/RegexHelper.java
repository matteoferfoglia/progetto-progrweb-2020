package it.units.progrweb.utils;

/**
 * Classe di utilità per la gestione delle RegEx.
 * @author Matteo Ferfoglia
 */
public class RegexHelper {

    // TODO : verifica che funzioni, implementare questa classe ed aggiungerla a git


    /** RegEx per email (<a href="https://html.spec.whatwg.org/multipage/input.html">Fonte</a>). */
    // TODO : verificare regex
    private final static String REGEX_EMAIL = "^[a-zA-Z0-9.!#$%&'*+\\/=?^_`{|}~-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*$";


    /** Valida un email sulla base di una regex. */
    public static boolean isEmailValida(String emailDaValidare) {
        return emailDaValidare.matches(REGEX_EMAIL);    // TODO: testare che funzioni
    }
}
