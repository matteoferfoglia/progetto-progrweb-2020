/** Questo script contiene delle funzioni utilizzabili in contesti diversi per
 * richiedere al server delle informazioni su un attore.*/

import {richiestaGet} from "./http";

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