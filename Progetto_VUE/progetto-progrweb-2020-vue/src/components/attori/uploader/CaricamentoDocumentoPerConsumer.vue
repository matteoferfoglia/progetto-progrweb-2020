<template>

  <p>Carica un nuovo documento per questo <i>Consumer</i>:</p>

  <FormConCsrfToken :id="idForm_caricaNuovoDocumento"
                    @submit="caricaNuovoDocumento"
                    @csrf-token-ricevuto="$emit('csrf-token-ricevuto', $event)">
    <!-- Fonte (Upload documento): https://stackoverflow.com/a/43014086 -->
    <div class="d-flex justify-content-around align-items-center flex-wrap form-items-container">
      <label>Nome documento   <input type="text" v-model="nomeDocumento" class="form-control" required></label>
      <label>Lista di hashtag <input type="text" v-model="listaHashtag" class="form-control" placeholder="hashtag1, hashtag2"></label>
      <label>Documento        <input type="file" class="form-control-file" required></label>
    </div>
    <button type="submit" class="btn btn-primary carica-file mx-auto d-block"> Carica documento</button>
  </FormConCsrfToken>


</template>

<script>
import {richiestaPostConFile} from "../../../utils/http";
import FormConCsrfToken from "../../layout/FormConCsrfToken";

export default {
  name: "CaricamentoDocumentoPerConsumer",
  components: {FormConCsrfToken},
  emits: [
    /** Evento emesso quando viene ricevuto il token CSRF dal server.*/
    'csrf-token-ricevuto'
  ],
  inheritAttrs: false,
  props: [

    /** Identificativo del consumer a cui i documenti si riferiscono.*/
    "idConsumer",

    /** Valore del token CSRF ricevuto dal padre.*/
    "csrfToken"
  ],
  data() {
    return {
      /** Valore dell'attributo "id" del form per il caricamento dei documenti.*/
      idForm_caricaNuovoDocumento: "caricaNuovoDocumento",

      // PROPRIETA CARICAMENTO NUOVO DOCUMENTO:
      nomeDocumento: "",
      listaHashtag: "",

      // NOMI DEI PARAMETRI ATTESI DAL SERVER // TODO : variabili d'ambiente sia per client sia per server
      nomeParametro_nomeDocumento:             "nomeFile",
      nomeParametro_contenutoDocumento:        "contenutoFile",
      nomeParametro_idConsumerDestinatario:    "identificativoConsumerDestinatario",
      nomeParametro_listaHashtag:              "listaHashtag",

      // Wrapper
      csrfToken_wrapper: this.csrfToken
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

  },
  watch: {

    csrfToken: {
      immediate: true,
      deep: true,
      handler(nuovoValore) {
        this.csrfToken_wrapper = nuovoValore;
      }
    }

  }
}
</script>

<style scoped>
label {
  width: unset;
}
</style>