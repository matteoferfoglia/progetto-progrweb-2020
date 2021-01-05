<template>
  <form  v-if="isCsrfTokenCaricato">
    <slot><!--qua i campi del form--></slot>
    <input type="hidden" v-model="csrfToken">
  </form>
</template>

<script>

// TODO : TESTARE e provare attacchi csrf

/** Questo componente offre un form già predisposto con il campo
 * per il token CSRF. Tramite l'evento 'csrf-token-ricevuto',
 * generato quando questo componente riceve il token dal server,
 * viene inoltrato tale token al componente padre.
 */

import {richiediCSRFTokenAlServer} from "../utils/CSRF";

export default {
  name: 'FormConCsrfToken',
  data: function () {
    return {
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
      this.$emit('csrf-token-ricevuto',valoreToken);
    }).catch( errore => {
      // Errore durante la ricezione del token csrf
      console.error("Errore in " +
          /*Nome componente: */ this.$options.name + ": " + errore);  // TODO : gestire questo errore
    });

  }
}

</script>

<style>
</style>