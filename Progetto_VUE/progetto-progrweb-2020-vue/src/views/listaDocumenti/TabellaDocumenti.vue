<template>
  <!-- Componente per mostrare una lista di documenti in forma tabellare -->

  <table v-if="elencoDocumentiDaMostrare.size > 0">
    <!-- Tabella dei documenti -->
    <thead>
    <tr>
      <td v-for="nomeColonna in nomiColonneIntestazione"
          :key="nomeColonna">
        {{ nomeColonna }}
      </td>
    </tr>
    </thead>
    <tbody>
    <tr v-for="(documento, idDocumento) in Object.fromEntries(elencoDocumentiDaMostrare)"
        :key="idDocumento"><!-- Ogni riga è un documento -->
      <td v-for="(propertyQuestaColonna, indiceColonna) in nomiColonneIntestazione.filter( nomeColonna => nomeColonna!==nomePropLinkDownload && nomeColonna!==nomePropLinkElimina )"
          :key="indiceColonna"
          id="{{idDocumento}}">
        {{ documento[propertyQuestaColonna] }}
      </td>
      <td v-if="nomePropLinkDownload">
        <a href="{{ documento[nomePropLinkDownload] }}">Download</a> <!-- Link download documento -->
      </td>
      <td v-if="nomePropLinkElimina">
        <a href="{{ documento[nomePropLinkDownload] }}">Elimina</a> <!-- Link download documento -->
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
export default {
  name: "TabellaDocumenti",
  components: {FormConCsrfToken},
  props: [

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
  ]
}
</script>

<style scoped>

</style>