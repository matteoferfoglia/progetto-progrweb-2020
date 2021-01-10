<template>
  <h2>Login > Accedi</h2>
    <Form @submit.prevent="validaEdInviaForm" @csrf-token-ricevuto="csrfToken = $event">
      <div>
      <label>Username<input type="text" v-model="username" autocomplete="off" required autofocus></label>
      <label>Password<input type="password" v-model="password" autocomplete="off" required></label>
      <input type="submit" value="Login">
      </div>
    </Form>
    <!-- TODO : VERIRICARE protezione csrf -->
</template>

<script>

/** Form viene mostrato solo dopo aver ricevuto dal server il CSRF token.*/

import {richiestaPost} from "../../utils/http";
import {getHttpResponseStatus, HTTP_STATUS_UNAUTHORIZED} from "../../utils/http";
import Form from "../../components/FormConCsrfToken";

export default {
  name: 'LoginUtenteGiaRegistrato',
  components: {Form},
  data: function () {
    return {
      username: undefined,
      password: undefined,
      csrfToken: undefined
    }
  },
  methods: {

    validaEdInviaForm() {

      const isFormValido = () => {
        return this.username.length>0 &&
            this.password.length > 0;
      };

      const informaUtenteFormInvalido = () => {
        alert("Compilare correttamente i campi del form.");
        this.password = "";
      }

      /** Se il login va a buon fine, viene impostato lo header <i>Authorization</i>
       * per le successive richieste HTTP.
       * Poi si viene reindirizzati alla pagina root di questa web application che
       * provvederà a mostrare i contenuti corretti per l'utente.
       * @param tokenAutenticazione è il token di autenticazione ricevuto.
       */
      const loginRiuscito = tokenAutenticazione => {

        const parametriRouter = {
          [process.env.VUE_APP_ROUTER_PARAMETRO_TOKEN_AUTENTICAZIONE]: tokenAutenticazione,

          // Login probabilmente richiesto perché utente ha tentato di accedere a risorsa protetta
          // Quindi aggiungo tutti i params che mi arrivano su routes, così l'utente può riprende
          // a lavorare da dove si era interrotto prima del login:  // TODO : un oggetto simile è creato anche nell'index di router: estrarre un metodo che faccia solo questo !
          [process.env.VUE_APP_ROUTER_PARAMETRO_FULLPATH_ROUTE_RICHIESTA_PRIMA]: this.$route.params[process.env.VUE_APP_ROUTER_PARAMETRO_FULLPATH_ROUTE_RICHIESTA_PRIMA],
          [process.env.VUE_APP_ROUTER_PARAMETRO_PARAMS_ROUTE_RICHIESTA_PRIMA]: JSON.stringify(this.$route.params[process.env.VUE_APP_ROUTER_PARAMETRO_PARAMS_ROUTE_RICHIESTA_PRIMA])
        };

        this.$router.push({ // inoltro con parametri
          name   : process.env.VUE_APP_ROUTER_NOME_COMPONENTE_AREA_RISERVATA,
          params : parametriRouter
        });

      }

      const loginFallito = risposta => {
        if(getHttpResponseStatus(risposta) === HTTP_STATUS_UNAUTHORIZED) {
          alert("Credenziali invalide");
          this.password = "";   // pulisci campo password
        } else {
          alert("Errore imprevisto durante l'autenticazione, riprovare in seguito.")
        }

        console.error("Errore durante l'autenticazione (" + risposta.status + " [" + risposta.statusText + "])");
        // TODO notificare l'errore ai gestori (via mail ?)
        // TODO : refactoring : questo metodo c'è anche in RegistrazioneNuovoConsumer

      }

      const inviaForm = () => {

        // TODO: refactoring: questo stesso metodo c'è anche nel form di registrazione nuovo consumer: potrebbe essere astratto per evitare duplicazione di codice, creando un metodo che riceve l' oggetto campiFormDaInviareAlServer e l' indirizzo del server, quindi provvede all' invio.

        const campiFormDaInviareAlServer = {
          [process.env.VUE_APP_LOGIN_USERNAME_INPUT_FIELD_NAME] : this.username,
          [process.env.VUE_APP_LOGIN_PASSWORD_INPUT_FIELD_NAME] : this.password,
          [process.env.VUE_APP_CSRF_INPUT_FIELD_NAME] : this.csrfToken
        };

        richiestaPost(process.env.VUE_APP_LOGIN_SERVER_URL, campiFormDaInviareAlServer)
            .then( risposta => loginRiuscito(risposta.data) )     // risposta.data contiene il token di autenticazione
            .catch(risposta => loginFallito(risposta) );          // risposta contiene response, status e statusText (Fonte: https://stackoverflow.com/a/39153411)
      };


      if(isFormValido()){
        inviaForm();
      } else {
        informaUtenteFormInvalido();
        // TODO : si potrebbe evidenziare i campi invalidi
      }

    }
  }
}

</script>

<style>
</style>
