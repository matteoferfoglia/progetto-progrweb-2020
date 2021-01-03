/* eslint-disable no-unused-vars */
/**
 * Codice JavaScript con funzioni di utilità, richiamate da
 * componenti diversi e definite qua per evitare duplicazione
 * di codice.
 */


import {getCookieValueByName} from "./Cookie";

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
 * @return true se l'utente è autenticato, false altrimenti.
 */
export const isAutenticato = () => {
    return esisteCookieAutenticazione();
}

/**
 *
 * @return true se esiste il cookie di autenticazione, false altrimenti.
 */
const esisteCookieAutenticazione = () => {
    return getCookieValueByName(process.env.VUE_APP_NOME_COOKIE_AUTENTICAZIONE_CLIENT) ? true : false;
}