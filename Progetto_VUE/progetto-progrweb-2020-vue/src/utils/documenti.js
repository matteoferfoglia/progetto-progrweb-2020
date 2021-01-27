/** Script per la gestione dei documenti. Include i metodi
 * che prescindono dal particolare componente in cui vengono
 * utilizzati.*/

// TODO : rivedere ed eventualmente ristrutturare questo script - serve??


import {isArray} from "./utilitaGenerale";
import {richiestaGet} from "./http";

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
 *         che contengono, implementato tramite Map.    // TODO : SPOSTARE QUESTO METODO NELLA CLASSE MAPPA DOCUMENTI
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

/** Ordina la mappa dei documenti in base alla data di caricamento
 * e portando in testa quelli ancora non visualizzati..
 * La mappa dei documenti deve avere come chiave l'identificativo del
 * documento nell'entry di quella chiave e come valore l'oggetto che
 * rappresenta il documento stesso.
 * Restituisce la mappa risultante come valore di una Promise risolta,
 * ammesso che la procedura vada a buon fine.*/ // TODO : SPOSTARE QUESTO METODO NELLA CLASSE MAPPA DOCUMENTI
export const ordinaMappaSuDataCaricamentoConNonVisualizzatiDavanti = mappaDocumenti => {

    return getNomePropertyDataCaricamentoDocumenti()

        // Ordina mappa in base alla data di caricamento ( Fonte: https://stackoverflow.com/a/50427905 )
        .then( nomePropertyDataCaricamento =>
            mappaDocumenti = new Map([...mappaDocumenti]
                .sort( (a,b) => a[1][nomePropertyDataCaricamento] < b[1][nomePropertyDataCaricamento] ? 1 : -1 ))
        )

        // Crea l'array con l'id dei documenti non ancora visualizzati
        .then( getNomePropertyDataVisualizzazioneDocumenti )
        .then( nomePropDataVisualizzazione =>
            Array.from(   mappaDocumenti.entries() )
                 .filter( entryDocumento => entryDocumento[1][nomePropDataVisualizzazione] == null ) // filtra documenti non ancora letti // TODO : verifica corretto funzionamento
                 .map(    entryDocumento => entryDocumento[0] )                                      // salva l'id dei documenti risultanti dal filtraggio
                                                                                                     // entryDocumento è nella forma:   [chiaveDoc, {prop1Doc: val1, prop2Doc: val2}]
        )   // restituisce array con id dei documenti non ancora visualizzati in una Promise

        // Riordina l'elenco dei documenti portando in testa quelli ancora non letti
        // mantenendo i più recenti in ordine, subito dopo quelli non letti
        .then( arrayIdDocumentiNonAncoraVisualizzati =>  {
            const mappaSoloDocumentiNonLetti =
                new Map( arrayIdDocumentiNonAncoraVisualizzati
                             .map( id => [id, mappaDocumenti.get(id)] ) );

            // Elimina dalla mappa generale dei documenti quelli già letti per non ricontarli
            arrayIdDocumentiNonAncoraVisualizzati
                .forEach( id => mappaDocumenti.delete(id) );

            // Restituisci il merge delle mappe
            return new Map([...mappaSoloDocumentiNonLetti, ...mappaDocumenti]); // Fonte: https://stackoverflow.com/a/32000937
        })

        .catch( errore => Promise.reject("Errore durante l'ordinamento dei documenti: " + errore) );
}

/** Questa funzione richiede al server il nome dell'attributo di un
 * oggetto "documento" il cui valore è la data di caricamento di
 * quel documento.
 * Se la richiesta va a buon fine, viene restituita una Promise
 * risolta con valore il nome dell'attributo restituito dal server.*/
const getNomePropertyDataCaricamentoDocumenti = async () => {

    return richiestaGet(process.env.VUE_APP_URL_GET_ELENCO_DOCUMENTI__RICHIESTA_DA_CONSUMER_NOME_PROP_DATA_CARICAMENTO)
        .then(  risposta       => risposta )
        .catch( rispostaErrore => {
            console.error("Errore durante la ricezione del nome dell'attributo " +
                "contenente la data di caricamento dei documenti: " + rispostaErrore );
            return Promise.reject(rispostaErrore);
            // TODO : gestire l'errore (invio mail ai gestori?)
            // TODO : cercare tutti i catch nel progetto e fare un gestore di eccezioni unico
        });

}

/** Questa funzione richiede al server il nome dell'attributo di un
 * oggetto "documento" il cui valore è la data di visualizzazione di
 * quel documento da parte del Consumer.
 * Se la richiesta va a buon fine, viene restituita una Promise
 * risolta con valore il nome dell'attributo restituito dal server.*/
const getNomePropertyDataVisualizzazioneDocumenti = async () => {

    return richiestaGet(process.env.VUE_APP_URL_GET_ELENCO_DOCUMENTI__RICHIESTA_DA_CONSUMER_NOME_PROP_DATA_VISUALIZZAZIONE)
        .then(  risposta       => risposta )
        .catch( rispostaErrore => {
            console.error("Errore durante la ricezione del nome dell'attributo " +
                "contenente la data di caricamento dei documenti: " + rispostaErrore );
            return Promise.reject(rispostaErrore);
            // TODO : gestire l'errore (invio mail ai gestori?)
            // TODO : cercare tutti i catch nel progetto e fare un gestore di eccezioni unico
        });

}


/** Classe per rappresentare una mappa avente per chiave
 * gli identificativi dei documenti e le properties di
 * quei documeni come valori corrispondenti. Esempio:
 * {
 *     "12345" => { nomeDocumento: "Fattura", Hashtag: "pagare" },
 *     "67890" => { nomeDocumento: "Bonifico", Hashtag: "soldi" },
 * }.
 * Si accede alla mappa tramite getter/setter.
 *
 * Si tratta di un oggetto wrapper: definisco un
 * oggetto avente una proprietà corrispondente alla mappa
 * sopra descritta.
 *
 * MOTIVAZIONE DI UN OGGETTO WRAPPER:
 *  Se passo ad un altro componente un oggetto immutabile
 *  e poi lo modifico nel primo componente, allora nel primo
 *  componente ottengo un nuovo oggetto (con un riferimento
 *  diverso da quello che ho passato all'altro componente):
 *  i due oggetti ormai sarebbero slegati.
 *  Soluzione: utilizzo un wrapper: se ne modifico una property
 *  da un componente e poi l'altro componente accede a quella
 *  property dello stesso oggetto, allora vedrà il valore aggiornato.
 */
export class MappaDocumenti{

    constructor() {
        this.mappaDocumenti = { mappa: new Map()};
    }

    /** @return la mappa rappresentata da questa istanza.*/
    get() {
        return this.mappaDocumenti.mappa;
    }

    /** @param nuovaMappaDocumenti La nuova mappa che deve essere
     * rappresentata da questa istanza.*/
    set(nuovaMappaDocumenti) {
        this.mappaDocumenti.mappa = nuovaMappaDocumenti;
    }

}