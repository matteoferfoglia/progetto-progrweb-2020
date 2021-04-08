<template>
  <div class="card card-autenticazione" v-show="csrfToken">
    <h2 class="card-header">Login</h2>
    <Form class="card-body d-flex justify-content-between flex-wrap"
          @submit="validaEdInviaForm"
          @csrf-token-ricevuto="aggiornaCsrfToken($event)">
      <label>Username
        <input type="text"
               class="form-control"
               v-model="username"
               autocomplete="off"
               required
               autofocus>
      </label>
      <label>Password
        <input type="password"
               class="form-control"
               v-model="password"
               autocomplete="off"
               required>
      </label>
      <input type="submit" value="Login" class="btn btn-primary order-1">
      <button @click.prevent="richiediResetPassword"
              class="btn btn-link">
        Password dimenticata
      </button>
    </Form>
  </div>

  <AutenticazioneFirebase @login-riuscito="loginRiuscito"
                          @login-fallito="loginFallito"  />


</template>

<script>

/** Form viene mostrato solo dopo aver ricevuto dal server il CSRF token.*/

import {richiestaPost} from "../../utils/http";
import {getHttpResponseStatus, HTTP_STATUS_UNAUTHORIZED} from "../../utils/http";
import Form from "../../components/layout/FormConCsrfToken";
import AutenticazioneFirebase from "../../components/autenticazione/AutenticazioneFirebase";

export default {
  name: 'LoginUtenteGiaRegistrato',
  emits: [
    /** Evento emesso quando viene ricevuto il token CSRF dal server.*/
    'csrf-token-ricevuto',

    /** Evento emesso se la procedura di login va a buon fine.*/
    'login'
  ],
  components: {AutenticazioneFirebase, Form},
  data: function () {
    return {
      username: undefined,
      password: undefined,
      csrfToken: undefined
    }
  },
  methods: {

    /** Richiede al server il reset della password.*/
    richiediResetPassword() {

      if( this.username )
        richiestaPost(process.env.VUE_APP_URL_RESET_PASSWORD,
            {[process.env.VUE_APP_FORM_USERNAME_INPUT_FIELD_NAME]: this.username})
            .then( alert )  // alert con la risposta del server
            .catch( risposta => console.log("ERRORE: " + risposta) );
      else
        alert("Inserire lo username");

    },

    aggiornaCsrfToken(nuovoValore) {
      this.csrfToken = nuovoValore;
      this.$emit('csrf-token-ricevuto', nuovoValore);
    },

    /** Se il login va a buon fine, bisogna impostare lo header <i>Authorization</i>
     * per le successive richieste HTTP e reindirizzare alla schermata principale.
     * @param tokenAutenticazione è il token di autenticazione ricevuto.
     */
    loginRiuscito(tokenAutenticazione) {

      const parametriRouter = {
        [process.env.VUE_APP_ROUTER_PARAMETRO_TOKEN_AUTENTICAZIONE]: tokenAutenticazione
      };
      this.$emit('login', parametriRouter);

    },

    /** Gestisce il caso in cui il login <strong>non<strong> va a buon fine.
     * @param risposta Risposta d'errore ricevuta dal server. */
    loginFallito(risposta){
      if(getHttpResponseStatus(risposta) === HTTP_STATUS_UNAUTHORIZED) {
        alert("Credenziali invalide");
        this.password = "";   // pulisci campo password
      }

      console.error("Errore durante l'autenticazione (" + risposta.status + " [" + risposta.statusText + "])");

    },

    validaEdInviaForm() {

      const isFormValido = () => {
        return this.username.length>0 &&
            this.password.length > 0;
      };

      const informaUtenteFormInvalido = () => {
        alert("Compilare correttamente i campi del form.");
        this.password = "";
      }

      const inviaForm = () => {

        const campiFormDaInviareAlServer = {
          [process.env.VUE_APP_FORM_USERNAME_INPUT_FIELD_NAME] : this.username,
          [process.env.VUE_APP_FORM_PASSWORD_INPUT_FIELD_NAME] : this.password,
          [process.env.VUE_APP_FORM_CSRF_INPUT_FIELD_NAME] : this.csrfToken
        };

        richiestaPost(process.env.VUE_APP_URL_LOGIN, campiFormDaInviareAlServer)
            .then( risposta => this.loginRiuscito(risposta) )     // risposta contiene il token di autenticazione
            .catch(risposta => this.loginFallito(risposta) );          // risposta contiene response, status e statusText (Fonte: https://stackoverflow.com/a/39153411)
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

<style scoped></style>