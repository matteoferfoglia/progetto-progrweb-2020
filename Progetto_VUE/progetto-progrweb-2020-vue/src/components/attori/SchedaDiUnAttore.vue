<template>

  <header>
    <h2>{{ tipoAttoreCuiQuestaSchedaSiRiferisce }}: {{ nominativo }}</h2>
  </header>
  <section :id="idQuestoComponente">
    <small>
      Modificare i campi del form per modificare i dati dell'utente nel sistema.
    </small>
    <FormCampiAttore :flag_mostrareLabelCampiInput="true"
                     :urlInvioFormTramitePost="urlModificaInfoAttore"
                     :flag_inviaDatiForm="flag_inviaDatiForm"
                     :isQuestoFormRiferitoAConsumer="isQuestaSchedaRiferitaAdUnConsumer"
                     :username_readOnly="true"
                     :tuttiICampi_readOnly="isConsumerAttualmenteAutenticato()"
                     :username="isConsumerAttualmenteAutenticato() ? null : username"
                     :nominativo="nominativo"
                     :email="email"
                     :resetCampiInputDopoInvioForm="false"
                     :ripristinaValoriProp = "ripristinaValoriProperty"
                     :datiAggiuntiviDaInviareAlServer="datiAggiuntiviDaInviareAlServer_onSubmit"
                     :csrfToken="csrfToken_wrapper"
                     @submit="flag_inviaDatiForm = true"
                     @ripristina-valori-prop="ripristinaValoriProperty = false"
                     @dati-form-inviati="formModificaAttoreInviato($event)"
                     @csrf-token-ricevuto="$emit('csrf-token-ricevuto',$event)">
      <input type="file" v-if="mostrareInputFilePerModificaLogoUploader" :name="LOGO_INPUT_FIELD_NAME">
      <button @click.prevent="eliminaAttore()" v-if="! isConsumerAttualmenteAutenticato()">Elimina</button>
      <input type="submit" value="Modifica informazioni" v-if="! isConsumerAttualmenteAutenticato()">
      <button @click.prevent="ripristinaValoriProperty = true" v-if="! isConsumerAttualmenteAutenticato()">Annulla tutte le modifiche</button>
    </FormCampiAttore>
  </section>

  <ResocontoDiUnAttore UnAttore :nomeUploaderCuiQuestoResocontoSiRiferisce="nominativo"
                       :identificativoUploader="idAttoreCuiQuestaSchedaSiRiferisce"
                       v-if="isAdministratorAttualmenteAutenticato()" />

  <ListaDocumentiPerConsumerVistaDaUploader v-if="isUploaderAttualmenteAutenticato()"
                                            :csrfToken="csrfToken_wrapper"
                                            :NOME_PROP_NOMINATIVO="NOME_PROP_NOMINATIVO"
                                            @csrf-token-ricevuto="$emit('csrf-token-ricevuto', $event)"/>

  <TabellaDocumenti v-if="isConsumerAttualmenteAutenticato()" />




</template>

<script>
import FormCampiAttore from "../layout/FormCampiAttore";
import {generaIdUnivoco} from "../../utils/utilitaGenerale";
import {richiestaDelete} from "../../utils/http";
import ListaDocumentiPerConsumerVistaDaUploader from "./uploader/ListaDocumentiPerConsumerVistaDaUploader";
import ResocontoDiUnAttore from "./administrator/ResocontoDiUnAttore";
import TabellaDocumenti from "../../views/areaRiservata/listaDocumenti/TabellaDocumenti";
export default {
name: "SchedaDiUnAttore",
  components: {TabellaDocumenti, ResocontoDiUnAttore, ListaDocumentiPerConsumerVistaDaUploader, FormCampiAttore},
  inheritAttrs: false,
  emits: [
    /** Evento emesso quando riceve un token CSRF da un componente figlio.*/
    'csrf-token-ricevuto'
  ],
  props: [

    /** Indica il tipo di attore che sta visualizzando questo componente.*/
    "tipoAttoreAttualmenteAutenticato",

    /** Indica il tipo di attore a cui si riferisce questa scheda.*/
    "tipoAttoreCuiQuestaSchedaSiRiferisce",

    /** Flag: true se questo componente deve mostrare il campo di input
     * da usare per modificare il logo di un Uploader.*/
    "mostrareInputFilePerModificaLogoUploader",

    // Nomi delle proprietà di un attore  // TODO : servono ? Non si può semplicemente iterare sulle prop dell'oggetto "attore"?
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

      // Proprietà ottenute da Vue Router caricate durante created()
      /** Identificativo dell'attore a cui questa scheda si riferisce.*/
      idAttoreCuiQuestaSchedaSiRiferisce: "",
      /** Oggetto con le proprietà dell'attore a cui questa scheda si riferisce.*/
      proprietaAttoreCuiQuestaSchedaSiRiferisce: {},

      /** Flag: true se l'utente attualmente autenticato può modificare
       * le informazioni di un attore mostrate da questo componente.*/
      utenteAutenticatoPuoModificareInfoAttore: ! this.isConsumerAttualmenteAutenticato(),

      /** ID html di questo componente.*/
      idQuestoComponente: "schedaAttore-" + generaIdUnivoco(),

      /** Flag: true se questa scheda si riferisce ad un Consumer.*/
      isQuestaSchedaRiferitaAdUnConsumer: this.isUploaderAttualmenteAutenticato(), // Se è un Uploader a visualizzare,
                                                                // allora sta guardando la scheda di un Consumer

      /** Nome del campo di input per caricare il nuovo logo di un Uploader.*/
      LOGO_INPUT_FIELD_NAME: process.env.VUE_APP_FORM_LOGO_UPLOADER_INPUT_FIELD_NAME,

      /** Url a cui i dati del form devono essere inviati
       * per la modifica delle informazioni di un attore.*/
      urlModificaInfoAttore: this.isUploaderAttualmenteAutenticato() ?  // url e permessi diversi in base a chi chiede la modifica
                             process.env.VUE_APP_URL_MODIFICA_CONSUMER_RICHIESTA_DA_UPLOADER :
                             process.env.VUE_APP_URL_MODIFICA_ATTORE_RICHIESTA_DA_ADMIN,

      /** Oggetto coi dati aggiuntivi da inviare al server insieme al
       * form quando si clicca submit.*/
      datiAggiuntiviDaInviareAlServer_onSubmit : {
        // vedere created()
      },


      /** Flag: quando diventa true, i dati del form vengono
       * inviati all'url sopra specificato.*/
      flag_inviaDatiForm: false,

      /** Flag: quando diventa true, i dati nel form vengono
       * reimpostati a quelli di default eliminando tutte le
       * eventuali modifiche dell'utente.*/
      ripristinaValoriProperty: false,

      // Valori da mostrare (impostati da created)
      username  : "",
      nominativo: "",
      email     : "",


      // Wrapper
      csrfToken_wrapper: this.csrfToken,

    }
  },
  created() {

    // Caricamento proprietà da Vue-Router
    this.idAttoreCuiQuestaSchedaSiRiferisce        = this.$route.params[process.env.VUE_APP_ROUTER_PARAMETRO_ID_ATTORE];
    this.proprietaAttoreCuiQuestaSchedaSiRiferisce = JSON.parse(String(this.$route.params[process.env.VUE_APP_ROUTER_PARAMETRO_PROPRIETA_ATTORE]));

    this.datiAggiuntiviDaInviareAlServer_onSubmit = {
      /** Identificativo dell'attore a cui si riferisce questa scheda.*/
      [process.env.VUE_APP_FORM_USERNAME_INPUT_FIELD_IDENTIFICATIVO_ATTORE]: this.idAttoreCuiQuestaSchedaSiRiferisce
    }

    if( this.proprietaAttoreCuiQuestaSchedaSiRiferisce ) {
      // Verifica che le proprietà da mostrare siano ben definite

      this.username   = this.NOME_PROP_USERNAME   ?
          ( this.proprietaAttoreCuiQuestaSchedaSiRiferisce[this.NOME_PROP_USERNAME] ?
              this.proprietaAttoreCuiQuestaSchedaSiRiferisce[this.NOME_PROP_USERNAME] : "" ) : "";

      this.nominativo = this.NOME_PROP_NOMINATIVO   ?
          ( this.proprietaAttoreCuiQuestaSchedaSiRiferisce[this.NOME_PROP_NOMINATIVO] ?
              this.proprietaAttoreCuiQuestaSchedaSiRiferisce[this.NOME_PROP_NOMINATIVO] : "" ) : "";

      this.email      = this.NOME_PROP_EMAIL   ?
          ( this.proprietaAttoreCuiQuestaSchedaSiRiferisce[this.NOME_PROP_EMAIL] ?
              this.proprietaAttoreCuiQuestaSchedaSiRiferisce[this.NOME_PROP_EMAIL] : "" ) : "";
    }

  },
  methods:{

    // TODO : i metodi isConsumer() / isUploader() / isAdministrator()  sono presenti in più componenti => refactoring

    /** Restituisce true se l'utente attualmente autenticato
     * è un Consumer, false altrimenti.*/
    isConsumerAttualmenteAutenticato() {
      return this.tipoAttoreAttualmenteAutenticato ===
          process.env.VUE_APP_TIPO_UTENTE_AUTENTICATO_CONSUMER;
    },

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
            this.$router.push({path: process.env.VUE_APP_ROUTER_PATH_AREA_RISERVATA});
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
          .then( rispostaServer => {
            // Aggiorna i dati della vista
            this.username   = rispostaServer[ process.env.VUE_APP_FORM_USERNAME_INPUT_FIELD_NAME ];
            this.nominativo = rispostaServer[ process.env.VUE_APP_FORM_NOMINATIVO_INPUT_FIELD_NAME ];
            this.email      = rispostaServer[ process.env.VUE_APP_FORM_EMAIL_INPUT_FIELD_NAME ];
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