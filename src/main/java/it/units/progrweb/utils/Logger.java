package it.units.progrweb.utils;

import java.util.logging.Level;

/**
 * Classe che fornisce il log.
 * @author Matteo Ferfoglia
 */
public abstract class Logger {

    /** Recupera o crea un logger per la classe passata come parametro.
     * Esempio invocazione di questo metodo:
     * <code>Logger.getLogger(NomeClasse.class)</code>.*/
    private static java.util.logging.Logger getLogger(Class<?> classe) {
        return java.util.logging.Logger.getLogger(classe.toString());
    }

    /** Scrive nel log.
     * @param classe è la classe per cui si vuole scrivere
     *                   nel log (ad es. nomeClasse.class)
     * @param livelloLogging Vedere {@link Level}.
     * @param messaggio Messaggio da scrivere nel log.
     * @param throwable Oggetto Throwable da riportare nel log.
     */
    private static void scriviNelLog(Class<?> classe, Level livelloLogging, String messaggio, Throwable throwable) {
        getLogger(classe).log(livelloLogging, messaggio, throwable);
    }

    /** Come {@link #scriviNelLog(Class, Level, String, Throwable)},
     * ma non riporta l'oggetto throwable.*/
    private static void scriviNelLog(Class<?> classe, Level livelloLogging, String messaggio) {
        getLogger(classe).log(livelloLogging, messaggio);
    }

    /** Scrive un'informazione nel log associato alla classe indicata nel primo parametro.*/
    @SuppressWarnings("unused") // anche se non usato, metodo mantenuto per completezza
    public static void scriviInfoNelLog(Class<?> classeCuiInfoSiRiferisce, String messaggio) {
        scriviNelLog(classeCuiInfoSiRiferisce, Level.INFO, messaggio);
    }

    /** Scrive un errore nel log associato alla classe indicata nel primo parametro.*/
    @SuppressWarnings("unused") // anche se non usato, metodo mantenuto per completezza
    public static void scriviErroreNelLog(Class<?> classeCuiErroreSiRiferisce, String messaggioErrore) {
        scriviNelLog(classeCuiErroreSiRiferisce, Level.SEVERE, messaggioErrore);
    }

    /** Riporta un'eccezione nel log associato alla classe indicata nel primo parametro.*/
    public static void scriviEccezioneNelLog(Class<?> classeCheHaGeneratoEccezione, String messaggioEccezione, Exception eccezione) {
        scriviNelLog(classeCheHaGeneratoEccezione, Level.SEVERE, messaggioEccezione, eccezione);
    }

    /** Riporta un'eccezione nel log associato alla classe indicata nel primo parametro.*/
    public static void scriviEccezioneNelLog(Class<?> classeCheHaGeneratoEccezione, Exception eccezione) {
        scriviNelLog(classeCheHaGeneratoEccezione, Level.SEVERE, "ECCEZIONE", eccezione);
    }

}
