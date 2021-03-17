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
      formData.append( process.env.VUE_APP_FORM_CARICA_DOCUMENTO_NOME_DOCUMENTO_INPUT_FIELD_NAME, this.nomeDocumento );
      formData.append( process.env.VUE_APP_FORM_CARICA_DOCUMENTO_ID_CONSUMER_DESTINATARIO_INPUT_FIELD_NAME, this.idConsumer );
      formData.append( process.env.VUE_APP_FORM_CARICA_DOCUMENTO_LISTA_HASHTAG_INPUT_FIELD_NAME, this.listaHashtag.trim().toLowerCase() );
      formData.append( process.env.VUE_APP_FORM_CARICA_DOCUMENTO_CONTENUTO_DOCUMENTO_INPUT_FIELD_NAME, documento );

      // Controllo validità campi del form
      const formValido = documento && this.nomeDocumento;  // che siano truthy

      if( formValido )
        richiestaPostConFile( process.env.VUE_APP_URL_POST_CARICA_DOCUMENTO__RICHIESTA_DA_UPLOADER, formData)
            .then( () => {
              alert("Documento caricato");

              // Pulisci i campi
              this.nomeDocumento = "";
              this.listaHashtag  = "";
              document.getElementById(this.idForm_caricaNuovoDocumento).reset();

            })
            .catch( error => {
              console.error(error);
              alert(error.data);
            });
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