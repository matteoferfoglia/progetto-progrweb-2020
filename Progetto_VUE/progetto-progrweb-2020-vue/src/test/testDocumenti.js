/* eslint-disable no-undef */
/** Test per la gestione dei documenti.*/

import {creaIndiceDeiFileRispettoAgliHashtagCheContengono} from "../utils/documenti";
import {areMappeEquivalenti} from "../utils/utilitaGenerale";

describe("Test in " + __filename + ": Indicizza documenti rispetto hashtag.",() => {

    const NOME_PROP_HASHTAGS_IN_DOC = "hashtags";

    test('indicizzaDocumentiRispettoHashtag',  async () => {

        const mappaDocumenti = new Map(Object.entries({
            // ogni prop è un documento, nomeProp è l'id del file, valoreProp è il file
            "9s5fe1" : {nomeDoc: "doc prova", [NOME_PROP_HASHTAGS_IN_DOC]: ["prova ", "doc", " test"]},
            "a0"     : {nomeDoc: "Fattura settembre", [NOME_PROP_HASHTAGS_IN_DOC]: ["   doc", "  setteMbre"], tipoDoc: "fattura"},
            "166"    : {nomeDoc: "Fattura agosto", [NOME_PROP_HASHTAGS_IN_DOC]: ["doc", "agoSto", " settembre"], tipoDoc: "fattura"},
            "vuoto"  : {}
        }));

        const indiceHashtagDocumentiAtteso = new Map();
        {
            // Costruzione del valore atteso dal test
            indiceHashtagDocumentiAtteso.set("prova", ["9s5fe1"]);
            indiceHashtagDocumentiAtteso.set("doc", ["9s5fe1", "a0", "166"]);
            indiceHashtagDocumentiAtteso.set("test", ["9s5fe1"]);
            indiceHashtagDocumentiAtteso.set("settembre", ["a0", "166"]);
            indiceHashtagDocumentiAtteso.set("agosto", ["166"]);
        }

        const indiceHashtagDocumentiCalcolato = creaIndiceDeiFileRispettoAgliHashtagCheContengono(mappaDocumenti, NOME_PROP_HASHTAGS_IN_DOC);

        expect(areMappeEquivalenti(indiceHashtagDocumentiCalcolato,indiceHashtagDocumentiAtteso)).toBe(true);
    });


    test('Se viene passato un oggetto senza documenti, si attende un indice vuoto.',  async () => {

        const mappaDocumentiVuota = new Map(Object.entries({}));
        const indiceHashtagDocumentiAtteso = new Map();
        const indiceHashtagDocumentiCalcolato = creaIndiceDeiFileRispettoAgliHashtagCheContengono(mappaDocumentiVuota, NOME_PROP_HASHTAGS_IN_DOC);
        expect(indiceHashtagDocumentiCalcolato).toStrictEqual(indiceHashtagDocumentiAtteso);
    });

})