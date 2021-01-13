<template>
  <main v-if="layoutCaricato">

    <!-- Nel caso in cui il Consumer abbia ricevuto documenti da più Uploaders,
          mostra la lista degli Uploaders che gli hanno inviato documenti (logo +
          descrizione); cliccando su uno di essi, appare la lista dei documenti
          caricati da questi. -->
    <div class="schermataSceltaUploader" v-if="mappa_uploaders.size > 1">
      <ol  v-for="uploader in Array.from(mappa_uploaders.entries())" :key="uploader[0]/*Id dell'uploader*/" >
        <li>
          <router-link :to="{ path: process.env.VUE_APP_ROUTER_PATH_LISTA_DOCUMENTI,
                              params: {
                                [process.env.VUE_APP_ROUTER_PARAMETRO_ID_UPLOADER_DI_CUI_MOSTRARE_DOCUMENTI_PER_CONSUMER]: uploader[0],                           // id uploader
                                [process.env.VUE_APP_ROUTER_PARAMETRO_LOGO_UPLOADER_DI_CUI_MOSTRARE_DOCUMENTI_PER_CONSUMER]: uploader[1][NOME_PROP_LOGO_UPLOADER] // logo uploader
                              }
                            }">
            <!-- Link alla lista documenti-->
            <img src="{{ uploader[1][NOME_PROP_LOGO_UPLOADER] }}" alt="">
            <!-- TODO : verificare che le immagini vengano codificate correttamente -->
            <!-- TODO : verificare che tutte le immagine siano caricate prima di mostrare https://vuejsexamples.com/a-vue-js-2-0-directive-to-detect-images-loading/ -->
            {{ uploader[1][NOME_PROP_NOME_UPLOADER] }} <!-- Nome uplooader -->
          </router-link>
        </li>
      </ol>
    </div>

    <!-- Nel caso in cui il Consumer abbia ricevuto documenti da un solo Uploader,
          mostra direttamente la lista dei documenti caricati da questi. -->
    <!-- TODO : verificare che questo div si carichi solo quando c'è esattamente un consumer (cioè controlla che il v-else-if faccia il suo lavoro) -->
    <div v-else-if="mappa_uploaders>0" @load="$router.push({
                                                                path: process.env.VUE_APP_ROUTER_PATH_LISTA_DOCUMENTI,
                                                                params: { [process.env.VUE_APP_ROUTER_PARAMETRO_ID_UPLOADER_DI_CUI_MOSTRARE_DOCUMENTI_PER_CONSUMER]: mappa_uploaders.keys().next().value }
                                                            })" />

  </main>
</template>

<script>
import {richiestaGet} from "../../utils/http";

// TODO : questa implementazione fa una richiesta al server per il nome dell'uploader
// TODO   dato l'id, poi ne fa un'altra per chiederne il logo, ma queste info devono
// TODO   essere richieste al database e gli accessi al data base costano: possibile
// TODO   soluzione da implementare: richiedere al server tutte le info (che restituisca
// TODO   al client una mappa del tipo {idUploader => { nomeUploader: "Pippo", logoUploader: **logo codificato in base64** } }

export default {
  name: "Consumer",
  data() {
    return {

      /** Flag, true quando il layout del componente è stato caricato.*/
      layoutCaricato: false,

      /** Mappa con chiave l'id di un Uploader e valore
       * la lista degli identificativi dei file che ha
       * caricato per questo Consumer.*/
      mappa_uploader_documenti: new Map(),

      /** Mappa con chiave l'id di un Uploader e valore
       * l'oggetto con le sue proprietà.*/
      mappa_uploaders: new Map(),

      /** Nome della proprietà contenente il nome dell'uploader
       * all'interno dell'oggetto nei valori della {@link #mappa_uploaders}.*/
      NOME_PROP_NOME_UPLOADER: undefined,

      /** Nome della proprietà contenente il logo dell'uploader
       * all'interno dell'oggetto nei valori della {@link #mappa_uploaders}.*/
      NOME_PROP_LOGO_UPLOADER: undefined

    }
  },
  created() {

    const caricaQuestoComponente = async () => {

      await getNomePropNomeUploader() // Richiede nomi delle properties negli oggetti ricevuti dal server
            .then( nomePropNomeUploader => this.NOME_PROP_NOME_UPLOADER = nomePropNomeUploader )
            .then( getNomePropLogoUploader )
            .then( nomePropLogoUploader => this.NOME_PROP_LOGO_UPLOADER = nomePropLogoUploader )

            // Richiede identificativi uploader e corrispondenti identificativi dei file
            .then( getIdentificativi_uploader_file )
            // e li salva nell prop di questo componente, poi restituisce la mappa ricevuta
            .then( mappa_idUploader_arrayIdFile => {
              this.mappa_uploader_documenti = mappa_idUploader_arrayIdFile;
              return mappa_idUploader_arrayIdFile;
            })

            // Recupera info di tutti gli uploader e restituisce mappa { idUploader -> { proprietà di questo uploader } }
            .then( mappa_idUploader_arrayIdDocumenti =>
                getInfoUploaders( Array.from(mappa_idUploader_arrayIdDocumenti.keys()) ) )

            // Ordina la mappa alfabeticamente rispetto al nome dell'uploader e salvala nelle prop di questo componente
            .then( mappa_idUploader_oggettoConPropUploader =>
                  this.mappa_uploaders = new Map( [...mappa_idUploader_oggettoConPropUploader.entries()]
                                                .sort((a,b) =>
                                                    a[1][this.NOME_PROP_NOME_UPLOADER] - b[1][this.NOME_PROP_NOME_UPLOADER] ) ) );// TODO : verificare

    }

    caricaQuestoComponente().then( () => this.layoutCaricato = true );

  }
}

/** Richiede al server una mappa avente per chiave l'identificativo
 * di un Uploader e come valore corrispondente la lista degli
 * identificativi dei file prodotti da quell'Uploader e destinati
 * al Consumer autenticato da cui è originata questa richiesta.
 * Se la richiesta va a buon fine, tale mappa viene restituita come
 * valore di una promise risolta.*/
const getIdentificativi_uploader_file = async () => {
  return richiestaGet(process.env.VUE_APP_GET_ID_UPLOADER_FILE_PER_QUESTO_CONSUMER)
      .then(  risposta       =>  new Map(Object.entries(risposta.data)) )
      .catch( rispostaErrore => {
        console.error("Errore durante il caricamento delle informazioni: " + rispostaErrore );
        return Promise.reject(rispostaErrore);
        // TODO : gestire l'errore (invio mail ai gestori?)
        // TODO : cercare tutti i catch nel progetto e fare un gestore di eccezioni unico
      });
}

/** Richiede al server le informazioni sugli Uploader i cui identificativi
 * sono passati in un array come parametro a questa funzione: viene costruita
 * una mappa avente per chiave l'identificativo dell'uploader e come valore
 * corrispondente un oggetto con la proprietà contenente il nome dell'Uploader.
 * Se la richiesta va a buon fine, tale mappa viene restituita come
 * valore di una promise risolta.
 * @param arrayIdUploader Array con gli identificativi degli Uploaders di
 *                        cui si vogliono ottenere le informazioni.
 */
const getInfoUploaders = async arrayIdUploader => {

  // TODO : verificare correttezza

  // Richiede al server info su ogni Uploader nell'array
  //  (una Promise per ogni Uploader).
  return Promise.all( arrayIdUploader.map( idUploader => {   // Fonte: https://stackoverflow.com/a/31414472

    return richiestaGet( process.env.VUE_APP_GET_INFO_UPLOADER + "/" + idUploader ) // richiesta info uploader
              .then( rispostaConNomeUploader => [ idUploader, rispostaConNomeUploader.data ] );

  }))
  .then( arrayConEntriesDaTutteLePromise => new Map(arrayConEntriesDaTutteLePromise) ) // then() aspetta tutte le promise prima di eseguire
  .catch( rispostaErrore => {
    console.error("Errore durante il caricamento delle informazioni sugli Uploader: " + rispostaErrore );
    return Promise.reject(rispostaErrore);
    // TODO : gestire l'errore (invio mail ai gestori?)
    // TODO : cercare tutti i catch nel progetto e fare un gestore di eccezioni unico
  });

}

/** Richiede al server il nome della proprietà contenente il nome di un
 * uploader nell'oggetto nel valore di un'entry della mappa restituita da
 * {@link getInfoUploaders}.
 */
const getNomePropNomeUploader = async () => {

  // TODO : verificare correttezza

  return richiestaGet(process.env.VUE_APP_GET_NOME_PROP_NOME_UPLOADER)
      .then(  risposta       => risposta.data )
      .catch( rispostaErrore => {
        console.error("Errore durante il caricamento delle informazioni: " + rispostaErrore );
        return Promise.reject(rispostaErrore);
        // TODO : gestire l'errore (invio mail ai gestori?)
        // TODO : cercare tutti i catch nel progetto e fare un gestore di eccezioni unico
      });

}

/** Richiede al server il nome della proprietà contenente l'immagine logo di un
 * uploader nell'oggetto nel valore di un'entry della mappa restituita da
 * {@link getInfoUploaders}.
 */
const getNomePropLogoUploader = async () => {

  // TODO : verificare correttezza

  return richiestaGet(process.env.VUE_APP_GET_NOME_PROP_LOGO_UPLOADER)
      .then(  risposta       => risposta.data )
      .catch( rispostaErrore => {
        console.error("Errore durante il caricamento delle informazioni: " + rispostaErrore );
        return Promise.reject(rispostaErrore);
        // TODO : gestire l'errore (invio mail ai gestori?)
        // TODO : cercare tutti i catch nel progetto e fare un gestore di eccezioni unico
      });

}

</script>

<style scoped>

</style>