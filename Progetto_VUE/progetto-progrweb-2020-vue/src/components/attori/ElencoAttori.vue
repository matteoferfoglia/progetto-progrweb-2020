<template>
  <!-- Componente per mostrare un elenco di attori -->


  <Loader :isComponenteCaricato="isComponenteCaricato">


    <AggiuntaAttore :tipoAttoreAutenticato="tipoAttoreAutenticato_wrapper"
                    :csrfToken="csrfToken_wrapper"
                    @csrf-token-ricevuto="$emit('csrf-token-ricevuto', $event)"
                    @aggiunto-nuovo-attore="aggiungiNuovoAttoreAllElenco($event)"
                    @nominativo-attore-modificato="$emit('nominativo-attore-modificato',$event)"
                    v-if="! isConsumerAttualmenteAutenticato()" />

    <nav v-if="isAdministratorAttualmenteAutenticato()" id="sceltaTipoAttoreDiCuiMostrareElenco">
      Visualizza l'elenco degli
      <!-- Administrator può scegliere se visualizzare Uploader o altri Administrator -->
      <router-link :class=classeRouterLinkUploader
                   :to="{
                        name: $route.name,  // questa stessa route (non bisogna cambiare componente, ma solo scegliere che cosa mostrare)
                        params:{[NOME_PARAM_TIPO_ATTORE]: tipoAttore_uploader}
                      }">
        Uploader
      </router-link> |
      <router-link :class=classeRouterLinkAdministrator
                   :to="{
                        name: $route.name,
                        params:{[NOME_PARAM_TIPO_ATTORE]: tipoAttore_administrator}
                      }">
        Administrator
      </router-link>
    </nav>

    <article class="card" id="elencoAttori">
      <h2>Elenco attori</h2>

      <FormConCsrfToken :csrf-token_prop="csrfToken_wrapper"
                        @csrf-token-ricevuto="$emit('csrf-token-ricevuto', $event)" >
        <!-- Dall'elenco è possibile eliminare un attore, cioè modificare
             lo stato, quindi necessaria protezione CSRF -->

        <ol v-if="mappa_idAttore_proprietaAttore.size>0">
          <li v-for="attore in Array.from(mappa_idAttore_proprietaAttore.entries())"
              class="list-group-item list-group-item-action d-flex"
              :key="attore[0]/*Id dell'attore*/">

            <button @click.prevent=" () => eliminaAttore( attore[0],
                                                       String(getIdentificativoAttoreAttualmenteAutenticato()),
                                                       tipoAttoreAutenticato,
                                                       csrfToken_wrapper,
                                                       attore[1][NOME_PROP_NOMINATIVO],
                                                       attore[0]===String(getIdentificativoAttoreAttualmenteAutenticato()),
                                                       $router                                                              )
                                               .then( () => mappa_idAttore_proprietaAttore.delete( attore[0] ) ) // elimina l'attore dall'elenco mostrato nel client
                                               .catch( () => {/*ignored, utente non ha confermato l'eliminazione */} ) "
                    class="x-circle btn btn-danger btn-elimina-attore"
                    v-if="isUploaderAttualmenteAutenticato()/* Funzione solo per Uplaoder */">
            </button>

            <router-link class="w-100 d-flex align-items-center"
                         :to="{
                          name: NOME_ROUTE_SCHEDA_ATTORE,
                          params: {
                            [NOME_PARAM_ID_ATTORE_router]       : attore[0],
                            [NOME_PARAM_TIPO_ATTORE]            : tipiAttoreCuiQuestoElencoSiRiferisce,
                            [nomeProp_mostrarePulsanteChiusuraSchedaAttore]: mappa_idAttore_proprietaAttore.size>1,
                            [NOME_PARAM_PROPRIETA_ATTORE_router]: JSON.stringify(attore[1]),
                              // JSON.stringify risolve il problema del passaggio di oggetti come props in Vue-Router
                          }
                        }">

              <img :src="creaUrlLogo(attore[0])"
                   alt=""
                   class="logo logo-elenco"
                   v-if="attore[1][nomePropTipoAttore]===tipoAttore_uploader"/>
              <div class="nominativo-attore w-100 d-flex justify-content-between">
                {{ attore[1][NOME_PROP_NOMINATIVO] }}
              </div>

            </router-link>
          </li>
        </ol>

        <p v-else>Nessun
          <i v-if="isAdministratorAttualmenteAutenticato()">attore</i>
          <i v-else-if="isConsumerAttualmenteAutenticato()">Uploader</i>
          <i v-else>Consumer</i>
          disponibile.
        </p>

      </FormConCsrfToken>



    </article>


  </Loader>

</template>

<script>
import {richiestaGet} from "@/utils/http";
import AggiuntaAttore from "./AggiuntaAttore";
import {creaUrlLogo, eliminaAttore, getMappa_idAttore_proprietaAttore} from "@/utils/richiesteSuAttori";
import Loader from "../layout/Loader";
import {areArrayEquivalenti} from "@/utils/utilitaGenerale";
import {getIdentificativoAttoreAttualmenteAutenticato} from "@/utils/autenticazione";
import FormConCsrfToken from "@/components/layout/FormConCsrfToken";

export default {
  name: "ElencoAttori",
  components: {FormConCsrfToken, Loader, AggiuntaAttore},
  inheritAttrs: false,
  emits: [
      /** Evento emesso quando riceve un token CSRF da un componente figlio.*/
      'csrf-token-ricevuto'
  ],
  props: [
    /** Tipo attore attualmente autenticato.*/
    "tipoAttoreAutenticato",

    /** Nome della proprietà contenente il nominativo di un attore nell'oggetto
     * che lo rappresenta.*/
    "NOME_PROP_NOMINATIVO",

    /** Token CSRF ricevuto dal padre.*/
    "csrfToken"
  ],
  data() {
    return {

      /** Flag, true quando il layout è caricato.*/
      isComponenteCaricato: false,

      /** Mappa { idAttore => oggettoConProprietaAttore },
       * ordinata alfabeticamente rispetto al nome dell'Attore.*/
      mappa_idAttore_proprietaAttore: new Map(),

      // Parametri Vue-Router
      /** Nome della route che conduce alla scheda di un attore.*/
      NOME_ROUTE_SCHEDA_ATTORE:           process.env.VUE_APP_ROUTER_NOME_SCHEDA_UN_ATTORE,
      /** Nome del parametro nella route contenente l'identificativo dell'attore
       * di cui si vedrà la scheda a seguito dell'inoltro nella route.*/
      NOME_PARAM_ID_ATTORE_router:        process.env.VUE_APP_ROUTER_PARAMETRO_ID_ATTORE,
      /** Nome del parametro nella route contenente l'oggetto con le proprietà
       *  dell'attore di cui si vedrà la scheda a seguito dell'inoltro nella route.*/
      NOME_PARAM_PROPRIETA_ATTORE_router: process.env.VUE_APP_ROUTER_PARAMETRO_PROPRIETA_ATTORE,
      /** Nel caso in cui vi sia attualmente autenticato un Administrator, gli
       * viene data la possibilità (tramite router-link) di scegliere se vedere
       * la lista di attori di tipo Administrator o Uploader: ciò dipende dal
       * valore di questo parametro.*/
      NOME_PARAM_TIPO_ATTORE: process.env.VUE_APP_ROUTER_PARAMETRO_TIPO_ATTORE_CUI_SCHEDA_SI_RIFERISCE,

      /** Tipi di attore cui questo elenco si riferisce (Administrator/Consumer/Uploader).*/
      tipiAttoreCuiQuestoElencoSiRiferisce: undefined,

      /** Timer per l'auto-aggiornamento del componente.*/
      timerAutoUpdate: undefined,

      /** Classe di stile per il router-link che permette di scegliere
       * di vedere l'elenco degli administrator. Questo attributo permette
       * di scegliere tale classe in modo programmatico. */
      classeRouterLinkAdministrator: "",

      /** Nome della classe di stile per il router-link che permette di scegliere
       * di vedere l'elenco degli uploader. Questo attributo permette
       * di scegliere tale classe in modo programmatico. */
      classeRouterLinkUploader: "",


      // Wrapper
      tipoAttoreAutenticato_wrapper: this.tipoAttoreAutenticato,
      csrfToken_wrapper: this.csrfToken,

      // Copia dalle variabili d'ambiente: bisogna dichiararle per usarle nel template
      tipoAttore_consumer: process.env.VUE_APP_TIPO_UTENTE__CONSUMER,
      tipoAttore_uploader: process.env.VUE_APP_TIPO_UTENTE__UPLOADER,
      tipoAttore_administrator: process.env.VUE_APP_TIPO_UTENTE__ADMINISTRATOR,
      nomePropTipoAttore: process.env.VUE_APP_FORM_TIPO_ATTORE_INPUT_FIELD_NAME,
      nomeProp_mostrarePulsanteChiusuraSchedaAttore: process.env.VUE_APP_ROUTER_PARAMETRO_MOSTRARE_PULSANTE_CHIUSURA_SCHEDA_ATTORE, // vedere componente mostrante la scheda di un attore

      // Import funzioni
      creaUrlLogo: creaUrlLogo,
      eliminaAttore: eliminaAttore,
      getIdentificativoAttoreAttualmenteAutenticato: getIdentificativoAttoreAttualmenteAutenticato

    }
  },
  created() {

    this.caricamentoQuestoComponente(); // altrimenti sarà fatto dal watch

    // Timer-auto aggiornamento
    this.timerAutoUpdate = setInterval(this.caricamentoQuestoComponente,
                                       process.env.VUE_APP_MILLISECONDI_AUTOAGGIORNAMENTO);

  },
  beforeUnmount () {
    clearInterval(this.timerAutoUpdate);
  },
  methods: {

    /** Metodo per il caricamento dell'intero componente (incluse le
     * richieste al server per l'elenco degli attori).*/
    async caricamentoQuestoComponente() {

      if( this.tipoAttoreAutenticato_wrapper ) {
        {
          // Decide il tipo attore di cui mostrare l'elenco
          this.tipiAttoreCuiQuestoElencoSiRiferisce = this.qualeTipoAttoriDiCuiMostrareElenco();
          if (this.isAdministratorAttualmenteAutenticato()) {
            if (this.tipiAttoreCuiQuestoElencoSiRiferisce === this.tipoAttore_administrator) {
              this.classeRouterLinkAdministrator = '"router-link-exact-active"';
              this.classeRouterLinkUploader = '""';
            } else {
              this.classeRouterLinkAdministrator = '""';
              this.classeRouterLinkUploader = '"router-link-exact-active"';
            }
          }
        }

        const richiestaElencoAttoriAlServer = async () => {
          // Il server fornirà una mappa { idAttore => {oggetto con le prop dell'attore idAttore} }

          const msg_NO_MODIFICHE = "Nessuna modifica rilevata";

          // Richiede l'elenco degli Attori associati con questo attualmente autenticato
          return getElencoAttori(this.tipoAttoreAutenticato_wrapper,
              this.tipiAttoreCuiQuestoElencoSiRiferisce)

              .then(arrayConIdTuttiGliAttoriDaMostrare => {
                if (areArrayEquivalenti(arrayConIdTuttiGliAttoriDaMostrare, Array.from(this.mappa_idAttore_proprietaAttore.keys())))
                  return Promise.reject(msg_NO_MODIFICHE);
                else
                  return arrayConIdTuttiGliAttoriDaMostrare;
              })

              // Richiede le info di ogni Attore nell'elenco restituito dalla Promise precedente
              // poi richiede la mappa { idAttore => {proprietaQuestoAttore} }
              .then(arrayConIdTuttiGliAttoriDaMostrare =>
                  getMappa_idAttore_proprietaAttore(arrayConIdTuttiGliAttoriDaMostrare,
                      this.tipoAttoreAutenticato_wrapper,
                      this.tipiAttoreCuiQuestoElencoSiRiferisce))

              // Ordina la mappa degli Attori (con relative proprietà) alfabeticamente e la salva nelle proprietà di questo componente
              .then(mappa_idAttore_proprietaAttore => {
                this.mappa_idAttore_proprietaAttore = mappa_idAttore_proprietaAttore;
                this.ordinaElencoAttori();
              })

              .catch(reason => {
                if (reason !== msg_NO_MODIFICHE)
                  console.error(reason)
              });
        };

        return richiestaElencoAttoriAlServer()
            .then(() => {

              if (this.isConsumerAttualmenteAutenticato() &&
                  this.mappa_idAttore_proprietaAttore.size === 1) {
                // Da requisiti:
                // Nel caso in cui il Consumer abbia ricevuto documenti da un solo Uploader,mostra direttamente la
                // lista dei documenti caricati da questi (in sintesi, non si mostra la schermata di scelta Uploader).

                const entryMappa_unicoAttore = Array.from(this.mappa_idAttore_proprietaAttore.entries())[0];

                return this.$router.push({
                  name: this.NOME_ROUTE_SCHEDA_ATTORE,
                  params: {
                    [process.env.VUE_APP_ROUTER_PARAMETRO_MOSTRARE_PULSANTE_CHIUSURA_SCHEDA_ATTORE]: false, // vedere componente SchedaAttore
                    [this.NOME_PARAM_ID_ATTORE_router]: entryMappa_unicoAttore[0],                          // idAttore nell'elemento [0]
                    [this.NOME_PARAM_PROPRIETA_ATTORE_router]: JSON.stringify(entryMappa_unicoAttore[1])    // propAttore nell'elemento [1]
                    // JSON.stringify risolve il problema del passaggio di oggetti come props in Vue-Router
                  }
                });

              }
            })
            .then( () => this.isComponenteCaricato = true )
            .catch(console.error);

      } else {
        // Se il corpo del metodo non è stato eseguito a causa di parametri invalidi
        // Allora riprova tra poco:
        setTimeout( this.caricamentoQuestoComponente, 10 );
      }

    },

    /** Aggiunge il nuovo attore, appena aggiunto dall'utente, all'elenco mostrato.*/
    aggiungiNuovoAttoreAllElenco( nuovoAttore ) {

      let isNuovoAttoreDaAggiungereAllElenco = true;  // true di default, ma Administrator vede elenchi separati:
                                                      // se è stato aggiunto un Uploader, non deve essere visinbile
                                                      // nell'elenco degli Administrator e viceversa

      if( this.isAdministratorAttualmenteAutenticato() ) {

        if( this.tipiAttoreCuiQuestoElencoSiRiferisce === process.env.VUE_APP_TIPO_UTENTE__UPLOADER ) {
          // SE qui, allora attualmente si sta mostrando la lista degli Uploader
          isNuovoAttoreDaAggiungereAllElenco =
              nuovoAttore[process.env.VUE_APP_FORM_TIPO_ATTORE_INPUT_FIELD_NAME] ===
                process.env.VUE_APP_TIPO_UTENTE__UPLOADER;

        } else if( this.tipiAttoreCuiQuestoElencoSiRiferisce === process.env.VUE_APP_TIPO_UTENTE__ADMINISTRATOR ) {
          isNuovoAttoreDaAggiungereAllElenco =
              nuovoAttore[process.env.VUE_APP_FORM_TIPO_ATTORE_INPUT_FIELD_NAME] ===
                process.env.VUE_APP_TIPO_UTENTE__ADMINISTRATOR;
        }

      }

      if( isNuovoAttoreDaAggiungereAllElenco ) {
        this.mappa_idAttore_proprietaAttore
            .set(nuovoAttore[process.env.VUE_APP_FORM_IDENTIFICATIVO_ATTORE_INPUT_FIELD],
                nuovoAttore);
        this.ordinaElencoAttori();
      }
    },

    /** Ordina per nominativo l'elenco degli attori. Le modifiche vengono
     * apportate sullo stesso oggetto (stesso indirizzo in memoria).*/
    ordinaElencoAttori() {

      // Algoritmo: copia le entries dell'elenco, ordinale, poi elimina
      // tutte le entries dall'elenco vero ed aggiungi una ad una quelle
      // dall'elenco ordinato.

      const copiaOrdinataEntriesDellElencoAttori =
        [...this.mappa_idAttore_proprietaAttore].sort( (a,b) =>
            a[1][this.NOME_PROP_NOMINATIVO].toLowerCase() > b[1][this.NOME_PROP_NOMINATIVO].toLowerCase() ? 1 : -1 );

      const chiaviInElenco = Array.from(this.mappa_idAttore_proprietaAttore.keys());
      chiaviInElenco.forEach( chiave => this.mappa_idAttore_proprietaAttore.delete(chiave) );

      copiaOrdinataEntriesDellElencoAttori.forEach( entryUnAttore =>
          this.mappa_idAttore_proprietaAttore.set( entryUnAttore[0], entryUnAttore[1] ) );

    },


    /** Restituisce il tipo degli attori di cui bisogna mostrare l'elenco.*/
    qualeTipoAttoriDiCuiMostrareElenco() {

      let tipoAttoreDiCuiMostrareElenco;  // valore restituito da questo metodo

      if( this.isAdministratorAttualmenteAutenticato() ) {
        // SE è un Administrator attualmente autenticato, deve poter scegliere
        //  se vedere l'elenco degli Uploader o degli Administrator

        tipoAttoreDiCuiMostrareElenco = this.tipoAttore_uploader; // valore default

        if( this.$route && this.$route.params &&  // Verifica che non siano undefined o null
            this.$route.params[this.NOME_PARAM_TIPO_ATTORE]) {
          let varComodoPerValutareSeParametroDefinito =
              this.$route.params[this.NOME_PARAM_TIPO_ATTORE];
          if( varComodoPerValutareSeParametroDefinito ) {
            tipoAttoreDiCuiMostrareElenco = varComodoPerValutareSeParametroDefinito;
          }
        } else {
          // route undefined, quindi imposta route default
          this.$router.push({to:"", params:{[this.NOME_PARAM_TIPO_ATTORE]: tipoAttoreDiCuiMostrareElenco}});
        }

      } else if ( this.isConsumerAttualmenteAutenticato() ) {
        tipoAttoreDiCuiMostrareElenco = this.tipoAttore_uploader;
      } else {
        tipoAttoreDiCuiMostrareElenco = this.tipoAttore_consumer;
      }

      return tipoAttoreDiCuiMostrareElenco;

    },

    /** Restituisce true se è un ConsumerAttualmenteAutenticato.*/
    isConsumerAttualmenteAutenticato() {
      return this.tipoAttoreAutenticato_wrapper ===
          process.env.VUE_APP_TIPO_UTENTE__CONSUMER;
    },

    /** Restituisce true se è un ConsumerAttualmenteAutenticato.*/
    isUploaderAttualmenteAutenticato() {
      return this.tipoAttoreAutenticato_wrapper ===
          process.env.VUE_APP_TIPO_UTENTE__UPLOADER;
    },

    /** Restituisce true se l'attore attualmente autenticato è un Administrator.*/
    isAdministratorAttualmenteAutenticato() {
      return this.tipoAttoreAutenticato_wrapper ===
          process.env.VUE_APP_TIPO_UTENTE__ADMINISTRATOR;
    }
  },
  watch: {

    /**
     * Osserva nel parametro della route se cambia il tipo degli attori di cui
     * si vuole vedere l'elenco.
     */
    '$route.params': {
      immediate: true,
      handler: function() {
        if ( this.isAdministratorAttualmenteAutenticato() ) {
          const nuovoTipoAttoriDiCuiMostrareElenco = this.qualeTipoAttoriDiCuiMostrareElenco();
          if (nuovoTipoAttoriDiCuiMostrareElenco !== this.tipoAttoriDiCuiMostrareElenco) {
            this.tipoAttoriDiCuiMostrareElenco = nuovoTipoAttoriDiCuiMostrareElenco;
            if( this.isComponenteCaricato )       // Se questo componente è già stato caricato
              this.caricamentoQuestoComponente(); // Allora, ricarica il componente quando cambiano gli attori da vedere
                                                  // Altrimenti ci pensa già la funzione di caricamento a mostrare le cose corrette (e non serve ricaricare)
          }
        }
      }
    },

    /** Osserva la property ottenuta dal componente padre, attendendo un valore
     * non undefined. */
    tipoAttoreAutenticato: {
      immediate: true,
      deep: true,
      handler: function(nuovoValore) {
        this.tipoAttoreAutenticato_wrapper = nuovoValore;
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

/** Richiede al server l'elenco di tutti gli Attore associati
 * con questo Uploader. Se la richiesta va a buon fine, questa
 * funzione restituisce una promise risolta il cui valore è
 * l'array con i valori richiesti.
 * @param tipoAttoreAttualmenteAutenticato Tipo dell'attore attualmente autenticato
 * @param tipoAttoriDiCuiMostrareElenco Tipo degli attori che dovranno essere presenti
 *                                      nell'elenco restituito dal server (es. Consumer
 *                                      dovrà richiedere elenco di Uploader).
 */
const getElencoAttori = async ( tipoAttoreAttualmenteAutenticato, tipoAttoriDiCuiMostrareElenco ) => {

  let urlRichiesta;

  if( tipoAttoreAttualmenteAutenticato === process.env.VUE_APP_TIPO_UTENTE__ADMINISTRATOR ) {
    urlRichiesta = process.env.VUE_APP_URL_GET_ELENCO_ATTORI_PER_QUESTO_ADMINISTRATOR  +
        "/" + tipoAttoriDiCuiMostrareElenco;  // tipo attori richiesti appeso come @PathParam
  } else if( tipoAttoreAttualmenteAutenticato === process.env.VUE_APP_TIPO_UTENTE__UPLOADER ) {
    urlRichiesta = process.env.VUE_APP_URL_GET_ELENCO_CONSUMER__RICHIESTA_DA_UPLOADER;
  } else if( tipoAttoreAttualmenteAutenticato === process.env.VUE_APP_TIPO_UTENTE__CONSUMER ) {
    urlRichiesta = process.env.VUE_APP_ELENCO_UPLOADER_PER_QUESTO_CONSUMER__RICHIESTA_DA_CONSUMER;
  } else {
    urlRichiesta = "";
  }

  if( urlRichiesta ) {
    return richiestaGet( urlRichiesta )
        .catch( rispostaErrore => {
          console.error("Errore durante il caricamento della lista di attori: " + rispostaErrore );
          return Promise.reject(rispostaErrore);
        });
  } else {
    return [];
  }

}


</script>

<style scoped>
  p,nav {
    padding: 0 1rem;
  }
  h2 {
    font-size: 1.7rem;
  }
  .list-group-item-action a {
    cursor: pointer;
  }
  .nominativo-attore::after {
    /* Fonte icona: https://icons.getbootstrap.com/icons/pen/ */
    content: url('data:image/svg+xml; utf8, <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" class="bi bi-caret-right-fill" viewBox="0 0 16 16"><path d="M12.14 8.753l-5.482 4.796c-.646.566-1.658.106-1.658-.753V3.204a1 1 0 0 1 1.659-.753l5.48 4.796a1 1 0 0 1 0 1.506z"/></svg>');
  }
  .logo-elenco {
    width: 2.5rem;
    height: auto;
    min-width: 2.5rem;
    max-height: 3rem;
    min-height: unset;
  }
  ol {
    padding: 0
  }
  #elencoAttori {
    border: 0;
  }
  .btn-elimina-attore {
    margin-right: 2rem;
  }
</style>