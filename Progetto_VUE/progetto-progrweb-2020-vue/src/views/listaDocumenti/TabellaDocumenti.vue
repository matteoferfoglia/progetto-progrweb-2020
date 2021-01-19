<template>
  <!-- Componente per mostrare una lista di documenti in forma tabellare -->

  <table v-if="elencoDocumentiDaMostrare.get().size > 0">
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
    <tr v-for="(documento, indice) in Object.fromEntries(elencoDocumentiDaMostrare.get())"
        :key="indice"><!-- Ogni riga è un documento -->
      <td v-for="propertyQuestaColonna in nomiColonneIntestazione.filter( nomeColonna => nomeColonna!==nomePropLinkDownload &&
                                                                                         nomeColonna!==nomePropLinkElimina     )"
          :key="propertyQuestaColonna" >
        {{ documento[propertyQuestaColonna] }}
      </td>
      <td v-if="nomePropLinkDownload">
        <a :href=documento[nomePropLinkDownload]
           download
           @click.prevent="scaricaDocumento(documento[nomePropLinkDownload], documento[nomePropNomeDocumento])">Download</a> <!-- Link download documento -->
      </td>
      <td v-if="nomePropLinkElimina">
        <a :href="documento[nomePropLinkElimina]"
           @click.prevent="eliminaDocumento(documento[nomePropLinkElimina])">Elimina</a> <!-- Link eliminazione documento -->
      </td>
    </tr>
    </tbody>
  </table>

  <p v-else>Nessun documento disponibile.</p>

</template>

<script>
import {camelCaseToHumanReadable} from "../../utils/utilitaGenerale";
import {richiestaDelete, richiestaGet} from "../../utils/http";
export default {
  name: "TabellaDocumenti",
  props: [

    // Passaggio di oggetti tramite props, Fonte: https://stackoverflow.com/a/57200056

    /** Nomi delle colonne nell'intestazione della tabella.*/
    "nomiColonneIntestazione",

    /** Elenco dei documenti da mostrare, di tipo {@link MappaDocumenti}.*/
    "elencoDocumentiDaMostrare",

    /** In un documento, il nome della property (se presente)
     * il cui valore è un link di download di quel documento.*/
    "nomePropLinkDownload",

    /** In un documento, il nome della property (se presente)
     * il cui valore è un link di eliminazione di quel documento.*/
    "nomePropLinkElimina",

    /** Nome del Consumer a cui questi documenti si riferiscono.*/
    "nomeConsumer"
  ],
  data() {
    return {

      /** "Import" della funzione per usarla nel template.*/
      camelCaseToHumanReadable: camelCaseToHumanReadable,

      /** In un documento, il nome della property (se presente)
       * il cui valore è il nome di quel documento.*/
      nomePropNomeDocumento: undefined

    }
  },
  created() {

      // Richiede il nome della property di un file contenente il nome del documento
      richiestaGet(process.env.VUE_APP_GET_NOME_PROP_NOME_DOCUMENTO)
          .then( nomeProp => this.nomePropNomeDocumento = nomeProp )
          .catch( console.error );

  },
  methods: {

    /** Metodo per il download di un documento (dipende dall'URL)
     * (<a href="https://stackoverflow.com/q/33247716">Fonte</a>).*/
    scaricaDocumento( urlDocumento, nomeDocumento ) {
      richiestaGet( urlDocumento )
        .then( risposta => {
          const blob = new Blob( [risposta], {type: "octet/stream"} );

          const downloadUrl = window.URL.createObjectURL(blob);
          const a = document.createElement("a");
          a.setAttribute("download", nomeDocumento);
          a.setAttribute("style","display: none");
          a.href = downloadUrl;
          document.body.appendChild(a);
          a.click();
          URL.revokeObjectURL(downloadUrl);

        })
        .catch( console.error );
    },

    /** Metodo per la cancellazione di un documento (dipende dall'URL).*/
    eliminaDocumento( urlEliminazioneDocumento ) {
      richiestaDelete( urlEliminazioneDocumento )
          .then( () => {
            this.$emit( "documento-eliminato", urlEliminazioneDocumento );
            alert("Documento eliminato");
          })
          .catch( console.error );
    }

  }
}
</script>

<style scoped>

</style>