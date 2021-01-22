<template v-if="isLayoutCaricato">

  <HeaderAreaRiservata :nomeUtenteAutenticato="nomeAttoreAutenticato"
                       @logout="logout"
                       @csrf-token-modificato="csrfToken = $event" />

  <h1>Area riservata</h1>

  <router-view :tipoAttoreAutenticato="tipoAttoreAutenticato"
               @nominativo-attore-modificato="nomeAttoreAutenticato = $event"
               @csrf-token-modificato="csrfToken = $event"/>

</template>

<script>

import {richiestaGet} from "../../utils/http";
import {eliminaTokenAutenticazione} from "../../utils/autenticazione";
import HeaderAreaRiservata from "../HeaderAreaRiservata";

export default {
  name: 'AreaRiservata',
  components: {HeaderAreaRiservata},
  data: function () {
    return {

      /** CSRF token.
       * Può essere sovrascritto dai componenti se per qualche motivo
       * ne chiedono un altro (perché cambia il cookie associato al token).*/
      csrfToken: undefined,

      /** Il nome dell'attore attualmente autenticato.*/
      nomeAttoreAutenticato: undefined,

      /** Il tipo di attore attualmente autenticato (Administrator/Uploader/Consumer).*/
      tipoAttoreAutenticato: undefined,

      /** Flag: diventa true quando il componente viene caricato.*/
      isLayoutCaricato: false

    }
  },
  created() {

    const caricaQuestoComponente = async () => {

      await getTipoAttoreAttualmenteAutenticato()
              .then(tipo => this.tipoAttoreAutenticato = tipo)
              .catch(console.error);

      await getNomeAttoreAttualmenteAutenticato()
              .then(nome => this.nomeAttoreAutenticato = nome)
              .catch(console.error);

    };

    caricaQuestoComponente()
      .then( () => this.isLayoutCaricato = true )
      .catch( console.error );


  },
  methods: {

    logout() {

      // Richiesta di logout al server
      const parametriRichiestaGet = {[process.env.VUE_APP_CSRF_INPUT_FIELD_NAME]: this.csrfToken};
      richiestaGet(process.env.VUE_APP_URL_LOGOUT, parametriRichiestaGet)
          .catch(risposta => console.log("Logout fallito !! " + risposta) )          // TODO : gestire il caso di logout fallito (può succedere??)
          .finally( () => { // logout sul client
            this.csrfToken = undefined;
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

/** Restituisce il tipo dell'attore attualmente autenticato.*/
const getTipoAttoreAttualmenteAutenticato = async () => {

  return richiestaGet(process.env.VUE_APP_URL_GET_TIPO_UTENTE_AUTENTICATO)
      .then(  risposta       => risposta )
      .catch( rispostaErrore => {
        console.error("Errore durante il caricamento delle informazioni: " + rispostaErrore );
        return Promise.reject(rispostaErrore);
        // TODO : gestire l'errore (invio mail ai gestori?)
        // TODO : cercare tutti i catch nel progetto e fare un gestore di eccezioni unico
      });

}

/** Restituisce il nome dell'attore attualmente autenticato.*/
const getNomeAttoreAttualmenteAutenticato = async () => {

  return richiestaGet(process.env.VUE_APP_URL_GET_NOME_QUESTO_ATTORE_AUTENTICATO)
      .then(  risposta       =>  risposta )
      .catch( rispostaErrore => {
        console.error("Errore durante il caricamento delle informazioni: " + rispostaErrore );
        return Promise.reject(rispostaErrore);
      });

}

</script>

<style>
</style>
