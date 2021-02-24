<template>
  <!-- Componente per mostrare una lista di documenti in forma tabellare -->

  <h4>Lista documenti</h4>

  <form class="filtro-hashtag d-flex flex-wrap align-items-center"
        v-if="mappa_hashtag_idDocumenti.size > 0 /*non mostrare se non ci sono hashtag*/ ">
    <!-- Form per filtraggio documenti rispetto ad hashtag -->
    <p>Filtra per hashtag: </p>
    <p>
      <input type="button"
             class="btn btn-link"
             @click="mostraTuttiIDocumenti"
             value="Mostra tutti">
    </p>
    <p>
      <input type="button"
             class="btn btn-link"
             @click="nascondiTuttiIDocumenti"
             value="Nascondi tutti">
    </p>
    <ol>
      <li class="form-check form-check-inline"
          v-for="hashtag in Array.from(mappa_hashtag_idDocumenti.keys()).sort()"
          :key="hashtag">
        <p>
          <label class="form-check-label">
            <input type="checkbox"
                   class="form-check-input"
                   @change="mostraDocumentiConHashtagFiltrato(hashtag, $event.target.checked)/*
                              Se cliccato, mostra documenti con questo hashtag
                              Fonte: https://stackoverflow.com/a/41001483 */"
                   :checked="listaHashtagDaMostrare.includes(hashtag)"/>
            {{ hashtag === '' ? 'Senza hashtag' : hashtag }}
          </label>
        </p>
      </li>
    </ol>
  </form>

  <FormConCsrfToken @csrf-token-ricevuto="$emit('csrf-token-ricevuto', $event)"
                    class="form-lista-documenti"
                    v-if="mappaDocumentiDaMostrare.get().size > 0"          >
    <!-- Eliminazione di un documento comporta il cambio di stato (serve token CSRF) -->

    <table class="table table-hover">
      <!-- Tabella dei documenti --><!-- TODO : aggiungere un altezza massima nello stile della tabella  -->
      <thead class="thead-dark">
        <tr>
          <th>#</th>
          <th v-for="nomeColonna in nomiPropDocumenti"
              :key="nomeColonna">
            {{ camelCaseToHumanReadable(nomeColonna) }}
          </th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="(documento, ignored, indice) in mappaDocumentiDaMostrare.getObjetFromEntries()"
            :key="indice"><!-- Ogni riga è un documento -->
          <td>{{ indice+1 }}</td>
          <td v-for="propertyQuestaColonna in nomiPropDocumenti.filter( nomeColonna => nomeColonna!==NOME_PROP_LINK_DOWNLOAD_DOCUMENTO &&
                                                                                       nomeColonna!==NOME_PROP_LINK_DELETE_DOCUMENTO     )"
              :key="propertyQuestaColonna" >
            {{ documento[propertyQuestaColonna] }}
          </td>
          <td v-if="urlDownloadDocumento">
            <a :href=documento[NOME_PROP_LINK_DOWNLOAD_DOCUMENTO]
               download
               class="link-download"
               @click.prevent="scaricaDocumento(documento)">
            </a>
          </td>
          <td v-if="urlEliminazioneDocumento">
            <a :href="documento[NOME_PROP_LINK_DELETE_DOCUMENTO]"
               class="link-delete"
               @click.prevent="eliminaDocumento(documento)">
            </a>
          </td>
        </tr>
      </tbody>
    </table>


  </FormConCsrfToken>

  <p v-else>Nessun documento disponibile.</p>

</template>

<script>

/** Questo componente Vue implementa i requisiti descritti nella sezione
 * <cite>Lista Documenti - Consumers</cite>.
 * Nota: questo componente fa spesso uso del tipo Map
 * (<a href="https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Map">Fonte</a>),
 * in quanto ne ha semplificato l'implementazione.*/


import {camelCaseToHumanReadable, capitalize} from "../../utils/utilitaGenerale";
import {richiestaDelete, richiestaGet} from "../../utils/http";
import {
  aggiungiDocumentoAdIndiceHashtag,
  creaIndiceDeiFileRispettoAgliHashtagCheContengono,
  getNomePropertyDataVisualizzazioneDocumenti,
  MappaDocumenti,
  ordinaMappaSuDataCaricamentoConNonVisualizzatiDavanti
} from "../../utils/documenti";
import FormConCsrfToken from "../layout/FormConCsrfToken";
export default {
  name: "TabellaDocumenti",
  components: {FormConCsrfToken},
  emits: [
    /**Evento emesso dopo aver aggiunto un nuovo documento, quando
     * richiesto dall'elemento padre.*/
    'nuovo-documento-mostrato',

    /** Evento emesso quando riceve un token CSRF da un componente figlio.*/
    'csrf-token-ricevuto'

  ],
  props: [

    /** URL verso cui richiedere l'elenco dei documenti per un attore.*/
    "urlRichiestaElencoDocumentiPerUnAttore",

    /** URL verso cui richiedere il download di un documento.*/
    "urlDownloadDocumento",

    /** URL verso cui richiedere la cancellazione di un documento.
     * Se questa prop non è specificata, si assume che l'utente
     * <strong>non</strong> possa eliminare documenti.*/
    "urlEliminazioneDocumento",

    /** Nuovo documento da aggiungere alla lista. Vedere descrizione
     * nel componente padre. Quando cambia stato ed è definito, il
     * documento che questa property rappresenta viene aggiunto alla
     * lista attualmente mostrata.*/
    "oggetto_idDocumentoDaAggiungere_proprietaDocumentoDaAggiungere",

    /** Nome del Consumer a cui questi documenti si riferiscono.*/
    "nomeConsumer",

    /** Tipo attore attualmente autenticato.*/
    "tipoAttoreAutenticato",

    /** Token CSRF ricevuto dal padre.*/
    "csrfToken"
  ],
  data() {
    return {

      // Variabili inizializzate in created() in base ai dati ricevuti dal server

      /** Mappa in cui ogni chiave è un hashtag ed il corrispettivo
       * valore è un array di identificativi (posting-list) di documenti
       * aventi tale hashtag, tra quelli mostrati in {@link #mappaDocumenti}.*/
      mappa_hashtag_idDocumenti: new Map(),

      /** Mappa dei documenti per uno specifico Consumer.
       * Ogni entry rappresenta un documento nella forma:
       *    [ idDocumento, { oggetto con proprietà del documento } ]
       */
      mappaDocumenti: new MappaDocumenti(), // TODO : serve una classe MappaDocumenti? Se l'oggetto non viene passato tramite props non basta una Map normale da usare qua?

      /** Come {@link #mappaDocumenti}, ma contiene solo i documenti da
       * mostrare, a seguito dell'eventuale filtraggio dell'utente.*/
      mappaDocumentiDaMostrare: new MappaDocumenti(),

      /** Lista di hashtag risultanti dal filtraggio: i documenti
       * mostrati all'utente contengono (per poter essere mostrati
       * da questo componente) almeno uno degli hashtag presenti in
       * questo attributo.*/
      listaHashtagDaMostrare: [],            // modificato dinamicamente in base al filtraggio dell'utente

      /** Nome delle colonne di intestazione della tabella coi documenti.*/
      nomiPropDocumenti: [],

      /** In un documento, è il nome della property (se presente)
       * il cui valore è un link di download di quel documento.*/
      NOME_PROP_LINK_DOWNLOAD_DOCUMENTO: "Download",

      /** In un documento, è il nome della property (se presente)
       * il cui valore è un link di eliminazione di quel documento.*/
      NOME_PROP_LINK_DELETE_DOCUMENTO: "Elimina",

      /** In un documento, è il nome della property (se presente)
       * il cui valore è il nome di quel documento.*/
      NOME_PROP_NOME_DOCUMENTO: undefined,

      /** In un documento, è il nome della property il cui valore
       * è la lista di hashtag di quel documento.*/
      NOME_PROP_LISTA_HASHTAG_DOCUMENTO: undefined,

      /** In un documento, è il nome della property il cui valore
       * è la data di visualizzazione di quel documento.*/
      NOME_PROP_DATA_VISUALIZZAZIONE_DOCUMENTO: undefined,    // TODO : aggiungerle alle variabili d'ambiente senza richiederle ogni volta, oppure creare un utility da eseguire una volta sola all'accesso nell'applicazione che richieda una volta per tutte tutti i parametri

      /** In un documento, è il nome della property il cui valore
       * è la data di caricamento di quel documento.*/
      NOME_PROP_DATA_CARICAMENTO_DOCUMENTO: undefined,

      /** "Import" della funzione per usarla nel template.*/
      camelCaseToHumanReadable: camelCaseToHumanReadable,

      /** Timer per l'auto-aggiornamento del componente.*/
      timerAutoUpdate: undefined,

      // Wrapper
      csrfToken_wrapper: this.csrfToken

    }
  },
  created() {

    // TODO : aggiungere un "loader" (flag layoutCaricato)

    const caricamentoComponente = async () => {

              // Richiede il nome della property di un documento contenente la data di visualizzazione del documento stesso
      await getNomePropertyDataVisualizzazioneDocumenti()
              .then(nomeProp => this.NOME_PROP_DATA_VISUALIZZAZIONE_DOCUMENTO = nomeProp)

              // Richiede il nome della property di un documento contenente la data di caricamento del documento stesso
              .then( getNomePropertyDataCaricamentoDocumenti )
              .then(nomeProp => this.NOME_PROP_DATA_CARICAMENTO_DOCUMENTO = nomeProp)

              // Richiede mappa idFile-propFile per questo attore
              .then( () => richiestaGet(this.urlRichiestaElencoDocumentiPerUnAttore) )

              // Crea l'indice degli hashtag
              .then(rispostaConMappaFile_id_prop => {
                richiestaGet(process.env.VUE_APP_URL_GET_NOME_PROP_HAHSTAGS_IN_DOCUMENTI)
                    .then(nomePropertyHashtags => {
                      this.NOME_PROP_LISTA_HASHTAG_DOCUMENTO = nomePropertyHashtags;
                      this.mappa_hashtag_idDocumenti =
                          creaIndiceDeiFileRispettoAgliHashtagCheContengono(rispostaConMappaFile_id_prop, nomePropertyHashtags);
                    });
                return rispostaConMappaFile_id_prop;
              })

              // Crea mappa
              .then(rispostaConMappaFile_id_prop => new Map(Object.entries(rispostaConMappaFile_id_prop)))
              // Ordina mappa
              .then( mappa => ordinaMappaSuDataCaricamentoConNonVisualizzatiDavanti( mappa,
                                              this.NOME_PROP_DATA_VISUALIZZAZIONE_DOCUMENTO,
                                              this.NOME_PROP_DATA_CARICAMENTO_DOCUMENTO))

              // In ogni documento, aggiungi link di cancellazione e di download, poi restituisci la mappa
              .then(mappa =>
                  new Map(
                      Array.from(mappa.entries())
                           .map(unDocumento => this.aggiungiUrlDownloadEliminazioneDocumentoERestituisciEntryDocumento(unDocumento))
                  )
              )

              // In ogni documento, modifica il formato di rappresentazione delle data
              .then(mappa =>
                  new Map(
                      Array.from(mappa.entries())
                           .map( this.formattaDate )
                  )
              )

              // Salva la mappa
              .then(mappa => {
                this.mappaDocumenti.set(mappa);
                this.mappaDocumentiDaMostrare.set(mappa);
              })
              .catch(console.error);


      // Richiede il nome della property di un documento contenente il nome del documento stesso
      await richiestaGet(process.env.VUE_APP_URL_GET_NOME_PROP_NOME_DOCUMENTO)
              .then(nomeProp => this.NOME_PROP_NOME_DOCUMENTO = nomeProp)
              .catch(console.error);

      // Richiede l'elenco dei nomi delle properties dei documenti
      await richiestaGet(process.env.VUE_APP_URL_GET_NOMI_TUTTE_LE_PROP_DOCUMENTI)
              // Aggiunge alle properties dei documenti le colonne con il link di download ed eliminazione
              // documento (da creare dinamicamente), poi salva l'array
              .then(nomiPropDocumenti => {
                nomiPropDocumenti.push(this.NOME_PROP_LINK_DOWNLOAD_DOCUMENTO);
                if (this.urlEliminazioneDocumento) {
                  nomiPropDocumenti.push(this.NOME_PROP_LINK_DELETE_DOCUMENTO)
                }
                this.nomiPropDocumenti = nomiPropDocumenti;
              })
              .catch(console.error);

    };


    caricamentoComponente()
      .then( () => this.listaHashtagDaMostrare = Array.from( this.mappa_hashtag_idDocumenti.keys() ) )  // All'inizio mostra tutti gli hashtag
      .catch( errore => {
        console.error( "Errore durante il caricamento del componente " + this.$options.name + ": " + errore );
        alert( "Errore durante il caricamento." );
      })

    // Gestione dell'auto-aggiornamento della tabella  // TODO: sarebbe meglio che l'autoaggiornamento richiedesse solo i nuovi documenti e non tutti di nuovo
    this.timerAutoUpdate = setInterval(caricamentoComponente, process.env.VUE_APP_MILLISECONDI_AUTOAGGIORNAMENTO);

  },
  beforeUnmount () {
    clearInterval(this.timerAutoUpdate)
  },
  methods: {

    /** Resetta tutti gli hashtag e mostra tutti i documenti disponibili.*/
    mostraTuttiIDocumenti() {
      this.listaHashtagDaMostrare = Array.from( this.mappa_hashtag_idDocumenti.keys() ).sort();
      this.mappaDocumentiDaMostrare.set( this.mappaDocumenti.get() );
    },

    /** Resetta tutti gli hashtag e nasconde tutti i documenti disponibili.*/
    nascondiTuttiIDocumenti() {
      this.listaHashtagDaMostrare = [];
      this.mappaDocumentiDaMostrare.set( new MappaDocumenti() );
    },

    /** Dati un hashtag ed un flag booleano, questa funzione aggiorna
     * l'array dei documenti da mostrare: se il flag è true, allora
     * saranno mostrati tutti i documenti che contengono l'hashtag passato
     * come parametro. Per poter essere mostrato, un documento deve
     * avere tra gli hashtag almeno uno tra quelli compresi nella lista
     * di hashtag da mostrare.*/
    mostraDocumentiConHashtagFiltrato(hashtag, daMostrare) {

      // TODO : da testare

      // Aggiorna lista di hashtag da mostrare
      if(daMostrare) {
        this.listaHashtagDaMostrare.push(hashtag);
      } else {
        this.listaHashtagDaMostrare = this.listaHashtagDaMostrare
                                          .filter( hashtagDaMostrare => hashtagDaMostrare!==hashtag);
      }

      const listaIdDocumentiDaMostrare = new Set(                                   // utilizzo Set per evitare chiavi di documenti duplicate
          Array.from(this.mappa_hashtag_idDocumenti.entries())
               .filter( entry => this.listaHashtagDaMostrare.includes(entry[0]) ) // entry[0] è la chiave (l'hashtag)
               .flatMap( entry => entry[1] )                               // entry[1] è l'array con gli id dei file che contengono l'hashtag specificato in entry[0]
          // Fonte (flatMap): https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Array/flatMap
      );

      this.mappaDocumentiDaMostrare.set( new Map(
          this.mappaDocumenti.getArrayEntries() // filter non si applica su Map, quindi converto in Array, poi riconverto il risultato in Map
              .filter( entryDocumento => Array.from( listaIdDocumentiDaMostrare.values() )
                                              .includes(entryDocumento[0]) )
      )); // entryDocumento[0] è l'id di un documento
      // Risultato filter: nella mappa restano solo i documenti che hanno id tra quelli filtrati prima

      // Osservazione: elencoDocumenti non viene sovrascritto, quindi l'ordine di visualizzazione è mantenuto
      // TODO : verificare che si mantenga l'ordine di visualizzazione dei documenti

    },

    /** Funzione per eliminare un documento dall'elenco attualmente mostrato,
     * a seguito della richiesta di eliminazione da parte dell'utente.
     * Questo metodo <strong>non</strong> richiede al server l'eliminazione
     * d tale documento.
     * @param idDocumentoEliminato Identificativo del documento da eliminare.*/
    rimuoviDocumentoDaListaAttualmenteMostrata(idDocumentoEliminato) {

      this.mappaDocumenti.delete(idDocumentoEliminato);
      this.mappaDocumentiDaMostrare.delete(idDocumentoEliminato); // TODO : creare una specie di proxy, in modo che la mappa dei documenti da mostrare sia un sottoinsieme di tutti i documenti, senza fare le stesse operazioni su entrambe le variabili

      // Ricrea indice degli hashtag
      this.mappa_hashtag_idDocumenti =
          creaIndiceDeiFileRispettoAgliHashtagCheContengono( this.mappaDocumenti.getObjetFromEntries(),
                                                             this.NOME_PROP_LISTA_HASHTAG_DOCUMENTO    );

      // Aggiorna lista di hashtag da mostrare (se elimino un documento ed un hashtag
      // era presenta solo in quel documento, allora devo eliminare quell'hashtag)
      this.listaHashtagDaMostrare = this.listaHashtagDaMostrare.filter( hashtag =>
          Array.from(this.mappa_hashtag_idDocumenti.keys()).includes( hashtag ) );

    },

    /** Metodo per il download di un documento (dipende dall'URL)
     * (<a href="https://stackoverflow.com/q/33247716">Fonte</a>).
     * @param documento Documento da scaricare.*/
    scaricaDocumento( documento ) {

      const nomeDocumento = documento[this.NOME_PROP_NOME_DOCUMENTO];
      const urlDownloadDocumento = documento[this.NOME_PROP_LINK_DOWNLOAD_DOCUMENTO];

      const idDocumento = this.getIdDocumentoDaUrlDownloadEliminazione( urlDownloadDocumento );

      richiestaGet( urlDownloadDocumento, {}, true)
        .then( risposta => {
          const blob = new Blob( [risposta], {type: "octet/stream"} );

          const downloadUrl = window.URL.createObjectURL(blob);
          const a = document.createElement("a");
          a.setAttribute("download", nomeDocumento);
          a.setAttribute("style","display: none");
          a.href = downloadUrl;
          document.body.appendChild(a);
          a.click();
          URL.revokeObjectURL(downloadUrl);

        })
        .catch( console.error );

      if( this.tipoAttoreAutenticato === process.env.VUE_APP_TIPO_UTENTE__CONSUMER ) {
        // Se un consumer ha scaricato il documento, si aggiorna la vista per mostrare la data/ora
        // (richiesta al server per conformità nel formato scelto dal server) a meno che tale
        // data/ora non sia già scritta (perché il documento era già stato visualizzato)

        if( ! documento[this.NOME_PROP_DATA_VISUALIZZAZIONE_DOCUMENTO] ) {
          richiestaGet(process.env.VUE_APP_URL_GET_DATAORA_VISUALIZZAZIONE_DOCUMENTO + '/' + idDocumento)
              .then(dataOraVisualizzazione => {
                // TODO : trovare soluzione in MappaDocumenti per evitare questa duplicazione di codice
                this.mappaDocumenti.getDaChiave(idDocumento)[this.NOME_PROP_DATA_VISUALIZZAZIONE_DOCUMENTO] =
                    dataOraVisualizzazione;
                this.mappaDocumentiDaMostrare.getDaChiave(idDocumento)[this.NOME_PROP_DATA_VISUALIZZAZIONE_DOCUMENTO] =
                    dataOraVisualizzazione;
                this.formattaDate([idDocumento, this.mappaDocumenti.getDaChiave(idDocumento)]);
                this.formattaDate([idDocumento, this.mappaDocumentiDaMostrare.getDaChiave(idDocumento)]);
              })
              .catch(console.error);
        }
      }
    },

    /** Metodo per la cancellazione di un documento (dipende dall'URL).
     * @param documento Documento da eliminare.*/
    eliminaDocumento( documento ) {

      const urlEliminazioneDocumento = documento[this.NOME_PROP_LINK_DELETE_DOCUMENTO];

      // Id del documento appeso in fondo all'url
      const idDocumentoEliminato = this.getIdDocumentoDaUrlDownloadEliminazione(urlEliminazioneDocumento);

      richiestaDelete( urlEliminazioneDocumento, {
        [process.env.VUE_APP_FORM_CSRF_INPUT_FIELD_NAME]: this.csrfToken_wrapper
      })
          .then( () => {
            this.rimuoviDocumentoDaListaAttualmenteMostrata(idDocumentoEliminato);
            alert("Documento eliminato");
          })
          .catch( console.error );

    },

    /** Dato l'url di download o eliminazione di un documento, ne restituisce l'url.*/
    getIdDocumentoDaUrlDownloadEliminazione( url ) {
      return url.substring( url.lastIndexOf("/") + 1 );
    },

    /** Data un'entry della mappa restituita dal server quando
     * gli si richiede l'elenco dei documenti (in pratica: dato
     * un documento, nella forma [ idDocumento, { proprietà documento} ] ),
     * questo metodo aggiunge alle proprietà di quel documento gli url
     * per il download e l'eliminazione del documento stesso.
     * Infine restituisce la entry corrispondente al documento, con
     * le properties (gli url) appena aggiunte.
     * @param unDocumento nella forma di una entry, cioè un array con due
     *                    elementi:   [ idDocumento, { proprietà documento} ] */
    aggiungiUrlDownloadEliminazioneDocumentoERestituisciEntryDocumento(unDocumento) {

      // Decomposizione di ogni entry
      const idDocumento = unDocumento[0];
      const propDocumento = unDocumento[1];

      propDocumento[this.NOME_PROP_LINK_DOWNLOAD_DOCUMENTO] = this.urlDownloadDocumento + "/" + idDocumento;

      if( this.urlEliminazioneDocumento ) {
        propDocumento[this.NOME_PROP_LINK_DELETE_DOCUMENTO] = this.urlEliminazioneDocumento + "/" + idDocumento;
      }

      return [idDocumento, propDocumento];

    },

    /** Metodo per aggiungere un nuovo documento alla lista di quelli mostrati.
     * Questo metodo <strong>non</strong> interroga il server, ma si aspetta come
     * parametro il nuovo documento nella forma di un oggetto come segue:
     *    { idDocumento : {proprieta del documento} }
     *
     * @param oggetto_idDocumentoDaAggiungere_proprietaDocumentoDaAggiungere
     */
    aggiungiNuovoDocumentoAllaListaMostrata( oggetto_idDocumentoDaAggiungere_proprietaDocumentoDaAggiungere ) {

      // Aggiungi url nelle properties
      oggetto_idDocumentoDaAggiungere_proprietaDocumentoDaAggiungere = new Map(
          [
              this.formattaDate(this.aggiungiUrlDownloadEliminazioneDocumentoERestituisciEntryDocumento(
                Object.entries(oggetto_idDocumentoDaAggiungere_proprietaDocumentoDaAggiungere)[0])
            )
          ]
      );

      // TODO : rivedere: c'è un metodo più furbo per aggiornare la mappa dei documenti (bisogna aggiornare anche l'elenco degli hashtag per mantenere consistenza) ?

      const oggetto_Id_PropNuovoDocumento = Object.fromEntries( oggetto_idDocumentoDaAggiungere_proprietaDocumentoDaAggiungere );
      aggiungiDocumentoAdIndiceHashtag( this.mappa_hashtag_idDocumenti,
          oggetto_Id_PropNuovoDocumento,
          this.NOME_PROP_LISTA_HASHTAG_DOCUMENTO );

      // nel valore della prima property dell'oggetto ci sono le properties del documento
      const oggettoConPropNuovoDocumento = Object.values( oggetto_Id_PropNuovoDocumento )[0] ;

      const listaHashtagNuovoDocumento   = oggettoConPropNuovoDocumento[this.NOME_PROP_LISTA_HASHTAG_DOCUMENTO];

      // Aggiungi gli hashtag di questo documento a quelli da mostrare
      this.listaHashtagDaMostrare =
          [...new Set([ ...this.listaHashtagDaMostrare, ...listaHashtagNuovoDocumento])].sort();
      // Set() evita eventuali duplicati negli hashtag,
      // ma non dispone di sort() quindi necessario prima riconvertire in Array


      // Aggiungi il documento all'elenco di quelli noti a questo componente
      this.mappaDocumenti.set(
        new Map([
          ...oggetto_idDocumentoDaAggiungere_proprietaDocumentoDaAggiungere,  // merge della nuova entry in cima alla mappa
          ... this.mappaDocumenti.get()
        ])
      );

      // Aggiungi il documento a quelli da mostrare
      this.mappaDocumentiDaMostrare.set(
          new Map([
            ...oggetto_idDocumentoDaAggiungere_proprietaDocumentoDaAggiungere,
            ... this.mappaDocumentiDaMostrare.get()
          ])
      );

    },

    /** Data un'entry rappresentante un documento, ne formatta le date presenti.
     * @param entry Array avente come primo elemento l'identificativo di un
     *              documento come secondo elemento gli attributi di quel documento.
     */
    formattaDate( entry ) {

      const formattaData = stringaRappresentanteData => {
        if (!stringaRappresentanteData)
          return stringaRappresentanteData;

        return capitalize(
            new Date(stringaRappresentanteData)
                .toLocaleString(undefined/*varies according to default locale*/,
                    {
                      weekday: 'short', year: 'numeric', month: 'short', day: 'numeric',
                      hour: '2-digit', minute: '2-digit'
                    })
        );
      }

      const proprietaQuestoDocumento = entry[1]; // è un oggetto
      proprietaQuestoDocumento[this.NOME_PROP_DATA_CARICAMENTO_DOCUMENTO] =
          formattaData(proprietaQuestoDocumento[this.NOME_PROP_DATA_CARICAMENTO_DOCUMENTO]);
      proprietaQuestoDocumento[this.NOME_PROP_DATA_VISUALIZZAZIONE_DOCUMENTO] =
          formattaData(proprietaQuestoDocumento[this.NOME_PROP_DATA_VISUALIZZAZIONE_DOCUMENTO]);

      return entry;

    }

  },
  watch: {

    csrfToken : {
      immediate: true,
      deep: true,
      handler(nuovoValore) {
        this.csrfToken_wrapper = nuovoValore;
      }
    },

    /** Cambia stato se è stato caricato un nuovo documento, quindi
     * è necessario aggiornare la vista.*/
    oggetto_idDocumentoDaAggiungere_proprietaDocumentoDaAggiungere: {
      immediate: true,
      deep: true,
      handler( nuovoDocumento ) {
        if( nuovoDocumento ) {  // che non sia falsy
          this.aggiungiNuovoDocumentoAllaListaMostrata( nuovoDocumento );
          this.$emit( 'nuovo-documento-mostrato' );
        }
      }
    }
  }
}

/** Questa funzione richiede al server il nome dell'attributo di un
 * oggetto "documento" il cui valore è la data di caricamento di
 * quel documento.
 * Se la richiesta va a buon fine, viene restituita una Promise
 * risolta con valore il nome dell'attributo restituito dal server.*/
const getNomePropertyDataCaricamentoDocumenti = async () => {

  return richiestaGet(process.env.VUE_APP_URL_GET_NOME_PROP_DATA_CARICAMENTO_IN_DOCUMENTI)
      .then(  risposta       => risposta )
      .catch( rispostaErrore => {
        console.error("Errore durante la ricezione del nome dell'attributo " +
            "contenente la data di caricamento dei documenti: " + rispostaErrore );
        return Promise.reject(rispostaErrore);
        // TODO : gestire l'errore (invio mail ai gestori?)
        // TODO : cercare tutti i catch nel progetto e fare un gestore di eccezioni unico
      });

}

</script>

<style scoped>
  h4 {
    padding: 0 1rem;
  }
  .filtro-hashtag>p {
    font-weight: bolder;
  }
  .filtro-hashtag * {
    margin-top: 0;
    margin-bottom: 0;
    padding-top: 0;
    padding-bottom: 0;
  }
  .form-lista-documenti {
    padding: 0;
    overflow: auto;
  }
  table {
    max-width: 95%;
    display: block;
    max-height: 500px;
    overflow: auto;
    margin: 1rem auto;
  }
  .link-download {
    /* Fonte icona: https://icons.getbootstrap.com/icons/cloud-download/ */
    content: url('data:image/svg+xml; utf8, <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="%23007bff" class="bi bi-cloud-download" viewBox="0 0 16 16"><path d="M4.406 1.342A5.53 5.53 0 0 1 8 0c2.69 0 4.923 2 5.166 4.579C14.758 4.804 16 6.137 16 7.773 16 9.569 14.502 11 12.687 11H10a.5.5 0 0 1 0-1h2.688C13.979 10 15 8.988 15 7.773c0-1.216-1.02-2.228-2.313-2.228h-.5v-.5C12.188 2.825 10.328 1 8 1a4.53 4.53 0 0 0-2.941 1.1c-.757.652-1.153 1.438-1.153 2.055v.448l-.445.049C2.064 4.805 1 5.952 1 7.318 1 8.785 2.23 10 3.781 10H6a.5.5 0 0 1 0 1H3.781C1.708 11 0 9.366 0 7.318c0-1.763 1.266-3.223 2.942-3.593.143-.863.698-1.723 1.464-2.383z"/><path d="M7.646 15.854a.5.5 0 0 0 .708 0l3-3a.5.5 0 0 0-.708-.708L8.5 14.293V5.5a.5.5 0 0 0-1 0v8.793l-2.146-2.147a.5.5 0 0 0-.708.708l3 3z"/></svg>');
  }
  .link-delete {
    /* Fonte icona: https://icons.getbootstrap.com/icons/trash/ */
    content: url('data:image/svg+xml; utf8, <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="%23007bff" class="bi bi-trash" viewBox="0 0 16 16"><path d="M5.5 5.5A.5.5 0 0 1 6 6v6a.5.5 0 0 1-1 0V6a.5.5 0 0 1 .5-.5zm2.5 0a.5.5 0 0 1 .5.5v6a.5.5 0 0 1-1 0V6a.5.5 0 0 1 .5-.5zm3 .5a.5.5 0 0 0-1 0v6a.5.5 0 0 0 1 0V6z"/><path fill-rule="evenodd" d="M14.5 3a1 1 0 0 1-1 1H13v9a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V4h-.5a1 1 0 0 1-1-1V2a1 1 0 0 1 1-1H6a1 1 0 0 1 1-1h2a1 1 0 0 1 1 1h3.5a1 1 0 0 1 1 1v1zM4.118 4L4 4.059V13a1 1 0 0 0 1 1h6a1 1 0 0 0 1-1V4.059L11.882 4H4.118zM2.5 3V2h11v1h-11z"/></svg>');
  }
</style>