<template v-if="isLayoutCaricato">

  <HeaderAreaRiservata :nomeUtenteAutenticato="nomeAttoreAttualmenteAutenticato"
                       @logout="logout"
                       @csrf-token-modificato="csrfToken = $event" />

  <h1>Area riservata</h1>

  <Consumer      v-if="isConsumer()" />
  <Uploader      v-else-if="isUploader()"
                 @csrf-token-modificato="csrfToken = $event" />
  <Administrator v-else-if="isAdministrator()" />


  <router-view/>

</template>

<script>

import {richiestaGet} from "../../utils/http";
import {eliminaTokenAutenticazione} from "../../utils/autenticazione";
import Consumer from "../attori/Consumer";
import Uploader from "../attori/uploader/Uploader";
import Administrator from "../attori/Administrator";
import HeaderAreaRiservata from "../HeaderAreaRiservata";

export default {
  name: 'AreaRiservata',
  components: {HeaderAreaRiservata, Consumer, Uploader, Administrator},
  data: function () {
    return {

      /** Flag: true quando il componente è stato caricato.*/
      isLayoutCaricato: false,

      /** CSRF token.
       * Può essere sovrascritto dai componenti se per qualche motivo
       * ne chiedono un altro (perché cambia il cookie associato al token).*/
      csrfToken: undefined,

      /** Il tipo di utente attualmente autenticato.*/
      tipoAttoreAutenticato: undefined, // Administrator/Uploader/Consumer

      /** Nome dell'attore attualmente autenticato.*/
      nomeAttoreAttualmenteAutenticato: undefined,

    }
  },
  created() {

    const caricaQuestoComponente = async () => {
      return getNomeAttoreAttualmenteAutenticato()
              .then(nome => this.nomeAttoreAttualmenteAutenticato = nome)
              .then(getTipoAttoreAttualmenteAutenticato)
              .then(tipo => this.tipoAttoreAutenticato = tipo)
              .catch(console.error);
    }

    caricaQuestoComponente()
      .then( () => this.isLayoutCaricato = true )
      .catch( console.error );


  },
  methods: {
    isConsumer() {
      return this.tipoAttoreAutenticato===process.env.VUE_APP_TIPO_UTENTE_AUTENTICATO_CONSUMER;
    },
    isUploader() {
      return this.tipoAttoreAutenticato===process.env.VUE_APP_TIPO_UTENTE_AUTENTICATO_UPLOADER;
    },
    isAdministrator() {
      return this.tipoAttoreAutenticato===process.env.VUE_APP_TIPO_UTENTE_AUTENTICATO_ADMINISTRATOR;
    },

    logout() {

      // Richiesta di logout al server
      const parametriRichiestaGet = {[process.env.VUE_APP_CSRF_INPUT_FIELD_NAME]: this.csrfToken};
      richiestaGet(process.env.VUE_APP_LOGOUT_SERVER_URL, parametriRichiestaGet)
          .catch(risposta => console.log("Logout fallito !! " + risposta) )          // TODO : gestire il caso di logout fallito (può succedere??)
          .finally( () => { // logout sul client
            eliminaTokenAutenticazione();
            this.$router.push({path: process.env.VUE_APP_ROUTER_ROOT_PATH})
          });

      // TODO : Alternative al refresh della pagina (nel finally):
      //this.$emit("logout");   // TODO : rivedere questa parte
      //this.$router.go(0);   //
      //this.$router.push({ name : process.env.VUE_APP_ROUTER_NOME_COMPONENTE_SCHERMATA_INIZIALE});// redirect a componente root togliendo il token di autenticazione

    }
  }
}


/** Restituisce il nome dell'attore attualmente autenticato.*/
const getNomeAttoreAttualmenteAutenticato = async () => {

  return richiestaGet(process.env.VUE_APP_GET_NOME_QUESTO_ATTORE_AUTENTICATO)
      .then(  risposta       =>  risposta )
      .catch( rispostaErrore => {
        console.error("Errore durante il caricamento delle informazioni: " + rispostaErrore );
        return Promise.reject(rispostaErrore);
        // TODO : gestire l'errore (invio mail ai gestori?)
        // TODO : cercare tutti i catch nel progetto e fare un gestore di eccezioni unico
      });

}

/** Restituisce il tipo dell'attore attualmente autenticato.*/
const getTipoAttoreAttualmenteAutenticato = async () => {

  return richiestaGet(process.env.VUE_APP_GET_TIPO_UTENTE_AUTENTICATO)
      .then(  risposta       => risposta )
      .catch( rispostaErrore => {
        console.error("Errore durante il caricamento delle informazioni: " + rispostaErrore );
        return Promise.reject(rispostaErrore);
        // TODO : gestire l'errore (invio mail ai gestori?)
        // TODO : cercare tutti i catch nel progetto e fare un gestore di eccezioni unico
      });

}

</script>

<style>
</style>
