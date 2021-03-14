<template>

  <header>



    <nav class="navbar navbar-expand-lg navbar-dark bg-dark">

      <router-link :to="{name: NOME_ROOT_APPLICAZIONE}" class="navbar-brand">
        <img class="logo d-inline-block align-top" :src="URL_LOGO_APPLICAZIONE" alt=""/>
        <span class="nome-applicazione">{{NOME_APPLICAZIONE}}</span>
      </router-link>

      <button v-if="isAutenticato()" class="navbar-toggler" type="button" data-toggle="collapse" data-target="#navbarSupportedContent" aria-controls="navbarSupportedContent" aria-expanded="false" aria-label="Toggle navigation">
        <span class="navbar-toggler-icon"></span>
      </button>

      <div class="collapse navbar-collapse" id="navbarSupportedContent" v-if="isAutenticato()">
        <p class="opzioni-utente-header mx-auto">{{ nomeUtenteAutenticato_wrapper }}</p>
        <ul class="navbar-nav ml-auto">
          <li>
            <router-link :to="{path: PERCORSO_AREA_RISERVATA}"
                         class="home">
              Home
            </router-link>
          </li>
          <li>
            <router-link :to="{path: PERCORSO_IMPOSTAZIONI_ACCOUNT}"
                         class="impostazioni">
              Impostazioni account
            </router-link>
          </li>
          <li class="logout">
            <FormConCsrfToken @csrf-token-ricevuto="$emit('csrf-token-ricevuto',$event)"
                              :csrfToken_prop="csrfToken_wrapper">
              <a @click.prevent="$emit('logout')">
                Logout
              </a>
            </FormConCsrfToken>
          </li>

        </ul>
      </div>
    </nav>

  </header>

</template>

<script>
import FormConCsrfToken from "./FormConCsrfToken";
export default {
  name: "Header",
  components: {FormConCsrfToken},
  props: [
    /** Nome dell'attore attualmente autenticato.*/
   'nomeUtenteAutenticato',

    /** Tipo dell'attore attualmente autenticato.*/
    'tipoUtenteAutenticato',

    /** Valore del CSRF token.*/
    'csrfToken'
  ],
  data() {
    return {

      nomeUtenteAutenticato_wrapper: this.nomeUtenteAutenticato,  // wrapper in data() per aggiornarlo dinamicamente
      tipoUtenteAutenticato_wrapper: this.tipoUtenteAutenticato,  // wrapper

      // Dati per header
      NOME_APPLICAZIONE          : process.env.VUE_APP_NOME_APPLICAZIONE,
      URL_LOGO_APPLICAZIONE      : process.env.VUE_APP_URL_PERCORSO_LOGO,
      NOME_ROOT_APPLICAZIONE     : process.env.VUE_APP_ROUTER_ROOT_NOME,

      // Percorso vue router
      PERCORSO_IMPOSTAZIONI_ACCOUNT: process.env.VUE_APP_ROUTER_PATH_IMPOSTAZIONI_ACCOUNT,
      PERCORSO_AREA_RISERVATA      : process.env.VUE_APP_ROUTER_PATH_AREA_RISERVATA,

      // CSRF token attualmente valido
      csrfToken_wrapper: undefined

    }
  },
  methods: {
    isAutenticato() {
      return this.tipoUtenteAutenticato_wrapper;  // verifica se truthy
    },
    isAdministrator() {
      return this.tipoUtenteAutenticato_wrapper === process.env.VUE_APP_TIPO_UTENTE__ADMINISTRATOR;
    }
  },
  watch: {
    // Watch per modifiche sulla prop da parte del componente padre
    nomeUtenteAutenticato: {
      immediate: true,
      handler( nuovoNome ) {
        this.nomeUtenteAutenticato_wrapper = nuovoNome;
      },
      deep: true
    },

    tipoUtenteAutenticato: {
      immediate: true,
      handler( nuovoTipo ) {
        this.tipoUtenteAutenticato_wrapper = nuovoTipo;
      },
      deep: true
    },

    /** CSRF token potrebbe venire modificato da un altro componente
     * (per il server fa fede l'ultimo CSRF che manda, quindi bisogna
     * aggiornare il componente.*/
    csrfToken: {
      immediately: true,
      handler(nuovoValore) {
        this.csrfToken_wrapper = nuovoValore;
      },
      deep: true
    }
  }
}

</script>

<style scoped>
  .logo {
    max-height: 2.5em;
    padding: 0;
    margin: 0;
  }
  .nome-applicazione {
    font-size: 1.5em;
    color: ghostwhite;
    margin-right: 3rem;
  }
  header * {
    color: white;
  }
  .opzioni-utente-header {
    font-size: 1.2rem;
    white-space: inherit;
    color: lightgrey;
    font-weight: bold;
    max-width: 35%;

    /* Gestione dell'indentazione se va a capo */
    padding-left: 25px;
    text-indent: -25px;
  }
  nav li, nav p, nav li>*{
    margin: auto 0 auto 1rem;
    white-space: nowrap;
    display: inline;
  }
  nav li a:hover {
    text-decoration: underline;
    cursor: pointer;
  }
  nav li form {
    padding: 0;
  }
</style>