<template id="app">

  <Header :nomeUtenteAutenticato="nomeAttoreAutenticato"
          :tipoUtenteAutenticato="tipoAttoreAutenticato"
          :csrfToken="csrfToken /*Informare il componente se cambia.*/"
          @logout="logout"
          @csrf-token-ricevuto="csrfToken = $event" />

  <router-view :tipoAttoreAutenticato="tipoAttoreAutenticato"
               :csrfToken="csrfToken /*Informare il componente se è cambiato.*/"
               @nominativo-attore-modificato="nomeAttoreAutenticato = $event"
               @csrf-token-ricevuto="csrfToken = $event"
               @login="login($event)" />

</template>

<script>

import Header from "./components/layout/Header";
import {eliminaTokenAutenticazione, verificaAutenticazione} from "./utils/autenticazione";
import {richiestaGet} from "./utils/http";
export default {
  name: 'App',
  components: { Header},
  data() {
    return {

      /** Flag: true se l'utente risulta autenticato.*/
      isUtenteAutenticato: undefined,

      /** Nome dell'attore (se) autenticato.*/
      nomeAttoreAutenticato: undefined,

      /** Tipo dell'attore (se) autenticato (Administrator/Consumer/Uploader).*/
      tipoAttoreAutenticato: undefined,

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

                if (isUtenteAutenticato) {
                  await getTipoAttoreAttualmenteAutenticato()
                          .then(tipo => this.tipoAttoreAutenticato = tipo )
                          .catch(console.error);

                  await getNomeAttoreAttualmenteAutenticato()
                          .then(nome => this.nomeAttoreAutenticato = nome)
                          .catch(console.error);
                }

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

    logout() {

      // Richiesta di logout al server
      const parametriRichiestaGet = {[process.env.VUE_APP_FORM_CSRF_INPUT_FIELD_NAME]: this.csrfToken};
      richiestaGet(process.env.VUE_APP_URL_LOGOUT, parametriRichiestaGet)
          .catch( risposta => console.log("Logout fallito: " + risposta) )
          .finally( () => { // logout sul client
            eliminaTokenAutenticazione();
            this.$router.push({name: process.env.VUE_APP_ROUTER_NOME_ROUTE_LOGIN})
                .then( () => {
                  this.csrfToken = undefined;
                  this.tipoAttoreAutenticato = undefined;
                  this.nomeAttoreAutenticato = undefined;
                  this.isUtenteAutenticato = false;
                });
          });

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

/** Restituisce il tipo dell'attore attualmente autenticato.*/
const getTipoAttoreAttualmenteAutenticato = async () => {

  return richiestaGet(process.env.VUE_APP_URL_GET_TIPO_UTENTE_AUTENTICATO)
      .catch( rispostaErrore => {
        console.error("Errore durante il caricamento delle informazioni: " + rispostaErrore );
        return Promise.reject(rispostaErrore);
        // TODO : gestire l'errore (invio mail ai gestori?)
        // TODO : cercare tutti i catch nel progetto e fare un gestore di eccezioni unico
      });

}

/** Restituisce il nome dell'attore attualmente autenticato.*/
const getNomeAttoreAttualmenteAutenticato = async () => {

  return richiestaGet(process.env.VUE_APP_URL_GET_NOME_QUESTO_ATTORE_AUTENTICATO)
      .catch( rispostaErrore => {
        console.error("Errore durante il caricamento delle informazioni: " + rispostaErrore );
        return Promise.reject(rispostaErrore);
      });

}

</script>


<style>
#app {
}

#nav a.router-link-exact-active {
  color: #42b983;
}
</style>
