<template>
  <h2>Login > Registrati</h2>

  <form @submit.prevent="validaEdInviaForm">
    <label>Codice fiscale<input type="text" v-model="codiceFiscale" autocomplete="off" placeholder="Codice fiscale" :pattern="REGEX_CODICE_FISCALE" required autofocus></label>
    <label>Nome e cognome<input type="text" v-model="nominativo" autocomplete="off" placeholder="Nome e cognome" maxlength="100" required></label>
    <label>Email<input type="email" v-model="email" autocomplete="off" placeholder="xxxxxx@example.com" maxlength="100" :pattern="REGEX_EMAIL" required></label>
    <label>Password<input type="password" v-model="password" autocomplete="off" maxlength="100" required></label>
    <label>Conferma password<input type="password" v-model="confermaPassword" autocomplete="off" maxlength="100" required></label>
    <input type="submit" value="Registrati">
    <input type="reset" value="Reset">
  </form>
</template>

<script>

// TODO in caso di errore (input inserito dall'utente non valido) evidenziare la casella di input invalida (selector css ":invalid")

import {getHttpResponseStatus, HTTP_STATUS_CONFLICT, richiestaPost} from "../../utils/http";

export default {
  name: 'RegistrazioneNuovoConsumer',
  data() {
    return {
      codiceFiscale: "",
      nominativo: "",
      email: "",
      password: "",
      confermaPassword: "",
      REGEX_CODICE_FISCALE: process.env.VUE_APP_REGEX_CODICE_FISCALE,
      REGEX_EMAIL: process.env.VUE_APP_REGEX_EMAIL
    }
  },
  methods: {
    validaEdInviaForm() {

      // TODO : questo metodo è presente anche nel form di autenticazione: refactoring per evitare duplicazione!

      const isFormValido = () => {
        return this.nominativo.length>0 &&
            RegExp(this.REGEX_EMAIL).test(this.email) &&
            RegExp(this.REGEX_CODICE_FISCALE).test(this.codiceFiscale) &&
            this.password===this.confermaPassword &&
            this.password.length > 0;
      };

      const informaUtenteFormInvalido = () => {
        alert("Compilare correttamente i campi del form.");
        this.confermaPassword = "";
      }

      const registrazioneCompletata = () => this.$router.push({path: process.env.VUE_APP_ROUTER_NOME_COMPONENTE_AREA_RISERVATA});

      const registrazioneFallita = ris => {
        console.error( "Errore durante la registrazione: " + JSON.stringify(ris) );

        if( getHttpResponseStatus( ris ) === HTTP_STATUS_CONFLICT ) {
          alert( ris.data )
        } else {
          alert("Si è verificato un errore durante la registrazione. Riprovare in seguito.");
        }
      }

      const inviaForm = () => {

        let campiFormDaInviareAlServer = {
          [process.env.VUE_APP_REGISTRAZIONE_CONSUMER_CODFISC_INPUT_FIELD_NAME] : this.codiceFiscale,
          [process.env.VUE_APP_REGISTRAZIONE_CONSUMER_NOMINATIVO_INPUT_FIELD_NAME] : this.nominativo,
          [process.env.VUE_APP_REGISTRAZIONE_CONSUMER_EMAIL_INPUT_FIELD_NAME] : this.email,
          [process.env.VUE_APP_REGISTRAZIONE_CONSUMER_PASSWORD_INPUT_FIELD_NAME] : this.password
        }

        richiestaPost(process.env.VUE_APP_REGISTRAZIONE_CONSUMER_SERVER_URL, campiFormDaInviareAlServer)
            .then(  ()       => registrazioneCompletata() )
            .catch( risposta => registrazioneFallita(risposta) );
      };


      if(isFormValido())
        inviaForm();
      else
        informaUtenteFormInvalido();

    }
  }
}

</script>

<style>
</style>