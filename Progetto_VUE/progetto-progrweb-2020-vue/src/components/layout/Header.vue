<template>

  <header>
    <nav class="navbar navbar-expand-mr bg-dark">

      <router-link :to="{name: NOME_ROOT_APPLICAZIONE}" class="navbar-brand">
        <img class="logo d-inline-block align-top" :src="URL_LOGO_APPLICAZIONE" alt=""/>
        <span class="nome-applicazione">{{NOME_APPLICAZIONE}}</span>
      </router-link>

      <div id="containerOpzioniUtente"
           class="nav-item dropdown"
           v-if="isAutenticato()">

        <button id="buttonOpzioniUtente"
                data-toggle="dropdown"
                class="opzioni-utente nav-link dropdown-toggle">
          {{ nomeUtenteAutenticato_wrapper }}
        </button>

        <ul id="elencoOpzioniUtenteDaHeader" class="dropdown-menu">

          <li>
            <router-link :to="{path: PERCORSO_IMPOSTAZIONI_ACCOUNT}"
                         class="impostazioni dropdown-item">
              Impostazioni account
            </router-link>
          </li>
          <li class="dropdown-item">
            <FormConCsrfToken @csrf-token-ricevuto="$emit('csrf-token-ricevuto',$event)"
                              :csrfToken="csrfToken_wrapper">
              <a @click.prevent="$emit('logout')" class="logout">
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
      NOME_APPLICAZIONE          : process.env.VUE_APP_NOME_APPLICAZIONE, // TODO : chiedere al server queste informazioni?
      URL_LOGO_APPLICAZIONE      : process.env.VUE_APP_URL_PERCORSO_LOGO,
      NOME_ROOT_APPLICAZIONE     : process.env.VUE_APP_ROUTER_ROOT_NOME,

      // Percorso vue router
      PERCORSO_IMPOSTAZIONI_ACCOUNT: process.env.VUE_APP_ROUTER_PATH_IMPOSTAZIONI_ACCOUNT,

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
  a.logout::before {
    content: url('data:image/svg+xml; utf8, <svg width="1em" height="1em" viewBox="0 0 16 16" class="bi bi-door-open" fill="currentColor" xmlns="http://www.w3.org/2000/svg"><path fill-rule="evenodd" d="M1 15.5a.5.5 0 0 1 .5-.5h13a.5.5 0 0 1 0 1h-13a.5.5 0 0 1-.5-.5zM11.5 2H11V1h.5A1.5 1.5 0 0 1 13 2.5V15h-1V2.5a.5.5 0 0 0-.5-.5z"/><path fill-rule="evenodd" d="M10.828.122A.5.5 0 0 1 11 .5V15h-1V1.077l-6 .857V15H3V1.5a.5.5 0 0 1 .43-.495l7-1a.5.5 0 0 1 .398.117z"/><path d="M8 9c0 .552.224 1 .5 1s.5-.448.5-1-.224-1-.5-1-.5.448-.5 1z"/></svg>');
  }
  a.logout {
    cursor: pointer
  }
  .impostazioni::before {
    content: url('data:image/svg+xml; utf8, <svg width="1em" height="1em" viewBox="0 0 16 16" class="bi bi-gear-fill" fill="currentColor" xmlns="http://www.w3.org/2000/svg"><path fill-rule="evenodd" d="M9.405 1.05c-.413-1.4-2.397-1.4-2.81 0l-.1.34a1.464 1.464 0 0 1-2.105.872l-.31-.17c-1.283-.698-2.686.705-1.987 1.987l.169.311c.446.82.023 1.841-.872 2.105l-.34.1c-1.4.413-1.4 2.397 0 2.81l.34.1a1.464 1.464 0 0 1 .872 2.105l-.17.31c-.698 1.283.705 2.686 1.987 1.987l.311-.169a1.464 1.464 0 0 1 2.105.872l.1.34c.413 1.4 2.397 1.4 2.81 0l.1-.34a1.464 1.464 0 0 1 2.105-.872l.31.17c1.283.698 2.686-.705 1.987-1.987l-.169-.311a1.464 1.464 0 0 1 .872-2.105l.34-.1c1.4-.413 1.4-2.397 0-2.81l-.34-.1a1.464 1.464 0 0 1-.872-2.105l.17-.31c.698-1.283-.705-2.686-1.987-1.987l-.311.169a1.464 1.464 0 0 1-2.105-.872l-.1-.34zM8 10.93a2.929 2.929 0 1 0 0-5.86 2.929 2.929 0 0 0 0 5.858z"/></svg>');
  }
  button.opzioni-utente::before {
    content: url('data:image/svg+xml; utf8, <svg width="1em" height="1em" viewBox="0 0 16 16" class="bi bi-person-fill" fill="currentColor" xmlns="http://www.w3.org/2000/svg"><path fill-rule="evenodd" d="M3 14s-1 0-1-1 1-4 6-4 6 3 6 4-1 1-1 1H3zm5-6a3 3 0 1 0 0-6 3 3 0 0 0 0 6z"></path></svg>');
  }
  .logo {
    max-height: 2.5em;
    padding: 0;
    margin: 0;
  }
  .nome-applicazione {
    font-size: 1.5em;
    color: ghostwhite;
  }
</style>