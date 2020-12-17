<template>
  <h2>Login > Registrati</h2>

  <form @submit.prevent="validaEdInviaForm" ref="form">
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
      REGEX_CODICE_FISCALE: process.env.VUE_APP_REGEX_CODICE_FISCALE,
      REGEX_MAIL: process.env.VUE_APP_REGEX_MAIL
    }
  },
  methods: {
    validaEdInviaForm() {

      const isFormValido = () => {
        return this.nomeCognome.length>0 &&
            RegExp(this.REGEX_MAIL).test(this.email) &&
            RegExp(this.REGEX_CODICE_FISCALE).test(this.codiceFiscale) &&
            this.password===this.confermaPassword &&
            this.password.length > 0;
      };

      const informaUtenteFormInvalido = () => {
        alert("Le password inserite sono diverse.");
        this.confermaPassword = "";
      }

      const registrazioneCompletata = () => this.$router.push("/");

      const registrazioneFallita = ris => {
        console.error("Errore durante la registrazione: " + ris);
        alert("Si è verificato un errore durante la registrazione. Riprovare in seguito.");
        // TODO notificare l'errore ai gestori (via mail ?)
      }

      const inviaForm = () => {

        let campiFormDaInviareAlServer = {};
        {
          campiFormDaInviareAlServer[process.env.VUE_APP_SIGNUP_CONSUMER_CODFISC_INPUT_FIELD_NAME] = this.codiceFiscale;
          campiFormDaInviareAlServer[process.env.VUE_APP_SIGNUP_CONSUMER_NOMECOGNOME_INPUT_FIELD_NAME] = this.nomeCognome;
          campiFormDaInviareAlServer[process.env.VUE_APP_SIGNUP_CONSUMER_EMAIL_INPUT_FIELD_NAME] = this.email;
          campiFormDaInviareAlServer[process.env.VUE_APP_SIGNUP_CONSUMER_PASSWORD_INPUT_FIELD_NAME] = this.password;
        }

        axios.post(process.env.VUE_APP_SIGNUP_CONSUMER_SERVER_URL, campiFormDaInviareAlServer)
            .then(() =>   registrazioneCompletata() )
            .catch(ris => registrazioneFallita(ris) );
      };



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