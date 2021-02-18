<template v-if="layoutCaricato">

  <!-- Componente per mostrare un elenco di attori -->

  <AggiuntaAttore :tipoAttoreAutenticato="tipoAttoreAutenticato_wrapper"
                  :csrfToken="csrfToken_wrapper"
                  @csrf-token-ricevuto="$emit('csrf-token-ricevuto', $event)"
                  @aggiunto-nuovo-attore="aggiungiNuovoAttoreAllElenco($event)"
                  @nominativo-attore-modificato="$emit('nominativo-attore-modificato',$event)"/>



  <!-- TODO : qua ci va l'elenco degli attori, iterando (v-for) su tutti gli attori associati all'attore autenticato
          Cliccando (<router-link :to="...">) su un attore si apre la sua scheda (SchedaDiUnAttore.vue)>
          Sarebbe carino implementare anche una piccola searchbox da mettere in testa a questo componente per filtrare
          gli attori per nome -->

  <nav v-if="isAdministratorAttualmenteAutenticato()" id="sceltaTipoAttoreDiCuiMostrareElenco">
    Visualizza elenco
    <!-- Administrator può scegliere se visualizzare Uploader o altri Administrator -->
    <router-link :to="{
        name: $route.name,  // questa stessa route (non bisogna cambiare componente, ma solo scegliere che cosa mostrare)
        params:{[NOME_PARAM_TIPO_ATTORE]: tipoAttore_uploader}
      }">
      Uploader
    </router-link>
    <router-link :to="{
        name: $route.name,
        params:{[NOME_PARAM_TIPO_ATTORE]: tipoAttore_administrator}
      }">
      Administrator
    </router-link>
  </nav>

  <ol>
    <li v-for="attore in Array.from(mappa_idAttore_proprietaAttore.entries())"
        :key="attore[0]/*Id dell'attore*/">
      <router-link :to="{
        name: NOME_ROUTE_SCHEDA_ATTORE,
        params: {
          [NOME_PARAM_ID_ATTORE_router]       : attore[0],
          [NOME_PARAM_TIPO_ATTORE]            : tipiAttoreCuiQuestoElencoSiRiferisce,
          [NOME_PARAM_PROPRIETA_ATTORE_router]: JSON.stringify(attore[1]),
            // JSON.stringify risolve il problema del passaggio di oggetti come props in Vue-Router
        }
      }">
        {{ attore[1][NOME_PROP_NOMINATIVO] }}
      </router-link>
    </li>
  </ol>

  <p v-if="isConsumerAttualmenteAutenticato() &&
          mappa_idAttore_proprietaAttore.size === 0">
    Nessun <i>Uploader</i> disponibile.
  </p>


</template>

<script>
import {richiestaGet} from "../../utils/http";
import AggiuntaAttore from "./AggiuntaAttore";
import {getMappa_idAttore_proprietaAttore} from "../../utils/richiesteInfoSuAttori";

export default {
  name: "ElencoAttori",
  components: {AggiuntaAttore},
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
      layoutCaricato: false,

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


      // Wrapper
      tipoAttoreAutenticato_wrapper: this.tipoAttoreAutenticato,
      csrfToken_wrapper: this.csrfToken,

      // Copia dalle variabili d'ambiente: bisogna dichiararle per usarle nel template  // TODO : serve ? C'è anche in SchermataPrincipaleAttoreAutenticato
      tipoAttore_consumer: process.env.VUE_APP_TIPO_UTENTE_AUTENTICATO_CONSUMER,
      tipoAttore_uploader: process.env.VUE_APP_TIPO_UTENTE_AUTENTICATO_UPLOADER,
      tipoAttore_administrator: process.env.VUE_APP_TIPO_UTENTE_AUTENTICATO_ADMINISTRATOR

    }
  },
  created() {

    this.caricamentoQuestoComponente();

  },
  methods: {

    /** Metodo per il caricamento dell'intero componente (incluse le
     * richieste al server per l'elenco degli attori).*/
    async caricamentoQuestoComponente() {

      this.layoutCaricato = false;

      this.tipiAttoreCuiQuestoElencoSiRiferisce =  this.qualeTipoAttoriDiCuiMostrareElenco();

      const richiestaElencoAttoriAlServer = async () => {
        // Il server fornirà una mappa { idAttore => {oggetto con le prop dell'attore idAttore} }

        // Richiede l'elenco degli Attori associati con questo attualmente autenticato
        await getElencoAttori( this.tipoAttoreAutenticato_wrapper,
                               this.tipiAttoreCuiQuestoElencoSiRiferisce )

            // Richiede le info di ogni Attore nell'elenco restituito dalla Promise precedente
            // poi richiede la mappa { idAttore => {proprietaQuestoAttore} }
            .then( arrayConIdTuttiGliAttoriDaMostrare =>
                getMappa_idAttore_proprietaAttore( arrayConIdTuttiGliAttoriDaMostrare,
                                                   this.tipoAttoreAutenticato_wrapper,
                                                   this.tipiAttoreCuiQuestoElencoSiRiferisce ) )

            // Ordina la mappa degli Attori (con relative proprietà) alfabeticamente e la salva nelle proprietà di questo componente
            .then( mappa_idAttore_proprietaAttore => {
              this.mappa_idAttore_proprietaAttore = mappa_idAttore_proprietaAttore;
              this.ordinaElencoAttori();
            })

            .catch( console.error ) ;
      };

      richiestaElencoAttoriAlServer()

        .then( () => {

          if( this.isConsumerAttualmenteAutenticato() &&
              this.mappa_idAttore_proprietaAttore.size === 1 ) {
            // Da requisiti:
            // Nel caso in cui il Consumer abbia ricevuto documenti da un solo Uploader,mostra direttamente la
            // lista dei documenti caricati da questi (in sintesi, non si mostra la schermata di scelta Uploader).

            const entryMappa_unicoAttore = Array.from( this.mappa_idAttore_proprietaAttore.entries() )[0];

            this.$router.push({
              name: this.NOME_ROUTE_SCHEDA_ATTORE,
              params: {
                [process.env.VUE_APP_ROUTER_PARAMETRO_MOSTRARE_PULSANTE_CHIUSURA_SCHEDA_ATTORE] : false,// vedere componente SchedaAttore
                [this.NOME_PARAM_ID_ATTORE_router]       : entryMappa_unicoAttore[0],                   // idAttore nell'elemento [0]
                [this.NOME_PARAM_PROPRIETA_ATTORE_router]: JSON.stringify(entryMappa_unicoAttore[1])    // propAttore nell'elemento [1]
                // JSON.stringify risolve il problema del passaggio di oggetti come props in Vue-Router
              }
            });

          }

          this.layoutCaricato = true;
        })
        .catch( console.error );  // TODO : aggiungere gestione dell'errore in tutti i componenti che usano questo "pattern" di caricamento contenuti

    },

    /** Aggiunge il nuovo attore, appena aggiunto dall'utente, all'elenco mostrato.*/
    aggiungiNuovoAttoreAllElenco( nuovoAttore ) {
      this.mappa_idAttore_proprietaAttore
          .set( nuovoAttore[process.env.VUE_APP_FORM_IDENTIFICATIVO_ATTORE_INPUT_FIELD],
                nuovoAttore );
      this.ordinaElencoAttori();
    },

    /** Ordina per nominativo l'elenco degli attori. Le modifiche vengono
     * apportate sullo stesso oggetto (stesso indirizzo in memoria).*/
    ordinaElencoAttori() {

      // TODO : cercare metodo più efficiente

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

        tipoAttoreDiCuiMostrareElenco = this.tipoAttore_uploader; // valore default

        if( this.$route && this.$route.params &&  // Verifica che non siano undefined o null
            this.$route.params[this.NOME_PARAM_TIPO_ATTORE]) {
          let varComodoPerValutareSeParametroDefinito =
              this.$route.params[this.NOME_PARAM_TIPO_ATTORE];
          if( varComodoPerValutareSeParametroDefinito ) {
            tipoAttoreDiCuiMostrareElenco = varComodoPerValutareSeParametroDefinito;
          }
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
          process.env.VUE_APP_TIPO_UTENTE_AUTENTICATO_CONSUMER;
    },

    /** Restituisce true se l'attore attualmente autenticato è un Administrator.*/
    isAdministratorAttualmenteAutenticato() {
      return this.tipoAttoreAutenticato_wrapper ===
          process.env.VUE_APP_TIPO_UTENTE_AUTENTICATO_ADMINISTRATOR;
    }
  },
  watch: {

    /**
     * Osserva nel parametro della route se cambia il tipo degli attori di cui
     * si vuole vedere l'elenco.
     */
    '$route.params': {
      // TODO : rivedere il meccanismo che permette agli Administrator di scegliere tra ElencoAttori di Uploader o di Consumer
      immediate: true,
      handler: function() {
        const nuovoTipoAttoriDiCuiMostrareElenco = this.qualeTipoAttoriDiCuiMostrareElenco();
        if( nuovoTipoAttoriDiCuiMostrareElenco !== this.tipoAttoriDiCuiMostrareElenco ) {
          this.tipoAttoriDiCuiMostrareElenco = nuovoTipoAttoriDiCuiMostrareElenco;
          this.caricamentoQuestoComponente();
        }
      }
    },

    tipoAttoreAutenticato: {
      immediate: true,
      deep: true,
      handler: function(nuovoValore) {
        this.tipoAttoreAutenticato_wrapper = nuovoValore;
        this.caricamentoQuestoComponente();
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

  if( tipoAttoreAttualmenteAutenticato === process.env.VUE_APP_TIPO_UTENTE_AUTENTICATO_ADMINISTRATOR ) {
    urlRichiesta = process.env.VUE_APP_URL_GET_ELENCO_ATTORI_PER_QUESTO_ADMINISTRATOR  +
        "/" + tipoAttoriDiCuiMostrareElenco;  // tipo attori richiesti appeso come @PathParam
  } else if( tipoAttoreAttualmenteAutenticato === process.env.VUE_APP_TIPO_UTENTE_AUTENTICATO_UPLOADER ) {
    urlRichiesta = process.env.VUE_APP_URL_GET_ELENCO_CONSUMER__RICHIESTA_DA_UPLOADER;
  } else if( tipoAttoreAttualmenteAutenticato === process.env.VUE_APP_TIPO_UTENTE_AUTENTICATO_CONSUMER ) {
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
  #sceltaTipoAttoreDiCuiMostrareElenco a:first-child::after {
    content: " | ";
  }
</style>