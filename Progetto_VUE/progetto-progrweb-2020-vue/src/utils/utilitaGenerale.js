/** Script con funzioni di utilità generale.*/

// TODO : rivedere ed eventualmente ristrutturare questo script - serve??


import {richiestaGet} from "./http";

/** Restituisce true se l'oggetto passato come parametro
 è un array, false altrimenti.*/
export const isArray = oggetto =>
    oggetto                             &&
    typeof oggetto        === 'object'  &&
    typeof oggetto.length === 'number'  &&
    typeof oggetto.splice === 'function'&&
    ! ({}.propertyIsEnumerable.call(oggetto, 'length'));


/** Definisce l'equivalenza tra due mappe, indipendentemente
 dall'ordine delle entry.
 Due mappe vengono considerate equivalenti se:
 <ol>
    <li>hanno le stesse chiavi, anche in ordine diverso</li>
    <li>due entry con la stessa chiave (nelle due mappe
        confrontate) hanno lo stesso valore (confronto
        eseguito con l'operatore <code>===</code> ).</li>
 </ol>
 @return true se le due mappe passate sono considerate
          equivalenti da questo metodo, false altrimenti.*/
export const areMappeEquivalenti = (mappa1, mappa2) => {

    if( mappa1 instanceof Map &&
        mappa2 instanceof Map &&
        mappa1.size === mappa2.size ) {

        // Se qui, i due oggetti sono entrambe mappe della stessa lunghezza
        for( const chiave in mappa1.keys() ) {

            if( ! mappa2.has(chiave) )
                return false;

            if( mappa1.get(chiave) !== mappa2.get(chiave) )
                return false;

        }

        // Se qui, tutti i confronti ok
        return true;

    }

    return false;

}

/** Dati due oggetti, ne restituisce uno avente le properties
 di entrambi. Se entrambi gli oggetti hanno la stessa property,
 quella del primo oggetto viene persa.*/
export const unisciOggetti = ( oggetto1, oggeto2 ) => {
    const oggettoUnito = {};
    for( let propName in oggetto1 )
        oggettoUnito[propName] = oggetto1[propName];
    for ( let propName in oggeto2 )
        oggettoUnito[propName] = oggeto2[propName];
    return oggettoUnito;
}

/** Data una stringa in formato CamelCase, la converte in formato
 * Human Readable e restituisce il risultato. Ad esempio, data la
 * string "nomeUtente", in uscita da questa funzione si avrà
 * "Nome utente"
 * (<a href="https://stackoverflow.com/a/7225450">Fonte</a>). */
export const camelCaseToHumanReadable = testoDaConvertire => {
    const risultato = testoDaConvertire.trim()
                                       .replace(/([A-Z])/g, " $1" )
                                       .toLowerCase();
    return risultato.charAt(0).toUpperCase() +
           risultato.slice(1);
}