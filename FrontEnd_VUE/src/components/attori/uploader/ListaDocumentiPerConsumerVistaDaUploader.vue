<template>

  <!-- Componente per mostrare la lista di documenti di un Consumer,
       visualizzata da un Uploader -->

  <nav class="d-flex justify-content-center">
    <router-link :to="{name: nomeRouteCaricamentoDocumenti, params: $route.params}">
      Caricamento documenti
    </router-link>
    <router-link :to="{name: nomeRouteTabellaDocumenti, params: $route.params}">
      Lista documenti
    </router-link>
  </nav>

  <router-view :idConsumer="idConsumer" :csrfToken="csrfToken_wrapper"
               :urlRichiestaElencoDocumentiPerUnAttore="urlRichiestaElencoDocumentiPerUnConsumer"
               :urlDownloadDocumento="urlDownloadDocumento"
               :urlEliminazioneDocumento="urlEliminazioneDocumento"
               @csrf-token-ricevuto="$emit('csrf-token-ricevuto', $event)"/>

</template>

<script>

export default {
  name: "ListaDocumentiDiUnConsumerVistaDaUploader",
  emits: [
    /** Evento emesso quando viene ricevuto il token CSRF dal server.*/
    'csrf-token-ricevuto'
  ],
  props: [

    /** Identificativo del consumer a cui i documenti si riferiscono.*/
    "idConsumer",

    /** Valore del token CSRF ricevuto dal padre.*/
    "csrfToken"
  ],
  data() {
    return {

      // URL GESTIONE DOCUMENTI
      urlRichiestaElencoDocumentiPerUnConsumer: process.env.VUE_APP_URL_GET_ELENCO_DOCUMENTI__RICHIESTA_DA_UPLOADER + "/" + this.idConsumer,
      urlDownloadDocumento:                     process.env.VUE_APP_URL_DOWNLOAD_DOCUMENTO__RICHIESTA_DA_UPLOADER,
      urlEliminazioneDocumento:                 process.env.VUE_APP_URL_DELETE_DOCUMENTO__RICHIESTA_DA_UPLOADER,

      // Parametri Vue-Router
      nomeRouteCaricamentoDocumenti:          process.env.VUE_APP_ROUTER_NOME_CARICAMENTO_DOCUMENTI,
      nomeRouteTabellaDocumenti:              process.env.VUE_APP_ROUTER_NOME_TABELLA_DOCUMENTI,
      nomeRouteListaDocumentiVistaDaUploader: process.env.VUE_APP_ROUTER_NOME_LISTA_DOCUMENTI_VISTA_DA_UPLOADER,
      nomeRouteSchedaAttore:                  process.env.VUE_APP_ROUTER_NOME_SCHEDA_UN_ATTORE,

      // Wrapper
      csrfToken_wrapper: this.csrfToken

    }
  },
  created() {
    // Redirect a route di default se non specificata
    if( this.$route.name === this.nomeRouteListaDocumentiVistaDaUploader ||
          this.$route.name === this.nomeRouteSchedaAttore )
      this.$router.push({
        name: this.nomeRouteTabellaDocumenti,
        params: this.$route.params
      });
  },
  watch: {

    csrfToken : {
      immediate: true,
      handler( nuovoValore ) {
        this.csrfToken_wrapper = nuovoValore;
      },
      deep: true
    }

  }
}


</script>

<style scoped>
  nav>* {
    padding: 0 1rem;
  }
</style>