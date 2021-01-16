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
    <!-- Ogni riga è un documento -->
    <tr v-for="(idDocumento, documento) in Object.fromEntries(elencoDocumentiDaMostrare)"
        :key="idDocumento">
      <td v-for="(numeroColonnaQuestaTabella, propertyQuestaColonna) in nomiColonneIntestazione.filter( nomeColonna => nomeColonna !== nomePropLinkDownload )"
          :key="numeroColonnaQuestaTabella"
          id="{{idDocumento}}">
        {{ documento[propertyQuestaColonna] }}
      </td>
      <td>
        <a href="{{ documento[nomePropLinkDownload] }}">Download</a> <!-- Link download documento -->
      </td>
    </tr>
    </tbody>
  </table>

  <p v-else>Nessun documento disponibile.</p>

</template>

<script>
export default {
  name: "TabellaDocumenti",
  props: [

      /** Nomi delle colonne nell'intestazione della tabella.*/
      "nomiColonneIntestazione",

      /** Elenco dei documenti da mostrare.*/
      "elencoDocumentiDaMostrare",

    /** In un documento, il nome della property (se presente)
     * il cui valore è un link di download di quel documento.*/
    "nomePropLinkDownload"
  ]
}
</script>

<style scoped>

</style>