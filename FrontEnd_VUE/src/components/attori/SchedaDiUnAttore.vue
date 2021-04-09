<template>

  <Loader :isComponenteCaricato="isComponenteCaricato">
    <section class="card" :id="idHtmlQuestoComponente">
      <header class="card-header titolo-scheda d-flex align-items-center">
        <img :src="urlLogoUploader"
             alt=""
             class="logo"
             v-if="urlLogoUploader && isQuestaSchedaRiferitaAdUnUploader"/>
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
                                                   !isQuestaSchedaRiferitaAdUnConsumer   &&
                                                   !isQuestaSchedaRiferitaAdUnUploader      }"
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
                             :tuttiICampi_required="true"
                             @submit="flag_inviaDatiForm = true"
                             @ripristina-valori-prop="ripristinaValoriProperty = false"
                             @dati-form-inviati="formModificaAttoreInviato($event)"
                             @csrf-token-ricevuto="$emit('csrf-token-ricevuto',$event)">

              <label class="mx-auto" v-if="mostrareInputFilePerModificaLogoUploader()">
                Modifica logo
                <input type="file" :name="LOGO_INPUT_FIELD_NAME"
                       class="form-control-file" >
              </label>


              <button type="submit"
                      class="modifica btn btn-info"
                      v-if="! isConsumerAttualmenteAutenticato()">
                Modifica utente
              </button>

              <button @click.prevent="ripristinaValoriForm"
                      class="reset btn btn-secondary"
                      v-if="! isConsumerAttualmenteAutenticato()">
                Reset modifiche
              </button>

              <button @click.prevent=" eliminaAttore( idAttoreCuiQuestaSchedaSiRiferisce,
                                                     String(getIdentificativoAttoreAttualmenteAutenticato()),
                                                     tipoAttoreAutenticato,
                                                     csrfToken_wrapper,
                                                     proprietaAttoreCuiQuestaSchedaSiRiferisce[NOME_PROP_USERNAME],
                                                     idAttoreCuiQuestaSchedaSiRiferisce===String(getIdentificativoAttoreAttualmenteAutenticato()),
                                                     $router                                                                                       )
                                          .catch( () => {/*ignored, utente non ha confermato l'eliminazione */} ) "
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
  </Loader>


</template>

<script>
import FormCampiAttore from "../layout/FormCampiAttore";
import {generaIdUnivoco} from "../../utils/utilitaGenerale";
import {richiestaGet} from "../../utils/http";
import ListaDocumentiPerConsumerVistaDaUploader from "./uploader/ListaDocumentiPerConsumerVistaDaUploader";
import ResocontoDiUnAttore from "./administrator/ResocontoDiUnAttore";
import TabellaDocumenti from "./TabellaDocumenti";
import Loader from "../layout/Loader";
import {
  getIdentificativoAttoreAttualmenteAutenticato,
  getTipoAttoreAttualmenteAutenticato, isAdministratorAttualmenteAutenticato,
  isConsumerAttualmenteAutenticato,
  isUploaderAttualmenteAutenticato,
  setTokenAutenticazione
} from "../../utils/autenticazione";
import {creaUrlLogo, eliminaAttore} from "../../utils/richiesteSuAttori";
export default {
  name: "SchedaDiUnAttore",
  components: {Loader, TabellaDocumenti, ResocontoDiUnAttore, ListaDocumentiPerConsumerVistaDaUploader, FormCampiAttore},
  inheritAttrs: false,
  emits: [

    /** Evento emesso quando viene modificato il nominativo dell'attore
     * che sta visualizzando la scheda (es. Administrator che visualizza
     * la sua stessa scheda)*/
    'nominativo-attore-modificato',

    /** Evento emesso quando riceve un token CSRF da un componente figlio.*/
    'csrf-token-ricevuto'
  ],
  props: [

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

      /** Indica il tipo di attore che sta visualizzando questo componente.*/
      tipoAttoreAutenticato: getTipoAttoreAttualmenteAutenticato(),

      // Proprietà ottenute da Vue Router caricate durante created()
      /** Identificativo dell'attore a cui questa scheda si riferisce.*/
      idAttoreCuiQuestaSchedaSiRiferisce: "",

      /** Oggetto con le proprietà dell'attore a cui questa scheda si riferisce.*/
      proprietaAttoreCuiQuestaSchedaSiRiferisce: {},

      /** Flag: true se bisogna mostrare il pulsante per la chiusura della
       * scheda dell'attore mostrata da questo componente. Dipende dai
       * parametri ricevuti da Vue-Router. Inizializzata in created().*/
      mostrarePulsanteChiusuraQuestaSchedaAttore: undefined,


      /** Indica il tipo di attore a cui si riferisce questa scheda.*/
      tipoAttoreCuiQuestaSchedaSiRiferisce: undefined,                     // inizializzata in created()

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
      utenteAutenticatoPuoModificareInfoAttore: isConsumerAttualmenteAutenticato(),

      /** ID html di questo componente.*/
      idHtmlQuestoComponente: "schedaAttore-" + generaIdUnivoco(),

      /** Flag: true se questa scheda si riferisce ad un Consumer.*/
      isQuestaSchedaRiferitaAdUnConsumer: isUploaderAttualmenteAutenticato(), // Se è un Uploader a visualizzare,
                                                                        // allora sta guardando la scheda di un Consumer

      /** Flag: true se questa scheda si riferisce ad un Uploader.*/
      isQuestaSchedaRiferitaAdUnUploader: undefined,                    // inizializzata in created()

      /** Nome del campo di input per caricare il nuovo logo di un Uploader.*/
      LOGO_INPUT_FIELD_NAME: process.env.VUE_APP_FORM_LOGO_UPLOADER_INPUT_FIELD_NAME,

      /** Url a cui i dati del form devono essere inviati
       * per la modifica delle informazioni di un attore.*/
      urlModificaInfoAttore: isUploaderAttualmenteAutenticato() ?  // url e permessi diversi in base a chi chiede la modifica
                             process.env.VUE_APP_URL_MODIFICA_CONSUMER__RICHIESTA_DA_UPLOADER :
                             process.env.VUE_APP_URL_MODIFICA_ATTORE__RICHIESTA_DA_ADMIN,

      /** Url a cui richiedere il logo dell'attore a cui questa scheda
       * si riferisce. */
      urlLogoUploader: undefined,                                       // inizializzata in created()

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

      /** Flag: diventa true quando il componente è caricato.*/
      isComponenteCaricato: false,

      // Import funzioni per visibilità in template
      getIdentificativoAttoreAttualmenteAutenticato: getIdentificativoAttoreAttualmenteAutenticato,
      eliminaAttore: eliminaAttore,
      isConsumerAttualmenteAutenticato: isConsumerAttualmenteAutenticato,
      isUploaderAttualmenteAutenticato: isUploaderAttualmenteAutenticato,
      isAdministratorAttualmenteAutenticato: isAdministratorAttualmenteAutenticato,


      // Wrapper
      NOME_PROP_USERNAME_wrapper  : this.NOME_PROP_USERNAME,
      NOME_PROP_NOMINATIVO_wrapper: this.NOME_PROP_NOMINATIVO,
      NOME_PROP_EMAIL_wrapper     : this.NOME_PROP_EMAIL,

      csrfToken_wrapper: this.csrfToken

    }
  },
  created() {

    this.caricaQuestoComponente()
          .then( () => {
            // Modifica lo stile dei toggle corrispondenti ai collapsible attualmente mostrati "aperti"
            document.querySelectorAll(".collapse.show")
                    .forEach( this.impostaClasseStileToggleDiElementoCollapsible );

            /** Aggiunge listener per modificare lo stile dei toggle corrispondenti
             * a card "aperti" o "collassati".
             * @param el Elemento html padre del toggle.*/
            const eventListenerHideCollapse = el => {
              const elIconaToggle = el.querySelector("*[class|=icona-toggle]");  // ricerca l'elemento html contenente l'icona toggle

              // L'elemento padre di quello contenente l'icona toggle contiene l'attributo "data-target"
              // indicante l'elemento a cui l'azione a cui il toggle si riferisce
              const collapsibleElement = document.querySelector(elIconaToggle.parentElement.getAttribute("data-target")); // elemento a cui il toggle si riferisce

              this.impostaClasseStileToggleDiElementoCollapsible(collapsibleElement, true);
            };

            document.querySelectorAll(".accordion>.card>.card-header")
                .forEach( el => {
                  el.addEventListener( 'click', () => eventListenerHideCollapse(el));
                })
          })
          .catch( console.error );

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
    async caricaQuestoComponente() {

      const MSG_ERRORE_SE_COMPONENTE_NON_CARICATO = "Non caricato, attendendo variabili.";

      ( async () => {

        if (this.NOME_PROP_USERNAME_wrapper && this.NOME_PROP_NOMINATIVO_wrapper && this.NOME_PROP_EMAIL_wrapper) {
          // Procede con le richieste al server solo se i wrapper di tutte le proprietà sono truthy


          // Caricamento proprietà da Vue-Router
          this.idAttoreCuiQuestaSchedaSiRiferisce =
              this.$route.params[process.env.VUE_APP_ROUTER_PARAMETRO_ID_ATTORE];

          this.proprietaAttoreCuiQuestaSchedaSiRiferisce =
              JSON.parse(String(this.$route.params[process.env.VUE_APP_ROUTER_PARAMETRO_PROPRIETA_ATTORE]));

          this.tipoAttoreCuiQuestaSchedaSiRiferisce =
              this.$route.params[process.env.VUE_APP_ROUTER_PARAMETRO_TIPO_ATTORE_CUI_SCHEDA_SI_RIFERISCE];

          this.isQuestaSchedaRiferitaAdUnUploader = this.tipoAttoreCuiQuestaSchedaSiRiferisce ===
              process.env.VUE_APP_TIPO_UTENTE__UPLOADER;

          if( this.isConsumerAttualmenteAutenticato() ) {
            if (this.$route && this.$route.params &&  // controllare che sia definita
                this.$route.params[process.env.VUE_APP_ROUTER_PARAMETRO_MOSTRARE_PULSANTE_CHIUSURA_SCHEDA_ATTORE]) {
              this.mostrarePulsanteChiusuraQuestaSchedaAttore =
                  this.$route.params[process.env.VUE_APP_ROUTER_PARAMETRO_MOSTRARE_PULSANTE_CHIUSURA_SCHEDA_ATTORE] === "true"; // parametri VueRouter salvato come String
            } else {
              // Se qui: è un Consumer attualmente autenticato, ma non
              //  è definito il parametro che indica se mostrare o meno
              //  il pulsante di chiusura della scheda
              // Può succedere ad esempio se il Consumer ricarica la pagina
              //  e si perdono i parametri di Vue Router

              richiestaGet( process.env.VUE_APP_ELENCO_UPLOADER_PER_QUESTO_CONSUMER__RICHIESTA_DA_CONSUMER )
                  .then( arrayIdAttoriServentiQuestoConsumer =>
                      this.mostrarePulsanteChiusuraQuestaSchedaAttore = arrayIdAttoriServentiQuestoConsumer.length>1 )
                  .catch( rispostaErrore => {
                    console.error("Errore durante il caricamento della lista di attori: " + rispostaErrore );
                    return Promise.reject(rispostaErrore);
                  });

            }
          } else {
            this.mostrarePulsanteChiusuraQuestaSchedaAttore = true;
          }

          this.urlLogoUploader = creaUrlLogo(this.idAttoreCuiQuestaSchedaSiRiferisce);

          this.urlRichiestaElencoDocumentiPerUnConsumerDaQuestoUploader =
              process.env.VUE_APP_URL_GET_ELENCO_DOCUMENTI__RICHIESTA_DA_CONSUMER +
              "/" + this.idAttoreCuiQuestaSchedaSiRiferisce;

          this.datiAggiuntiviDaInviareAlServer_onSubmit = {
            /** Identificativo dell'attore a cui si riferisce questa scheda.*/
            [process.env.VUE_APP_FORM_IDENTIFICATIVO_ATTORE_INPUT_FIELD]: this.idAttoreCuiQuestaSchedaSiRiferisce
          }

          if (this.proprietaAttoreCuiQuestaSchedaSiRiferisce) {
            // Verifica che le proprietà da mostrare siano ben definite

            this.username = this.NOME_PROP_USERNAME_wrapper ?
                (this.proprietaAttoreCuiQuestaSchedaSiRiferisce[this.NOME_PROP_USERNAME_wrapper] ?
                    this.proprietaAttoreCuiQuestaSchedaSiRiferisce[this.NOME_PROP_USERNAME_wrapper] : "") : "";

            this.nominativo = this.NOME_PROP_NOMINATIVO_wrapper ?
                (this.proprietaAttoreCuiQuestaSchedaSiRiferisce[this.NOME_PROP_NOMINATIVO_wrapper] ?
                    this.proprietaAttoreCuiQuestaSchedaSiRiferisce[this.NOME_PROP_NOMINATIVO_wrapper] : "") : "";

            this.email = this.NOME_PROP_EMAIL_wrapper ?
                (this.proprietaAttoreCuiQuestaSchedaSiRiferisce[this.NOME_PROP_EMAIL_wrapper] ?
                    this.proprietaAttoreCuiQuestaSchedaSiRiferisce[this.NOME_PROP_EMAIL_wrapper] : "") : "";
          }

        } else {
          throw new Error(MSG_ERRORE_SE_COMPONENTE_NON_CARICATO);
        }
      })()
      .then( () => this.isComponenteCaricato = true )
      .catch( errore => {
        if(errore.message!==MSG_ERRORE_SE_COMPONENTE_NON_CARICATO)
          console.error(errore);
      });
    },

    /** Restituisce true se è possibile modificare il logo di un uploader.*/
    mostrareInputFilePerModificaLogoUploader() {
      return this.isAdministratorAttualmenteAutenticato() &&
          this.tipoAttoreCuiQuestaSchedaSiRiferisce ===
          process.env.VUE_APP_TIPO_UTENTE__UPLOADER;
    },


    /** Ripristina i valori del form antecedenti le modifiche. */
    ripristinaValoriForm() {
      this.ripristinaValoriProperty = true; // vedere componente figlio che gestisce il form
    },

    /** Metodo invocato quando il form per la modifica di un attore viene
     * inviato.
     * @param oggetto Oggetto restituito dal gestore di
     *        {@link FormCampiAttore.script.default.watch.flag_inviaDatiForm}.
     */
    formModificaAttoreInviato ( oggetto ) {

      if( oggetto && oggetto.promiseRispostaServer!==undefined ) {
        oggetto.promiseRispostaServer
            .then( rispostaServer => {

              const tmp_vecchioNominativo = this.nominativo;

              // Aggiorna i dati della vista
              // NOTA: aggiungere un carattere in fondo e rimuoverlo con slice() è un trucco per far attivare il watch
              //       che aggiorna i valori scritti nel form, così se i valori dovessero "sporcarsi", con queste istruzioni
              //       vengono ripristinati ai valori giusti
              this.username   = (rispostaServer[ process.env.VUE_APP_FORM_USERNAME_INPUT_FIELD_NAME ] + ' ').slice(0,-1);
              this.nominativo = (rispostaServer[ process.env.VUE_APP_FORM_NOMINATIVO_INPUT_FIELD_NAME ] + ' ').slice(0,-1);
              this.email      = (rispostaServer[ process.env.VUE_APP_FORM_EMAIL_INPUT_FIELD_NAME ] + ' ').slice(0,-1);

              this.urlLogoUploader = creaUrlLogo(this.idAttoreCuiQuestaSchedaSiRiferisce);

              const idAttore = getIdentificativoAttoreAttualmenteAutenticato();
              if( idAttore === Number(this.idAttoreCuiQuestaSchedaSiRiferisce) &&  // true se questa scheda si riferisce proprio all'attore che la sta guardando
                  tmp_vecchioNominativo !== this.nominativo ) {                    //  ed è stato modificato il nominativo
                richiestaGet(process.env.VUE_APP_URL_RICHIESTA_NUOVO_TOKEN_AUTENTICAZIONE)
                    .then( setTokenAutenticazione )
                    .then( () => this.$emit('nominativo-attore-modificato', this.nominativo) )
                    .catch( console.error );
              }

              alert( "Modifiche effettuate!" );
            })
            .catch( rispostaServer => {
              console.error( rispostaServer );
              alert( "ERRORE: "+ rispostaServer.data );
            });
      } else {
        this.ripristinaValoriForm();  // utente non ha confermato le modifiche, quindi si ripristano i valori precedenti
      }

      // In ogni caso, reset delle proprietà per predisporre la pagina alle interazioni successive
      this.flag_inviaDatiForm = false;
      document.querySelector('#' + this.idHtmlQuestoComponente + ' form').reset();  // pulizia dei campi (altrimenti rimane input file)

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
          this.caricaQuestoComponente();
        }
      }
    },

    /**
     * Osserva nel parametro della route se cambia l'identificativo
     * dell'attore cui questa scheda si riferisce.*/
    '$route.params': {
      immediate: true,
      deep: true,
      handler: function( nuoviParametriRoute ) {
        if ( nuoviParametriRoute[process.env.VUE_APP_ROUTER_PARAMETRO_ID_ATTORE] ) {
          this.isComponenteCaricato = false;
          this.caricaQuestoComponente();
        }
      }
    },

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