<template>
  <!-- Lista documenti caricati da un Uploader per un Consumer -->
  <table>   <!-- Ogni riga è un documento -->
    <thead> <!-- Intestazione tabella -->
      <tr v-for="nomeColonna in nomiColonneIntestazione" :key="nomeColonna">
        <td>{{ nomeColonna }}</td>
      </tr>
    </thead>
    <tbody>
    </tbody>
  </table>
</template>

<script>
import {richiestaGet} from "../utils/httpUtils";

export default {
  name: "ListaDocumenti",
  data() {
    return {
      nomiColonneIntestazione: []
    }
  },
  created() {

    /** Riempie l'intestazione della tabella dei documenti.*/
    richiestaGet(process.env.VUE_APP_GET_INTESTAZIONE_TABELLA_DOCUMENTI)
              .then(risposta => {
                const arrayCampiIntestazioneListaDocumenti = risposta.data;
                this.nomiColonneIntestazione = arrayCampiIntestazioneListaDocumenti;
              })
              .catch( rispostaErrore => {
                console.error("Errore durante il caricamento dell'intestazione della lista dei documenti: " + rispostaErrore );
                return Promise.reject(rispostaErrore);
                // TODO : gestire l'errore (invio mail ai gestori?)
                // TODO : cercare tutti i catch nel progetto e fare un gestore di eccezioni unico
              });
  }
}
</script>

<style scoped>

</style>