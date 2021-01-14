/** Script per la gestione dei documenti. Include i metodi
 * che prescindono dal particolare componente in cui vengono
 * utilizzati.*/

// TODO : rivedere ed eventualmente ristrutturare questo script - serve??


import {isArray} from "./utilitaGenerale";

/** Dato l'elenco dei documenti (formato di questo parametro
 * descritto di seguito nel campo param), ne fa il parsing per
 * individuare, per ogni documento dell'array, gli hashtag che
 * esso contiene, quindi crea un indice, implementato tramite Map,
 * in cui ogni chiave è un hashtag ed il corrispondente valore è un
 * array contenente gli identificativi di tutti i documenti che
 * contengono quel particolare hashtag.
 * Gli hashtag vengono indicizzati in modo case-insensitive.
 * @param elencoDocumenti Oggetto in cui ogni property ha come nome
 *                        l'identificativo di un documento e come
 *                        valore quel documento.
 * @param nomePropertyHashtagDocumenti Con riferimento all'elenco
 *                        dei documenti: ogni documento è un oggetto
 *                        con una property contenente la lista degli
 *                        hashtag di quel documento: questo parametro
 *                        corrisponde al nome di tale property.
 * @return indice dei documenti, indicizzati rispetto agli hashtag
 *         che contengono, implementato tramite Map.
 */
export const creaIndiceDeiFileRispettoAgliHashtagCheContengono = (elencoDocumenti, nomePropertyHashtagDocumenti) => {

    const indice_Hashtag_Documenti = new Map();

    // Iterazione sulle properties dell'oggetto,
    //  fonte: https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Object/entries
    for (const [idDocumento, documento] of elencoDocumenti) {

        // Ogni documento è un oggetto in cui una property è l'array degli hashtag
        const hashtagsDiQuestoDocumento = documento[nomePropertyHashtagDocumenti];  // array degli hashtag

        if( isArray(hashtagsDiQuestoDocumento) && hashtagsDiQuestoDocumento.length > 0) {
            hashtagsDiQuestoDocumento.forEach(hashtag => {
                hashtag = hashtag.toLowerCase().trim();       // preprocessing di ogni hashtag prima di salvarlo nell'indice
                if (indice_Hashtag_Documenti.has(hashtag)) {
                    indice_Hashtag_Documenti.get(hashtag)                                 // recupera entry corretta dalla mappa
                        .push(idDocumento); // aggiunge alla posting list (array) l'id di questo documento
                } else {
                    // hashtag non ancora presente nell'indice, quindi posting list da creare
                    indice_Hashtag_Documenti.set(hashtag, [idDocumento]);   // crea un array quale valore di questa entry
                }
            });
        }

    }

    return indice_Hashtag_Documenti;
}