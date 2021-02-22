<template>

  <h1>Area riservata</h1>
  <img :src="logoUploader_base64"
       alt="Proprio logo"
       v-if="isUploaderAttualmenteAutenticato()">

  <router-view :tipoAttoreAutenticato="tipoAttoreAutenticato_wrapper"
               :csrfToken="csrfToken_wrapper"
               @nominativo-attore-modificato="$emit('nominativo-attore-modificato', $event)"
               @csrf-token-ricevuto="$emit('csrf-token-ricevuto', $event)"  />

</template>

<script>
import {richiestaGet} from "../utils/http";
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

      /** Se si tratta di un Uploader, questo attributo salva il suo
       * logo in formato Base64.*/
      logoUploader_base64: undefined,

      // Wrapper
      tipoAttoreAutenticato_wrapper: this.tipoAttoreAutenticato,
      csrfToken_wrapper: this.csrfToken
    }
  },
  created() {
    this.caricaLogoUploader();
  },
  methods: {

    /** Restituisce true se è un Uploader attualmente autenticato.*/
    isUploaderAttualmenteAutenticato() {
      return this.tipoAttoreAutenticato_wrapper ===
          process.env.VUE_APP_TIPO_UTENTE__UPLOADER;
    },

    async caricaLogoUploader () {
      if(this.isUploaderAttualmenteAutenticato()) {
        await getIdentificativoAttoreAttualmenteAutenticato()
            .then(identificativoAttore => richiestaGet(process.env.VUE_APP_URL_GET_LOGO_UPLOADER + "/" + identificativoAttore))
            .then(immagineLogo_dataUrl => this.logoUploader_base64 = immagineLogo_dataUrl)
            .catch(console.error);
      }
    }

  },
  watch: {
    tipoAttoreAutenticato: {
      immediate: true,
      deep: true,
      handler( nuovoValore ) {
        this.tipoAttoreAutenticato_wrapper = nuovoValore;
        this.caricaLogoUploader();  // potrebbe essere che non fosse ancora disponibile
                                    // l'attributo tipoAttore durante created()
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
</style>
