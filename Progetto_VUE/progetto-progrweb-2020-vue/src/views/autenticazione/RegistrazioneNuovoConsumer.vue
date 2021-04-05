<template>

  <div class="card card-autenticazione">
    <h2 class="card-header">Registrazione consumer</h2>
    <form class="card-body"
          @submit.prevent="validaEdInviaForm">

      <p class="form-group row"><label>Codice fiscale
        <input type="text"
               v-model="codiceFiscale"
               autocomplete="off"
               placeholder="Codice fiscale"
               :pattern="REGEX_CODICE_FISCALE"
               class="form-control"
               required
               autofocus>
      </label></p>
      <p class="form-group row"><label>Nome e cognome
        <input type="text"
               v-model="nominativo"
               autocomplete="off"
               placeholder="Nome e cognome"
               maxlength="100"
               class="form-control"
               required>
      </label></p>
      <p class="form-group row"><label>Email
        <input type="email"
               v-model="email"
               autocomplete="off"
               placeholder="xxxxxx@example.com"
               maxlength="100"
               :pattern="REGEX_EMAIL"
               class="form-control"
               required>
      </label></p>
      <div  class="form-group row d-flex justify-content-between flex-nowrap">
        <p><label>Password
          <input type="password"
                 v-model="password"
                 autocomplete="off"
                 placeholder="Password"
                 maxlength="100"
                 class="form-control"
                 required>
        </label></p>
        <p><label>Conferma password
          <input type="password"
                 v-model="confermaPassword"
                 autocomplete="off"
                 placeholder="Conferma password"
                 maxlength="100"
                 class="form-control"
                 required>
        </label></p>
      </div>

      <div class="d-flex justify-content-center">
        <input type="submit" value="Registrati" class="btn btn-primary">
        <input type="reset" value="Reset" class="btn btn-secondary">
      </div>

    </form>
  </div>

</template>

<script>

import {getHttpResponseStatus, HTTP_STATUS_CONFLICT, richiestaPost} from "@/utils/http";

export default {
  name: 'RegistrazioneNuovoConsumer',
  inheritAttrs: false,
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

      const registrazioneCompletata = risposta => {
        alert(risposta);
        this.$router.push({path: process.env.VUE_APP_ROUTER_NOME_COMPONENTE_AREA_RISERVATA});
      }

      const registrazioneFallita = ris => {
        console.error( "Errore durante la registrazione: " + JSON.stringify(ris) );

        if( getHttpResponseStatus( ris ) === HTTP_STATUS_CONFLICT ) {
          alert( ris.data );
        } else {
          alert("Si è verificato un errore durante la registrazione. Riprovare in seguito.");
        }
      }

      const inviaForm = () => {

        const campiFormDaInviareAlServer = {
          [process.env.VUE_APP_FORM_CODFISC_INPUT_FIELD_NAME]    : this.codiceFiscale,
          [process.env.VUE_APP_FORM_NOMINATIVO_INPUT_FIELD_NAME] : this.nominativo,
          [process.env.VUE_APP_FORM_EMAIL_INPUT_FIELD_NAME]      : this.email,
          [process.env.VUE_APP_FORM_PASSWORD_INPUT_FIELD_NAME]   : this.password
        }

        richiestaPost(process.env.VUE_APP_URL_REGISTRAZIONE_CONSUMER, campiFormDaInviareAlServer)
            .then(  risposta => registrazioneCompletata(risposta) )
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

<style scoped></style>