<template>

  <!-- Componente per mostrare una lista di documenti in forma tabellare -->

  <Loader :isComponenteCaricato="isComponenteCaricato">

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

      <div class="mx-auto table-container">
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
              :class="{ 'font-weight-bold' : documento[NOME_PROP_DATA_VISUALIZZAZIONE_DOCUMENTO]==='' }"
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
      </div>


    </FormConCsrfToken>

    <p v-else>Nessun documento disponibile.</p>

  </Loader>

</template>

<script>

/** Questo componente Vue implementa i requisiti descritti nella sezione
 * <cite>Lista Documenti - Consumers</cite>.
 * Nota: questo componente fa spesso uso del tipo Map
 * (<a href="https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Map">Fonte</a>),
 * in quanto ne ha semplificato l'implementazione.*/


import {camelCaseToHumanReadable, capitalize} from "../../utils/utilitaGenerale";
import {HTTP_STATUS_NOT_MODIFIED, richiestaDelete, richiestaGet} from "../../utils/http";
import {
  aggiungiDocumentoAdIndiceHashtag,
  creaIndiceDeiFileRispettoAgliHashtagCheContengono,
  getNomePropertyDataVisualizzazioneDocumenti,
  MappaDocumenti,
  ordinaMappaSuDataCaricamentoConNonVisualizzatiDavanti
} from "../../utils/documenti";
import FormConCsrfToken from "../layout/FormConCsrfToken";
import Loader from "../layout/Loader";
export default {
  name: "TabellaDocumenti",
  components: {Loader, FormConCsrfToken},
  emits: [
    /**Evento emesso dopo aver aggiunto un nuovo documento, quando
     * richiesto dall'elemento padre.*/
    'nuovo-documento-mostrato',

    /** Evento emesso quando riceve un token CSRF da un componente figlio.*/
    'csrf-token-ricevuto'

  ],
  inheritAttrs: false,
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

      /** Flag: diventa true dopo aver caricato il componente.*/
      isComponenteCaricato: false,

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

    const richiestaInfo = async () => {
          // Richiede il nome della property di un documento contenente la data di visualizzazione del documento stesso
      await getNomePropertyDataVisualizzazioneDocumenti()
          .then(nomeProp => this.NOME_PROP_DATA_VISUALIZZAZIONE_DOCUMENTO = nomeProp)

          // Richiede il nome della property di un documento contenente la data di caricamento del documento stesso
          .then( getNomePropertyDataCaricamentoDocumenti )
          .then(nomeProp => this.NOME_PROP_DATA_CARICAMENTO_DOCUMENTO = nomeProp);
    }

    const caricamentoComponente = async () => {

        // Richiede mappa idFile-propFile per questo attore
      await richiestaGet(this.urlRichiestaElencoDocumentiPerUnAttore)

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
        })

        // Richiede il nome della property di un documento contenente il nome del documento stesso
        .then( () => richiestaGet(process.env.VUE_APP_URL_GET_NOME_PROP_NOME_DOCUMENTO) )
        .then(nomeProp => this.NOME_PROP_NOME_DOCUMENTO = nomeProp)

        // Richiede l'elenco dei nomi delle properties dei documenti
        .then( () => richiestaGet(process.env.VUE_APP_URL_GET_NOMI_TUTTE_LE_PROP_DOCUMENTI) )
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


    richiestaInfo()
      .then(caricamentoComponente)
      .then( () => {
        // All'inizio mostra tutti gli hashtag
        this.listaHashtagDaMostrare = Array.from( this.mappa_hashtag_idDocumenti.keys() );
        this.listaHashtagDaMostrare.forEach(unHashtagDaMostrare => this.mostraDocumentiConHashtagFiltrato(unHashtagDaMostrare, true) );
        this.isComponenteCaricato = true;
      })
      .catch( errore => {
        console.error( "Errore durante il caricamento del componente " + this.$options.name + ": " + errore );
        alert( "Errore durante il caricamento dei documenti." );
      })

    // Gestione dell'auto-aggiornamento della tabella  // TODO: sarebbe meglio che l'autoaggiornamento richiedesse solo i nuovi documenti e non tutti di nuovo
    this.timerAutoUpdate = setInterval(async () => {
      const listaHashtagMostratiPreAggiornamento = this.listaHashtagDaMostrare;

      // Richiesta al server se l'elenco dei documenti è stato modificato
      richiestaGet(this.urlRichiestaElencoDocumentiPerUnAttore,
                         {[process.env.VUE_APP_URL_QUERYPARAM_NUM_ELEMENTI_NOTI_AL_CLIENT]: this.mappaDocumenti.size()})
        .then( caricamentoComponente )  // Se risposta è NOT_MODIFIED, allora non esegue then e va direttamente a catch
        .then( () => {
          // Nei nuovi documenti caricati, mostra solo gli hashtag filtrati
          this.listaHashtagDaMostrare = listaHashtagMostratiPreAggiornamento;
          this.listaHashtagDaMostrare.forEach(unHashtagDaMostrare => this.mostraDocumentiConHashtagFiltrato(unHashtagDaMostrare, true));
          // Nascondi i documenti con hashtag da non mostrare
          const listaHashtagNonMostrati = Array.from(this.mappa_hashtag_idDocumenti.keys())
              .filter(unHashtag => !this.listaHashtagDaMostrare.includes(unHashtag));
          listaHashtagNonMostrati.forEach(unHashtagDaNonMostrare => this.mostraDocumentiConHashtagFiltrato(unHashtagDaNonMostrare, false));

        })
        .catch( errore => {
          if(errore.status!==HTTP_STATUS_NOT_MODIFIED) {
            console.error( errore );
            alert("Errore nel caricamento dei documenti: " + errore);
          }
        })

    }, process.env.VUE_APP_MILLISECONDI_AUTOAGGIORNAMENTO);

  },
  beforeUnmount () {
    clearInterval(this.timerAutoUpdate);
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

                    this.mappaDocumentiDaMostrare.set(
                        ordinaMappaSuDataCaricamentoConNonVisualizzatiDavanti( this.mappaDocumentiDaMostrare.get(),   // TODO : rivedere questo metodo
                            this.NOME_PROP_DATA_VISUALIZZAZIONE_DOCUMENTO,
                            this.NOME_PROP_DATA_CARICAMENTO_DOCUMENTO )
                    );
                    this.mappaDocumenti.set(
                        ordinaMappaSuDataCaricamentoConNonVisualizzatiDavanti( this.mappaDocumenti.get(),   // TODO : rivedere questo metodo
                            this.NOME_PROP_DATA_VISUALIZZAZIONE_DOCUMENTO,
                            this.NOME_PROP_DATA_CARICAMENTO_DOCUMENTO )
                    );

                  })
                  .catch(console.error);

            }
          }

        })
        .catch( console.error );


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
                      hour: '2-digit', minute: '2-digit', second: '2-digit'
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

    /** Cambia stato se è stato caricato (dall'Uploader) un nuovo documento, quindi
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
    width: 100%;
    overflow: auto;
  }
  table {
    display: table;
    margin: 1rem auto;
  }
  .table-container {
    width: 95%;
    display: block; /*Necessario per max-height*/
    max-height: 500px;
    overflow: auto;
  }
  th, td {
    text-align: center;
  }
</style>