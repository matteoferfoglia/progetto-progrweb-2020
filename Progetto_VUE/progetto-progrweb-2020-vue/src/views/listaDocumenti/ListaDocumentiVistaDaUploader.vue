<template v-if="isLayoutCaricato">

  <!-- Componente per mostrare la lista di documenti di un Consumer,
       visualizzata da un Uploader -->

  <section>
    <p>Consumer: {{ nomeConsumer }}</p>
    <TabellaDocumenti :nomiColonneIntestazione  ="nomiPropDocumenti"
                      :elencoDocumentiDaMostrare="mappaDocumentiPerUnConsumer"
                      :nomePropLinkDownload     ="NOME_PROP_DOWNLOAD_DOCUMENTO"
                      :nomePropLinkElimina      ="NOME_PROP_DELETE_DOCUMENTO"  />
  </section>

  <form @submit.prevent="caricaNuovoDocumento" id="caricaNuovoDocumento">  <!-- TODO : aggiungere csrf token -->
    <!-- Fonte (Upload documento): https://stackoverflow.com/a/43014086 -->
    <p>Carica un nuovo documento:</p>
    <label>Nome documento   <input type="text" v-model="nomeDocumento" required></label>
    <label>Lista di hashtag <input type="text" v-model="listaHashtag" placeholder="hashtag1, hashtag2"></label>
    <label>Documento        <input type="file"></label><!-- TODO : caricare documento-->
    <input type="submit" value="Carica">
  </form>

  <button @click="chiudiListaDocumentiUnConsumer">Chiudi</button>



</template>

<script>
import TabellaDocumenti from "./TabellaDocumenti";
import {richiestaGet, richiestaPostConFile} from "../../utils/http";
import {ordinaMappaSuDataCaricamentoConNonVisualizzatiDavanti} from "../../utils/documenti";

export default {
  name: "ListaDocumentiVistaDaUploader",
  components: {TabellaDocumenti},
  data() {
    return {

      /** Flag, true quando questo componente è caricato.*/
      isLayoutCaricato: false,

      /** Nome del consumer a cui i documenti si riferiscono.*/
      nomeConsumer: this.$route.params[process.env.VUE_APP_ROUTER_PARAMETRO_NOME_CONSUMER_DI_CUI_MOSTRARE_DOCUMENTI_PER_UPLOADER],

      /** Identificativo del consumer a cui i documenti si riferiscono.*/
      idConsumer: this.$route.params[process.env.VUE_APP_ROUTER_PARAMETRO_ID_CONSUMER_DI_CUI_MOSTRARE_DOCUMENTI_PER_UPLOADER],

      /** Nome delle colonne di intestazione della tabella coi documenti.*/
      nomiPropDocumenti: [],

      /** Nome della colonna con l'url di download del documento.*/
      NOME_PROP_DOWNLOAD_DOCUMENTO: "Link download",

      /** Nome della colonna con l'url di eliminazione del documento.*/
      NOME_PROP_DELETE_DOCUMENTO: "Link eliminazione",

      /** Mappa documenti ( id => proprietà ) per uno specifico Consumer.
       * Questa mappa è caricata dinamicamente in base alle azioni
       * dell'utente.*/
      mappaDocumentiPerUnConsumer: new Map(),



      // NOMI DEI PARAMETRI ATTESI DAL SERVER
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

    const caricaQuestoComponente = async () => {

      // Richiede mappa idFile-propFile per questo consumer
      await richiestaGet( process.env.VUE_APP_GET_MAPPA_FILE_DI_UPLOADER_PER_CONSUMER + "/" + this.idConsumer )
            // Crea mappa
            .then( rispostaConMappaFile_id_prop => new Map(Object.entries( rispostaConMappaFile_id_prop ) ) )
            // Ordina mappa
            .then( ordinaMappaSuDataCaricamentoConNonVisualizzatiDavanti )

            // In ogni documento, aggiungi link di cancellazione e di download, poi restituisci la mappa
            .then( mappa =>
                new Map(
                   Array.from( mappa.entries() )
                        .map( unDocumento => {

                          // Decomposizione di ogni entry
                          const idDocumento   = unDocumento[0];
                          const propDocumento = unDocumento[1];

                          propDocumento[this.NOME_PROP_DELETE_DOCUMENTO]   = process.env.VUE_APP_DELETE_DOCUMENTO_DI_QUESTO_UPLOADER + "/" + idDocumento;
                          propDocumento[this.NOME_PROP_DOWNLOAD_DOCUMENTO] = process.env.VUE_APP_DOWNLOAD_DOCUMENTO_DI_QUESTO_UPLOADER + "/" + idDocumento;

                          return [ idDocumento, propDocumento ];

                        })
                )
            )

            // Salva la mappa
            .then( mappa => this.mappaDocumentiPerUnConsumer = mappa );


      // Richiede l'elenco dei nomi delle properties dei documenti per i Consumer
      await richiestaGet( process.env.VUE_APP_GET_NOMI_TUTTE_LE_PROP_DOCUMENTI )
          // Aggiunge alle properties dei documenti le colonne con il link di download ed eliminazione
          // documento (da creare dinamicamente), poi salva l'array
          .then( nomiPropDocumenti => {
            nomiPropDocumenti.push( this.NOME_PROP_DOWNLOAD_DOCUMENTO );
            nomiPropDocumenti.push( this.NOME_PROP_DELETE_DOCUMENTO )
            this.nomiPropDocumenti = nomiPropDocumenti;
          })

    };

    caricaQuestoComponente()
        .then( () => this.isLayoutCaricato = true )
        .catch( console.error );

  },
  methods: {

    /** Funzione per inviare al server il nuovo documento
     * oltre che i valori dei campi di input presi dal form.*/
    caricaNuovoDocumento() {

      // Recupera il documento dal form
      const inputFile = document.querySelector('input[type=file]');
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
        richiestaPostConFile( process.env.VUE_APP_POST_CARICA_DOCUMENTO_DI_QUESTO_UPLOADER, formData)
          .then( () => {
            alert("Documento caricato");

            // Pulisci i campi
            document.getElementById("caricaNuovoDocumento").reset();

          })
          .catch( console.error );
      else
        console.error( "Campi del form non validi." );

    },

    chiudiListaDocumentiUnConsumer() {
      this.$router.push({path: process.env.VUE_APP_ROUTER_PATH_AREA_RISERVATA});
    }

  }
}


</script>

<style scoped>

</style>