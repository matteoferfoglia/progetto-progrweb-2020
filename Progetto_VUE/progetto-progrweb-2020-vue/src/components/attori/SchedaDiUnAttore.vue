<template>

  <!-- TODO : rivedere e semplificare questa scheda -->

  <header>
    <img :src="logoBase64_dataUrl"
         alt=""
         v-if="isQuestaSchedaRiferitaAdUnUploader"/>
    <h2>{{ nominativo }}</h2>
  </header>

  <section :id="idHtmlQuestoComponente">
    <small v-if="! isConsumerAttualmenteAutenticato()/* Consumer non può modificare nulla */">
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

        <input type="file" :name="LOGO_INPUT_FIELD_NAME"
               v-if="mostrareInputFilePerModificaLogoUploader()" ><!-- TODO : testare la modifica del logo -->

        <button @click="document.querySelector('#' + idHtmlQuestoComponente + ' form').submit"
                v-if="! isConsumerAttualmenteAutenticato()">
          <!-- Fonte icona: https://icons.getbootstrap.com/icons/pen/ --><!-- TODO : icone da aggiungere via CSS -->
          <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" class="bi bi-pen" viewBox="0 0 16 16">
            <path d="M13.498.795l.149-.149a1.207 1.207 0 1 1 1.707 1.708l-.149.148a1.5 1.5 0 0 1-.059 2.059L4.854 14.854a.5.5 0 0 1-.233.131l-4 1a.5.5 0 0 1-.606-.606l1-4a.5.5 0 0 1 .131-.232l9.642-9.642a.5.5 0 0 0-.642.056L6.854 4.854a.5.5 0 1 1-.708-.708L9.44.854A1.5 1.5 0 0 1 11.5.796a1.5 1.5 0 0 1 1.998-.001zm-.644.766a.5.5 0 0 0-.707 0L1.95 11.756l-.764 3.057 3.057-.764L14.44 3.854a.5.5 0 0 0 0-.708l-1.585-1.585z"/>
          </svg>
          Modifica utente
        </button>

        <button @click.prevent="eliminaAttore()"
                v-if="! isConsumerAttualmenteAutenticato()">
          <!-- Fonte icona: https://icons.getbootstrap.com/icons/x-circle/ --><!-- TODO : icone da aggiungere via CSS -->
          <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" class="bi bi-x-circle" viewBox="0 0 16 16">
            <path d="M8 15A7 7 0 1 1 8 1a7 7 0 0 1 0 14zm0 1A8 8 0 1 0 8 0a8 8 0 0 0 0 16z"/>
            <path d="M4.646 4.646a.5.5 0 0 1 .708 0L8 7.293l2.646-2.647a.5.5 0 0 1 .708.708L8.707 8l2.647 2.646a.5.5 0 0 1-.708.708L8 8.707l-2.646 2.647a.5.5 0 0 1-.708-.708L7.293 8 4.646 5.354a.5.5 0 0 1 0-.708z"/>
          </svg>
          Elimina utente
        </button>

        <button @click.prevent="ripristinaValoriProperty = true"
                v-if="! isConsumerAttualmenteAutenticato()">
          <!-- Fonte icona: https://icons.getbootstrap.com/icons/arrow-counterclockwise/ --><!-- TODO : icone da aggiungere via CSS -->
          <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" class="bi bi-arrow-counterclockwise" viewBox="0 0 16 16">
            <path fill-rule="evenodd" d="M8 3a5 5 0 1 1-4.546 2.914.5.5 0 0 0-.908-.417A6 6 0 1 0 8 2v1z"/>
            <path d="M8 4.466V.534a.25.25 0 0 0-.41-.192L5.23 2.308a.25.25 0 0 0 0 .384l2.36 1.966A.25.25 0 0 0 8 4.466z"/>
          </svg>
          Annulla modifiche
        </button>

      </FormCampiAttore>
  </section>

  <ResocontoDiUnAttore :nomeUploaderCuiQuestoResocontoSiRiferisce="nominativo"
                       :identificativoUploader="idAttoreCuiQuestaSchedaSiRiferisce"
                       v-if="isAdministratorAttualmenteAutenticato() &&
                             isQuestaSchedaRiferitaAdUnUploader" />

  <ListaDocumentiPerConsumerVistaDaUploader v-if="isUploaderAttualmenteAutenticato()"
                                            :idConsumer="idAttoreCuiQuestaSchedaSiRiferisce"
                                            :csrfToken="csrfToken_wrapper"
                                            @csrf-token-ricevuto="$emit('csrf-token-ricevuto', $event)"/>

  <TabellaDocumenti v-if="isConsumerAttualmenteAutenticato()"
                    :urlRichiestaElencoDocumentiPerUnAttore=
                        "urlRichiestaElencoDocumentiPerUnConsumerDaQuestoUploader"
                    :urlDownloadDocumento="urlDownloadDocumentoPerConsumer"
                    :csrfToken="csrfToken_wrapper"
                    @csrf-token-ricevuto="$emit('csrf-token-ricevuto', $event)"/>


  <button @click="chiudiSchedaAttore">Chiudi</button>




</template>

<script>
import FormCampiAttore from "../layout/FormCampiAttore";
import {generaIdUnivoco} from "../../utils/utilitaGenerale";
import {richiestaDelete, richiestaGet} from "../../utils/http";
import ListaDocumentiPerConsumerVistaDaUploader from "./uploader/ListaDocumentiPerConsumerVistaDaUploader";
import ResocontoDiUnAttore from "./administrator/ResocontoDiUnAttore";
import TabellaDocumenti from "./TabellaDocumenti";
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
    "tipoAttoreAutenticato",

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

      /** Indica il tipo di attore a cui si riferisce questa scheda.*/
      tipoAttoreCuiQuestaSchedaSiRiferisce: undefined,                     // inizializzata in created()

      /** Immagine logo (se l'attore rappresentato da questa scheda ne
       * ha uno) codificato in Base64.*/
      logoBase64_dataUrl: undefined,                                       // inizializzata in created()

      /** URL per la richiesta dell'elenco dei documenti destinati al Consumer
       * attualmente autenticato e caricati dall'Uploader di cui si sta caricando
       * la scheda.*/
      urlRichiestaElencoDocumentiPerUnConsumerDaQuestoUploader: undefined, // inizializzata in created()

      /** URL per la richiesta del download di uno specifico documento.
       * Richiesta di download gestita dal componente che si occupa
       * della visualizzazione dei documenti.*/
      urlDownloadDocumentoPerConsumer:
        process.env.VUE_APP_URL_DOWNLOAD_DOCUMENTO__RICHIESTA_DA_CONSUMER,

    /** Flag: true se l'utente attualmente autenticato può modificare
       * le informazioni di un attore mostrate da questo componente.*/
      utenteAutenticatoPuoModificareInfoAttore: ! this.isConsumerAttualmenteAutenticato(),

      /** ID html di questo componente.*/
      idHtmlQuestoComponente: "schedaAttore-" + generaIdUnivoco(),

      /** Flag: true se questa scheda si riferisce ad un Consumer.*/
      isQuestaSchedaRiferitaAdUnConsumer: this.isUploaderAttualmenteAutenticato(), // Se è un Uploader a visualizzare,
                                                                // allora sta guardando la scheda di un Consumer

      /** Flag: true se questa scheda si riferisce ad un Uploader.*/
      isQuestaSchedaRiferitaAdUnUploader: undefined,                       // inizializzata in created()

      /** Nome del campo di input per caricare il nuovo logo di un Uploader.*/
      LOGO_INPUT_FIELD_NAME: process.env.VUE_APP_FORM_LOGO_UPLOADER_INPUT_FIELD_NAME,

      /** Url a cui i dati del form devono essere inviati
       * per la modifica delle informazioni di un attore.*/
      urlModificaInfoAttore: this.isUploaderAttualmenteAutenticato() ?  // url e permessi diversi in base a chi chiede la modifica
                             process.env.VUE_APP_URL_MODIFICA_CONSUMER__RICHIESTA_DA_UPLOADER :
                             process.env.VUE_APP_URL_MODIFICA_ATTORE__RICHIESTA_DA_ADMIN,

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
      /** Username dell'attore a cui si riferisce questa scheda.*/
      username  : "",
      /** Nominativo dell'attore a cui si riferisce questa scheda.*/
      nominativo: "",
      /** email dell'attore a cui si riferisce questa scheda.*/
      email     : "",


      // Wrapper
      csrfToken_wrapper: this.csrfToken

    }
  },
  created() {

    this.caricaQuestoComponente();

  },
  methods:{

    /** Metodo per i caricamento di questo componente.*/
    caricaQuestoComponente() {

      // Caricamento proprietà da Vue-Router
      this.idAttoreCuiQuestaSchedaSiRiferisce =
          this.$route.params[process.env.VUE_APP_ROUTER_PARAMETRO_ID_ATTORE];

      this.proprietaAttoreCuiQuestaSchedaSiRiferisce =
          JSON.parse(String(this.$route.params[process.env.VUE_APP_ROUTER_PARAMETRO_PROPRIETA_ATTORE]));

      this.tipoAttoreCuiQuestaSchedaSiRiferisce =
          this.$route.params[process.env.VUE_APP_ROUTER_PARAMETRO_TIPO_ATTORE_CUI_SCHEDA_SI_RIFERISCE];

      this.isQuestaSchedaRiferitaAdUnUploader = this.tipoAttoreCuiQuestaSchedaSiRiferisce ===
                                                  process.env.VUE_APP_TIPO_UTENTE_AUTENTICATO_UPLOADER

      if( this.isQuestaSchedaRiferitaAdUnUploader ) {
        richiestaGet(process.env.VUE_APP_URL_GET_LOGO_ATTORE + "/" + this.idAttoreCuiQuestaSchedaSiRiferisce)
            .then(immagineLogo_dataUrl => this.logoBase64_dataUrl = immagineLogo_dataUrl)
            .catch(console.error);

        this.logoBase64_dataUrl =
            this.proprietaAttoreCuiQuestaSchedaSiRiferisce[process.env.VUE_APP_FORM_LOGO_UPLOADER_INPUT_FIELD_NAME];
      } else {
        this.logoBase64_dataUrl = "";
      }

      this.urlRichiestaElencoDocumentiPerUnConsumerDaQuestoUploader =
          process.env.VUE_APP_URL_GET_ELENCO_DOCUMENTI__RICHIESTA_DA_CONSUMER +
          "/" + this.idAttoreCuiQuestaSchedaSiRiferisce;

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

    // TODO : i metodi isConsumer() / isUploader() / isAdministrator()  sono presenti in più componenti => refactoring

    /** Restituisce true se è possibile modificare il logo di un uploader.*/
    mostrareInputFilePerModificaLogoUploader() {
      return this.isAdministratorAttualmenteAutenticato() &&
          this.tipoAttoreCuiQuestaSchedaSiRiferisce ===
          process.env.VUE_APP_TIPO_UTENTE_AUTENTICATO_UPLOADER;
    },

    /** Restituisce true se l'utente attualmente autenticato
     * è un Consumer, false altrimenti.*/
    isConsumerAttualmenteAutenticato() {
      return this.tipoAttoreAutenticato ===
          process.env.VUE_APP_TIPO_UTENTE_AUTENTICATO_CONSUMER;
    },

    /** Restituisce true se l'utente attualmente autenticato
     * è un Uploader, false altrimenti.*/
    isUploaderAttualmenteAutenticato() {
      return this.tipoAttoreAutenticato ===
          process.env.VUE_APP_TIPO_UTENTE_AUTENTICATO_UPLOADER;
    },

    /** Restituisce true se l'utente attualmente autenticato
     * è un Uploader, false altrimenti.*/
    isAdministratorAttualmenteAutenticato() {
      return this.tipoAttoreAutenticato ===
          process.env.VUE_APP_TIPO_UTENTE_AUTENTICATO_ADMINISTRATOR;
    },

    /** Richiede al server l'eliminazione di un attore.*/
    eliminaAttore() {
      const urlEliminazioneAttore = ( this.isUploaderAttualmenteAutenticato() ?
              process.env.VUE_APP_URL_DELETE_CONSUMER_PER_QUESTO_UPLOADER__RICHIESTA_DA_UPLOADER  :
              process.env.VUE_APP_URL_DELETE_ATTORE__RICHIESTA_DA_ADMIN ) +
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
    },

    /** Chiude la scheda dell'attore attualmente mostrato.*/
    chiudiSchedaAttore() {
      this.$router.push({path: process.env.VUE_APP_ROUTER_PATH_AREA_RISERVATA});
    }


  },
  watch: {

    /** Aggiorna il contenuto della pagina se cambia la route, ad
     * es.: si chiedono i dati di un altro Attore
     * (<a href="https://stackoverflow.com/a/50140702">Fonte</a>)*/
    /*'$route' : {      // TODO : se cambia id attore, modificare la vista (prova '$route.params[process.env.VUE_APP_ROUTER_PARAMETRO_ID_ATTORE]')
      immediate: true,
      deep: true,
      handler (nuovaRoute) {
        // Se la nuova route fa riferimento a questo componente,
        // Allora devo caricare i nuovi dati
        // Altrimenti non devo fare nulla
        if ( nuovaRoute.name ===
              process.env.VUE_APP_ROUTER_NOME_SCHEDA_UN_ATTORE ) {
          this.caricaQuestoComponente();
        }
      }
    },*/

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