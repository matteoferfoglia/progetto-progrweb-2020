<template>
  <form v-show="isCsrfTokenCaricato"
        @submit.prevent="$emit('submit')/*Deve essere propagato*/">
    <slot><!--qua i campi del form--></slot>
    <input type="hidden" v-model="csrfToken">
  </form>
</template>

<script>

/** Questo componente offre un form già predisposto con il campo
 * per il token CSRF. Tramite l'evento 'csrf-token-ricevuto',
 * generato quando questo componente riceve il token dal server,
 * viene inoltrato tale token al componente padre.
 */

import {richiediCSRFTokenAlServer} from "../../utils/CSRF";

export default {
  name: 'FormConCsrfToken',
  emits: [
      /** Evento emesso quando viene ricevuto il token CSRF dal server.*/
      'csrf-token-ricevuto',

      /** Evento emesso se si clicca "submit.*/
      'submit'
  ],
  props: [

    /** Valore CSRF token, se modificato dal componente padre.*/
    'csrfToken_prop',

    /** Chiave utilizzata per "forzare" l'aggiornamento del componente.
     * (<a href="https://stackoverflow.com/a/47466574">Fonte</a>).*/
    'valoreQualsiasiPerAggiornareIlComponenteSeModificato'
  ],
  data: function () {
    return {
      csrfToken: undefined,
      isCsrfTokenCaricato: false,
    }
  },
  created() {

    this.richiestaCSRFTokenAlServer();

  },
  methods: {

    richiestaCSRFTokenAlServer() {  // definito in methods per essere riutilizzato
      richiediCSRFTokenAlServer()
        .then( valoreToken => {
          // Se qui, allora token csrf ricevuto dal server
          this.csrfToken = valoreToken;
          this.isCsrfTokenCaricato = true;
          this.$emit('csrf-token-ricevuto',valoreToken);
        })
        .catch( errore => {
          // Errore durante la ricezione del token csrf
          console.error("Errore in " + this.$options.name /*Nome componente: */ + ": " + errore);
        });
    }

  },
  watch: {

    valoreQualsiasiPerAggiornareIlComponenteSeModificato: {
      immediately: true,
      handler() {
        this.richiestaCSRFTokenAlServer();
      },
      deep: true
    },

    csrfToken_prop: {
      immediately: true,
      handler(nuovoValore) {
        this.csrfToken = nuovoValore;
      },
      deep: true
    }

  }
}

</script>

<style>
</style>