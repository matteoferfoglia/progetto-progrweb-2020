/** Script JavaScript di supporta a richieste e risposte HTTP. */

import axios from "axios";

// TODO : ristrutturare meglio questo script

// TODO : non funziona: quando si aggiorna la pagina, i dati sono persi ! Pssare il token di autenticazione in cookie, così viene inviato automaticamente

// TODO : rivedere ed eventualmente ristrutturare questo script - serve??


/** Stato della risposta HTTP: credenziali invalide.*/
export const HTTP_STATUS_UNAUTHORIZED = 401;

/** Stato della risposta HTTP: ok.*/
export const HTTP_STATUS_OK = 200;

/** Nome dell' Authorization headere nelle richieste HTTP.*/
const HTTP_HEADER_AUTHORIZATION_NOME = "Authorization";    // è standard, ma meglio evitare valori "literal" nel codice


/** Oggetto contenente il token di autenticazione, accessibile
 * solo tramite getter e setter, realizzati grazie a closure.*/
const tokenAutenticazione = ( () => {

    let valoreTokenAutenticazione;

    // Restituisco oggetto con properties read-only
    let oggettoDaRestituire = {};
    Object.defineProperty(oggettoDaRestituire, "getValoreTokenAutenticazione", {
        value: () => valoreTokenAutenticazione,
        writable: false
    });
    Object.defineProperty(oggettoDaRestituire, "setValoreTokenAutenticazione", {
        value: nuovoValoreTokenAutenticazione => valoreTokenAutenticazione = nuovoValoreTokenAutenticazione,
        writable: false
    });

    return oggettoDaRestituire;

}) ();


/** Oggetto di configurazione per le richieste HTTP (ad es. contenente
 * degli header particolari, come "Authorization"), realizzato con closure.*/
let configRichiesteHttp = ( () => {     // TODO : testare

    let oggettoConfig = {headers : {}}; // è l'oggetto di configurazione vero e proprio

    return {
        "impostaHeader" : (nomeHeader, valoreHeader) => oggettoConfig.headers[nomeHeader] = valoreHeader,
        "rimuoviHeader" : nomeHeaderDaRimuovere      => delete oggettoConfig.headers[nomeHeaderDaRimuovere],
        "getConfig"     : ()                         => oggettoConfig
    }
}) ();



/** Imposta header "Authorization" per le successive richieste,
 * con il valore del token passato come parametro.
 * Fonte: <a href="https://stackoverflow.com/a/45581882">
 *   How to set header and options in axios?
 * </a>.
 * @param token Token da inserire nell'header "Authorization" delle
 *              successive richieste HTTP
 */
export const impostaAuthorizationHeaderInRichiesteHttp = token => {
    tokenAutenticazione.setValoreTokenAutenticazione(token);
    configRichiesteHttp.impostaHeader(HTTP_HEADER_AUTHORIZATION_NOME, "Bearer " + token);
}

/** Rimuove l'eventuale Authorization header (se l'Authorization
 * header non è presente, allora questa funzione non fa nulla).*/
export const rimuoviAuthorizationHeader = () => {
    configRichiesteHttp.rimuoviHeader(HTTP_HEADER_AUTHORIZATION_NOME);
}


/** Legge il token impostato per l'header "Authorization" delle richieste HTTP.*/
export const getAuthorizationHeaderRichiesteHttp = () => tokenAutenticazione.getValoreTokenAutenticazione();


/** Restituisce lo stato della response HTTP.*/
export const getHttpResponseStatus = (responseHttp) => responseHttp.status;


/** Effettua una richiesta GET.
 * @param url è l'url di destinazione delle request.
 * @return una Promise con la risposta ricevuta: la promise è "thenable"
 *          se non ci sono stati errori, "catchable" altrimenti.
 */
export const richiestaGet = (url,oggettoConParametri) => {

    const cloneConfigRichiesteHttp = aggiungiParametriAllaRequest(oggettoConParametri);

    return axios.get(url, cloneConfigRichiesteHttp)
                .then(risposta => Promise.resolve(risposta) )
                .catch(errore  => Promise.reject(errore) ) ;    //  errore contiene la property response
                                                                //      Fonte: https://stackoverflow.com/a/39153411
}


/** Effettua una richiesta POST.
 * @param url è l'url di destinazione delle request.
 * @param dati è il dato da inviare.
 * @return una Promise con la risposta: la promise è "thenable"
 *          se non ci sono stati errori, "catchable" altrimenti.
 */
export const richiestaPost = (url, dati) => {
    return axios.post(url, dati, configRichiesteHttp.getConfig())
                .then(risposta => Promise.resolve(risposta) )
                .catch(errore  => Promise.reject(errore.response) ) ;
}


/** Clona l'oggetto di configurazione per le richieste e vi aggiunge
 * come parametri le properties dell'oggetto dato come parametro a
 * questo metodo. Infine restituisce l'oggetto di configurazione
 * ottenuta. I parametri passati a questo metodo NON diventano
 * "definitivi" per tutte le future richieste, ma solo per quelle
 * in cui si usa l'oggetto di configurazione restituito.
 */
const aggiungiParametriAllaRequest = oggettoConParametri => {

    //Aggiunta degli eventuali parametri all'oggetto di configurazione
    const cloneConfigRichiesteHttp = JSON.parse(JSON.stringify(configRichiesteHttp.getConfig()));   // clona oggetto di configurazione per non sovrascriverlo
    if (oggettoConParametri &&   // controlla che non sia null or undefined
        Object.keys(oggettoConParametri).length !== 0) { // se ci sono parametri vanno aggiunti alla request

        cloneConfigRichiesteHttp["params"] = oggettoConParametri;   // aggiunge i parametri
    }
    return cloneConfigRichiesteHttp;
}

/** Effettua una richiesta DELETE.
 * @param url è l'url della risorsa da eliminare.
 * @param oggettoConParametri Oggetto in cui ogni property è
 *                            un parametro per la richiesta.
 * @return una Promise con l'eventuale corpo della risposta,
 *          oppure una Promise rigettata in caso di errore.
 */
export const richiestaDelete = (url, oggettoConParametri) => {

    const cloneConfigRichiesteHttp = aggiungiParametriAllaRequest(oggettoConParametri);

    return axios.delete(url, cloneConfigRichiesteHttp)
        .then(risposta => Promise.resolve(risposta) )
        .catch(errore  => Promise.reject(errore.response) ) ;
}