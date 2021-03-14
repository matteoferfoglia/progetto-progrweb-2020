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


/** Ricerca il claim JWT il cui nome è specificato come parametro nel
 * token JWT di autenticazione: se lo trova, allora restituisce una
 * Promise risolta contenente il valore corrispondente a tale claim,
 * altrimenti effettua una richiesta GET all'inidirizzo specificato
 * come secondo parametro e restituisce la Promise risultante da tale
 * richiesta.
 * L'idea di questo metodo è: se il claim che si sta cercando
 * è presente nel token JWT di autenticazione, allora viene restituito
 * (in una Promise risolta) altrimenti lo si chiede al server. */
const getValoreDaTokenJwtORichiediloAlServer = async ( nomeClaimJwt, urlRichiestaServerSeClaimNonPresente) => {

    const valoreClaimDaTokenJwtAutenticazione = getValoreClaimDaTokenJwtAutenticazione(nomeClaimJwt);

    if ( ! valoreClaimDaTokenJwtAutenticazione ) {  // se undefined
        return richiestaGet(urlRichiestaServerSeClaimNonPresente)   // restituisce una Promise
                .catch(rispostaErrore => {
                    console.error("Errore durante il caricamento delle informazioni: " + rispostaErrore);
                    return Promise.reject(rispostaErrore);
                });
    } else {
        return Promise.resolve( valoreClaimDaTokenJwtAutenticazione );
    }
}


/** Restituisce il tipo dell'attore attualmente autenticato in una Promise.*/
export const getTipoAttoreAttualmenteAutenticato = async () => {

    return getValoreDaTokenJwtORichiediloAlServer( process.env.VUE_APP_NOME_CLAIM_JWT_TIPO_ATTORE,
                                                   process.env.VUE_APP_URL_GET_TIPO_UTENTE_AUTENTICATO );

}

/** Restituisce il nome dell'attore attualmente autenticato in una Promise.*/
export const getNomeAttoreAttualmenteAutenticato = async () => {

    return getValoreClaimDaTokenJwtAutenticazione( process.env.VUE_APP_NOME_CLAIM_JWT_NOMINATIVO_ATTORE,
                                                   process.env.VUE_APP_URL_GET_NOME_QUESTO_ATTORE_AUTENTICATO );

}

/** Restituisce l'identificativo dell'attore attualmente autenticato in una Promise.*/
export const getIdentificativoAttoreAttualmenteAutenticato = async () => {

    return getValoreClaimDaTokenJwtAutenticazione( process.env.VUE_APP_NOME_CLAIM_JWT_IDENTIFICATIVO_ATTORE,
                                                   process.env.VUE_APP_URL_GET_IDENTIFICATIVO_QUESTO_ATTORE_AUTENTICATO );

}

