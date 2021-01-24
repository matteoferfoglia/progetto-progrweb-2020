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
import RegistrazioneNuovoConsumer from '../views/autenticazione/RegistrazioneNuovoConsumer';


// Setup: falsa implementazione di window.alert
jest.spyOn(window, 'alert').mockImplementation(x => x);

// Setup: falsa implementazione di axios
let mockSarebberoInviatiIDatiDalForm_varGlobale;   // true quando si effettua una richiesta asincrona
let mockUrl_varGlobale, mockDatiInviati_varGlobale;           // url e data della richiesta asincrona
jest.mock('axios', () => ({
    get: url => {
        mockSarebberoInviatiIDatiDalForm_varGlobale = true;
        mockUrl_varGlobale = url;
        mockDatiInviati_varGlobale = null;
        return Promise.resolve({url: url, data: 'value'});
    },
    post: (url, data) => {
        mockSarebberoInviatiIDatiDalForm_varGlobale = true;
        mockUrl_varGlobale = url;
        mockDatiInviati_varGlobale = data;
        return Promise.resolve(data);
    }
}));

// noinspection SpellCheckingInspection
const tabellaParametriDeiTest = [
    ["SPNFRZ63D22H5L1S","Mario Rossi","prova@example.com","pssw","pssw",true],
    ["SPNFRZ63D22H5L1S","Mario Rossi","prova@example.com","pssw","",false],
    ["SPNFRZ63D22H5L1S","Mario Rossi","a@a","pssw","pssw",true],
    ["aa","Mario Rossi","prova@example.com","pssw","pssw",false],
    ["SPNFRZ63D22H5L1S","","prova@example.com","pssw","pssw",false]
];


// noinspection SpellCheckingInspection
describe.each(tabellaParametriDeiTest)('RegistrazioneNuovoConsumer.vue',
    (codFisc, nominativo, email, password, confermaPassword, formRiempitoCorrettamente) => {

    const wrapper = mount(RegistrazioneNuovoConsumer, {
        global: {
            mocks: {
                $router: {          //https://vue-test-utils.vuejs.org/v2/guide/vue-router.html#using-a-mocked-router
                    push: jest.fn()
                }
            }
        }
    });
    const formInputFields = wrapper.findAll("form input:not([type=submit])");

    test('Se esiste un campo del form non riempito, allora non si invia il form.',  async () => {
        await simulaRiempimentoFormRegistrazioneESubmit();
        
        if(esisteUnCampoNonRiempito(formInputFields))
            expect(mockSarebberoInviatiIDatiDalForm_varGlobale).toStrictEqual(false);
    });

    test('Se la password inserita o quella di conferma non coincidono, allora non si invia il form.',  async () => {
        await simulaRiempimentoFormRegistrazioneESubmit();

        if(password!==confermaPassword)
            expect(mockSarebberoInviatiIDatiDalForm_varGlobale).toStrictEqual(false);
    });

    test('Se il form è stato riempito correttamente, si invia il form al server.',  async () => {
        await simulaRiempimentoFormRegistrazioneESubmit();
        expect(mockSarebberoInviatiIDatiDalForm_varGlobale).toStrictEqual(formRiempitoCorrettamente);
    });

        // TODO test se, dopo che viene fatto submit nel form, al server arrivano i dati corretti (usando jest.mock)
    /** Simula il server. */
    test('Verifica dei dati ricevuti dal server.',  async () => {

        await simulaRiempimentoFormRegistrazioneESubmit();

        if(formRiempitoCorrettamente) {

            const urlAzioneForm = mockUrl_varGlobale;
            const datiJSONInviati = JSON.stringify(mockDatiInviati_varGlobale);

            const datiAttesi = {
                [process.env.VUE_APP_FORM_CODFISC_INPUT_FIELD_NAME]     : codFisc,
                [process.env.VUE_APP_FORM_NOMINATIVO_INPUT_FIELD_NAME] : nominativo,
                [process.env.VUE_APP_FORM_EMAIL_INPUT_FIELD_NAME]       : email,
                [process.env.VUE_APP_FORM_PASSWORD_INPUT_FIELD_NAME]    : password
            }
            const datiJSONAttesi = JSON.stringify(datiAttesi);

            // console.log("Inviato: \t{url=" + urlAzioneForm + ", dati=" + datiJSONInviati + " }");
            // console.log("Atteso: \t{url=" + process.env.VUE_APP_URL_REGISTRAZIONE_CONSUMER + ", dati=" + datiJSONAttesi + " }");

            expect(datiJSONInviati).toStrictEqual(datiJSONAttesi);
            expect(urlAzioneForm).toStrictEqual(process.env.VUE_APP_URL_REGISTRAZIONE_CONSUMER);

        }

    });



    /** Simula la compilazione del form */
    async function simulaRiempimentoFormRegistrazioneESubmit() {
        mockSarebberoInviatiIDatiDalForm_varGlobale = false;
        mockUrl_varGlobale = undefined;
        mockDatiInviati_varGlobale = undefined;

        await riempiCampiForm(formInputFields, [codFisc, nominativo, email, password, confermaPassword]);
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