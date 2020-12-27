import axios from "axios";

/**
 * Classe per la gestione del CSRF token.
 */

/**
 * Richiede il CSRF token al server e restituisce una promise.
 * Il seguente esempio mostra come ottenere il token CSRF utilizzando
 * questa funzione:
 * <code>let CSRF_token = await richiediCSRFTokenAlServer().then(valoreToken => valoreToken)</code>
 * @return {Promise<unknown>}
 */
export const richiediCSRFTokenAlServer = async () => {
    return await axios.get(process.env.VUE_APP_GET_CSRF_TOKEN_URL)
        .then(risposta => risposta.data)
        .catch(err => {
            console.error("Errore durante il recupero del token CSRF: " + err);
            // TODO : gestire l'errore (invio mail ai gestori?)
        });
}