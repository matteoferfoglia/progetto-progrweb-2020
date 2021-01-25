<template>

  <h1>Area riservata</h1>

  <router-view :tipoAttoreAutenticato="tipoAttoreAutenticato_wrapper"
               :csrfToken="csrfToken_wrapper"
               @nominativo-attore-modificato="$emit('nominativo-attore-modificato', $event)"
               @csrf-token-ricevuto="$emit('csrf-token-ricevuto', $event)"  />

</template>

<script>
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
      tipoAttoreAutenticato_wrapper: this.tipoAttoreAutenticato,

      csrfToken_wrapper: this.csrfToken
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
</style>
