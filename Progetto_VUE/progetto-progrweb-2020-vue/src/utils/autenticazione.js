/**
 * Codice JavaScript con funzioni di utilità, richiamate da
 * componenti diversi e definite qua per evitare duplicazione
 * di codice.
 */

// TODO : rivedere ed eventualmente ristrutturare questo script - serve??

import {
    impostaAuthorizationHeaderInRichiesteHttp,
    richiestaGet,
    rimuoviAuthorizationHeader,
} from "./http";

/**
 * Funzione per verificare se un utente è già autenticato.
 * Richiede Vue-Router per funzionare (vedere il parametro).
 * @param $route La property $route (this.$route) del componente che invoca questo metodo.
 * @return Promise il cui valore (se la Promise è risolta) è
 *          true se l'utente è autenticato, false altrimenti.
 */
export const verificaAutenticazione = async $route => {

    // Prima di chiedere al server se autenticato
    // imposta il token che potrebbe essere arrivato dal parametro
    impostaTokenDiAutenticazioneSeEsiste($route);

    // Richiesta al server se l'utente è attualmente autenticato
    let promiseConEsitoAutenticazione;   // inizializzazione
    await richiestaGet(process.env.VUE_APP_VERIFICA_TOKEN_AUTENTICAZIONE)
            .then(  esito  => promiseConEsitoAutenticazione = Promise.resolve(esito.data) )
            .catch( errore => promiseConEsitoAutenticazione = Promise.reject(errore) );


    return promiseConEsitoAutenticazione;
}

/**
 * Cerca nei parametri della route corrente il token di autenticazione
 * e, se presente, lo salva per le successive richieste HTTP.
 * @param $route la property $route (this.$route) del componente che invoca questo metodo.
 */
const impostaTokenDiAutenticazioneSeEsiste = $route => {

    // TODO : rivedere questo metodo

    // Cerco token autenticazione nei parametri di Vue Router
    if($route) {    // controllo che $route (parametro di questo metodo) sia definita
        const tokenDaRoute = $route.params[process.env.VUE_APP_ROUTER_PARAMETRO_TOKEN_AUTENTICAZIONE];
        if(tokenDaRoute)
            salvaTokenAutenticazione(tokenDaRoute);
    }

    // Cerco token in local storage
    const tokenAutenticazione = getTokenAutenticazione();

    // Imposto header di autenticazione per richieste http
    if(tokenAutenticazione) // truthy se token presente non nullo né stringa vuota né undefined
        impostaAuthorizationHeaderInRichiesteHttp(tokenAutenticazione);
    else                    // altrimenti rimuovo (se presente) header di autenticazione (se non presente, non succede niente)
        rimuoviAuthorizationHeader();
}

/** Nome della variabile nello storageLocale in cui è memorizzato il token di autenticazione.*/
const nomeVariabileInCuiSalvareIlTokenAutenticazione = process.env.VUE_APP_NOME_VARIABILE_IN_LOCAL_STORAGE_CON_TOKEN_AUTENTICAZIONE;

/** Salva il token di autenticazione nello storage locale.
 * @param valoreTokenAutenticazione Il valore del token di autenticazione.*/
export const salvaTokenAutenticazione = valoreTokenAutenticazione =>
    localStorage.setItem(nomeVariabileInCuiSalvareIlTokenAutenticazione, valoreTokenAutenticazione);

/** Legge il valore del token di autenticazione dallo storage
 * locale e lo restituisce.*/
export const getTokenAutenticazione = () =>
    localStorage.getItem(nomeVariabileInCuiSalvareIlTokenAutenticazione);

/** Elimina il token di autenticazione dallo storage locale.*/
export const eliminaTokenAutenticazione = () =>
    localStorage.removeItem(nomeVariabileInCuiSalvareIlTokenAutenticazione);

