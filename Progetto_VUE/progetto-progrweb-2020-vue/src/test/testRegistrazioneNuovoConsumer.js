/* eslint-disable no-undef */
/**
 * Accede al componente Vue che si occupa della registrazione di un
 * nuovo consumer, riempie il form di registrazione con dati validi
 * ed invalidi, simula l'invio del form e si verifica il comportamento.
 *
 * Riferimenti: https://vue-test-utils.vuejs.org/guides,
 * https://jestjs.io/docs/en/mock-functions,
 */

import {mount} from "@vue/test-utils"
import RegistrazioneNuovoConsumer from '../views/login/RegistrazioneNuovoConsumer';


// Setup: falsa implementazione di window.alert
jest.spyOn(window, 'alert').mockImplementation(x => x);

// Setup: falsa implementazione di axios
let sarebberoInviatiIDatiDalForm;   // true quando si effettua una richiesta asincrona
jest.mock('axios', () => ({
    get: url => {
        sarebberoInviatiIDatiDalForm = true;
        return Promise.resolve({url: url, data: 'value'});
    },
    post: (url, data) => {
        sarebberoInviatiIDatiDalForm = true;
        return Promise.resolve(data)
    }
}));

// noinspection SpellCheckingInspection,SpellCheckingInspection,SpellCheckingInspection,SpellCheckingInspection,SpellCheckingInspection,SpellCheckingInspection,SpellCheckingInspection,SpellCheckingInspection,SpellCheckingInspection
const tabellaParametriDeiTest = [
    ["SPNFRZ63D22H5L1S","Mario Rossi","prova@example.com","pssw","pssw",true],
    ["SPNFRZ63D22H5L1S","Mario Rossi","prova@example.com","pssw","",false],
    ["SPNFRZ63D22H5L1S","Mario Rossi","a@a","pssw","pssw",true],
    ["aa","Mario Rossi","prova@example.com","pssw","pssw",false],
    ["SPNFRZ63D22H5L1S","","prova@example.com","pssw","pssw",false]
];


// noinspection SpellCheckingInspection
describe.each(tabellaParametriDeiTest)('RegistrazioneNuovoConsumer.vue',
    (codFisc, nomeCognome, email, password, confermaPassword, formRiempitoCorrettamente) => {

    const wrapper = mount(RegistrazioneNuovoConsumer);
    const formInputFields = wrapper.findAll("form input:not([type=submit])");

    test('Se esiste un campo del form non riempito, allora non si invia il form.',  async () => {
        await simulaRiempimentoFormRegistrazione();
        
        if(esisteUnCampoNonRiempito(formInputFields))
            expect(sarebberoInviatiIDatiDalForm).toStrictEqual(false);
    });

    test('Se la password inserita o quella di conferma non coincidono, allora non si invia il form.',  async () => {
        await simulaRiempimentoFormRegistrazione();

        if(password!==confermaPassword)
            expect(sarebberoInviatiIDatiDalForm).toStrictEqual(false);
    });

    test('Se il form è stato riempito correttamente, si invia il form al server.',  async () => {
        await simulaRiempimentoFormRegistrazione();
        expect(sarebberoInviatiIDatiDalForm).toStrictEqual(formRiempitoCorrettamente);
    });

    /** Simula la compilazione del form */
    async function simulaRiempimentoFormRegistrazione() {
        sarebberoInviatiIDatiDalForm = false;

        await riempiCampiForm(formInputFields, [codFisc, nomeCognome, email, password, confermaPassword]);
        await wrapper.find("form").trigger("submit");
    }

    /**Riempie i campi di input dati come array nel parametro arrayCampiForm con i valori dati in arrayCoiValoriDaInserire*/
    const riempiCampiForm = async (arrayCampiForm, arrayCoiValoriDaInserire) => {
        for(let i=0; i<arrayCampiForm.length; i++)
            arrayCampiForm[i].setValue(arrayCoiValoriDaInserire[i]);
    }

    /** Restituisce true se tutti i campi sono riempiti, altrimenti false */
    const esisteUnCampoNonRiempito = formInputFields => {
        let esisteUnCampoNonRiempito = false;
        formInputFields.forEach( inputElement =>
            esisteUnCampoNonRiempito = esisteUnCampoNonRiempito || (inputElement.element.value.length === 0)
        );
        return esisteUnCampoNonRiempito;
    }

})