<template v-if="isLayoutCaricato">

  <!-- Componente per mostrare la lista di documenti di un Consumer,
       visualizzata da un Uploader -->

  <section>
    <p>Consumer: {{ nomeConsumer }}</p>
    <TabellaDocumenti :nomiColonneIntestazione  ="nomiPropDocumenti"
                      :elencoDocumentiDaMostrare="mappaDocumentiPerUnConsumer"
                      :nomePropLinkDownload     ="NOME_PROP_DOWNLOAD_DOCUMENTO"
                      :nomePropLinkElimina      ="NOME_PROP_DELETE_DOCUMENTO"  />
  </section>

  <form @submit.prevent="caricaNuovoDocumento()" >  <!-- TODO : aggiungere csrf token -->
    <!-- Fonte (Upload documento): https://stackoverflow.com/a/43014086 -->
    <p>Carica un nuovo documento:</p>
    <label>Nome documento   <input type="text" v-model="nomeDocumento" required></label>
    <label>Lista di hashtag <input type="text" v-model="listaHashtag" placeholder="hashtag1, hashtag2" required></label>
    <label>Documento        <input type="file"></label><!-- TODO : caricare documento-->
    <input type="submit" value="Carica">
  </form>



</template>

<script>
import TabellaDocumenti from "./TabellaDocumenti";
import {richiestaGet, richiestaPostConFile} from "../../utils/http";
import {ordinaMappaSuDataCaricamentoConNonVisualizzatiDavanti} from "../../utils/documenti";

export default {
  name: "ListaDocumentiVistaDaUploader",
  components: {TabellaDocumenti},
  data() {
    return {

      /** Flag, true quando questo componente è caricato.*/
      isLayoutCaricato: false,

      /** Nome del consumer a cui i documenti si riferiscono.*/
      nomeConsumer: this.$route.params[process.env.VUE_APP_ROUTER_PARAMETRO_NOME_CONSUMER_DI_CUI_MOSTRARE_DOCUMENTI_PER_UPLOADER],

      /** Identificativo del consumer a cui i documenti si riferiscono.*/
      idConsumer: this.$route.params[process.env.VUE_APP_ROUTER_PARAMETRO_ID_CONSUMER_DI_CUI_MOSTRARE_DOCUMENTI_PER_UPLOADER],

      /** Nome delle colonne di intestazione della tabella coi documenti.*/
      nomiPropDocumenti: [],

      /** Nome della colonna con l'url di download del documento.*/
      NOME_PROP_DOWNLOAD_DOCUMENTO: "Link download",

      /** Nome della colonna con l'url di eliminazione del documento.*/
      NOME_PROP_DELETE_DOCUMENTO: "Link eliminazione",

      /** Mappa documenti ( id => proprietà ) per uno specifico Consumer.
       * Questa mappa è caricata dinamicamente in base alle azioni
       * dell'utente.*/
      mappaDocumentiPerUnConsumer: new Map(),



      // NOMI DEI PARAMETRI ATTESI DAL SERVER
      nomeParametro_nomeDocumento:             "nomeFile",
      nomeParametro_contenutoDocumento:        "contenutoFile",
      nomeParametro_idConsumerDestinatario:    "usernameConsumerDestinatario",
      nomeParametro_listaHashtag:              "listaHashtag",


      // PROPRIETA CARICAMENTO NUOVO DOCUMENTO:
      nomeDocumento: "",
      listaHashtag: ""

    }
  },
  created() {

    const caricaQuestoComponente = async () => {

      // Richiede mappa idFile-propFile per questo consumer
      await getMappa_idFile_arrayIdFiles_perConsumer( this.idConsumer )
            .then( ordinaMappaSuDataCaricamentoConNonVisualizzatiDavanti )

            // In ogni documento, aggiungi link di cancellazione e di download, poi restituisci la mappa
            .then( mappa =>
                new Map(
                    Array.from( mappa.entries() )
                        .map( unDocumento => {

                          // Decomposizione di ogni entry
                          const idDocumento   = unDocumento[0];
                          const propDocumento = unDocumento[1];

                          propDocumento[this.NOME_PROP_DELETE_DOCUMENTO]   = process.env.VUE_APP_DELETE_DOCUMENTO_DI_QUESTO_UPLOADER + "/" + idDocumento;
                          propDocumento[this.NOME_PROP_DOWNLOAD_DOCUMENTO] = process.env.VUE_APP_DOWNLOAD_DOCUMENTO_DI_QUESTO_UPLOADER + "/" + idDocumento;

                        })
                )
            )

            // Salva la mappa
            .then( mappa => this.mappaDocumentiPerUnConsumer = mappa );


      // Richiede l'elenco dei nomi delle properties dei documenti per i Consumer
      await getNomiPropDocumenti()
          // Aggiunge alle properties dei documenti le colonne con il link di download ed eliminazione
          // documento (da creare dinamicamente), poi salva l'array
          .then( nomiPropDocumenti => {
            nomiPropDocumenti.push( this.NOME_PROP_DOWNLOAD_DOCUMENTO );
            nomiPropDocumenti.push( this.NOME_PROP_DELETE_DOCUMENTO )
            this.nomiPropDocumenti = nomiPropDocumenti;
          })

    };

    caricaQuestoComponente()
        .then( () => this.isLayoutCaricato = true )
        .catch( console.error );

  },
  methods: {

    /** Funzione per inviare al server il nuovo documento
     * oltre che i valori dei campi di input presi dal form.*/
    caricaNuovoDocumento() {

      // Costruzione dei parametri da inviare
      const formData = new FormData();

      formData.append( this.nomeParametro_contenutoDocumento,
                       document.querySelector('input[type=file]').files[0] );
      formData.append( this.nomeParametro_nomeDocumento, this.nomeDocumento );
      formData.append( this.nomeParametro_idConsumerDestinatario, this.idConsumer );
      formData.append( this.nomeParametro_listaHashtag, this.listaHashtag.trim().toLowerCase() );

      // Controllo validità campi del form (false se qualche valore è falsy)
      const formValido = Array.from(formData.values())
                              .map(val => val ? 1 : 0)
                              .reduce((a, b) => a * b) === 1;

      if( formValido )
        richiestaPostConFile( process.env.VUE_APP_POST_CARICA_DOCUMENTO_DI_QUESTO_UPLOADER, formData)
          .then( () => alert( "Documento caricato" ) )
          .catch( console.error );
      else
        console.error( "Campi del form non validi." );

    }

  }
}

/** Richiede al server la lista dei nomi delle proprietà
 * dei documenti inviati da questo Uploader ai Consumer e,
 * se la richiesta va a buon fine, li restituisce in un array
 * come valore di una Promise risolta.*/
const getNomiPropDocumenti = async () => {

  return richiestaGet( process.env.VUE_APP_GET_NOMI_TUTTE_LE_PROP_DOCUMENTI )
      .then( risposta => risposta )
      .catch( rispostaErrore => {
        console.error("Errore: " + rispostaErrore );
        return Promise.reject(rispostaErrore);
      });

}


/** Richiede al server una mappa avente per chiavi gli identificativi dei
 * File serviti da questo Uploader ad un certo Consumer (indicato come
 * parametro) e per valori l'oggetto corrispondente al file indicato dalla
 * chiave dell'entry nella mappa.
 * Se tutto va a buon fine, restituisce tale mappa in una Promise risolta.
 * @param usernameConsumerDestinazione  Lo username del consumer a cui i
 *                                      documenti sono destinati.
 */
const getMappa_idFile_arrayIdFiles_perConsumer = async idConsumer => {

  // TODO : verificare correttezza

  return richiestaGet( process.env.VUE_APP_GET_MAPPA_FILE_DI_UPLOADER_PER_CONSUMER + "/" + idConsumer )

      // Crea mappa
      .then( rispostaConMappaFile_id_prop => new Map(Object.entries(rispostaConMappaFile_id_prop)) )

      // TODO : molto simile al metodo in Lista Documenti Per Consumer

      // Ordina e restituisci la mappa in base alla data di caricamento (prima i documenti più recenti)
      .then( elencoDocumenti => ordinaMappaSuDataCaricamentoConNonVisualizzatiDavanti(elencoDocumenti) )

      .catch( rispostaErrore => {
        console.error("Errore durante il caricamento della mappa coi documenti: " + rispostaErrore );
        return Promise.reject(rispostaErrore);
      });

}


</script>

<style scoped>

</style>