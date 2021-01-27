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
        <td v-if="urlDownloadDocumento">
          <a :href=documento[NOME_PROP_LINK_DOWNLOAD_DOCUMENTO]
             download
             @click.prevent="scaricaDocumento(documento[NOME_PROP_LINK_DOWNLOAD_DOCUMENTO],
                                              documento[NOME_PROP_NOME_DOCUMENTO])">
            <!-- Link download, Fonte icona: https://icons.getbootstrap.com/icons/cloud-download/ --><!-- TODO : icone da aggiungere via CSS -->
            <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" class="bi bi-cloud-download" viewBox="0 0 16 16">
              <path d="M4.406 1.342A5.53 5.53 0 0 1 8 0c2.69 0 4.923 2 5.166 4.579C14.758 4.804 16 6.137 16 7.773 16 9.569 14.502 11 12.687 11H10a.5.5 0 0 1 0-1h2.688C13.979 10 15 8.988 15 7.773c0-1.216-1.02-2.228-2.313-2.228h-.5v-.5C12.188 2.825 10.328 1 8 1a4.53 4.53 0 0 0-2.941 1.1c-.757.652-1.153 1.438-1.153 2.055v.448l-.445.049C2.064 4.805 1 5.952 1 7.318 1 8.785 2.23 10 3.781 10H6a.5.5 0 0 1 0 1H3.781C1.708 11 0 9.366 0 7.318c0-1.763 1.266-3.223 2.942-3.593.143-.863.698-1.723 1.464-2.383z"/>
              <path d="M7.646 15.854a.5.5 0 0 0 .708 0l3-3a.5.5 0 0 0-.708-.708L8.5 14.293V5.5a.5.5 0 0 0-1 0v8.793l-2.146-2.147a.5.5 0 0 0-.708.708l3 3z"/>
            </svg>
          </a>
        </td>
        <td v-if="urlEliminazioneDocumento">
          <a :href="documento[NOME_PROP_LINK_DELETE_DOCUMENTO]"
             @click.prevent="eliminaDocumento(documento[NOME_PROP_LINK_DELETE_DOCUMENTO])">
            <!-- Link eliminazione, Fonte icona: https://icons.getbootstrap.com/icons/trash/  --><!-- TODO : icone da aggiungere via CSS -->
            <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" class="bi bi-trash" viewBox="0 0 16 16">
              <path d="M5.5 5.5A.5.5 0 0 1 6 6v6a.5.5 0 0 1-1 0V6a.5.5 0 0 1 .5-.5zm2.5 0a.5.5 0 0 1 .5.5v6a.5.5 0 0 1-1 0V6a.5.5 0 0 1 .5-.5zm3 .5a.5.5 0 0 0-1 0v6a.5.5 0 0 0 1 0V6z"/>
              <path fill-rule="evenodd" d="M14.5 3a1 1 0 0 1-1 1H13v9a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V4h-.5a1 1 0 0 1-1-1V2a1 1 0 0 1 1-1H6a1 1 0 0 1 1-1h2a1 1 0 0 1 1 1h3.5a1 1 0 0 1 1 1v1zM4.118 4L4 4.059V13a1 1 0 0 0 1 1h6a1 1 0 0 0 1-1V4.059L11.882 4H4.118zM2.5 3V2h11v1h-11z"/>
            </svg>
          </a>
        </td>
      </tr>
      </tbody>
    </table>


  </FormConCsrfToken>

  <p v-else>Nessun documento disponibile.</p>

</template>

<script>
import {camelCaseToHumanReadable} from "../../utils/utilitaGenerale";
import {richiestaDelete, richiestaGet} from "../../utils/http";
import {MappaDocumenti, ordinaMappaSuDataCaricamentoConNonVisualizzatiDavanti} from "../../utils/documenti";
import FormConCsrfToken from "../layout/FormConCsrfToken";
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

    /** URL verso cui richiedere l'elenco dei documenti per un attore.*/
    "urlRichiestaElencoDocumentiPerUnAttore",

    /** URL verso cui richiedere il download di un documento.*/
    "urlDownloadDocumento",

    /** URL verso cui richiedere la cancellazione di un documento.
     * Se questa prop non è specificata, si assume che l'utente
     * <strong>non</strong> possa eliminare documenti.*/
    "urlEliminazioneDocumento",

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
      NOME_PROP_LINK_DOWNLOAD_DOCUMENTO: "Download",

      /** In un documento, il nome della property (se presente)
       * il cui valore è un link di eliminazione di quel documento.*/
      NOME_PROP_LINK_DELETE_DOCUMENTO: "Elimina",

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

    // Richiede mappa idFile-propFile per questo attore
    richiestaGet( this.urlRichiestaElencoDocumentiPerUnAttore )
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
          if( this.urlEliminazioneDocumento ) {
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

      propDocumento[this.NOME_PROP_LINK_DOWNLOAD_DOCUMENTO] = this.urlDownloadDocumento + "/" + idDocumento;

      if( this.urlEliminazioneDocumento ) {
        propDocumento[this.NOME_PROP_LINK_DELETE_DOCUMENTO] = this.urlEliminazioneDocumento + "/" + idDocumento;
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