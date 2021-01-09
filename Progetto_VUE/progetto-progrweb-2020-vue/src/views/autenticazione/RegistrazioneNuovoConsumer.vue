<template>
  <h2>Login > Registrati</h2>

  <form @submit.prevent="validaEdInviaForm">
    <label>Codice fiscale<input type="text" v-model="codiceFiscale" autocomplete="off" placeholder="Codice fiscale" :pattern="REGEX_CODICE_FISCALE" required autofocus></label>
    <label>Nome e cognome<input type="text" v-model="nomeCognome" autocomplete="off" placeholder="Nome e cognome" maxlength="100" required></label>
    <label>Email<input type="email" v-model="email" autocomplete="off" placeholder="xxxxxx@example.com" maxlength="100" required></label>
    <label>Password<input type="password" v-model="password" autocomplete="off" maxlength="100" required></label>
    <label>Conferma password<input type="password" v-model="confermaPassword" autocomplete="off" maxlength="100" required></label>
    <input type="submit" value="Registrati">
  </form>
</template>

<script>

// TODO in caso di errore (input inserito dall'utente non valido) evidenziare la casella di input invalida (selector css ":invalid")

import {richiestaPost} from "../../utils/http";

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
      REGEX_EMAIL: process.env.VUE_APP_REGEX_EMAIL
    }
  },
  methods: {
    validaEdInviaForm() {

      // TODO : questo metodo è presente anche nel form di autenticazione: refactoring per evitare duplicazione!

      const isFormValido = () => {
        return this.nomeCognome.length>0 &&
            RegExp(this.REGEX_EMAIL).test(this.email) &&
            RegExp(this.REGEX_CODICE_FISCALE).test(this.codiceFiscale) &&
            this.password===this.confermaPassword &&
            this.password.length > 0;
      };

      const informaUtenteFormInvalido = () => {
        alert("Compilare correttamente i campi del form.");
        // TODO: migliorare messaggio dell' alert, magari evidenziare con CSS i campi input invalidi e mostrare il popup con l'errore specifico.
        this.confermaPassword = "";
      }

      const registrazioneCompletata = () => this.$router.push({name: process.env.VUE_APP_ROUTER_NOME_COMPONENTE_SCHERMATA_INIZIALE});   // redirect a schermata iniziale // TODO : cosa bisogna fare qui ?

      const registrazioneFallita = ris => {
        console.error("Errore durante la registrazione: " + ris);
        alert("Si è verificato un errore durante la registrazione. Riprovare in seguito.");
        // TODO notificare l'errore ai gestori (via mail ?)
      }

      const inviaForm = () => {

        let campiFormDaInviareAlServer = {
          [process.env.VUE_APP_REGISTRAZIONE_CONSUMER_CODFISC_INPUT_FIELD_NAME] : this.codiceFiscale,
          [process.env.VUE_APP_REGISTRAZIONE_CONSUMER_NOMECOGNOME_INPUT_FIELD_NAME] : this.nomeCognome,
          [process.env.VUE_APP_REGISTRAZIONE_CONSUMER_EMAIL_INPUT_FIELD_NAME] : this.email,
          [process.env.VUE_APP_REGISTRAZIONE_CONSUMER_PASSWORD_INPUT_FIELD_NAME] : this.password
        }

        richiestaPost(process.env.VUE_APP_REGISTRAZIONE_CONSUMER_SERVER_URL, campiFormDaInviareAlServer)
            .then(  ()       => registrazioneCompletata() )
            .catch( risposta => registrazioneFallita(risposta.data) );
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