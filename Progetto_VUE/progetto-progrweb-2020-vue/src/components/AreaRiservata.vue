<template v-if="isLayoutCaricato">
  <h1>Area riservata di {{ nomeAttoreAttualmenteAutenticato }}</h1>

  <Form @submit.prevent="logout" @csrf-token-ricevuto="csrfTokenLogout = $event">
    <input type="submit" value="Logout">
  </Form>

  <!-- CSRF token può essere sovrascritto dai componenti se per qualche motivo
       ne chiedono un altro (perché cambia il cookie) -->
  <Consumer      v-if="isConsumer()" />
  <Uploader      v-else-if="isUploader()"
                 @csrf-token-modificato="csrfTokenLogout = $event" />
  <Administrator v-else-if="isAdministrator()" />


  <router-view/>

</template>

<script>

import {richiestaGet} from "../utils/http";
import Form from "./FormConCsrfToken";
import {eliminaTokenAutenticazione} from "../utils/autenticazione";
import Consumer from "../views/attori/Consumer";
import Uploader from "../views/attori/uploader/Uploader";
import Administrator from "../views/attori/Administrator";

export default {
  name: 'AreaRiservata',
  components: {Consumer, Uploader, Administrator, Form},
  data: function () {
    return {

      /** Flag: true quando il componente è stato caricato.*/
      isLayoutCaricato: false,

      /** CSRF token.*/
      csrfTokenLogout: undefined,

      /** Il tipo di utente attualmente autenticato.*/
      tipoUtenteAutenticato: undefined, // Administrator/Uploader/Consumer

      /** Nome dell'attore attualmente autenticato.*/
      nomeAttoreAttualmenteAutenticato: undefined,

    }
  },
  created() {

    const caricaQuestoComponente = async () => {
      return getNomeAttoreAttualmenteAutenticato()
              .then(nome => this.nomeAttoreAttualmenteAutenticato = nome)
              .then(getTipoAttoreAttualmenteAutenticato)
              .then(tipo => this.tipoUtenteAutenticato = tipo)
              .catch(console.error);
    }

    caricaQuestoComponente()
      .then( () => this.isLayoutCaricato = true )
      .catch( console.error );


  },
  methods: {
    isConsumer() {
      return this.tipoUtenteAutenticato===process.env.VUE_APP_TIPO_UTENTE_AUTENTICATO_CONSUMER;
    },
    isUploader() {
      return this.tipoUtenteAutenticato===process.env.VUE_APP_TIPO_UTENTE_AUTENTICATO_UPLOADER;
    },
    isAdministrator() {
      return this.tipoUtenteAutenticato===process.env.VUE_APP_TIPO_UTENTE_AUTENTICATO_ADMINISTRATOR;
    },

    logout() {

      // Richiesta di logout al server
      const parametriRichiestaGet = {[process.env.VUE_APP_CSRF_INPUT_FIELD_NAME]: this.csrfTokenLogout};
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
