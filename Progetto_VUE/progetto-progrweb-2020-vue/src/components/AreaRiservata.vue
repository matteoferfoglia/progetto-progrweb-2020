<template>
  <h1>Benvenuto nell'area riservata</h1>

  <Form @submit.prevent="logout" @csrf-token-ricevuto="csrfToken = $event">
    <input type="submit" value="Logout">
  </Form>

  <Consumer v-if="isConsumer()"/>
  <Uploader v-else-if="isUploader()"/>
  <Administrator v-else-if="isAdministrator()"/>

</template>

<script>

/** Form viene mostrato solo dopo aver ricevuto dal server il CSRF token.*/

// TODO : QUESTO COMPONENTE è INTERAMENTE DA IMPLEMENTARE

import {richiestaGet} from "../utils/http";
import Form from "./FormConCsrfToken";
import {eliminaTokenAutenticazione} from "../utils/autenticazione";
import Consumer from "../views/attori/Consumer";
import Uploader from "../views/attori/Uploader";
import Administrator from "../views/attori/Administrator";

// TODO : refactoring : molto di questo componente è in comune con LoginUtenteGiaRegistrato

export default {
  name: 'AreaRiservata',
  components: {Consumer, Uploader, Administrator, Form},
  data: function () {
    return {
      csrfToken: undefined,

      /** Il tipo di utente attualmente autenticato.*/
      tipoUtenteAutenticato: undefined, // Administrator/Uploader/Consumer

    }
  },
  created() {
    richiestaGet(process.env.VUE_APP_GET_TIPO_UTENTE_AUTENTICATO)
        .then(  risposta       => this.tipoUtenteAutenticato = risposta.data )
        .catch( rispostaErrore => {
          console.error("Errore durante il caricamento delle informazioni: " + rispostaErrore );
          return Promise.reject(rispostaErrore);
          // TODO : gestire l'errore (invio mail ai gestori?)
          // TODO : cercare tutti i catch nel progetto e fare un gestore di eccezioni unico
        });
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
      const parametriRichiestaGet = {[process.env.VUE_APP_CSRF_INPUT_FIELD_NAME]: this.csrfToken};
      richiestaGet(process.env.VUE_APP_LOGOUT_SERVER_URL, parametriRichiestaGet)
          .catch(risposta => console.log("Logout fallito !! " + risposta) )          // TODO : gestire il caso di logout fallito (può succedere??)
          .finally( () => { // logout sul client
            eliminaTokenAutenticazione();
            this.$router.push({path: process.env.VUE_APP_ROUTER_ROOT_PATH})  //  todo : funziona, ma avrei preferito con push (per evitare il reload dell'intera pagina, ma solo del componente)
          });

      // TODO : Alternative al refresh della pagina (nel finally):
      //this.$emit("logout");   // TODO : rivedere questa parte
      //this.$router.go(0);   //
      //this.$router.push({ name : process.env.VUE_APP_ROUTER_NOME_COMPONENTE_SCHERMATA_INIZIALE});// redirect a componente root togliendo il token di autenticazione

    }
  }
}

</script>

<style>
</style>
