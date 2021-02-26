<template>

  <section class="card" :id="idHtmlQuestoComponente">
    <header class="card-header titolo-scheda d-flex align-items-center">
      <img :src="logoBase64_dataUrl"
           alt=""
           v-if="isQuestaSchedaRiferitaAdUnUploader"/>
      <h2>{{ nominativo }}</h2>
    </header>

    <div class="accordion">

      <article class="card">
        <h2 class="card-header">
          <button class="btn btn-link btn-block text-left d-flex justify-content-between align-items-center"
                  type="button" data-toggle="collapse" data-target="#informazioniAttore">
            Informazioni
            <span class="icona-toggle"/>
          </button>
        </h2>
        <div class="collapse" :class="{'show': isAdministratorAttualmenteAutenticato() &&
                                                   !isQuestaSchedaRiferitaAdUnConsumer &&
                                                   !isQuestaSchedaRiferitaAdUnUploader    }"
             id="informazioniAttore"><div class="card-body"><!--Limitazione di Bootstrap: necessario doppio div -->


          <p v-if="! isConsumerAttualmenteAutenticato()/* Consumer non può modificare nulla */">
            Modificare i campi del form per modificare i dati dell'utente nel sistema.
          </p>
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

            <label class="mx-auto" v-if="mostrareInputFilePerModificaLogoUploader()">
              Modifica logo
              <input type="file" :name="LOGO_INPUT_FIELD_NAME"
                     class="form-control-file" >
            </label>

            <button @click="modificaAttore()"
                    class="modifica btn btn-info"
                    v-if="! isConsumerAttualmenteAutenticato()">
              Modifica utente
            </button>

            <button @click.prevent="ripristinaValoriProperty = true"
                    class="reset btn btn-secondary"
                    v-if="! isConsumerAttualmenteAutenticato()">
              Reset modifiche
            </button>

            <button @click.prevent="eliminaAttore()"
                    class="x-circle btn btn-danger"
                    v-if="! isConsumerAttualmenteAutenticato()">
              Elimina utente
            </button>

          </FormCampiAttore>


        </div></div>
      </article>

      <article class="card" v-if="!(isAdministratorAttualmenteAutenticato() && !isQuestaSchedaRiferitaAdUnUploader)">
        <!-- Parte principale della pagina, diversa in base all'attore autenticato -->
        <h2 class="card-header">
          <button class="btn btn-link btn-block text-left d-flex justify-content-between align-items-center"
                  type="button" data-toggle="collapse" data-target="#documenti">
            <span v-if="isAdministratorAttualmenteAutenticato()">Resoconto</span>
            <span v-else>Documenti</span>
            <span class="icona-toggle"></span>
          </button>
        </h2>
        <div class="collapse show" id="documenti"><div class="card-body"><!--Limitazione di Bootstrap: necessario doppio div -->

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
                            :tipoAttoreAutenticato="tipoAttoreAutenticato"
                            :csrfToken="csrfToken_wrapper"
                            @csrf-token-ricevuto="$emit('csrf-token-ricevuto', $event)"/>
        </div></div>
      </article>

    </div>

  </section>

  <button @click="chiudiSchedaAttore"
          class="x-circle btn btn-dark mx-auto d-block"
          v-if="mostrarePulsanteChiusuraQuestaSchedaAttore===true" >
    Chiudi Scheda
  </button>





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

      /** Flag: true se bisogna mostrare il pulsante per la chiusura della
       * scheda dell'attore mostrata da questo componente. Dipende dai
       * parametri ricevuti da Vue-Router. Inizializzata in created().*/
      mostrarePulsanteChiusuraQuestaSchedaAttore: true,


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
      NOME_PROP_USERNAME_wrapper  : this.NOME_PROP_USERNAME,
      NOME_PROP_NOMINATIVO_wrapper: this.NOME_PROP_NOMINATIVO,
      NOME_PROP_EMAIL_wrapper     : this.NOME_PROP_EMAIL,

      csrfToken_wrapper: this.csrfToken

    }
  },
  created() {

    this.caricaQuestoComponente();

  },
  mounted() {

    // Modifica lo stile dei toggle corrispondenti ai collapsible attualmente mostrati "aperti"
    document.querySelectorAll(".collapse.show")
        .forEach( this.impostaClasseStileToggleDiElementoCollapsible );

    /** Aggiunge listener per modificare lo stile dei toggle corrispondenti
     * a card "aperti" o "collassati".
     * @param el Elemento html padre del toggle.*/
    const eventListenerHideCollapse = (el) => {
      el = el.querySelector("*[class|=icona-toggle]");
      const collapsibleElement = document.querySelector(el.parentElement.getAttribute("data-target"));
      this.impostaClasseStileToggleDiElementoCollapsible(collapsibleElement, true);
    };
    document.querySelectorAll(".card-header")
        .forEach( el => {
          el.addEventListener( 'click', () => eventListenerHideCollapse(el));
        })

  },
  methods:{

    /** Dato un elemento della classe collapse, restituisce il corrispondente
     * pulsante toggle che gestisce la sua attivazione.
     * @param el Elemento html di classe "collapse".
     * @return null se non ha trovato il toggle corrispondente,
     *         altrimenti restituisce l'array [collapseShow, toggleElement]
     *         in cui il primo elemento è un flag (true se il collapsible
     *         element è attualmente mostrato, false altrimenti) ed il secondo
     *         è l'elemento toggle di attivazione dell'elemento collapsible
     *         dato come parametro.*/
    trovaToggleDiElementoCollapsible(el) {

      if(el===null)
        return null;

      // Flag: true se el è attualmente mostrato
      const collapseShow = el.className.includes("show");

      // Trova il card element
      while( el!==null && !el.className.includes("card") )
        el = el.parentElement;

      if( el===null )
        return null; // card element non trovato

      const toggleElement = el.querySelector('*[class|=icona-toggle]');  // trova l'elemento toogle per aprire una card

      if( toggleElement===null )
        return null;

      return [collapseShow, toggleElement];

    },

    /** Dato un elemento collapsible, imposta lo stile del suo toggle.
     * @param el Elemento html collapsible
     * @param invertiStato Flag booleano. Se false, allora questo metodo
     *        imposta lo stile del toggle in modo coerente con l'attuale
     *        stile del collapsible, altrimenti in modo opposto (ad esempio
     *        questa funzione può essere invocata come handler nel caso in
     *        cui l'utente esegua una qualche azione (esempio: click) ed in
     *        tal caso bisogna invertire la classe di stile del toggle, ma
     *        viceversa questa funzione può anche essere invocata per rendere
     *        la classe di stile del toggle coerente con l'attuale stato del
     *        collapsible (quindi invertiStato deve essere false).*/
    impostaClasseStileToggleDiElementoCollapsible(el, invertiStato=false) {
      const tmp = this.trovaToggleDiElementoCollapsible(el);
      if( tmp===null )
        return;
      let [isShow, toggleEl] = tmp;
      isShow = invertiStato ? !isShow : isShow; // inversione del flag in base al valore del parametro invertiStato
      toggleEl.className =
          toggleEl.className
              .split(" ")
              .map( nomeDiUnaClasse => {
                if( nomeDiUnaClasse.includes("icona-toggle") )
                  nomeDiUnaClasse = isShow ?
                      "icona-toggle-show-collapsible" :
                      "icona-toggle-hide-collapsible";
                return nomeDiUnaClasse;
              })
              .join(" ");
    },

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
                                                  process.env.VUE_APP_TIPO_UTENTE__UPLOADER;

      if( this.$route && this.$route.params &&  // controllare che sia definita
          this.$route.params[process.env.VUE_APP_ROUTER_PARAMETRO_MOSTRARE_PULSANTE_CHIUSURA_SCHEDA_ATTORE] ) {
        this.mostrarePulsanteChiusuraQuestaSchedaAttore =
            this.$route.params[process.env.VUE_APP_ROUTER_PARAMETRO_MOSTRARE_PULSANTE_CHIUSURA_SCHEDA_ATTORE];
        this.mostrarePulsanteChiusuraQuestaSchedaAttore = this.mostrarePulsanteChiusuraQuestaSchedaAttore === undefined ?
            true : this.mostrarePulsanteChiusuraQuestaSchedaAttore;
      } else {
        this.mostrarePulsanteChiusuraQuestaSchedaAttore = true;
      }

      this.caricaLogoUploader();

      this.urlRichiestaElencoDocumentiPerUnConsumerDaQuestoUploader =
          process.env.VUE_APP_URL_GET_ELENCO_DOCUMENTI__RICHIESTA_DA_CONSUMER +
          "/" + this.idAttoreCuiQuestaSchedaSiRiferisce;

      this.datiAggiuntiviDaInviareAlServer_onSubmit = {
        /** Identificativo dell'attore a cui si riferisce questa scheda.*/
        [process.env.VUE_APP_FORM_IDENTIFICATIVO_ATTORE_INPUT_FIELD]: this.idAttoreCuiQuestaSchedaSiRiferisce
      }

      if( this.proprietaAttoreCuiQuestaSchedaSiRiferisce ) {
        // Verifica che le proprietà da mostrare siano ben definite

        this.username   = this.NOME_PROP_USERNAME_wrapper   ?
            ( this.proprietaAttoreCuiQuestaSchedaSiRiferisce[this.NOME_PROP_USERNAME_wrapper] ?
                this.proprietaAttoreCuiQuestaSchedaSiRiferisce[this.NOME_PROP_USERNAME_wrapper] : "" ) : "";

        this.nominativo = this.NOME_PROP_NOMINATIVO_wrapper   ?
            ( this.proprietaAttoreCuiQuestaSchedaSiRiferisce[this.NOME_PROP_NOMINATIVO_wrapper] ?
                this.proprietaAttoreCuiQuestaSchedaSiRiferisce[this.NOME_PROP_NOMINATIVO_wrapper] : "" ) : "";

        this.email      = this.NOME_PROP_EMAIL_wrapper   ?
            ( this.proprietaAttoreCuiQuestaSchedaSiRiferisce[this.NOME_PROP_EMAIL_wrapper] ?
                this.proprietaAttoreCuiQuestaSchedaSiRiferisce[this.NOME_PROP_EMAIL_wrapper] : "" ) : "";
      }

    },

    /** Carica il logo di un Uploader, se questa scheda si riferisce ad un Uploader.*/
    caricaLogoUploader() {
      if( this.isQuestaSchedaRiferitaAdUnUploader ) {
        richiestaGet(process.env.VUE_APP_URL_GET_LOGO_UPLOADER + "/" + this.idAttoreCuiQuestaSchedaSiRiferisce)
            .then(immagineLogo_dataUrl => this.logoBase64_dataUrl = immagineLogo_dataUrl)
            .catch(console.error);

        this.logoBase64_dataUrl =
            this.proprietaAttoreCuiQuestaSchedaSiRiferisce[process.env.VUE_APP_FORM_LOGO_UPLOADER_INPUT_FIELD_NAME];
      } else {
        this.logoBase64_dataUrl = "";
      }
    },

    // TODO : i metodi isConsumer() / isUploader() / isAdministrator()  sono presenti in più componenti => refactoring

    /** Restituisce true se è possibile modificare il logo di un uploader.*/
    mostrareInputFilePerModificaLogoUploader() {
      return this.isAdministratorAttualmenteAutenticato() &&
          this.tipoAttoreCuiQuestaSchedaSiRiferisce ===
          process.env.VUE_APP_TIPO_UTENTE__UPLOADER;
    },

    /** Restituisce true se l'utente attualmente autenticato
     * è un Consumer, false altrimenti.*/
    isConsumerAttualmenteAutenticato() {
      return this.tipoAttoreAutenticato ===
          process.env.VUE_APP_TIPO_UTENTE__CONSUMER;
    },

    /** Restituisce true se l'utente attualmente autenticato
     * è un Uploader, false altrimenti.*/
    isUploaderAttualmenteAutenticato() {
      return this.tipoAttoreAutenticato ===
          process.env.VUE_APP_TIPO_UTENTE__UPLOADER;
    },

    /** Restituisce true se l'utente attualmente autenticato
     * è un Uploader, false altrimenti.*/
    isAdministratorAttualmenteAutenticato() {
      return this.tipoAttoreAutenticato ===
          process.env.VUE_APP_TIPO_UTENTE__ADMINISTRATOR;
    },

    /** Richiede al server la modifica di un attore in base ai valori attualmente
     * inseriti nel form.*/
    modificaAttore() {
      document.querySelector('#' + this.idHtmlQuestoComponente + ' form').submit();
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
      this.$router.push({
        name: process.env.VUE_APP_ROUTER_NOME_ELENCO_ATTORI,
        params: {
          [process.env.VUE_APP_ROUTER_PARAMETRO_TIPO_ATTORE_CUI_SCHEDA_SI_RIFERISCE]:
            this.tipoAttoreCuiQuestaSchedaSiRiferisce
        }
      });
    }


  },
  watch: {

    // necessario wrapper per aggiornare i valori
    NOME_PROP_USERNAME: {
      immediate: true,
      deep: true,
      handler(nuovoValore) {
        if(nuovoValore) {
          this.NOME_PROP_USERNAME_wrapper = nuovoValore;
          this.caricaQuestoComponente();
        }
      }
    },
    NOME_PROP_NOMINATIVO: {
      immediate: true,
      deep: true,
      handler(nuovoValore) {
        if(nuovoValore) {
          this.NOME_PROP_NOMINATIVO_wrapper = nuovoValore;
          this.caricaQuestoComponente();
        }
      }
    },
    NOME_PROP_EMAIL: {
      immediate: true,
      deep: true,
      handler(nuovoValore) {
        if(nuovoValore) {
          this.NOME_PROP_EMAIL_wrapper = nuovoValore;
          this.caricaQuestoComponente();  // TODO : ogni volta che cambia una di queste proprietà ricarica tutto ... spreco (si potrebbe usare una property inizializzata a 3 e decrementata man mano che si ottengono le property non-undefined, quindi metterci un watch che si attiva quando arriva a zero e fa partire il setup dell'applicazione)
        }
      }
    },

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
  .accordion .card {
    margin: 0;
  }
  .titolo-scheda * {
    max-height: 4rem;
    padding: 0 1%;
  }
  h2.card-header {
    padding: 0;
  }
  h2.card-header>button {
    font-size: 1.5rem;
    margin: 0;
  }
  article.card {
    padding: 0;
  }
  .icona-toggle-hide-collapsible::after {
    transform: rotate(0deg);
  }
  .icona-toggle-show-collapsible::after {
    transform: rotate(180deg);
  }
</style>