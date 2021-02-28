<template>

  <slot v-if="isComponenteCaricato_wrapper"></slot>
  <p class="d-flex align-items-center justify-content-center mx-auto" v-else>
    <span class="spinner-border" role="status" aria-hidden="true"></span>
    <span class="mx-3">Caricamento</span>
  </p>

</template>

<script>
/** Loader per gestire il caricamento dei componenti che fanno
 * uso di richieste asincrone. */
export default {
  name: "Loader",
  props: [
      /** Flag: diventa true quando il componente è caricato. */
      "isComponenteCaricato"
  ],
  data() {
    return {
      isComponenteCaricato_wrapper: this.isComponenteCaricato
    }
  },
  watch: {
    isComponenteCaricato: {
      immediate: true,
      deep: true,
      handler(nuovoValore) {
        if(nuovoValore) {
          this.isComponenteCaricato_wrapper = nuovoValore;
        }
      }
    }
  }
}
</script>

<style scoped>

</style>