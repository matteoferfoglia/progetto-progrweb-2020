<template>

  <div class="d-flex justify-content-between">
    <h1>Area riservata</h1>
    <img :src="urlLogoUploader_wrapper"
         alt="Logo uploader"
         class="logo"
         v-if="urlLogoUploader_wrapper && isUploaderAttualmenteAutenticato()"/>
  </div>

  <router-view :tipoAttoreAutenticato="tipoAttoreAutenticato_wrapper"
               :csrfToken="csrfToken_wrapper"
               @nominativo-attore-modificato="$emit('nominativo-attore-modificato', $event)"
               @logo-attore-modificato="urlLogoUploader_wrapper = urlLogoUploader + '?' + new Date().getTime()
                                        /*query string per aggiornare l'immagine allo stesso url*/"
               @csrf-token-ricevuto="$emit('csrf-token-ricevuto', $event)"  />

</template>

<script>

import {getIdentificativoAttoreAttualmenteAutenticato} from "../utils/autenticazione";

export default {
  inheritAttrs: false,  // Fonte (warning when using dynamic components and custom-events): https://stackoverflow.com/a/65555712
  emits: [

    /** Evento emesso quando viene modificato il nominativo
     * dell'utente attualmente autenticato.
     * Ha come valore il nuovo nominativo.*/
    'nominativo-attore-modificato',

    /** Evento emesso quando viene modificato il CSRF token.
     * Ha come valore il nuovo valore del token.*/
    'csrf-token-ricevuto'
  ],
  props: [
    /** Tipo dell'attore autenticato.*/
    'tipoAttoreAutenticato',

    /** CSRF token attualmente valido.*/
    'csrfToken'
  ],
  name: 'AreaRiservata',
  data() {
    return {

      /** Identificativo dell'attore a cui questa scheda si riferisce.*/
      idAttoreCuiQuestaSchedaSiRiferisce: undefined,  // caricato in created

      /** Url a cui richiedere il logo dell'attore a cui questa scheda
       * si riferisce. */
      urlLogoUploader: undefined,                     // caricato in created

      // Wrapper
      tipoAttoreAutenticato_wrapper: this.tipoAttoreAutenticato,
      urlLogoUploader_wrapper: this.urlLogoUploader,
      csrfToken_wrapper: this.csrfToken
    }
  },
  created() {
    // Richiede l'identificativo dell'attore attualmente autenticato ed imposta l'uri a cui richiedere il suo logo
    getIdentificativoAttoreAttualmenteAutenticato()
        .then(identificativoAttore => {
          this.idAttoreCuiQuestaSchedaSiRiferisce = identificativoAttore;
          this.urlLogoUploader = process.env.VUE_APP_URL_GET_LOGO_UPLOADER + "/" + identificativoAttore;
          this.urlLogoUploader_wrapper = this.urlLogoUploader + '?' + new Date().getTime(); // query string per evitare cache
        })
        .catch(console.error);
  },
  methods: {

    /** Restituisce true se è un Uploader attualmente autenticato.*/
    isUploaderAttualmenteAutenticato() {
      return this.tipoAttoreAutenticato_wrapper ===
          process.env.VUE_APP_TIPO_UTENTE__UPLOADER;
    }

  },
  watch: {
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

<style>
img.logo {
  max-height: 5rem;
  width: auto;
  margin: 1em;
}
h1 {
  padding: 1rem;
}
</style>
