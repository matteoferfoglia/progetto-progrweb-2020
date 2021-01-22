import {richiestaGet} from "./http";

// TODO : rivedere ed eventualmente ristrutturare questo script - serve??


/**
 * Script JavaScript per la gestione del CSRF token.
 */

/**
 * Richiede il CSRF token al server e restituisce una promise.
 * Il seguente esempio mostra come ottenere il token CSRF utilizzando
 * questa funzione:
 * <code>let CSRF_token = await richiediCSRFTokenAlServer().then(valoreToken => valoreToken)</code>
 * @return il token csrf oppure Promise.reject(risposta) se ci sono errori
 * nel recupero del token.
 */
export const richiediCSRFTokenAlServer = async () => {
    return await richiestaGet(process.env.VUE_APP_URL_GET_CSRF_TOKEN)
        .then(risposta => risposta)
        .catch(risposta => {
            console.error("Errore durante il recupero del token CSRF: " + risposta );
            return Promise.reject(risposta);
            // TODO : gestire l'errore (invio mail ai gestori?)
        });
}