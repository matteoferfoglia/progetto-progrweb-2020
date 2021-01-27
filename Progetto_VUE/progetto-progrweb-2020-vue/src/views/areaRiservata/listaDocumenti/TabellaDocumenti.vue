<template>
  <!-- Componente per mostrare una lista di documenti in forma tabellare -->

  <FormConCsrfToken @csrf-token-ricevuto="$emit('csrf-token-ricevuto', $event)"
                    v-if="mappaDocumentiPerUnConsumer.get().size > 0"          >
    <!-- Eliminazione di un documento comporta il cambio di stato (serve token CSRF) -->

    <table>
      <!-- Tabella dei documenti --><!-- TODO : aggiungere un altezza massima nello stile della tabella  -->
      <thead>
      <tr>
        <td v-for="nomeColonna in nomiPropDocumenti"
            :key="nomeColonna">
          {{ camelCaseToHumanReadable(nomeColonna) }}
        </td>
      </tr>
      </thead>
      <tbody>
      <tr v-for="(documento, indice) in Object.fromEntries(mappaDocumentiPerUnConsumer.get())"
          :key="indice"><!-- Ogni riga è un documento -->
        <td v-for="propertyQuestaColonna in nomiPropDocumenti.filter( nomeColonna => nomeColonna!==NOME_PROP_LINK_DOWNLOAD_DOCUMENTO &&
                                                                                   nomeColonna!==NOME_PROP_LINK_DELETE_DOCUMENTO     )"
            :key="propertyQuestaColonna" >
          {{ documento[propertyQuestaColonna] }}
        </td>
        <td v-if="NOME_PROP_LINK_DOWNLOAD_DOCUMENTO">
          <a :href=documento[NOME_PROP_LINK_DOWNLOAD_DOCUMENTO]
             download
             @click.prevent="scaricaDocumento(documento[NOME_PROP_LINK_DOWNLOAD_DOCUMENTO], documento[NOME_PROP_NOME_DOCUMENTO])">Download</a> <!-- Link download documento -->
        </td>
        <td v-if="NOME_PROP_LINK_DELETE_DOCUMENTO">
          <a :href="documento[NOME_PROP_LINK_DELETE_DOCUMENTO]"
             @click.prevent="eliminaDocumento(documento[NOME_PROP_LINK_DELETE_DOCUMENTO])">Elimina</a> <!-- Link eliminazione documento -->
        </td>
      </tr>
      </tbody>
    </table>


  </FormConCsrfToken>

  <p v-else>Nessun documento disponibile.</p>

</template>

<script>
import {camelCaseToHumanReadable} from "../../../utils/utilitaGenerale";
import {richiestaDelete, richiestaGet} from "../../../utils/http";
import {MappaDocumenti, ordinaMappaSuDataCaricamentoConNonVisualizzatiDavanti} from "../../../utils/documenti";
import FormConCsrfToken from "../../../components/layout/FormConCsrfToken";
export default {
  name: "TabellaDocumenti",
  components: {FormConCsrfToken},
  emits: [
    /**Evento emesso dopo aver aggiunto un nuovo documento, quando
     * richiesto dall'elemento padre.*/
    'nuovo-documento-mostrato',

    /** Evento emesso quando riceve un token CSRF da un componente figlio.*/
    'csrf-token-ricevuto'

  ],
  props: [

    /** Identificativo dell'attore associato con questi documenti: se è un
     * Uploader che li sta visualizzando, allora questo identificativo è
     * quello del Consumer a cui i documenti sono destinati, viceversa,
     * se è un Consumer a visualizzare, questo identificativo è quello del-
     * l'Uploader che li ha caricati.*/
    "idAttoreRiferimentoDocumenti",

    /** Flag: true se l'utente che sta visualizzando questa lista può
     * eliminare un documento dalla lista. Se non specificato, si assume
     * che l'utente <strong>non</strong> non possa eliminare documenti.*/
    "puoEliminareUnDocumento",

    /** Nuovo documento da aggiungere alla lista. Vedere descrizione
     * nel componente padre. Quando cambia stato ed è definito, il
     * documento che questa property rappresenta viene aggiunto alla
     * lista attualmente mostrata.*/
    "oggetto_idDocumentoDaAggiungere_proprietaDocumentoDaAggiungere",

    /** Nome del Consumer a cui questi documenti si riferiscono.*/
    "nomeConsumer",

    /** Token CSRF ricevuto dal padre.*/
    "csrfToken"
  ],
  data() {
    return {

      // Variabili inizializzate in created() in base ai dati ricevuti dal server

      /** Mappa dei documenti per uno specifico Consumer.
       * Ogni entry rappresenta un documento nella forma:
       *    [ idDocumento, { oggetto con proprietà del documento } ]
       */
      mappaDocumentiPerUnConsumer: new MappaDocumenti(),

      /** Nome delle colonne di intestazione della tabella coi documenti.*/
      nomiPropDocumenti: [],

      /** In un documento, il nome della property (se presente)
       * il cui valore è un link di download di quel documento.*/
      NOME_PROP_LINK_DOWNLOAD_DOCUMENTO: "Link download",

      /** In un documento, il nome della property (se presente)
       * il cui valore è un link di eliminazione di quel documento.*/
      NOME_PROP_LINK_DELETE_DOCUMENTO: "Link eliminazione",

      /** In un documento, il nome della property (se presente)
       * il cui valore è il nome di quel documento.*/
      NOME_PROP_NOME_DOCUMENTO: undefined,

      /** "Import" della funzione per usarla nel template.*/
      camelCaseToHumanReadable: camelCaseToHumanReadable,

      // Wrapper
      csrfToken_wrapper: this.csrfToken

    }
  },
  created() {

    // TODO : aggiungere un "loader" (flag layoutCaricato)

    // Richiede mappa idFile-propFile per questo consumer
    richiestaGet( process.env.VUE_APP_URL_GET_MAPPA_FILE_DI_UPLOADER_PER_CONSUMER +
                    "/" + this.idAttoreRiferimentoDocumenti )
        // Crea mappa
        .then( rispostaConMappaFile_id_prop => new Map(Object.entries( rispostaConMappaFile_id_prop ) ) )
        // Ordina mappa
        .then( ordinaMappaSuDataCaricamentoConNonVisualizzatiDavanti )

        // In ogni documento, aggiungi link di cancellazione e di download, poi restituisci la mappa
        .then( mappa =>
            new Map(
                Array.from( mappa.entries() )
                     .map( unDocumento => this.aggiungiUrlDownloadEliminazioneDocumentoERestituisciEntryDocumento(unDocumento) )
            )
        )

        // Salva la mappa
        .then( mappa => this.mappaDocumentiPerUnConsumer.set( mappa ) )
        .catch( console.error );


    // Richiede il nome della property di un documento contenente il nome del documento stesso
    richiestaGet(process.env.VUE_APP_URL_GET_NOME_PROP_NOME_DOCUMENTO)
        .then( nomeProp => this.NOME_PROP_NOME_DOCUMENTO = nomeProp )
        .catch( console.error );


    // Richiede l'elenco dei nomi delle properties dei documenti
    richiestaGet( process.env.VUE_APP_URL_GET_NOMI_TUTTE_LE_PROP_DOCUMENTI )
        // Aggiunge alle properties dei documenti le colonne con il link di download ed eliminazione
        // documento (da creare dinamicamente), poi salva l'array
        .then( nomiPropDocumenti => {
          nomiPropDocumenti.push( this.NOME_PROP_LINK_DOWNLOAD_DOCUMENTO );
          if( this.puoEliminareUnDocumento ) {
            nomiPropDocumenti.push( this.NOME_PROP_LINK_DELETE_DOCUMENTO )
          }
          this.nomiPropDocumenti = nomiPropDocumenti;
        })
        .catch( console.error );

  },
  methods: {

    /** Funzione per eliminare un documento dall'elenco attualmente mostrato,
     * a seguito della richiesta di eliminazione da parte dell'utente.
     * Questo metodo <strong>non</strong> richiede al server l'eliminazione
     * d tale documento*/
    rimuoviDocumentoDaListaAttualmenteMostrata(urlEliminazioneDocumento) {

      // Id del documento appeso in fondo all'url
      const idDocumentoEliminato = urlEliminazioneDocumento
          .substring(urlEliminazioneDocumento.lastIndexOf("/")+1);

      this.mappaDocumentiPerUnConsumer.get().delete(idDocumentoEliminato);

    },

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
      richiestaDelete( urlEliminazioneDocumento, {
        [process.env.VUE_APP_FORM_CSRF_INPUT_FIELD_NAME]: this.csrfToken_wrapper
      })
          .then( () => {
            this.rimuoviDocumentoDaListaAttualmenteMostrata(urlEliminazioneDocumento);
            alert("Documento eliminato");
          })
          .catch( console.error );
    },

    /** Data un'entry della mappa restituita dal server quando
     * gli si richiede l'elenco dei documenti (in pratica: dato
     * un documento, nella forma [ idDocumento, { proprietà documento} ] ),
     * questo metodo aggiunge alle proprietà di quel documento gli url
     * per il download e l'eliminazione del documento stesso.
     * Infine restituisce la entry corrispondente al documento, con
     * le properties (gli url) appena aggiunte.
     * @param unDocumento nella forma di una entry, cioè un array con due
     *                    elementi:   [ idDocumento, { proprietà documento} ] */
    aggiungiUrlDownloadEliminazioneDocumentoERestituisciEntryDocumento(unDocumento) {

      // Decomposizione di ogni entry
      const idDocumento = unDocumento[0];
      const propDocumento = unDocumento[1];

      propDocumento[this.NOME_PROP_LINK_DELETE_DOCUMENTO] = process.env.VUE_APP_URL_DELETE_DOCUMENTO_DI_QUESTO_UPLOADER + "/" + idDocumento;

      if( this.puoEliminareUnDocumento ) {
        propDocumento[this.NOME_PROP_LINK_DOWNLOAD_DOCUMENTO] = process.env.VUE_APP_URL_DOWNLOAD_DOCUMENTO_DI_QUESTO_UPLOADER + "/" + idDocumento;
      }

      return [idDocumento, propDocumento];

    },

    /** Metodo per aggiungere un nuovo documento alla lista di quelli mostrati.
     * Questo metodo <strong>non</strong> interroga il server, ma si aspetta come
     * parametro il nuovo documento nella forma di un oggetto come segue:
     *    { idDocumento : {proprieta del documento} }
     *
     * @param oggetto_idDocumentoDaAggiungere_proprietaDocumentoDaAggiungere
     */
    aggiungiNuovoDocumentoAllaListaMostrata( oggetto_idDocumentoDaAggiungere_proprietaDocumentoDaAggiungere ) {

      // Aggiungi url nelle properties
      oggetto_idDocumentoDaAggiungere_proprietaDocumentoDaAggiungere = new Map(
          [
            this.aggiungiUrlDownloadEliminazioneDocumentoERestituisciEntryDocumento(
                Object.entries(oggetto_idDocumentoDaAggiungere_proprietaDocumentoDaAggiungere)[0]
            )
          ]
      );

      // Aggiungi il documento all'elenco di quelli mostrati da questo componente
      this.mappaDocumentiPerUnConsumer.set(
          new Map( [ ...oggetto_idDocumentoDaAggiungere_proprietaDocumentoDaAggiungere,  // merge della nuova entry in cima alla mappa
                     ... this.mappaDocumentiPerUnConsumer.get() ] )
      );
    }

  },
  watch: {

    csrfToken : {
      immediate: true,
      deep: true,
      handler(nuovoValore) {
        this.csrfToken_wrapper = nuovoValore;
      }
    },

    /** Cambia stato se è stato caricato un nuovo documento, quindi
     * è necessario aggiornare la vista.*/
    oggetto_idDocumentoDaAggiungere_proprietaDocumentoDaAggiungere: {
      immediate: true,
      deep: true,
      handler( nuovoDocumento ) {
        if( nuovoDocumento ) {  // che non sia falsy
          this.aggiungiNuovoDocumentoAllaListaMostrata( nuovoDocumento );
          this.$emit( 'nuovo-documento-mostrato' );
        }
      }
    }
  }
}
</script>

<style scoped>

</style>