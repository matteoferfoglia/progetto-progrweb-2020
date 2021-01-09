/** Script con funzioni di utilità generale.*/

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