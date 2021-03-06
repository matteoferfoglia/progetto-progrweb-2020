<template>

  <router-view :tipoAttoreAutenticato="tipoAttoreAutenticato_wrapper"
               :csrfToken="csrfToken_wrapper"
               :NOME_PROP_NOMINATIVO ="NOME_PROP_NOME_ATTORE"
               :NOME_PROP_USERNAME   ="NOME_PROP_USERNAME_ATTORE"
               :NOME_PROP_EMAIL      ="NOME_PROP_EMAIL_ATTORE"
               @nominativo-attore-modificato="$emit('nominativo-attore-modificato',$event)"
               @csrf-token-ricevuto="$emit('csrf-token-ricevuto',$event)"/>


</template>

<script>

// Questo componente mostra la schermata principale di un attore autenticato

import {richiestaGet} from "../../utils/http";

export default {
  name: "SchermataPrincipaleAttore",
  inheritAttrs: false,
  emits: ['csrf-token-ricevuto','nominativo-attore-modificato'],
  props: ['tipoAttoreAutenticato', 'csrfToken'],
  data() {

    return {

      tipoAttoreAutenticato_wrapper: this.tipoAttoreAutenticato,  //wrapper (prop non è modificabile)
      csrfToken_wrapper: this.csrfToken,

      /** Nell'oggetto restituito dal server contenente le informazioni di un
       * Attore, il nome dell'Attore è salvato nella proprietà con il nome
       * indicato in questa variabile.*/
      NOME_PROP_NOME_ATTORE: undefined,

      /** Nell'oggetto restituito dal server contenente le informazioni di un
       * Attore, l'email dell'Attore è salvata nella proprietà con il nome
       * indicato in questa variabile.*/
      NOME_PROP_EMAIL_ATTORE: undefined,

      /** Nell'oggetto restituito dal server contenente le informazioni di un
       * Attore, lo username dell'Attore è salvato nella proprietà con il nome
       * indicato in questa variabile.*/
      NOME_PROP_USERNAME_ATTORE: undefined,

    }

  },
  created() {

    // TODO : aggiungere una proprietà isLayoutCaricato (visto che created() non può avere comportamento asincrono)

    // richiede il nome della prop contenente il nominativo di un attore nell'oggetto
    // che sarà restituito dal server con le info di un attore
    richiestaGet( process.env.VUE_APP_URL_GET_NOME_PROP_NOME_ATTORE )
        .then( nomePropNomeAttore => this.NOME_PROP_NOME_ATTORE = nomePropNomeAttore )

        // richiede il nome della prop contenente l'email
        .then( () => richiestaGet( process.env.VUE_APP_URL_GET_NOME_PROP_EMAIL_ATTORE ) )
        .then( nomePropEmailAttore => this.NOME_PROP_EMAIL_ATTORE = nomePropEmailAttore )

        // richiede il nome della prop contenente lo username
        .then( () => richiestaGet( process.env.VUE_APP_URL_GET_NOME_PROP_USERNAME_ATTORE ) )
        .then( nomePropUsernameAttore => this.NOME_PROP_USERNAME_ATTORE = nomePropUsernameAttore )

        .catch( console.error );

  },
  watch: {

    /** Watch per il tipo di attore attualmente autenticato (potrebbe
     * fare logout e potrebbe autenticarsi subito dopo un attore
     * con un'altra qualifica).*/
    tipoAttoreAutenticato: {
      immediate: true,
      deep: true,
      handler( nuovoValore ) {
        this.tipoAttoreAutenticato_wrapper = nuovoValore;
      }
    },

    csrfToken : {
      immediate: true,
      deep: true,
      handler(nuovoValore) {
        this.csrfToken_wrapper = nuovoValore;
      }
    }
  }
}

</script>

<style scoped>
</style>