<template v-if="layoutCaricato">

  <!-- Componente per mostrare un elenco di attori -->

    <ol>
      <li v-for="attore in Array.from(mappa_idAttore_proprietaAttore.entries())"
          :key="attore[0]/*Id dell'attore*/">
        <router-link :to="{
          name: NOME_ROUTE_SCHEDA_ATTORE,
          params: {
            [NOME_PARAM_ID_ATTORE_router]       : attore[0],
            [NOME_PARAM_PROPRIETA_ATTORE_router]: JSON.stringify(attore[1])
              // JSON.stringify risolve il problema del passaggio di oggetti come props in Vue-Router
          }
        }">
          {{ attore[1][NOME_PROP_NOME_ATTORE] }}
        </router-link>
      </li>
    </ol>

  <router-view :csrfToken="csrfToken_wrapper"
               :tipoAttoreAttualmenteAutenticato="tipoAttoreAutenticato"
               :tipoAttoreCuiQuestaSchedaSiRiferisce="tipiAttoreCuiQuestoElencoSiRiferisce"
               :mostrareInputFilePerModificaLogoUploader="mostrareInputFilePerModificaLogoUploader()"
               :NOME_PROP_NOMINATIVO ="NOME_PROP_NOME_ATTORE"
               :NOME_PROP_USERNAME   ="NOME_PROP_USERNAME_ATTORE"
               :NOME_PROP_EMAIL      ="NOME_PROP_EMAIL_ATTORE"
               @csrf-token-ricevuto="$emit('csrf-token-ricevuto',$event)"/>

</template>

<script>
import {richiestaGet} from "../../utils/http";

export default {
  name: "ElencoAttori",
  emits: [
      /** Evento emesso quando riceve un token CSRF da un componente figlio.*/
      'csrf-token-ricevuto'
  ],
  props: [
    /** Tipo attore attualmente autenticato.*/
    "tipoAttoreAutenticato",

    /** Tipi di attore cui questo elenco si riferisce (Administrator/Consumer/Uploader).*/
    "tipiAttoreCuiQuestoElencoSiRiferisce",

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

      /** Nell'oggetto restituito dal server contenente le informazioni di un
       * Attore, il nome dell'Attore è salvato nella proprietà con il nome
       * indicato in questa variabile.
       * Vedere valori di {@link #mappa_idAttore_proprietaAttore}*/
      NOME_PROP_NOME_ATTORE: undefined,

      /** Nell'oggetto restituito dal server contenente le informazioni di un
       * Attore, l'email dell'Attore è salvata nella proprietà con il nome
       * indicato in questa variabile.
       * Vedere valori di {@link #mappa_idAttore_proprietaAttore}*/
      NOME_PROP_EMAIL_ATTORE: undefined,

      /** Nell'oggetto restituito dal server contenente le informazioni di un
       * Attore, lo username dell'Attore è salvato nella proprietà con il nome
       * indicato in questa variabile.
       * Vedere valori di {@link #mappa_idAttore_proprietaAttore}*/
      NOME_PROP_USERNAME_ATTORE: undefined,

      // Parametri Vue-Router
      /** Nome della route che conduce alla scheda di un attore.*/
      NOME_ROUTE_SCHEDA_ATTORE:           process.env.VUE_APP_ROUTER_NOME_SCHEDA_UN_ATTORE,
      NOME_PARAM_ID_ATTORE_router:        process.env.VUE_APP_ROUTER_PARAMETRO_ID_ATTORE,
      NOME_PARAM_PROPRIETA_ATTORE_router: process.env.VUE_APP_ROUTER_PARAMETRO_PROPRIETA_ATTORE,

      // Wrapper
      csrfToken_wrapper: this.csrfToken,

    }
  },
  created() {

    const caricaQuestoComponente = async () => {

      // Il server fornirà una mappa { idAttore => {oggetto con le prop dell'attore idAttore} }

          // richiede il nome della prop contenente il nominativo di un attore nell'oggetto
          // che sarà restituito dal server con le info dell'attore
      await richiestaGet( process.env.VUE_APP_URL_GET_NOME_PROP_NOME_ATTORE )
          .then( nomePropNomeAttore => this.NOME_PROP_NOME_ATTORE = nomePropNomeAttore )

          // richiede il nome della prop contenente l'email
          .then( () => richiestaGet( process.env.VUE_APP_URL_GET_NOME_PROP_EMAIL_ATTORE ) )
          .then( nomePropEmailAttore => this.NOME_PROP_EMAIL_ATTORE = nomePropEmailAttore )

          // richiede il nome della prop contenente lo username
          .then( () => richiestaGet( process.env.VUE_APP_URL_GET_NOME_PROP_USERNAME_ATTORE ) )
          .then( nomePropUsernameAttore => this.NOME_PROP_USERNAME_ATTORE = nomePropUsernameAttore )

          // Richiede l'elenco degli Attori associati con questo attualmente autenticato
          .then( () => getElencoAttori( this.tipoAttoreAutenticato ) )

          // Richiede le info di ogni Attore nell'elenco restituito dalla Promise precedente
          // poi richiede la mappa { idAttore => {proprietaQuestoAttore} }
          .then( arrayIdAttore => getMappa_idAttore_proprietaAttore( arrayIdAttore, this.tipoAttoreAutenticato ) )

          // Ordina la mappa degli Attori (con relative proprietà) alfabeticamente e la salva nelle proprietà di questo componente
          .then( mappa_idAttore_proprietaAttore =>
              this.mappa_idAttore_proprietaAttore =
                  new Map( [...mappa_idAttore_proprietaAttore.entries()]
                          .sort((a,b) =>
                              a[1][this.NOME_PROP_NOME_ATTORE] - b[1][this.NOME_PROP_NOME_ATTORE] ) ) )

          .catch( console.error ) ;

    }

    caricaQuestoComponente().then( () => this.layoutCaricato = true )
                            .catch( console.error );  // TODO : aggiungere gestione dell'errore in tutti i componenti che usano questo "pattern" di caricamento contenuti

  },
  methods: {

    /** Restituisce true se è possibile modificare il logo di un uploader.*/
    mostrareInputFilePerModificaLogoUploader() {
      return this.isAdministratorAttualmenteAutenticato() &&
          this.tipiAttoreCuiQuestoElencoSiRiferisce ===
            process.env.VUE_APP_TIPO_UTENTE_AUTENTICATO_UPLOADER;
    },

    /** Restituisce true se l'attore attualmente autenticato è un Administrator.*/
    isAdministratorAttualmenteAutenticato() {
      return this.tipoAttoreAutenticato ===
          process.env.VUE_APP_TIPO_UTENTE_AUTENTICATO_ADMINISTRATOR;
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

/** Richiede al server l'elenco di tutti gli Attore associati
 * con questo Uploader. Se la richiesta va a buon fine, questa
 * funzione restituisce una promise risolta il cui valore è
 * l'array con i valori richiesti.
 */
const getElencoAttori = async tipoAttoreAttualmenteAutenticato => {

  const urlRichiesta = tipoAttoreAttualmenteAutenticato === process.env.VUE_APP_TIPO_UTENTE_AUTENTICATO_ADMINISTRATOR ?
                        process.env.VUE_APP_URL_GET_ELENCO_UPLOADER_PER_QUESTO_ADMINISTRATOR :  // TODO : amministratore deve poter cercare sia Uploader sia Administrator
                        ( tipoAttoreAttualmenteAutenticato === process.env.VUE_APP_TIPO_UTENTE_AUTENTICATO_UPLOADER ?
                           process.env.VUE_APP_URL_GET_ELENCO_CONSUMER_PER_QUESTO_UPLOADER :
                                ( tipoAttoreAttualmenteAutenticato === process.env.VUE_APP_TIPO_UTENTE_AUTENTICATO_CONSUMER ?
                                   process.env.VUE_APP_ELENCO_UPLOADER_PER_QUESTO_CONSUMER : "" )
                        );


  return richiestaGet( urlRichiesta )
      .catch( rispostaErrore => {
        console.error("Errore durante il caricamento della lista di attori: " + rispostaErrore );
        return Promise.reject(rispostaErrore);
      });

}

/** Richiede le informazioni relative all'attore il cui identificativo
 * è passato come parametro. Se la richiesta va a buon fine, viene
 * restituita una Promise risolta contenente un array di due elementi,
 * in cui il primo contiene l'identificativo dell'Attore ed il secondo
 * contiene un oggetto in cui ogni property è una proprietà dell'Attore.*/
const getInfoAttore = async (idAttore, tipoAttoreAttualmenteAutenticato) => {// TODO : rivedere a cosa serve (probabilmente sbagliato, l'url??)

  let urlRichiesta = tipoAttoreAttualmenteAutenticato === process.env.VUE_APP_TIPO_UTENTE_AUTENTICATO_ADMINISTRATOR ?
                      process.env.VUE_APP_URL_GET_INFO_UPLOADER :
                      (tipoAttoreAttualmenteAutenticato === process.env.VUE_APP_TIPO_UTENTE_AUTENTICATO_UPLOADER ?
                              process.env.VUE_APP_URL_GET_INFO_CONSUMER :
                              (tipoAttoreAttualmenteAutenticato === process.env.VUE_APP_TIPO_UTENTE_AUTENTICATO_CONSUMER ?
                                  process.env.VUE_APP_URL_GET_INFO_UPLOADER : "")
                      );

  urlRichiesta += "/" + idAttore;

  return await richiestaGet( urlRichiesta )
                .then(rispostaConProprietaAttore => [idAttore, rispostaConProprietaAttore]);  // restituisce l'entry: [ idAttore, {propQuestoAttore} ]
}


/** Dato l'array avente per elementi gli identificativi degli Attori,
 * richiede al server una mappa avente per chiavi gli identificativi degli
 * Attori e per valori l'oggetto con le proprietà dell'Attore
 * indicato dalla chiave.
 */
const getMappa_idAttore_proprietaAttore = async (arrayIdAttore, tipoAttoreAttualmenteAutenticato) => {

  // TODO : verificare correttezza

  // Richiede al server info su ogni Attore nell'array

    // TODO : si può evitare duplicazione di codice ? Stesso pattern usato anche in Consumer
  return Promise.all( arrayIdAttore.map( idAttore => getInfoAttore( idAttore, tipoAttoreAttualmenteAutenticato ) )) //  una Promise per ogni Attore, quindi Promise.all per poi aspettarle tutte (Fonte: https://stackoverflow.com/a/31414472)
                .then( arrayConEntriesDaTutteLePromise => new Map(arrayConEntriesDaTutteLePromise) ) // then() aspetta tutte le promise prima di eseguire
                .catch( rispostaErrore =>
                    console.error("Errore durante il caricamento delle informazioni sugli Attori: " +
                                   rispostaErrore ) );

}

</script>

<style scoped>
</style>