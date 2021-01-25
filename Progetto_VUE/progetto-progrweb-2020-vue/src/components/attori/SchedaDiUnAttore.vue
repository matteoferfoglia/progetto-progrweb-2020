<template>

  <section :id="idQuestoComponente">
    <p>
      Modificare i campi del form per modificare i dati dell'utente nel sistema.
    </p>
    <FormCampiAttore :flag_mostrareLabelCampiInput="true"
                     :urlInvioFormTramitePost="urlModificaInfoAttore"
                     :flag_inviaDatiForm="flag_inviaDatiForm"
                     :isQuestoFormRiferitoAConsumer="isQuestaSchedaRiferitaAdUnConsumer"
                     :username_readOnly="true"
                     :username="username"
                     :nominativo="nominativo"
                     :email="email"
                     :csrfToken="csrfToken_wrapper"
                     @submit="flag_inviaDatiForm = true"
                     @dati-form-inviati="formModificaAttoreInviato($event)"
                     @csrf-token-ricevuto="$emit('csrf-token-ricevuto',$event)">
      <button @click="eliminaAttore()">Elimina</button>
      <input type="submit" value="Modifica informazioni">
    </FormCampiAttore>
  </section>

  <router-view />

  <!-- TODO : i seguenti componenti devono essere raggiungibili tramite Vue Router
  <ResocontoDiUnAttore :nomeUploaderCuiQuestoResocontoSiRiferisce="nominativo"
                       :identificativoUploader="idAttoreCuiQuestaSchedaSiRiferisce"
                       v-if="isAdministratorAttualmenteAutenticato()" />

  <ListaDocumentiDiUnConsumerVistaDaUploader v-if="isUploaderAttualmenteAutenticato()" /> -->




</template>

<script>
import FormCampiAttore from "../layout/FormCampiAttore";
import {generaIdUnivoco} from "../../utils/utilitaGenerale";
import {richiestaDelete} from "../../utils/http";
import ResocontoDiUnAttore from "./administrator/ResocontoDiUnAttore";
import ListaDocumentiDiUnConsumerVistaDaUploader from "./uploader/ListaDocumentiDiUnConsumerVistaDaUploader";
export default {
name: "SchedaDiUnAttore",
  components: {ListaDocumentiDiUnConsumerVistaDaUploader, ResocontoDiUnAttore, FormCampiAttore},
  props: [
    /** Flag: true se l'utente attualmente autenticato può modificare
     * le informazioni di un attore mostrate da questo componente.*/
    "utenteAutenticatoPuoModificareInfoAttore",

    /** Indica il tipo di attore che sta visualizzando questo componente.*/
    "tipoAttoreAttualmenteAutenticato",

    /** Identificativo dell'attore a cui questa scheda si riferisce.*/
    "idAttoreCuiQuestaSchedaSiRiferisce",

    /** Oggetto con le proprietà dell'attore a cui questa scheda si riferisce.*/
    "proprietaAttoreCuiQuestaSchedaSiRiferisce",

    // Nomi delle proprietà di un attore
    /** Nome della proprietà contenente lo username di un attore nell'oggetto
     * che lo rappresenta.*/
    "NOME_PROP_USERNAME",

    /** Nome della proprietà contenente il nominativo di un attore nell'oggetto
     * che lo rappresenta.*/
    "NOME_PROP_NOMINATIVO",

    /** Nome della proprietà contenente l'email di un attore nell'oggetto
     * che lo rappresenta.*/
    "NOME_PROP_EMAIL",

    /** Token CSRF ricevuto dal padre.*/
    "csrfToken"
  ],
  data() {
    return{
      /** ID html di questo componente.*/
      idQuestoComponente: "schedaAttore-" + generaIdUnivoco(),

      /** Flag: true se questa scheda si riferisce ad un Consumer.*/
      isQuestaSchedaRiferitaAdUnConsumer: this.isUploaderAttualmenteAutenticato(), // Se è un Uploader a visualizzare,
                                                                // allora sta guardando la scheda di un Consumer

      /** Url a cui i dati del form devono essere inviati
       * per la modifica delle informazioni di un attore.*/
      urlModificaInfoAttore: this.isUploaderAttualmenteAutenticato() ?  // url e permessi diversi in base a chi chiede la modifica
                             process.env.VUE_APP_URL_MODIFICA_CONSUMER_RICHIESTA_DA_UPLOADER :
                             process.env.VUE_APP_URL_MODIFICA_ATTORE_RICHIESTA_DA_ADMIN,

      /** Flag: quando diventa true, i dati del form vengono
       * inviati all'url sopra specificato.*/
      flag_inviaDatiForm: false,

      // Valori da mostrare (impostati da created)
      username  : "",
      nominativo: "",
      email     : "",


      // Wrapper
      csrfToken_wrapper: this.csrfToken,

    }
  },
  created() {

    if( this.proprietaAttoreCuiQuestaSchedaSiRiferisce ) {
      this.username   = this.NOME_PROP_USERNAME   ? this.NOME_PROP_USERNAME   : "";
      this.nominativo = this.NOME_PROP_NOMINATIVO ? this.NOME_PROP_NOMINATIVO : "";
      this.email      = this.NOME_PROP_EMAIL      ? this.NOME_PROP_EMAIL      : "";
    }

  },
  methods:{

    /** Restituisce true se l'utente attualmente autenticato
     * è un Uploader, false altrimenti.*/
    isUploaderAttualmenteAutenticato() {
      return this.tipoAttoreAttualmenteAutenticato ===
          process.env.VUE_APP_TIPO_UTENTE_AUTENTICATO_UPLOADER;
    },

    /** Restituisce true se l'utente attualmente autenticato
     * è un Uploader, false altrimenti.*/
    isAdministratorAttualmenteAutenticato() {
      return this.tipoAttoreAttualmenteAutenticato ===
          process.env.VUE_APP_TIPO_UTENTE_AUTENTICATO_ADMINISTRATOR;
    },

    /** Richiede al server l'eliminazione di un attore.*/
    eliminaAttore() {
      const urlEliminazioneAttore = ( this.isUploaderAttualmenteAutenticato() ?
              process.env.VUE_APP_URL_DELETE_CONSUMER_PER_QUESTO_UPLOADER  :
              process.env.VUE_APP_URL_DELETE_ATTORE ) +
          "/" + this.idAttoreCuiQuestaSchedaSiRiferisce;

      const parametriRichiestaDelete = {[process.env.VUE_APP_FORM_CSRF_INPUT_FIELD_NAME]: this.csrfToken};

      richiestaDelete( urlEliminazioneAttore, parametriRichiestaDelete )
          .then( () => {
            alert(this.proprietaAttoreCuiQuestaSchedaSiRiferisce[this.NOME_PROP_USERNAME] + "\" eliminato." );
          })
          .catch( rispostaErrore => {
            console.error("Errore durante l'eliminazione: " + rispostaErrore );
          });

    },


    /** Metodo invocato quando il form per la modifica di un attore viene
     * inviato.
     * @param oggetto Oggetto restituito dal gestore di
     *        {@link FormCampiAttore.script.default.watch.flag_inviaDatiForm}.
     */
    formModificaAttoreInviato ( oggetto ) {
      oggetto.promiseRispostaServer
          .then( () => {
            alert( "Modifiche effettuate!" );
          })
          .catch( rispostaServer => {
            console.error( rispostaServer );
            alert( "ERRORE: "+ rispostaServer.data );
          })
          .finally( () => {
            this.flag_inviaDatiForm = false;
          });
    }


  },
  watch: {
    csrfToken : {
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
</style>