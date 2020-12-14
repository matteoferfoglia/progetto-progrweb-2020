<template>
  <h2>Login > Registrati</h2>

  <form @submit.prevent="validaEdInviaForm">
    <label>Codice fiscale<input type="text" v-model="codiceFiscale" autocomplete="off" placeholder="Codice fiscale" :pattern="`${REGEX_CODICE_FISCALE}`" required autofocus></label>
    <label>Nome e cognome<input type="text" v-model="nomeCognome" autocomplete="off" placeholder="Nome e cognome" maxlength="100" required></label>
    <label>Email<input type="email" v-model="email" autocomplete="off" placeholder="xxxxxx@example.com" maxlength="100" required></label>
    <label>Password<input type="password" v-model="password" autocomplete="off" maxlength="100" required></label>
    <label>Conferma password<input type="password" v-model="confermaPassword" autocomplete="off" maxlength="100" required></label>
    <input type="submit" value="Registrati">
  </form>
</template>

<script>

import axios from "axios";

export default {
  name: 'RegistrazioneNuovoConsumer',
  data() {
    return {
      codiceFiscale: "",
      nomeCognome: "",
      email: "",
      password: "",
      confermaPassword: "",
      REGEX_CODICE_FISCALE: "^([A-Za-z]{6}[0-9lmnpqrstuvLMNPQRSTUV]{2}[abcdehlmprstABCDEHLMPRST]{1}[0-9lmnpqrstuvLMNPQRSTUV]{2}[A-Za-z]{1}[0-9lmnpqrstuvLMNPQRSTUV]{3}[A-Za-z]{1})|([0-9]{11})$"  // TODO : verificare correttazza RegEx del codice fiscale, tratto da: https://regexlib.com/(A(CAg_Bth78XI7Ry2C7vo_2HR3yuG9GuP1YeCHLd1AH53pIpI-z7JHENTvKnhDjJJhOkjyka4kah-CTZaupEY3MAVWa6qYC256houEBaNXoG01))/UserPatterns.aspx?authorId=10d43491-1297-481f-ae66-db9f2263575c&AspxAutoDetectCookieSupport=1
    }
  },
  methods: {
    validaEdInviaForm() {

      let isFormValido = () => {
        // altri campi dovrebbero essere controllati dallo user-agent ("the user agent will check the user's input before the form is submitted", https://html.spec.whatwg.org/multipage/forms.html#client-side-form-validation)
        return this.password===this.confermaPassword && this.password.length > 0;
      };

      let inviaForm = () => {

        let campiFormDaInviareAlServer = {};
        {
          campiFormDaInviareAlServer[process.env.VUE_APP_SIGNUP_CONSUMER_CODFISC_INPUT_FIELD_NAME] = this.codiceFiscale;
          campiFormDaInviareAlServer[process.env.VUE_APP_SIGNUP_CONSUMER_NOMECOGNOME_INPUT_FIELD_NAME] = this.nomeCognome;
          campiFormDaInviareAlServer[process.env.VUE_APP_SIGNUP_CONSUMER_EMAIL_INPUT_FIELD_NAME] = this.email;
          campiFormDaInviareAlServer[process.env.VUE_APP_SIGNUP_CONSUMER_PASSWORD_INPUT_FIELD_NAME] = this.password;
        }

        axios.post(process.env.VUE_APP_SIGNUP_CONSUMER_SERVER_URL, campiFormDaInviareAlServer)
          .then(ris => console.log(ris))     // TODO: fai qualcosa qui, serve questo then?
          .catch(ris => {
            console.error("Errore durante la registrazione: " + ris);
            alert("Si è verificato un errore durante la registrazione. Riprovare in seguito.");

            (() => { console.error(ris) })(); // TODO notificare l'errore ai gestori (via mail ?)
          });
      };

      let informaUtenteFormInvalido = () => {
        alert("Le password inserite sono diverse.");
        this.confermaPassword = "";
      }


      if(isFormValido()){
        inviaForm();
      } else {
        informaUtenteFormInvalido();
      }

    }
  }
}

</script>

<style>
</style>