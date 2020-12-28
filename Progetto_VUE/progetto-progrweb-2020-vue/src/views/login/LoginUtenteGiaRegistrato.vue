<template>
  <h2>Login > Accedi</h2>
  <div v-if="isCaricato">
    <form @submit.prevent="validaEdInviaForm"> <!-- TODO: action del form da fare -->
      <label>Username<input type="text" v-model="username" autocomplete="off" required autofocus></label>
      <label>Password<input type="password" v-model="password" autocomplete="off" required></label>
      <input type="hidden" v-model="CSRF_token">
      <input type="submit" value="Login">
    </form>
  </div>
  <div v-else></div>
</template>

<script>

import axios from "axios";
import {richiediCSRFTokenAlServer} from "../../utils/CSRF";

export default {
  name: 'LoginUtenteGiaRegistrato',
  data() {
    return {
      username: "",
      password: "",
      isCaricato: false,  // modificato al termine della promise per il CSRF token
      CSRF_token: ""
    }
  },
  created() {
    this.CSRF_token = richiediCSRFTokenAlServer().then( valoreToken => {
      // "rendering is a synchronous process, it can’t wait for asynchronous code to finish"
      // Se qui, allora token csrf ricevuto
        this.isCaricato = true;
        this.CSRF_token = valoreToken;
      }).catch( errore => {
      // Errore durante la ricezione del token csrf
      console.error("Errore in " + this.$options.name + ": " + errore);  // TODO : gestire questo errore
    });
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

      const loginRiuscito = () => this.$router.push("/");

      const loginFallito = ris => {
        console.error("Errore durante il login: " + ris);
        alert("Si è verificato un errore durante il login. Riprovare in seguito.");
        // TODO notificare l'errore ai gestori (via mail ?)
      }

      const inviaForm = () => {

        // TODO: refactoring: questo stesso metodo c'è anche nel form di registrazione nuovo consumer: potrebbe essere astratto per evitare duplicazione di codice, creando un metodo che riceve l' oggetto campiFormDaInviareAlServer e l' indirizzo del server, quindi provvede all' invio.

        let campiFormDaInviareAlServer = {};
        {
          campiFormDaInviareAlServer[process.env.VUE_APP_LOGIN_USERNAME_INPUT_FIELD_NAME] = this.username;
          campiFormDaInviareAlServer[process.env.VUE_APP_LOGIN_PASSWORD_INPUT_FIELD_NAME] = this.password;
          campiFormDaInviareAlServer[process.env.VUE_APP_CSRF_INPUT_FIELD_NAME] = this.CSRF_token;
        }

        axios.post(process.env.VUE_APP_LOGIN_SERVER_URL, campiFormDaInviareAlServer)
            .then(() =>   loginRiuscito() )
            .catch(ris => loginFallito(ris) );
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
