<template>
  <h2>Login > Accedi</h2>
    <form v-if="isCsrfTokenCaricato" @submit.prevent="validaEdInviaForm"> <!-- TODO : VERIRICARE protezione csrf -->
      <label>Username<input type="text" v-model="username" autocomplete="off" required autofocus></label>
      <label>Password<input type="password" v-model="password" autocomplete="off" required></label>
      <input type="hidden"  style="display: none" v-model="csrfToken">
      <input type="submit" value="Login">
    </form>
</template>

<script>

/** Form viene mostrato solo dopo aver ricevuto dal server il CSRF token.*/

import {richiediCSRFTokenAlServer} from "../../utils/CSRF";
import {richiestaPost} from "../../utils/httpUtils";
import {getHttpResponseStatus, HTTP_STATUS_UNAUTHORIZED} from "../../utils/httpUtils";

export default {
  name: 'LoginUtenteGiaRegistrato',
  data: function () {
    return {
      username: undefined,
      password: undefined,
      csrfToken: undefined,
      isCsrfTokenCaricato: false
    }
  },
  created() {
    // dati già disponibili e modificabili in questo hook

    richiediCSRFTokenAlServer().then( valoreToken => {
      // Se qui, allora token csrf ricevuto dal server
      this.csrfToken = valoreToken;
      this.isCsrfTokenCaricato = true;
    }).catch( errore => {
      // Errore durante la ricezione del token csrf
      console.error("Errore in " +
          /*Nome componente: */ this.$options.name + ": " + errore);  // TODO : gestire questo errore
    });

  },
  methods: {
    validaEdInviaForm() {

      // TODO : TESTARE e provare attacchi csrf

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

        const parametriRouterPush = {};   // oggetto con i parametri utilizzati da Vue Router
        parametriRouterPush[process.env.VUE_APP_ROUTER_PARAMETRO_TOKEN_AUTENTICAZIONE] = tokenAutenticazione;
        this.$router.push({
          name   : process.env.VUE_APP_ROUTER_NOME_COMPONENTE_SCHERMATA_INIZIALE,
          params : parametriRouterPush
        });  // redirect a componente root con parametro il token di autenticazione

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

        let campiFormDaInviareAlServer = {};
        {
          campiFormDaInviareAlServer[process.env.VUE_APP_LOGIN_USERNAME_INPUT_FIELD_NAME] = this.username;
          campiFormDaInviareAlServer[process.env.VUE_APP_LOGIN_PASSWORD_INPUT_FIELD_NAME] = this.password;
          campiFormDaInviareAlServer[process.env.VUE_APP_CSRF_INPUT_FIELD_NAME] = this.csrfToken;
        }

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
