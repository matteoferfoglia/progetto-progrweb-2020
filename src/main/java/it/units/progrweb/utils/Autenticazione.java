package it.units.progrweb.utils;

import it.units.progrweb.entities.attori.Attore;

/**
 * Classe di utilità per gestire l'autenticazione degli utenti.
 *
 * @author Matteo Ferfoglia
 */
public class Autenticazione {

    // TODO implementare questa classe

    /** Rappresentazione di un attore non autenticato (es. credenziali non valide)*/
    private static final Attore ATTORE_NON_AUTENTICATO = null;


    /** Restituisce l'attore corrispondente alle credenziali date
     * come parametri se le credenziali sono valide, {@link #ATTORE_NON_AUTENTICATO} altrimenti.*/
    public static Attore getAttoreDaCredenziali(String username, String password) {
        // TODO : cercare le credenziali nel database di autenticazione e restituire l'attore corrispondente
        // TODO : restituire ATTORE_NON_AUTENTICATO se credenziali invalide
        return ATTORE_NON_AUTENTICATO;
    }

    /** Restituisce true se l'attore è autenticato, false altrimenti.*/
    public static boolean isAttoreAutenticato(Attore attore) {
        return attore != ATTORE_NON_AUTENTICATO;    // TODO : verificare corretto funzionamento di questo metodo
    }
}
