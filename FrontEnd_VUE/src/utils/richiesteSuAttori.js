/** Questo script contiene delle funzioni utilizzabili in contesti diversi per
 * richiedere al server delle informazioni su un attore.*/

import {richiestaDelete, richiestaGet} from "./http";
import {logout} from "@/utils/autenticazione";

/** Richiede le informazioni relative all'attore il cui identificativo
 * è passato come parametro. Se la richiesta va a buon fine, viene
 * restituita una Promise risolta contenente un array di due elementi,
 * in cui il primo contiene l'identificativo dell'Attore ed il secondo
 * contiene un oggetto in cui ogni property è una proprietà dell'Attore.
 *
 * @param idAttoreDiCuiRichiedereInfo Identificativo dell'attore per cui si richiedono le informazioni.
 * @param tipoAttoreAttualmenteAutenticato Tipo di attore attualmente autenticato.
 * @param tipoAttoreDiCuiRichiedereInfo Tipo di attore per cui si richiedono le informazioni.*/
export const getInfoAttore = async (idAttoreDiCuiRichiedereInfo,
                                    tipoAttoreAttualmenteAutenticato,
                                    tipoAttoreDiCuiRichiedereInfo) => {

    let urlRichiesta;

    if( tipoAttoreAttualmenteAutenticato === process.env.VUE_APP_TIPO_UTENTE__ADMINISTRATOR ) {
        if( tipoAttoreDiCuiRichiedereInfo === process.env.VUE_APP_TIPO_UTENTE__ADMINISTRATOR ) {
            urlRichiesta = process.env.VUE_APP_URL_GET_INFO_ADMINISTRATOR__RICHIESTA_DA_ADMINISTRATOR;
        } else {
            urlRichiesta = process.env.VUE_APP_URL_GET_INFO_UPLOADER;
        }
    } else if( tipoAttoreAttualmenteAutenticato === process.env.VUE_APP_TIPO_UTENTE__UPLOADER ) {
        urlRichiesta = process.env.VUE_APP_URL_GET_INFO_CONSUMER__RICHIESTA_DA_UPLOADER;
    } else if( tipoAttoreAttualmenteAutenticato === process.env.VUE_APP_TIPO_UTENTE__CONSUMER ) {
        urlRichiesta = process.env.VUE_APP_URL_GET_INFO_UPLOADER;
    } else {
        urlRichiesta = "";
    }

    urlRichiesta += "/" + idAttoreDiCuiRichiedereInfo;

    return await richiestaGet( urlRichiesta )
        .then(rispostaConProprietaAttore => [idAttoreDiCuiRichiedereInfo, rispostaConProprietaAttore]);  // restituisce l'entry: [ idAttoreDiCuiRichiedereInfo, {propQuestoAttore} ]
}

/** Dato l'array avente per elementi gli identificativi degli Attori,
 * richiede al server una mappa avente per chiavi gli identificativi degli
 * Attori e per valori l'oggetto con le proprietà dell'Attore
 * indicato dalla chiave.
 */
export const getMappa_idAttore_proprietaAttore = async (arrayIdAttore,
                                                        tipoAttoreAttualmenteAutenticato,
                                                        tipoAttoriDiCuiRichiedereInfo) => {
    // Richiede al server info su ogni Attore nell'array

    // "noinspection JSCheckFunctionSignatures" perché segnalava un warning sul costruttore 'new Map(...)' ma in realtà
    // il parametro inserito rispetta le specifiche (Iterable is an Array or other iterable object whose elements are
    // key-value pairs (2-element Arrays).
    // Fonte: https://developer.mozilla.org/it/docs/Web/JavaScript/Reference/Global_Objects/Map#parameters )
    // noinspection JSCheckFunctionSignatures
    return Promise.all( arrayIdAttore.map( idAttore => getInfoAttore( idAttore, tipoAttoreAttualmenteAutenticato, tipoAttoriDiCuiRichiedereInfo ) ))
        //  una Promise per ogni Attore, quindi Promise.all per poi aspettarle tutte (Fonte: https://stackoverflow.com/a/31414472)

        .then( arrayConEntriesDaTutteLePromise => new Map(arrayConEntriesDaTutteLePromise) ) // then() aspetta tutte le promise prima di eseguire
        .catch( rispostaErrore =>
            console.error("Errore durante il caricamento delle informazioni sugli Attori: " + rispostaErrore ) );

}


/** Dato l'id di un attore, se l'utente attualmente autenticato è autorizzato
 * a saperlo, restituisce il ruolo dell'attore "target" il cui id è stato fornito
 * in una Promise risolta, altrimenti restituisce una Promise rigettata.*/
export const getTipoAttoreTarget = async idAttoreTarget => {
    return richiestaGet(process.env.VUE_APP_URL_GET_TIPO_ATTORE_CORRISPONDENTE + "/" + idAttoreTarget)
            .then( risposta => risposta )
            .catch( errore => {
                console.error(errore);
                return Promise.reject(errore);
            })
}

/** Dato l'id di un attore, se l'utente attualmente autenticato è autorizzato
 * a saperlo, restituisce le proprietà dell'attore "target" il cui id è stato fornito
 * in una Promise risolta, altrimenti restituisce una Promise rigettata.*/
export const getProprietaAttoreTarget = async idAttoreTarget => {
    return richiestaGet(process.env.VUE_APP_URL_GET_PROPRIETA_ATTORE_CORRISPONDENTE + "/" + idAttoreTarget)
        .then( risposta => risposta )
        .catch( errore => {
            console.error(errore);
            return Promise.reject(errore);
        })
}

/** Dato l'identificativo di un attore, restituisce l'url per richiedere
 * al server il logo di quell'attore. Se il parametro risulta falsy, questo
 * metodo restituisce undefined. */
export const creaUrlLogo = identificativoAttore => {
    if( identificativoAttore )
        return process.env.VUE_APP_URL_GET_LOGO + "/" + identificativoAttore +
                                                  '?' + new Date().getTime(); // query string per evitare cache;
    else
        return undefined;
};

/** Richiede al server l'eliminazione di un attore.
 * @param idAttoreDaEliminare Identificativo dell'attore per cui
 *                            viene richiesta l'eliminazione.
 * @param idAttoreRichiedenteEliminazione Eliminazione Identificativo dell'attore
 *                            che richiede l'eliminazione.
 * @param tipoAttoreRichiedente Tipo dell'attore che richiede l'eliminazione.
 * @param csrfToken Token CSRF da allegare alla richiesta.*
 * @param usernameONominativoAttoreDaEliminare Stringa che comparirà nel messaggio
 *                            di confermata eliminazione.
 * @param logoutAFineOperazione Flag: true se al termine dell'operazione di eliminazione
 *                            bisogna eseguire il logout (es.: se un Administrator elimina
 *                            se stesso, poi non esisterà più e non potrà fare nulla, quindi
 *                            è corretto eseguire il logout).
 * @param router L'oggetto $router.
 * @return Promise risolta se l'eliminazione va a buon fine, oppure Promise risolta e
 *          reindirizzamento a schermata iniziale tramite $router.push() se è richiesto
 *          il logout al termine dell'operazione, oppure Promise rigettata se l'utente
 *          non conferma le modifiche.*/
export const eliminaAttore = ( idAttoreDaEliminare, idAttoreRichiedenteEliminazione,
                               tipoAttoreRichiedente, csrfToken,
                               usernameONominativoAttoreDaEliminare, logoutAFineOperazione,
                               router ) => {

    // Uploader può eliminare Consumer
    // Administrator può eliminare Uploader o Administrator (sé compreso)



    const MSG_ERR_UTENTE_NON_CONFERMA = "Utente non ha confermato modifiche";
    return confirm("Eliminare " + usernameONominativoAttoreDaEliminare + '?')   // richiede conferma prima di eliminare
            .catch( () => Promise.reject(MSG_ERR_UTENTE_NON_CONFERMA) ) // utente non ha confermato
            .then( () => {                                              // eseguito solo se utente ha confermato

                const urlEliminazioneAttore = ( tipoAttoreRichiedente ?
                    process.env.VUE_APP_URL_DELETE_CONSUMER_PER_QUESTO_UPLOADER__RICHIESTA_DA_UPLOADER  :
                    process.env.VUE_APP_URL_DELETE_ATTORE__RICHIESTA_DA_ADMIN ) + "/" + idAttoreDaEliminare;

                const parametriRichiestaDelete = {[process.env.VUE_APP_FORM_CSRF_INPUT_FIELD_NAME]: csrfToken};

                return richiestaDelete( urlEliminazioneAttore, parametriRichiestaDelete );

            })
            .then( () => {
                alert( usernameONominativoAttoreDaEliminare + " eliminato." );

                if( logoutAFineOperazione )
                    return logout();   // logout se un Administrator ha eliminato sé

                return router.push({path: process.env.VUE_APP_ROUTER_PATH_AREA_RISERVATA});
            })
            .catch( errore => {

                let msgErrore;

                if( errore===MSG_ERR_UTENTE_NON_CONFERMA ) {
                    msgErrore = "Nessuna modifica apportata."
                    alert(msgErrore);
                } else {
                    msgErrore = "Si è verificato un errore durante l'aggiornamento delle informazioni. " + errore.data;
                    alert(msgErrore);
                    console.error(msgErrore + "Errore durante l'eliminazione: " + errore );
                }

                return Promise.reject(msgErrore);

            });

};