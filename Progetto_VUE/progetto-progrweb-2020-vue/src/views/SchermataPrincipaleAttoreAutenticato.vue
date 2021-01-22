<template>

  <Consumer      v-if="isConsumer()" />
  <Uploader      v-else-if="isUploader()"
                 @csrf-token-modificato="$emit('csrf-token-modificato',$event)"  />
  <Administrator v-else-if="isAdministrator()" />

  <router-view/>

</template>

<script>

// Questo componente mostra la schermata principale di un attore autenticato

import Consumer from "../components/attori/Consumer";
import Uploader from "../components/attori/uploader/Uploader";
import Administrator from "../components/attori/Administrator";

export default {
  name: "SchermataPrincipaleAttore",
  components: {Administrator, Uploader, Consumer},
  emits: ['csrf-token-modificato','nominativo-attore-modificato'],
  props: ['tipoAttoreAutenticato','nomeUtenteAutenticato'],
  methods: {
    isConsumer() {
      return this.tipoAttoreAutenticato===process.env.VUE_APP_TIPO_UTENTE_AUTENTICATO_CONSUMER;
    },
    isUploader() {
      return this.tipoAttoreAutenticato===process.env.VUE_APP_TIPO_UTENTE_AUTENTICATO_UPLOADER;
    },
    isAdministrator() {
      return this.tipoAttoreAutenticato===process.env.VUE_APP_TIPO_UTENTE_AUTENTICATO_ADMINISTRATOR;
    }
  }
}

</script>

<style scoped>

</style>