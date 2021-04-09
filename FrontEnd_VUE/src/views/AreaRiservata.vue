<template>

  <div class="d-flex justify-content-between">
    <h1>Area riservata</h1>
    <img :src="urlLogoUploader"
         alt="Logo uploader"
         class="logo"
         v-if="urlLogoUploader && isUploaderAttualmenteAutenticato()"/>
  </div>

  <router-view :csrfToken="csrfToken_wrapper"
               @nominativo-attore-modificato="$emit('nominativo-attore-modificato', $event)"
               @logo-attore-modificato="urlLogoUploader= creaUrlLogo(this.idAttoreCuiQuestaSchedaSiRiferisce)
                                        /*query string per aggiornare l'immagine allo stesso url*/"
               @csrf-token-ricevuto="$emit('csrf-token-ricevuto', $event)"  />

</template>

<script>

import {getIdentificativoAttoreAttualmenteAutenticato, isUploaderAttualmenteAutenticato} from "../utils/autenticazione";
import {creaUrlLogo} from "../utils/richiesteSuAttori";

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
      csrfToken_wrapper: this.csrfToken,

      // Funzione importata
      creaUrlLogo: creaUrlLogo,
      isUploaderAttualmenteAutenticato: isUploaderAttualmenteAutenticato
    }
  },
  created() {
    // Richiede l'identificativo dell'attore attualmente autenticato ed imposta l'uri a cui richiedere il suo logo
    const identificativoAttore = getIdentificativoAttoreAttualmenteAutenticato()
    this.idAttoreCuiQuestaSchedaSiRiferisce = identificativoAttore;
    this.urlLogoUploader = creaUrlLogo(identificativoAttore);
  },
  watch: {

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
  max-height: 4rem;
  min-height: 2rem;
  width: auto;
  margin: 1em;
}
h1 {
  padding: 1rem;
}
</style>
