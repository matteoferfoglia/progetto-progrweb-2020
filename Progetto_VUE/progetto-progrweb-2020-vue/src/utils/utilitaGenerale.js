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

/** Dati due array, restituisce true se hanno la stessa lunghezza e
 * contengono gli stessi valori (dello stesso tipo), indipendentemente
 * dall'ordine, altrimenti false. */
export const areArrayEquivalenti = (array1, array2) => {

    if( !(isArray(array1) && isArray(array2)) )
        return false;
    if( array1.length !== array2.length )
        return false;

    // Copia locale degli array ordinati, per confrontarne
    // i valori indipendentemente dall'ordine
    const array1_ordinato = array1.sort();
    const array2_ordinato = array2.sort();

    let areArrayEquivalenti = true;
    for( let i=0; i<array1_ordinato.length; i++ ) {
        if(array1_ordinato[i]!==array2_ordinato[i])
            areArrayEquivalenti = false;
    }
    return areArrayEquivalenti;
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
                                       .toLowerCase()
                                       .trim();
    return risultato.charAt(0).toUpperCase() +
           risultato.slice(1);
}

/** Questa funzione restituisce un identificativo numerico univoco
 * all'interno del progetto, finché non viene ricaricata la pagina
 * (es. per elementi HTML).
 * I valori generati che soddisfano la proprietà di univocità sono
 * compresi tra zero e {@link Number.MAX_SAFE_INTEGER} (compresi),
 * dopodiché il conteggio ricomincia da 0.
 * I valori sono generati in modo incrementale.*/
export const generaIdUnivoco = (() => {

    let contatore = 0;

    return () => {

        if( contatore < Number.MAX_SAFE_INTEGER )
            contatore = contatore + 1 ;
        else contatore = 0

        return contatore;
    }

})();

/** Funzione per rendere maiscuola la prima lettera di ogni parola
 * nella stringa data.*/
export const capitalize = str =>
    str.split(' ')
       .map( unaParola => unaParola.charAt(0).toUpperCase() + unaParola.slice(1) )
       .join(' ');