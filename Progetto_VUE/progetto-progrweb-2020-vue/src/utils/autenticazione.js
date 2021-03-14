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

/** Restituisce il tipo dell'attore attualmente autenticato.*/
export const getTipoAttoreAttualmenteAutenticato = async () => {

    let necessariaRichiestaAlServer = false;    // diventerà true se sarà necessario richiedere al server

    const tokenAutenticazione = getTokenAutenticazione();

    if( tokenAutenticazione && typeof tokenAutenticazione === "string" ) {
        const [, payloadJwt, ] = tokenAutenticazione.split('.');    // un-packing del JWT ignorando header e signature
        if( payloadJwt ) {  // bisogna verificare che sia definito
            const payloadJwt_decodificatoDaBase64_comeStringa = atob(payloadJwt);   // Fonte: https://developer.mozilla.org/en-US/docs/Web/API/WindowOrWorkerGlobalScope/atob
            const payloadJwt_decodificatoDaBase64_comeOggettoJS = JSON.parse(payloadJwt_decodificatoDaBase64_comeStringa);
            const tipoAttoreAttualmenteAutenticato = payloadJwt_decodificatoDaBase64_comeOggettoJS[process.env.VUE_APP_NOME_CLAIM_JWT_TIPO_ATTORE];
            if( tipoAttoreAttualmenteAutenticato )
                return tipoAttoreAttualmenteAutenticato;
            else necessariaRichiestaAlServer = true;
        } else {
            necessariaRichiestaAlServer = true;
        }
    }

    if ( necessariaRichiestaAlServer ) {
        return richiestaGet(process.env.VUE_APP_URL_GET_TIPO_UTENTE_AUTENTICATO)
            .catch( rispostaErrore => {
                console.error("Errore durante il caricamento delle informazioni: " + rispostaErrore );
                return Promise.reject(rispostaErrore);
            });
    }



}

/** Restituisce il nome dell'attore attualmente autenticato.*/
export const getNomeAttoreAttualmenteAutenticato = async () => {

    // TODO: potrebbe essere ottenuto dal token JWT senza "sprecare" una richiesta al server

    return richiestaGet(process.env.VUE_APP_URL_GET_NOME_QUESTO_ATTORE_AUTENTICATO)
        .catch( rispostaErrore => {
            console.error("Errore durante il caricamento delle informazioni: " + rispostaErrore );
            return Promise.reject(rispostaErrore);
        });

}

/** Restituisce l'identificativo dell'attore attualmente autenticato.*/
export const getIdentificativoAttoreAttualmenteAutenticato = async () => {

    // TODO: potrebbe essere ottenuto dal token JWT senza "sprecare" una richiesta al server

    return richiestaGet(process.env.VUE_APP_URL_GET_IDENTIFICATIVO_QUESTO_ATTORE_AUTENTICATO)
        .catch( rispostaErrore => {
            console.error("Errore durante il caricamento delle informazioni: " + rispostaErrore );
            return Promise.reject(rispostaErrore);
        });

}

