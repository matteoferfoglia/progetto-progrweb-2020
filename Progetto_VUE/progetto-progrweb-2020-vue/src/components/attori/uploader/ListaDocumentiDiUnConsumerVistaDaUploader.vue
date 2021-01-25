<template v-if="isLayoutCaricato">

  <!-- Componente per mostrare la lista di documenti di un Consumer,
       visualizzata da un Uploader -->

  <section>
    <p>Consumer: {{ nomeConsumer }}</p>
    <TabellaDocumenti :nomiColonneIntestazione  ="nomiPropDocumenti"
                      :elencoDocumentiDaMostrare="mappaDocumentiPerUnConsumer"
                      :nomePropLinkDownload     ="NOME_PROP_DOWNLOAD_DOCUMENTO"
                      :nomePropLinkElimina      ="NOME_PROP_DELETE_DOCUMENTO"
                      @documento-eliminato="rimuoviDocumentoDaLista"/>
  </section>

  <form @submit.prevent="caricaNuovoDocumento" :id="idForm_caricaNuovoDocumento">  <!-- TODO : aggiungere csrf token -->
    <!-- Fonte (Upload documento): https://stackoverflow.com/a/43014086 -->
    <p>Carica un nuovo documento per {{ nomeConsumer }}:</p>
    <label>Nome documento   <input type="text" v-model="nomeDocumento" required></label>
    <label>Lista di hashtag <input type="text" v-model="listaHashtag" placeholder="hashtag1, hashtag2"></label>
    <label>Documento        <input type="file" required></label><!-- TODO : caricare documento-->
    <input type="submit" value="Carica">
  </form>

  <button @click="chiudiListaDocumentiUnConsumer">Chiudi</button>



</template>

<script>
import TabellaDocumenti from "../../../views/areaRiservata/listaDocumenti/TabellaDocumenti";
import {richiestaGet, richiestaPostConFile} from "../../../utils/http";
import {MappaDocumenti, ordinaMappaSuDataCaricamentoConNonVisualizzatiDavanti} from "../../../utils/documenti";

export default {
  name: "ListaDocumentiDiUnConsumerVistaDaUploader",
  components: {TabellaDocumenti},

  // props (specifico consumer cui questo componente si riferisce) ottenute da Vue Router

  data() {
    return {

      /** Flag, true quando questo componente è caricato.*/
      isLayoutCaricato: false,

      /** Nome del consumer a cui i documenti si riferiscono.*/
      nomeConsumer: undefined,

      /** Identificativo del consumer a cui i documenti si riferiscono.*/
      idConsumer: undefined,

      /** Nome delle colonne di intestazione della tabella coi documenti.*/
      nomiPropDocumenti: [],

      /** Nome della colonna con l'url di download del documento.*/
      NOME_PROP_DOWNLOAD_DOCUMENTO: "Link download",

      /** Nome della colonna con l'url di eliminazione del documento.*/
      NOME_PROP_DELETE_DOCUMENTO: "Link eliminazione",

      /** Mappa documenti ( id => proprietà ) per uno specifico Consumer.
       * Questa mappa è caricata dinamicamente in base alle azioni
       * dell'utente.*/
      mappaDocumentiPerUnConsumer: new MappaDocumenti(),



      /** Valore dell'attributo "id" del form per il caricamento dei documenti.*/
      idForm_caricaNuovoDocumento: "caricaNuovoDocumento",

      // NOMI DEI PARAMETRI ATTESI DAL SERVER // TODO : variabili d'ambiente sia per client sia per server
      nomeParametro_nomeDocumento:             "nomeFile",
      nomeParametro_contenutoDocumento:        "contenutoFile",
      nomeParametro_idConsumerDestinatario:    "identificativoConsumerDestinatario",
      nomeParametro_listaHashtag:              "listaHashtag",


      // PROPRIETA CARICAMENTO NUOVO DOCUMENTO:
      nomeDocumento: "",
      listaHashtag: ""

    }
  },
  created() {

    this.caricaQuestoComponente()
        .then( () => this.isLayoutCaricato = true )
        .catch( console.error );

  },
  watch: {

    /** Aggiorna il contenuto della pagina se cambia la route, ad
     * es.: si chiedono i dati di un altro Consumer
     * (<a href="https://stackoverflow.com/a/50140702">Fonte</a>)*/
    '$route' (nuovaRoute) {

      // Se la nuova route fa riferimento a questo componente,
      // Allora devo caricare i nuovi dati
      // Altrimenti non devo fare nulla
      if( nuovaRoute.name === process.env.VUE_APP_ROUTER_NOME_LISTA_DOCUMENTI_VISTA_DA_UPLOADER ) {
        this.caricaQuestoComponente();
      }
    }

  },
  methods: {

    /** Data un'entry della mappa restituita dal server quando
     * gli si richiede l'elenco dei documenti (in pratica: dato
     * un documento, nella forma [ idDocumento, { proprietà documento} ] ),
     * questo metodo aggiunge alle proprietà di quel documento gli url
     * per il download e l'eliminazione del documento stesso.
     * Infine restituisce la entry corrispondente al documento, con
     * le properties (gli url) appena aggiunte.
     * @param unDocumento nella forma di una entry, cioè un array con due
     *                    elementi:   [ idDocumento, { proprietà documento} ] */
    aggiungiUrlDownloadEliminazioneDocumento: function (unDocumento) {

      // Decomposizione di ogni entry
      const idDocumento = unDocumento[0];
      const propDocumento = unDocumento[1];

      propDocumento[this.NOME_PROP_DELETE_DOCUMENTO] = process.env.VUE_APP_URL_DELETE_DOCUMENTO_DI_QUESTO_UPLOADER + "/" + idDocumento;
      propDocumento[this.NOME_PROP_DOWNLOAD_DOCUMENTO] = process.env.VUE_APP_URL_DOWNLOAD_DOCUMENTO_DI_QUESTO_UPLOADER + "/" + idDocumento;

      return [idDocumento, propDocumento];

    },

    /** Funzione di setup.*/
    async caricaQuestoComponente() {

      // Carica proprietà da Vue Router
      this.nomeConsumer = this.$route.params[process.env.VUE_APP_ROUTER_PARAMETRO_NOME_CONSUMER_DI_CUI_MOSTRARE_DOCUMENTI_PER_UPLOADER];
      this.idConsumer   = this.$route.params[process.env.VUE_APP_ROUTER_PARAMETRO_ID_CONSUMER_DI_CUI_MOSTRARE_DOCUMENTI_PER_UPLOADER];


      // Richiede mappa idFile-propFile per questo consumer
      await richiestaGet( process.env.VUE_APP_URL_GET_MAPPA_FILE_DI_UPLOADER_PER_CONSUMER + "/" + this.idConsumer )
          // Crea mappa
          .then( rispostaConMappaFile_id_prop => new Map(Object.entries( rispostaConMappaFile_id_prop ) ) )
          // Ordina mappa
          .then( ordinaMappaSuDataCaricamentoConNonVisualizzatiDavanti )

          // In ogni documento, aggiungi link di cancellazione e di download, poi restituisci la mappa
          .then( mappa =>
              new Map(
                  Array.from( mappa.entries() )
                       .map( unDocumento => this.aggiungiUrlDownloadEliminazioneDocumento(unDocumento) )
              )
          )

          // Salva la mappa
          .then( mappa => this.mappaDocumentiPerUnConsumer.set( mappa ) );


      // Richiede l'elenco dei nomi delle properties dei documenti per i Consumer
      await richiestaGet( process.env.VUE_APP_URL_GET_NOMI_TUTTE_LE_PROP_DOCUMENTI )
          // Aggiunge alle properties dei documenti le colonne con il link di download ed eliminazione
          // documento (da creare dinamicamente), poi salva l'array
          .then( nomiPropDocumenti => {
            nomiPropDocumenti.push( this.NOME_PROP_DOWNLOAD_DOCUMENTO );
            nomiPropDocumenti.push( this.NOME_PROP_DELETE_DOCUMENTO )
            this.nomiPropDocumenti = nomiPropDocumenti;
          })

    },

    /** Funzione per eliminare un documento dall'elenco, a seguito
     * della richiesta di eliminazione da parte dell'utente.*/
    rimuoviDocumentoDaLista(urlEliminazioneDocumento) {

      // Id del documento appeso in fondo all'url
      const idDocumentoEliminato = urlEliminazioneDocumento.substring(urlEliminazioneDocumento.lastIndexOf("/")+1);

      this.mappaDocumentiPerUnConsumer.get().delete(idDocumentoEliminato);

    },

    /** Funzione per inviare al server il nuovo documento
     * oltre che i valori dei campi di input presi dal form.*/
    caricaNuovoDocumento() {

      // Recupera il documento dal form
      const inputFile = document.querySelector('#' + this.idForm_caricaNuovoDocumento + ' input[type=file]');
      const documento = inputFile.files[0];

      // Costruzione dei parametri da inviare
      const formData = new FormData();
      formData.append( this.nomeParametro_contenutoDocumento, documento );
      formData.append( this.nomeParametro_nomeDocumento, this.nomeDocumento );
      formData.append( this.nomeParametro_idConsumerDestinatario, this.idConsumer );
      formData.append( this.nomeParametro_listaHashtag, this.listaHashtag.trim().toLowerCase() );

      // Controllo validità campi del form
      const formValido = documento && this.nomeDocumento;  // che siano truthy

      if( formValido )
        richiestaPostConFile( process.env.VUE_APP_URL_POST_CARICA_DOCUMENTO_DI_QUESTO_UPLOADER, formData)
          .then( mappa_idDocumento_proprietaDocumento => {
            // Server restituisce una mappa avente per chiave l'id del file aggiunto
            //  e per valore l'oggetto con le properties del file: l'unica entry è
            //  il file appena aggiunto

            alert("Documento caricato");

            // Aggiungi url nelle properties
            mappa_idDocumento_proprietaDocumento = new Map(
                [this.aggiungiUrlDownloadEliminazioneDocumento( Object.entries(mappa_idDocumento_proprietaDocumento)[0] ) ]
            );

            // Aggiungi il documento all'elenco di quelli mostrati da questo componente
            this.mappaDocumentiPerUnConsumer.set(
                new Map([ ...mappa_idDocumento_proprietaDocumento,  // merge della nuova entry in cima alla mappa
                                ... this.mappaDocumentiPerUnConsumer.get()] )
            );

            // Pulisci i campi
            document.getElementById(this.idForm_caricaNuovoDocumento).reset();

          })
          .catch( console.error );
      else
        alert( "Campi del form non validi." );

    },

    chiudiListaDocumentiUnConsumer() {
      this.$router.push({path: process.env.VUE_APP_ROUTER_PATH_AREA_RISERVATA});
    }

  }
}


</script>

<style scoped>

</style>