<template v-if="layoutCaricato">

  <header>
    <img class="logoUploader"
         src="{{ this.$route.params[process.env.VUE_APP_ROUTER_PARAMETRO_LOGO_UPLOADER_DI_CUI_MOSTRARE_DOCUMENTI_PER_CONSUMER] }}"
         alt="Logo dell'Uploader">

    <!-- Mostra hashtag associati ai documenti mostrati e permette di filtrare -->
    <form v-if=" mappa_hashtag_idDocumenti.size > 0 /*non mostrare se non ci sono hashtag*/ ">
      <!-- Form per filtraggio documenti rispetto ad hashtag -->
      <p>Hashtags: </p>
      <ol>
        <li v-for="hashtag in Array.from(mappa_hashtag_idDocumenti.keys()).sort()"
            :key="hashtag">
          <p>
            <input type="checkbox"
                   @change="mostraDocumentiConHashtagFiltrato(hashtag, this.checked)/* Se cliccato, mostra documenti con questo hashtag */"
                   :checked="listaHashtagDaMostrare.includes(hashtag)"/>
            {{ hashtag }}
          </p>
        </li>
      </ol>
    </form>
  </header>

  <TabellaDocumenti :nomiColonneIntestazione  ="nomiColonneIntestazione"
                    :elencoDocumentiDaMostrare="elencoDocumentiDaMostrare"
                    :nomePropLinkDownload     ="NOME_PROP_URL_DOWNLOAD_IN_LISTA_DOCUMENTI"/>


</template>

<script>

/** Questo componente Vue implementa i requisiti descritti nella sezione
 * <cite>Lista Documenti - Consumers</cite>.
 * Nota: questo componente fa spesso uso del tipo Map
 * (<a href="https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Map">Fonte</a>),
 * in quanto ne ha semplificato l'implementazione.
 * Questo componente è deputato alla visualizzazione della Lista Documenti
 * caricati da un Uploader (il cui identificativo è un parametro per questo
 * componente, cercato nei parametri di this.$route), per il Consumer
 * attualmente autenticato.*/

import {richiestaGet} from "../../utils/http";
import {
  creaIndiceDeiFileRispettoAgliHashtagCheContengono,
  ordinaMappaSuDataCaricamentoConNonVisualizzatiDavanti
} from "../../utils/documenti";
import TabellaDocumenti from "./TabellaDocumenti";
import {camelCaseToHumanReadable} from "../../utils/utilitaGenerale";

export default {

  name: "ListaDocumenti",
  components: {TabellaDocumenti},
  data() {
    return {
      layoutCaricato: false,  // diventa true quando il layout è caricato
      nomiColonneIntestazione: [],

      /** Oggetto di tipo Map in cui ogni entry ha come chiave
       * l'identificativo del documento e come valore la sua
       * rappresentazione come oggetto (in base alle sue property).
       * Le entry sono ordinate in base alla data del documento:
       * dal più recente al meno recente.*/
      elencoDocumenti: new Map(),

      /** Come {@link #elencoDocumenti}, ma contiene solo i
       * documenti da mostrare, a seguito del filtraggio dell'utente.*/
      elencoDocumentiDaMostrare: new Map(),

      /** Nella rappresentazione di un documento, questo attributo
       * è il nome della property contenente la lista di hashtag.*/
      nomeProprietaHashtagInListaDocumenti: undefined, // nome da richiedere al server

      /** Nella rappresentazione di un documento, questo attributo
       * è il nome della property contenente l'url per il download
       * del documento.*/
      NOME_PROP_URL_DOWNLOAD_IN_LISTA_DOCUMENTI: "Link download",

      /** Lista di hashtag risultanti dal filtraggio: i documenti
       * mostrati all'utente contengono (per poter essere mostrati
       * da questo componente) almeno uno degli hashtag presenti in
       * questo attributo.*/
      listaHashtagDaMostrare: [],                     // modificato dinamicamente in base al filtraggio dell'utente

      /** Mappa in cui ogni chiave è un hashtag ed
       * il corrispettivo valore è un array contenente gli
       * identificativi dei file con quel particolare hashtag.
       * In pratica è un indice per i documenti mostrati,
       * indicizzati rispetto agli hashtag che contengono.
       * Vedere {@link #creaIndiceDeiFileRispettoAgliHashtagCheContengono}*/
      mappa_hashtag_idDocumenti: new Map(),

      /** Array in cui ogni elemento è l'identificativo di un
       * documento che non è ancora stato visualizzato.*/
      arrayIdDocumentiNonAncoraVisualizzati: []   // TODO : gestire l'aggiornamento di questo l'array quando l'utente clicca sul file per visualizzarlo
    }
  },

  methods: {

    /** Dati un hashtag ed un flag booleano, questa funzione aggiorna
     * l'array dei documenti da mostrare: se il flag è true, allora
     * saranno mostrati tutti i documenti che contengono l'hashtag passato
     * come parametro. Per poter essere mostrato, un documento deve
     * avere tra gli hashtag almeno uno tra quelli compresi nella lista
     * di hashtag da mostrare.*/
    mostraDocumentiConHashtagFiltrato(hashtag, daMostrare) {

      // TODO : da testare

      // Aggiorna lista di hashtag da mostrare
      if(daMostrare)
        this.listaHashtagDaMostrare.push(hashtag);
      else
        this.listaHashtagDaMostrare = this.listaHashtagDaMostrare
                                          .filter( hashtagDaMostrare => hashtagDaMostrare!==hashtag);

      const setIdDocumentiDaMostrare = new Set(                                    // utilizzo Set per evitare chiavi di documenti duplicate
          Array.from(this.mappa_hashtag_idDocumenti.entries())
               .filter( entry => this.listaHashtagDaMostrare.includes(entry[0]) )  // entry[0] è la chiave (l'hashtag)
               .flatMap( entry => entry[1] )                                // entry[1] è l'array con gli id dei file che contengono l'hashtag specificato in entry[0]
               // Fonte (flatMap): https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Array/flatMap
      );

      this.elencoDocumentiDaMostrare = this.elencoDocumenti
                                           .filter( entryDocumento => setIdDocumentiDaMostrare.includes(entryDocumento[0]) ); // entryDocumento[0] è l'id di un documento
                                            // Risultato filter: nella mappa restano solo i documenti che hanno id tra quelli filtrati prima

      // Osservazione: elencoDocumenti non viene sovrascritto, quindi l'ordine di visualizzazione è mantenuto
      // TODO : verificare che si mantenga l'ordine di visualizzazione dei documenti

    }
  },

  created() {

    // TODO : testare

    const caricaContenuto = async () => {   // Grazie ad async, restituisce una Promise (usata dopo)

      await getNomiProprietaDocumenti()
              .then( intestazione =>
                  this.nomiColonneIntestazione =
                      intestazione.forEach( nomeProp => camelCaseToHumanReadable( nomeProp ) )
                                  .push( this.NOME_PROP_URL_DOWNLOAD_IN_LISTA_DOCUMENTI) );

      await getElencoDocumentiPerQuestoConsumer( this.$route.params[process.env.VUE_APP_ROUTER_PARAMETRO_ID_UPLOADER_DI_CUI_MOSTRARE_DOCUMENTI_PER_CONSUMER] )
            // Promise restituisce mappa { idDocumento => documento }

              // Crea l'indice degli hashtag
              .then( elencoDocumenti => {
                getNomePropertyHashtagsDeiDocumenti()
                      .then( nomePropertyHashtags => {
                        this.mappa_hashtag_idDocumenti =
                            creaIndiceDeiFileRispettoAgliHashtagCheContengono(elencoDocumenti, nomePropertyHashtags);
                        this.nomeProprietaHashtagInListaDocumenti = nomePropertyHashtags;
                      });
                return elencoDocumenti;
              })

              // Ordina la mappa in base alla data di caricamento (prima i documenti più recenti)
              .then( elencoDocumenti => ordinaMappaSuDataCaricamentoConNonVisualizzatiDavanti(elencoDocumenti) )  // TODO verificare ordinamento corretto

              // Aggiunge la property "link per download documento"
              // e salva l'elenco dei documenti risultante (correttamente ordinati)
              .then( elencoDocumenti => {
                elencoDocumenti.forEach( (propsDocumento, idDocumento) =>
                    propsDocumento[this.NOME_PROP_URL_DOWNLOAD_IN_LISTA_DOCUMENTI] =
                        process.env.VUE_APP_URL_DOWNLOAD_DOCUMENTO_PER_CONSUMER + "/" + idDocumento );

                this.elencoDocumenti = elencoDocumenti;
                return elencoDocumenti;
              });

    }

    // created() è un processo sincrono, quindi non si può usare async:
    // risolto usando un flag (layoutCaricato) che cambia stato quando è tutto caricato

    caricaContenuto().then(   ()    => {
                        this.listaHashtagDaMostrare = Array.from( this.mappa_hashtag_idDocumenti.keys() );  // All'inizio mostra tutti gli hashtag
                        this.layoutCaricato = true
                      })
                     .catch( errore => {
                       // TODO : gestire questo errore
                       console.error("Errore nel caricamento del componente " + this.$options.name + ": " + errore);
                       alert("Errore durante il caricamento.")
                     });

  }

}

/** Richiede al server l'intestazione della tabella dei documenti
 * e la restituisce come valore di una promise.*/
const getNomiProprietaDocumenti = async () => {

  return richiestaGet(process.env.VUE_APP_GET_NOMI_PROP_DOCUMENTI_PER_CONSUMER)
    .then(  risposta       => risposta.data )
    .catch( rispostaErrore => {
      console.error("Errore durante il caricamento dell'intestazione della lista dei documenti: " + rispostaErrore );
      return Promise.reject(rispostaErrore);
      // TODO : gestire l'errore (invio mail ai gestori?)
      // TODO : cercare tutti i catch nel progetto e fare un gestore di eccezioni unico
    });

}

/** Richiede al server tutti i documenti destinati al Consumer
 * attualmente autenticato. Con <i>documento</i>, in questo
 * contesto, si intende la sua rappresentazione come oggetto,
 * senza il contenuto del vero documento (è un'astrazione).
 * Se la richiesta va a buon fine, questa funzione
 * restituisce una Promise risolta che ha per valore una mappa
 * in cui ogni entry è un documento ed ogni entry
 * ha come chiave l'identificativo del documento e come valore
 * il documento (rappresentato come oggetto).
 * @param idUploader L'identificativo dell'Uploader di cui
 *                    mostrare i documenti.*/
const getElencoDocumentiPerQuestoConsumer = async idUploader => {           // TODO : passare come parametro l'id dell'Uploader e richiedere documenti solo da quell'Uploader

  return richiestaGet(process.env.VUE_APP_GET_DOCUMENTI_PER_CONSUMER + "/" + idUploader)
        .then(  risposta       => {
          // Conversione da oggetto a mappa (Fonte: https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Object/entries#converting_an_object_to_a_map)
          return new Map(Object.entries(risposta.data));
        })
        .catch( rispostaErrore => {
          console.error("Errore durante il caricamento dell'intestazione della lista dei documenti: " + rispostaErrore );
          return Promise.reject(rispostaErrore);
          // TODO : gestire l'errore (invio mail ai gestori?)
          // TODO : cercare tutti i catch nel progetto e fare un gestore di eccezioni unico
        });

}

/** Con riferimento a {@link #getElencoDocumentiPerQuestoConsumer},
 * questa funzione richiede al server il nome dell'attributo di un
 * oggetto "documento" il cui valore è la lista degli hashtag di
 * quel documento.
 * L'oggetto "lista documenti" viene costruito dal server, quindi
 * deve essere il server a dire al client quale property contiene
 * la lista di documenti.
 * Se la richiesta va a buon fine, viene restituita una Promise
 * risolta con valore il nome dell'attributo restituito dal server.*/
const getNomePropertyHashtagsDeiDocumenti = async () => {

  return richiestaGet(process.env.VUE_APP_GET_DOCUMENTI_PER_CONSUMER_NOME_PROP_HAHSTAGS)
        .then(  risposta       => risposta.data )
        .catch( rispostaErrore => {
          console.error("Errore durante la ricezione del nome dell'attributo " +
              "contenente gli hashtag nei documenti: " + rispostaErrore );
          return Promise.reject(rispostaErrore);
          // TODO : gestire l'errore (invio mail ai gestori?)
          // TODO : cercare tutti i catch nel progetto e fare un gestore di eccezioni unico
        });

}
</script>

<style scoped>
</style>