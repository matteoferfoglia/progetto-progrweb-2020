<template id="app">

  <Header :nomeUtenteAutenticato="nomeAttoreAutenticato"
          :csrfToken="csrfToken /*Informare il componente se cambia.*/"
          @logout="logout"
          @csrf-token-ricevuto="csrfToken = $event" />

  <router-view :csrfToken="csrfToken /*Informare il componente se è cambiato.*/"
               @nominativo-attore-modificato="nomeAttoreAutenticato = $event"
               @csrf-token-ricevuto="csrfToken = $event"
               @login="login($event)" />

</template>

<script>

import Header from "./components/layout/Header";
import {
  getNomeAttoreAttualmenteAutenticato,
  logout,
  verificaAutenticazione
} from "./utils/autenticazione";
export default {
  name: 'App',
  components: { Header},
  data() {
    return {

      /** Flag: true se l'utente risulta autenticato.*/
      isUtenteAutenticato: undefined,

      /** Nome dell'attore (se) autenticato.*/
      nomeAttoreAutenticato: undefined,

      /** Valore del token CSRF attualmente valido.*/
      csrfToken: undefined

    }
  },
  created() {

    this.setup();

  },
  methods: {

    /** Setup della pagina.*/
    async setup() {

      await verificaAutenticazione( this.$route )
              .then( async isUtenteAutenticato => {

                if (isUtenteAutenticato)
                  this.nomeAttoreAutenticato = getNomeAttoreAttualmenteAutenticato();

                return isUtenteAutenticato;

              })
              .catch( console.error );

    },

    /** Metodo invocato quando il login di un utente ha successo.
     *
     * @param parametriRouter Configurazione per Vue Router. Vedere
     *        i dettagli nel componente che si occupa del login.
     */
    login(parametriRouter) {

      this.$router.push({ // inoltro con parametri
        name   : process.env.VUE_APP_ROUTER_NOME_COMPONENTE_AREA_RISERVATA,
        params : parametriRouter
      }).then( this.setup );

    },

    /** Richiesta di logout al server. */
    logout() {
      logout( this.csrfToken )  // funzione importata dal modulo di autenticazione
        .then( () => {
          this.csrfToken             = undefined;
          this.nomeAttoreAutenticato = undefined;
          this.isUtenteAutenticato   = false;
        })
        .catch( console.error );
    }

  },
  watch: {
    /**
     * Osserva nel parametro della route se l'utente risulta o meno autenticato.
     * Il parametro nella route è salvato come String.
     */
    '$route': {
      immediate: true,
      handler: function(nuovaRoute) {
        const eraUtenteAutenticato = this.isUtenteAutenticato;
        this.isUtenteAutenticato =
            nuovaRoute.params[process.env.VUE_APP_ROUTER_PARAMETRO_IS_UTENTE_AUTENTICATO] === "true"; // necessaria conversione in boolean (la stringa "false" sarebbe truthy)
        if( eraUtenteAutenticato && !this.isUtenteAutenticato ) {
          this.logout();
        }
      }
    }
  }
}

</script>


<style>
  a.router-link-exact-active, a.router-link-active {
    color: #42b983;
    font-weight: bold;
  }
  .card {
    margin: 1rem 5%;
  }
  form,article,section p,small{
    padding: .5em .5em 0 .5em;
  }
  input[type=submit], button {
    margin: 1rem auto;
  }
  .form-items-container>p,label,div>p {
    min-width: 30%;
  }
  div>button {
    margin-top: .5em;
    margin-bottom: .5em;
  }
  button {
    margin: auto 1%;
  }
  label {
    max-width: 30rem;
  }

  /** Icone dei pulsanti e dei link */
  button.modifica::before {
    /* Fonte icona: https://icons.getbootstrap.com/icons/pen/ */
    content: url('data:image/svg+xml; utf8, <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="white" class="bi bi-pen" viewBox="0 0 16 16"><path d="M13.498.795l.149-.149a1.207 1.207 0 1 1 1.707 1.708l-.149.148a1.5 1.5 0 0 1-.059 2.059L4.854 14.854a.5.5 0 0 1-.233.131l-4 1a.5.5 0 0 1-.606-.606l1-4a.5.5 0 0 1 .131-.232l9.642-9.642a.5.5 0 0 0-.642.056L6.854 4.854a.5.5 0 1 1-.708-.708L9.44.854A1.5 1.5 0 0 1 11.5.796a1.5 1.5 0 0 1 1.998-.001zm-.644.766a.5.5 0 0 0-.707 0L1.95 11.756l-.764 3.057 3.057-.764L14.44 3.854a.5.5 0 0 0 0-.708l-1.585-1.585z"/></svg>');
  }
  button.x-circle::before {
    /* Fonte (icona): https://icons.getbootstrap.com/icons/x-circle/ */
    content: url('data:image/svg+xml; utf8, <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="white" class="bi bi-x-circle" viewBox="0 0 16 16"><path d="M8 15A7 7 0 1 1 8 1a7 7 0 0 1 0 14zm0 1A8 8 0 1 0 8 0a8 8 0 0 0 0 16z"/><path d="M4.646 4.646a.5.5 0 0 1 .708 0L8 7.293l2.646-2.647a.5.5 0 0 1 .708.708L8.707 8l2.647 2.646a.5.5 0 0 1-.708.708L8 8.707l-2.646 2.647a.5.5 0 0 1-.708-.708L7.293 8 4.646 5.354a.5.5 0 0 1 0-.708z"/></svg>');
  }
  button.reset::before {
    /* Fonte icona: https://icons.getbootstrap.com/icons/arrow-counterclockwise/ */
    content: url('data:image/svg+xml; utf8, <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="white" class="bi bi-arrow-counterclockwise" viewBox="0 0 16 16"><path fill-rule="evenodd" d="M8 3a5 5 0 1 1-4.546 2.914.5.5 0 0 0-.908-.417A6 6 0 1 0 8 2v1z"/><path d="M8 4.466V.534a.25.25 0 0 0-.41-.192L5.23 2.308a.25.25 0 0 0 0 .384l2.36 1.966A.25.25 0 0 0 8 4.466z"/></svg>');
  }
  button.check-icon::before {
    /* Fonte icona: https://icons.getbootstrap.com/icons/check-circle/ */
    content: url('data:image/svg+xml; utf8, <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="white" class="bi bi-check-circle" viewBox="0 0 16 16"><path d="M8 15A7 7 0 1 1 8 1a7 7 0 0 1 0 14zm0 1A8 8 0 1 0 8 0a8 8 0 0 0 0 16z"/><path d="M10.97 4.97a.235.235 0 0 0-.02.022L7.477 9.417 5.384 7.323a.75.75 0 0 0-1.06 1.06L6.97 11.03a.75.75 0 0 0 1.079-.02l3.992-4.99a.75.75 0 0 0-1.071-1.05z"/></svg>');
  }
  .opzioni-utente-header::before {
    content: url('data:image/svg+xml; utf8, <svg width="1em" height="1em" viewBox="0 0 16 16" class="bi bi-person-fill" fill="darkgray" xmlns="http://www.w3.org/2000/svg"><path fill-rule="evenodd" d="M3 14s-1 0-1-1 1-4 6-4 6 3 6 4-1 1-1 1H3zm5-6a3 3 0 1 0 0-6 3 3 0 0 0 0 6z"></path></svg>');
    margin-right: .5em;
  }
  *[class|="icona-toggle"]::after {
    content: url('data:image/svg+xml; utf8, <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="%23007bff" class="bi bi-chevron-compact-down" viewBox="0 0 16 16"><path fill-rule="evenodd" d="M1.553 6.776a.5.5 0 0 1 .67-.223L8 9.44l5.776-2.888a.5.5 0 1 1 .448.894l-6 3a.5.5 0 0 1-.448 0l-6-3a.5.5 0 0 1-.223-.67z"/></svg>');
    display: block;
  }
  .link-download {
    /* Fonte icona: https://icons.getbootstrap.com/icons/cloud-download/ */
    content: url('data:image/svg+xml; utf8, <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="%23007bff" class="bi bi-cloud-download" viewBox="0 0 16 16"><path d="M4.406 1.342A5.53 5.53 0 0 1 8 0c2.69 0 4.923 2 5.166 4.579C14.758 4.804 16 6.137 16 7.773 16 9.569 14.502 11 12.687 11H10a.5.5 0 0 1 0-1h2.688C13.979 10 15 8.988 15 7.773c0-1.216-1.02-2.228-2.313-2.228h-.5v-.5C12.188 2.825 10.328 1 8 1a4.53 4.53 0 0 0-2.941 1.1c-.757.652-1.153 1.438-1.153 2.055v.448l-.445.049C2.064 4.805 1 5.952 1 7.318 1 8.785 2.23 10 3.781 10H6a.5.5 0 0 1 0 1H3.781C1.708 11 0 9.366 0 7.318c0-1.763 1.266-3.223 2.942-3.593.143-.863.698-1.723 1.464-2.383z"/><path d="M7.646 15.854a.5.5 0 0 0 .708 0l3-3a.5.5 0 0 0-.708-.708L8.5 14.293V5.5a.5.5 0 0 0-1 0v8.793l-2.146-2.147a.5.5 0 0 0-.708.708l3 3z"/></svg>');
  }
  .link-delete {
    /* Fonte icona: https://icons.getbootstrap.com/icons/trash/ */
    content: url('data:image/svg+xml; utf8, <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="%23007bff" class="bi bi-trash" viewBox="0 0 16 16"><path d="M5.5 5.5A.5.5 0 0 1 6 6v6a.5.5 0 0 1-1 0V6a.5.5 0 0 1 .5-.5zm2.5 0a.5.5 0 0 1 .5.5v6a.5.5 0 0 1-1 0V6a.5.5 0 0 1 .5-.5zm3 .5a.5.5 0 0 0-1 0v6a.5.5 0 0 0 1 0V6z"/><path fill-rule="evenodd" d="M14.5 3a1 1 0 0 1-1 1H13v9a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V4h-.5a1 1 0 0 1-1-1V2a1 1 0 0 1 1-1H6a1 1 0 0 1 1-1h2a1 1 0 0 1 1 1h3.5a1 1 0 0 1 1 1v1zM4.118 4L4 4.059V13a1 1 0 0 0 1 1h6a1 1 0 0 0 1-1V4.059L11.882 4H4.118zM2.5 3V2h11v1h-11z"/></svg>');
  }
  .carica-file::before {
    /* Fonte icona: https://icons.getbootstrap.com/icons/file-earmark-arrow-up/ */
    content: url('data:image/svg+xml; utf8, <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="white" class="bi bi-file-earmark-arrow-up" viewBox="0 0 16 16"><path d="M8.5 11.5a.5.5 0 0 1-1 0V7.707L6.354 8.854a.5.5 0 1 1-.708-.708l2-2a.5.5 0 0 1 .708 0l2 2a.5.5 0 0 1-.708.708L8.5 7.707V11.5z"/><path d="M14 14V4.5L9.5 0H4a2 2 0 0 0-2 2v12a2 2 0 0 0 2 2h8a2 2 0 0 0 2-2zM9.5 3A1.5 1.5 0 0 0 11 4.5h2V14a1 1 0 0 1-1 1H4a1 1 0 0 1-1-1V2a1 1 0 0 1 1-1h5.5v2z"/></svg>');
  }
  .home::before {
    content: url('data:image/svg+xml; utf8, <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="darkgray" class="bi bi-house-door" viewBox="0 0 16 16"><path d="M8.354 1.146a.5.5 0 0 0-.708 0l-6 6A.5.5 0 0 0 1.5 7.5v7a.5.5 0 0 0 .5.5h4.5a.5.5 0 0 0 .5-.5v-4h2v4a.5.5 0 0 0 .5.5H14a.5.5 0 0 0 .5-.5v-7a.5.5 0 0 0-.146-.354L13 5.793V2.5a.5.5 0 0 0-.5-.5h-1a.5.5 0 0 0-.5.5v1.293L8.354 1.146zM2.5 14V7.707l5.5-5.5 5.5 5.5V14H10v-4a.5.5 0 0 0-.5-.5h-3a.5.5 0 0 0-.5.5v4H2.5z"/></svg>');
    margin-right: .5em;
  }
  .impostazioni::before {
    content: url('data:image/svg+xml; utf8, <svg width="1em" height="1em" viewBox="0 0 16 16" class="bi bi-gear-fill" fill="darkgray" xmlns="http://www.w3.org/2000/svg"><path fill-rule="evenodd" d="M9.405 1.05c-.413-1.4-2.397-1.4-2.81 0l-.1.34a1.464 1.464 0 0 1-2.105.872l-.31-.17c-1.283-.698-2.686.705-1.987 1.987l.169.311c.446.82.023 1.841-.872 2.105l-.34.1c-1.4.413-1.4 2.397 0 2.81l.34.1a1.464 1.464 0 0 1 .872 2.105l-.17.31c-.698 1.283.705 2.686 1.987 1.987l.311-.169a1.464 1.464 0 0 1 2.105.872l.1.34c.413 1.4 2.397 1.4 2.81 0l.1-.34a1.464 1.464 0 0 1 2.105-.872l.31.17c1.283.698 2.686-.705 1.987-1.987l-.169-.311a1.464 1.464 0 0 1 .872-2.105l.34-.1c1.4-.413 1.4-2.397 0-2.81l-.34-.1a1.464 1.464 0 0 1-.872-2.105l.17-.31c.698-1.283-.705-2.686-1.987-1.987l-.311.169a1.464 1.464 0 0 1-2.105-.872l-.1-.34zM8 10.93a2.929 2.929 0 1 0 0-5.86 2.929 2.929 0 0 0 0 5.858z"/></svg>') ;
    margin-right: .5em;
  }
  .logout a::before {
    content: url('data:image/svg+xml; utf8, <svg width="1em" height="1em" viewBox="0 0 16 16" class="bi bi-door-open" fill="darkgray" xmlns="http://www.w3.org/2000/svg"><path fill-rule="evenodd" d="M1 15.5a.5.5 0 0 1 .5-.5h13a.5.5 0 0 1 0 1h-13a.5.5 0 0 1-.5-.5zM11.5 2H11V1h.5A1.5 1.5 0 0 1 13 2.5V15h-1V2.5a.5.5 0 0 0-.5-.5z"/><path fill-rule="evenodd" d="M10.828.122A.5.5 0 0 1 11 .5V15h-1V1.077l-6 .857V15H3V1.5a.5.5 0 0 1 .43-.495l7-1a.5.5 0 0 1 .398.117z"/><path d="M8 9c0 .552.224 1 .5 1s.5-.448.5-1-.224-1-.5-1-.5.448-.5 1z"/></svg>');
    margin-right: .5em;
  }

  input:required {
    box-shadow:none;
  }
</style>
