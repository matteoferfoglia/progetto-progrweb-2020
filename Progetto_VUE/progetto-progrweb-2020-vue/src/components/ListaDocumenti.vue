<template>
  <!-- Lista documenti caricati da un Uploader per un Consumer -->
  <table v-if="layoutCaricato && elencoDocumenti.length>0">   <!-- Ogni riga è un documento -->
    <thead>
      <tr v-for="nomeColonna in nomiColonneIntestazione" :key="nomeColonna">
        <td>{{ nomeColonna }}</td>
      </tr>
    </thead>
    <tbody v-for="entryDocumento in elencoDocumenti" :key="entryDocumento">
      <tr v-for="unaProprietaDelDocumento in entryDocumento" :key="unaProprietaDelDocumento">
        <td>{{ unaProprietaDelDocumento }}</td>
      </tr>
    </tbody>
  </table>
</template>

<script>
import {richiestaGet} from "../utils/httpUtils";

// TODO : Vanno inoltre mostrati i documenti sotto forma di tabella, ordinati dal più recente al meno recente,
// TODO :  avendo comunque in cima sempre quelli non ancora letti);
// TODO : La tabella deve mostrare il nome del
// TODO :  documento, la data di caricamento e l’eventuale data di lettura da parte del Consumer
// TODO : Questa maschera mostra anche la lista degli hashtag collegati ai documenti presenti, e l’utente può
// TODO :  filtrarli selezionando l’hash tag corrispondente

export default {
  name: "ListaDocumenti",
  data() {
    return {
      layoutCaricato: false,  // diventa true quando il layout è caricato
      nomiColonneIntestazione: [],
      elencoDocumenti: [],
      linkATuttiIDocumentiPerQuestoConsumer: [],
      hashtagsCollegatiAiDocumentiPresenti: []
    }
  },
  created() {

    const caricaContenuto = async () => {   // Grazie ad async, restituisce una Promise (usata dopo)
      await riempiIntestazioneTabella(this);
      await getElencoDocumentiPerQuestoConsumer(this); // TODO
    }

    // created() è un processo sincrono, quindi non si può usare async:
    // risolto usando un flag (layoutCaricato) che cambia stato quando è tutto caricato

    caricaContenuto().then(   ()    => this.layoutCaricato = true )
                     .catch( errore => {
                       // TODO : gestire questo errore
                       console.err("Errore nel caricamento del componente " + this.$options.name + ": " + errore);
                       alert("Errore durante il caricamento.")
                     });

  }
}

/** Riempie l'intestazione della tabella dei documenti.
 * Restituisce una promise.
 * @param istanza del componente Vue.*/
const riempiIntestazioneTabella = async that => {
  richiestaGet(process.env.VUE_APP_GET_INTESTAZIONE_TABELLA_DOCUMENTI)
    .then(  risposta       => that.nomiColonneIntestazione = risposta.data )
    .catch( rispostaErrore => {
      console.error("Errore durante il caricamento dell'intestazione della lista dei documenti: " + rispostaErrore );
      return Promise.reject(rispostaErrore);
      // TODO : gestire l'errore (invio mail ai gestori?)
      // TODO : cercare tutti i catch nel progetto e fare un gestore di eccezioni unico
    });
}

/** Carica tutti i documenti destinati al Consumer
 * attualmente autenticato. Con <i>documento</i>, in questo
 * contesto, si intende la sua rappresentazione come oggetto,
 * senza il contenuto del vero documento (è un'astrazione).
 * Restituisce una Promise.
 * @param istanza del componente Vue.*/
const getElencoDocumentiPerQuestoConsumer = async that => {
  richiestaGet(process.env.VUE_APP_GET_DOCUMENTI_CONSUMER)
      .then(  risposta       => that.elencoDocumenti = risposta.data )
      .catch( rispostaErrore => {
        console.error("Errore durante il caricamento dell'intestazione della lista dei documenti: " + rispostaErrore );
        return Promise.reject(rispostaErrore);
        // TODO : gestire l'errore (invio mail ai gestori?)
        // TODO : cercare tutti i catch nel progetto e fare un gestore di eccezioni unico
      });
}

</script>

<style scoped>
</style>