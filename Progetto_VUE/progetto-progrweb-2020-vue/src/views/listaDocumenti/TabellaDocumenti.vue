<template>
  <!-- Componente per mostrare una lista di documenti in forma tabellare -->

  <table v-if="elencoDocumentiDaMostrare.size > 0">
    <!-- Tabella dei documenti -->
    <thead>
    <tr>
      <td v-for="nomeColonna in nomiColonneIntestazione"
          :key="nomeColonna">
        {{ camelCaseToHumanReadable(nomeColonna) }}
      </td>
    </tr>
    </thead>
    <tbody>
    <tr v-for="documento in Object.fromEntries(elencoDocumentiDaMostrare_wrapper)"
        :key="documento[nomePropLinkDownload]"><!-- Ogni riga è un documento -->
      <td v-for="propertyQuestaColonna in nomiColonneIntestazione.filter( nomeColonna => nomeColonna!==nomePropLinkDownload && nomeColonna!==nomePropLinkElimina )"
          :key="propertyQuestaColonna" >
        {{ documento[propertyQuestaColonna] }}
      </td>
      <td v-if="nomePropLinkDownload">
        <a :href=documento[nomePropLinkDownload]>Download</a> <!-- Link download documento -->
      </td>
      <td v-if="nomePropLinkElimina">
        <a :href="documento[nomePropLinkElimina]">Elimina</a> <!-- Link download documento -->
      </td>
    </tr>
    </tbody>
  </table>

  <p v-else>Nessun documento disponibile.</p>

  <FormConCsrfToken v-if="possibileAggiungereDocumento">
    <p>Carica un documento per {{ nomeConsumer }}</p>
    <input type="text">
  </FormConCsrfToken>

</template>

<script>
import FormConCsrfToken from "../../components/FormConCsrfToken";
import {camelCaseToHumanReadable} from "../../utils/utilitaGenerale";
export default {
  name: "TabellaDocumenti",
  components: {FormConCsrfToken},
  props: [

    // Passaggio di oggetti tramite props, Fonte: https://stackoverflow.com/a/57200056

    /** Nomi delle colonne nell'intestazione della tabella.*/
    "nomiColonneIntestazione",

    /** Elenco dei documenti da mostrare.*/
    "elencoDocumentiDaMostrare",

    /** In un documento, il nome della property (se presente)
     * il cui valore è un link di download di quel documento.*/
    "nomePropLinkDownload",

    /** In un documento, il nome della property (se presente)
     * il cui valore è un link di eliminazione di quel documento.*/
    "nomePropLinkElimina",

    /** Flag: true se è possibile aggiungere un documento.*/
    "possibileAggiungereDocumento",

    /** Nome del Consumer a cui questi documenti si riferiscono.*/
    "nomeConsumer"
  ],
  data() {
    return {
      camelCaseToHumanReadable: camelCaseToHumanReadable,

      /** Wrapper per {@link #elencoDocumentiDaMostrare} per modificarne
       * i valori quando il padre li aggiorna.*/
      elencoDocumentiDaMostrare_wrapper: new Map()
    }
  },
  watch: {

    /** Watcher per aggiornare l'elenco dei documenti se modificato dal paadre
     * (<a href="https://stackoverflow.com/a/42134176">Fonte</a>).
     * @param valoreAggiornato */
    elencoDocumentiDaMostrare: {
      handler(valoreAggiornato) {
        this.elencoDocumentiDaMostrare_wrapper = valoreAggiornato;
      }
    }
  }
}
</script>

<style scoped>

</style>