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

import axios from "axios";
import {richiediCSRFTokenAlServer} from "../../utils/CSRF";

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
      console.error("Errore in " + this.$options.name + ": " + errore);  // TODO : gestire questo errore
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

      const loginRiuscito = () => this.$router.push(process.env.VUE_APP_ROUTER_ROOT_PATH);

      const loginFallito = response => {
        const STATO_RISPOSTA_CREDENZIALI_INVALIDE = 401;
        if(response.status === STATO_RISPOSTA_CREDENZIALI_INVALIDE)
          alert("Credenziali invalide");
        else
          alert("Errore imprevisto durante l'autenticazione, riprovare in seguito.")

        console.error("Errore durante l'autenticazione (" + response.status + " [" + response.statusText + "])");
        // TODO notificare l'errore ai gestori (via mail ?)
        // TODO : refactoring : questo metodo c'è anche in RegistrazioneNuovoConsumer

        this.$router.go(0);  // refresh della pagina (fonte: https://stackoverflow.com/a/47005895)
      }

      const inviaForm = () => {

        // TODO: refactoring: questo stesso metodo c'è anche nel form di registrazione nuovo consumer: potrebbe essere astratto per evitare duplicazione di codice, creando un metodo che riceve l' oggetto campiFormDaInviareAlServer e l' indirizzo del server, quindi provvede all' invio.

        let campiFormDaInviareAlServer = {};
        {
          campiFormDaInviareAlServer[process.env.VUE_APP_LOGIN_USERNAME_INPUT_FIELD_NAME] = this.username;
          campiFormDaInviareAlServer[process.env.VUE_APP_LOGIN_PASSWORD_INPUT_FIELD_NAME] = this.password;
          campiFormDaInviareAlServer[process.env.VUE_APP_CSRF_INPUT_FIELD_NAME] = this.csrfToken;
        }

        axios.post(process.env.VUE_APP_LOGIN_SERVER_URL, campiFormDaInviareAlServer)
            .then(() =>   loginRiuscito() )
            .catch(ris => loginFallito(ris.response) );
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
