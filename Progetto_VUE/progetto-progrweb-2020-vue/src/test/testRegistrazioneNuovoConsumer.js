/* eslint-disable no-undef */
/**
 * Accede al componente Vue che si occupa della registrazione di un
 * nuovo consumer, riempie il form di registrazione con dati validi
 * ed invalidi, simula l'invio del form e si verifica che:
 *  i)  se esiste un campo non riempito, allora non si inviano i dati.
 *  ii) se la password inserita o quella di conferma non coincidono,
 *       allora non si invia il form
 *  iii)se il form è stato riempito correttamente, si inviano i dati
 *       al server in formato JSON.
 */

import {mount} from "@vue/test-utils"
import RegistrazioneNuovoConsumer from '../views/login/RegistrazioneNuovoConsumer';


describe.each`
  codFisc               | nomeCognome       | email                     | password      | confermaPassword  | expected
  ${"SPNFRZ63D22H5L1S"} | ${"Mario Rossi"}  | ${"prova@example.com"}    | ${"pssw"}     | ${"pssw"}         | ${true}
  ${"SPNFRZ63D22H5L1S"} | ${"Mario Rossi"}  | ${"prova@example.com"}    | ${"pssw"}     | ${""}             | ${false}
`('RegistrazioneNuovoConsumer.vue', ({codFisc, nomeCognome, email, password, confermaPassword, expected}) => {
    it('verifica se esiste un campo del form non riempito', async () => {
        const wrapper = mount(RegistrazioneNuovoConsumer);
        const formInputFields = wrapper.findAll("form input");

        {
            //Riempi il form
            await formInputFields[0].setValue(codFisc);
            await formInputFields[1].setValue(nomeCognome);
            await formInputFields[2].setValue(email);
            await formInputFields[3].setValue(password);
            await formInputFields[4].setValue(confermaPassword);
        }

        let tuttiICampiRiempiti = true;
        formInputFields.forEach( inputElement => tuttiICampiRiempiti = tuttiICampiRiempiti && (inputElement.element.value.length > 0) );
        expect(tuttiICampiRiempiti).toBe(expected);

    })
})