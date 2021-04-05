/**
 * Codice JavaScript con funzioni di autenticazione, richiamate da
 * componenti diversi e definite qua per evitare duplicazione
 * di codice.
 */

import {
    impostaAuthorizationHeaderInRichiesteHttp,
    richiestaGet,
    rimuoviAuthorizationHeader,
} from "./http";
import router from "../router";

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
    return await richiestaGet(process.env.VUE_APP_URL_VERIFICA_TOKEN_AUTENTICAZIONE)
            .then(  esito  => Promise.resolve(esito) )
            .catch( errore => Promise.reject(errore) );
}

/** Elimina le informazioni di autenticazione dal client
 * (in pratica esegue il logout).
 * @param csrfToken CSRF Token per la richiesta di logout.
 *          Se non specificato, comunque vengono eliminate
 *          le informazioni di autenticazione dal client.*/
export const logout = csrfToken => {

    const parametriRichiestaGet = csrfToken ? {[process.env.VUE_APP_FORM_CSRF_INPUT_FIELD_NAME]: csrfToken} : {};
    richiestaGet(process.env.VUE_APP_URL_LOGOUT, parametriRichiestaGet)
        .catch( risposta => console.log("Logout fallito: " + risposta) );

    eliminaInfoAutenticazione();

    // logout sul client
    return router.push({name: process.env.VUE_APP_ROUTER_NOME_ROUTE_LOGIN});
}

/** Funzione per eliminare dal cliente le informazioni di autenticazione.
 * Il client, dopo l'esecuzione di questo metodo, risulterà non autenticato.*/
export const eliminaInfoAutenticazione = () => {
    eliminaTokenAutenticazione();
    impostaTokenDiAutenticazioneSeEsiste();
    rimuoviAuthorizationHeader();
}

/**
 * Cerca nei parametri della route corrente il token di autenticazione
 * e, se presente, lo salva per le successive richieste HTTP.
 * @param $route la property $route (this.$route) del componente che invoca questo metodo.
 */
const impostaTokenDiAutenticazioneSeEsiste = $route => {

    // Cerco token autenticazione nei parametri di Vue Router
    if($route) {    // controllo che $route (parametro di questo metodo) sia definita
        const tokenDaRoute = $route.params[process.env.VUE_APP_ROUTER_PARAMETRO_TOKEN_AUTENTICAZIONE];
        if(tokenDaRoute)
            salvaTokenAutenticazioneInLocalStorage(tokenDaRoute);
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
export const salvaTokenAutenticazioneInLocalStorage = valoreTokenAutenticazione =>
    localStorage.setItem(nomeVariabileInCuiSalvareIlTokenAutenticazione, valoreTokenAutenticazione);

/** Legge il valore del token di autenticazione dallo storage
 * locale e lo restituisce.*/
export const getTokenAutenticazione = () =>
    localStorage.getItem(nomeVariabileInCuiSalvareIlTokenAutenticazione);

/** Elimina il token di autenticazione dallo storage locale.*/
export const eliminaTokenAutenticazione = () =>
    localStorage.removeItem(nomeVariabileInCuiSalvareIlTokenAutenticazione);

/** Imposta token di autenticazione per le successive richieste
 * effettuate da questo client*/
export const setTokenAutenticazione = nuovoValoreTokenAutenticazione => {
    salvaTokenAutenticazioneInLocalStorage(nuovoValoreTokenAutenticazione);
    impostaAuthorizationHeaderInRichiesteHttp(nuovoValoreTokenAutenticazione);
}

/** Cerca nel token JWT di autenticazione e restituisce il valore del
 * claim JWT il cui nome è specificato come parametro. Se il claim
 * cercato non è presente nel token, allora restituirà undefined. */
const getValoreClaimDaTokenJwtAutenticazione = nomeClaimJwt => {

    const tokenAutenticazione = getTokenAutenticazione();

    if( tokenAutenticazione && typeof tokenAutenticazione === "string" ) {
        const [, payloadJwt, ] = tokenAutenticazione.split('.');    // un-packing del JWT ignorando header e signature
        if( payloadJwt ) {  // bisogna verificare che sia definito
            const payloadJwt_decodificatoDaBase64_comeStringa = atob(payloadJwt);   // Fonte: https://developer.mozilla.org/en-US/docs/Web/API/WindowOrWorkerGlobalScope/atob
            const payloadJwt_decodificatoDaBase64_comeOggettoJS = JSON.parse(payloadJwt_decodificatoDaBase64_comeStringa);
            const tipoAttoreAttualmenteAutenticato = payloadJwt_decodificatoDaBase64_comeOggettoJS[nomeClaimJwt];
            if( tipoAttoreAttualmenteAutenticato )
                return tipoAttoreAttualmenteAutenticato;
            else return undefined;
        } else {
            return undefined;
        }
    } else {
        return undefined;
    }

};

/** Restituisce il tipo dell'attore attualmente autenticato in una Promise.*/
export const getTipoAttoreAttualmenteAutenticato = () => {
    return getValoreClaimDaTokenJwtAutenticazione( process.env.VUE_APP_NOME_CLAIM_JWT_TIPO_ATTORE );
}

/** Restituisce il nome dell'attore attualmente autenticato.*/
export const getNomeAttoreAttualmenteAutenticato = () => {
    return getValoreClaimDaTokenJwtAutenticazione( process.env.VUE_APP_NOME_CLAIM_JWT_NOMINATIVO_ATTORE );
}

/** Restituisce lo username dell'attore attualmente autenticato.*/
export const getUsernameAttoreAttualmenteAutenticato = () => {
    return getValoreClaimDaTokenJwtAutenticazione( process.env.VUE_APP_NOME_CLAIM_JWT_USERNAME_ATTORE );
}

/** Restituisce l'email dell'attore attualmente autenticato.*/
export const getEmailAttoreAttualmenteAutenticato = () => {
    return getValoreClaimDaTokenJwtAutenticazione( process.env.VUE_APP_NOME_CLAIM_JWT_EMAIL_ATTORE );
}

/** Restituisce l'identificativo dell'attore attualmente autenticato.*/
export const getIdentificativoAttoreAttualmenteAutenticato = () => {
    return getValoreClaimDaTokenJwtAutenticazione( process.env.VUE_APP_NOME_CLAIM_JWT_IDENTIFICATIVO_ATTORE );
}

