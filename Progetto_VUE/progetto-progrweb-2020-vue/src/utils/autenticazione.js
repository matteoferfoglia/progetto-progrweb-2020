/**
 * Codice JavaScript con funzioni di utilità, richiamate da
 * componenti diversi e definite qua per evitare duplicazione
 * di codice.
 */

// TODO : rivedere ed eventualmente ristrutturare questo script

import {
    getAuthorizationHeaderRichiesteHttp,
    impostaAuthorizationHeaderInRichiesteHttp, rimuoviAuthorizationHeader,
} from "./http";

/**
 * Funzione per verificare se un utente è già autenticato.
 * Questa procedura NON effettua una verifica interrogando
 * il server circa l'autenticazione di questo client, ma
 * semplicemente verifica che questo client possegga il
 * token di autenticazione, perciò il risultato affermativo
 * di questa funzione NON deve intendersi come "questo
 * client si è veramente autenticato presso il server",
 * ma piuttosto come "questo client dice di essersi autenticato
 * presso il server e gli crediamo", quindi il risultato
 * di questa funzione potrebbe essere usato, ad esempio, per
 * mostrare un particolare layout estetico (ad esempio, se
 * questo client dice di essersi autenticato, allora gli mostro
 * la pagina "area riservata" -ma solo il layout, perché il
 * contenuto deve essere fornito dal server che dovrebbe
 * verificare anche l'autenticazione e l'autorizzazione del
 * client per vedere il contenuto richiesto-, altrimenti
 * (se il client mi dice di non essersi autenticato) gli mostro
 * la pagina di autenticazione).
 *
 * @param $route la property $route (this.$route) del componente che invoca questo metodo.
 * @return true se l'utente è autenticato, false altrimenti.
 */
export const isAutenticato = $route => {
    return impostaTokenDiAutenticazioneSeEsiste($route);
}

/**
 * Cerca nei parametri della route corrente il token di autenticazione
 * e, se presente, lo salva per le successive richieste HTTP.
 * Se il token di autenticazione è salvato, restituisce true, altrimenti
 * false.
 * @param $route la property $route (this.$route) del componente che invoca questo metodo.
 * @return true se esiste il token di autenticazione, false altrimenti.
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


    return getAuthorizationHeaderRichiesteHttp() ?    // sia stringa vuota sia undefined sono falsy
                true : false;
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

