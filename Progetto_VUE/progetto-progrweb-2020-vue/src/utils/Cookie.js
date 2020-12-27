/**
 * Classe per rappresentare un cookie come oggetto.
 */
export default class Cookie {
    constructor(nomeCookie, valoreCookie) {

        // Proprietà private ottenute con closure, getter/setter pubblici
        ( () => {
            let nome = nomeCookie;
            let valore = valoreCookie;

            this.getNomeCookie = () => nome;
            this.getValoreCookie = () => valore;
            this.setNomeCookie = nuovoNomeCookie => nome = nuovoNomeCookie;
            this.setValoreCookie = nuovoValoreCookie => valore = nuovoValoreCookie;

            this.toString = () => `${nome}=${valore}`;
        })();

    }
}


/**
 * Cerca un cookie per nome e ne restituisce il valore.
 * @param nomeCookie: il nome del cookie per cui si sta
 * cercando il valore.
 * @return la stringa con il valore del cookie cercato,
 * undefined se il cookie non si trova.
 */
export const getCookieValueByName = nomeCookie => {

    const cookie = cookieParser()
        .filter(cookie => cookie.getNomeCookie() === nomeCookie)[0];
    if(cookie !== undefined) {
        return  cookie.getValoreCookie();
    } else {
        return undefined;
    }
}

/**
 * Parser per i cookie.
 * @return array di oggetti, in cui ogni oggetto è un cookie.
 */
export const cookieParser = () => {
    const cookieStringArray = getAllCookiesInAString()
        .replace(/\s/g,'')
        .split(';');
    const cookieArray = cookieStringArray.map(cookie => {
        const questoCookieInArray = cookie.split('=');
        return new Cookie(questoCookieInArray[0], questoCookieInArray[1])
    })
    return cookieArray;
}

/**
 * Restituisce una stringa con tutti i cookie.
 * @return stringa con tutti i cookie.
 */
export const getAllCookiesInAString = () => document.cookie;