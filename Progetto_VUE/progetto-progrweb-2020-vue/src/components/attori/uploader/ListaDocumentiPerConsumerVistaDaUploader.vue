<template>

  <!-- Componente per mostrare la lista di documenti di un Consumer,
       visualizzata da un Uploader -->

  <FormConCsrfToken :id="idForm_caricaNuovoDocumento"
                    @submit="caricaNuovoDocumento"
                    @csrf-token-ricevuto="$emit('csrf-token-ricevuto', $event)">
    <!-- Fonte (Upload documento): https://stackoverflow.com/a/43014086 -->
    <p>Carica un nuovo documento per questo <i>Consumer</i>:</p>
    <label>Nome documento   <input type="text" v-model="nomeDocumento" required></label>
    <label>Lista di hashtag <input type="text" v-model="listaHashtag" placeholder="hashtag1, hashtag2"></label>
    <label>Documento        <input type="file" required></label>
    <input type="submit" value="Carica">
  </FormConCsrfToken>

  <TabellaDocumenti :urlRichiestaElencoDocumentiPerUnAttore="urlRichiestaElencoDocumentiPerUnConsumer"
                    :urlDownloadDocumento="urlDownloadDocumento"
                    :urlEliminazioneDocumento="urlEliminazioneDocumento"
                    :oggetto_idDocumentoDaAggiungere_proprietaDocumentoDaAggiungere=
                        "oggetto_idDocumento_proprietaDocumento_nuovoDocumentoCaricato"
                    :puoEliminareUnDocumento="true"
                    :csrfToken="csrfToken_wrapper"
                    @csrf-token-ricevuto="$emit('csrf-token-ricevuto', $event)"
                    @nuovo-documento-mostrato=
                        "oggetto_idDocumento_proprietaDocumento_nuovoDocumentoCaricato=undefined"/>

</template>

<script>
import TabellaDocumenti from "../TabellaDocumenti";
import {richiestaPostConFile} from "../../../utils/http";
import FormConCsrfToken from "../../layout/FormConCsrfToken";

export default {
  name: "ListaDocumentiDiUnConsumerVistaDaUploader",
  components: {FormConCsrfToken, TabellaDocumenti},
  emits: [
    /** Evento emesso quando viene ricevuto il token CSRF dal server.*/
    'csrf-token-ricevuto'
  ],
  props: [

    /** Identificativo del consumer a cui i documenti si riferiscono.*/
    "idConsumer",

    /** Valore del token CSRF ricevuto dal padre.*/
    "csrfToken"

    // Altre props (specifiche per l'attore cui questo componente si riferisce) sono ottenute da Vue Router
  ],
  data() {
    return {

      /** Valore dell'attributo "id" del form per il caricamento dei documenti.*/
      idForm_caricaNuovoDocumento: "caricaNuovoDocumento",

      // NOMI DEI PARAMETRI ATTESI DAL SERVER // TODO : variabili d'ambiente sia per client sia per server
      nomeParametro_nomeDocumento:             "nomeFile",
      nomeParametro_contenutoDocumento:        "contenutoFile",
      nomeParametro_idConsumerDestinatario:    "identificativoConsumerDestinatario",
      nomeParametro_listaHashtag:              "listaHashtag",

      // URL GESTIONE DOCUMENTI
      urlRichiestaElencoDocumentiPerUnConsumer: process.env.VUE_APP_URL_GET_ELENCO_DOCUMENTI__RICHIESTA_DA_UPLOADER + "/" + this.idConsumer,
      urlDownloadDocumento:                     process.env.VUE_APP_URL_DOWNLOAD_DOCUMENTO__RICHIESTA_DA_UPLOADER,
      urlEliminazioneDocumento:                 process.env.VUE_APP_URL_DELETE_DOCUMENTO__RICHIESTA_DA_UPLOADER,

      // PROPRIETA CARICAMENTO NUOVO DOCUMENTO:
      nomeDocumento: "",
      listaHashtag: "",

      /** Oggetto con solo una property, che contiene un documento, nella forma
       *    { idDocumento : {proprieta del documento} }
       * Questo oggetto è definito solamente dopo che l'utente ha richiesto
       * tramite l'apposito form il caricamento del soprascritto documento,
       * dal momento in cui il server ha risposto confermandone l'aggiunta
       * fino al momento in cui il componente deputato alla rappresentazione
       * della lista dei documenti non lo mostra (questo componente viene
       * avvertito tramite un evento generato dal componente che mostra
       * i documenti).*/
      oggetto_idDocumento_proprietaDocumento_nuovoDocumentoCaricato: undefined,

      // Wrapper
      csrfToken_wrapper: this.csrfToken

    }
  },
  watch: {

    csrfToken : {
      immediate: true,
      handler( nuovoValore ) {
        this.csrfToken_wrapper = nuovoValore;
      },
      deep: true
    }

  },
  methods: {

    /** Funzione per inviare al server il nuovo documento
     * oltre che i valori dei campi di input presi dal form.*/
    caricaNuovoDocumento() {

      // Recupera il documento dal form
      const inputFile = document.querySelector('#' + this.idForm_caricaNuovoDocumento + ' input[type=file]');
      const documento = inputFile.files[0];

      // Costruzione dei parametri da inviare
      const formData = new FormData();
      formData.append( process.env.VUE_APP_FORM_CSRF_INPUT_FIELD_NAME, this.csrfToken_wrapper );
      formData.append( this.nomeParametro_nomeDocumento, this.nomeDocumento );
      formData.append( this.nomeParametro_idConsumerDestinatario, this.idConsumer );
      formData.append( this.nomeParametro_listaHashtag, this.listaHashtag.trim().toLowerCase() );
      formData.append( this.nomeParametro_contenutoDocumento, documento );

      // Controllo validità campi del form
      const formValido = documento && this.nomeDocumento;  // che siano truthy

      if( formValido )
        richiestaPostConFile( process.env.VUE_APP_URL_POST_CARICA_DOCUMENTO__RICHIESTA_DA_UPLOADER, formData)
          .then( oggetto_idDocumento_proprietaDocumento_documentoAppenaCaricato => {
            // Server restituisce una mappa avente per chiave l'id del file aggiunto
            //  e per valore l'oggetto con le properties del file: l'unica entry è
            //  il file appena aggiunto

            alert("Documento caricato");

            this.oggetto_idDocumento_proprietaDocumento_nuovoDocumentoCaricato =
                oggetto_idDocumento_proprietaDocumento_documentoAppenaCaricato;

            // Pulisci i campi
            this.nomeDocumento = "";
            this.listaHashtag  = "";
            document.getElementById(this.idForm_caricaNuovoDocumento).reset();

          })
          .catch( console.error );
      else
        alert( "Campi del form non validi." );

    }

  }
}


</script>

<style scoped>

</style>