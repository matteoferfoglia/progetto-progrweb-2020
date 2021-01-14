<template v-if="layoutCaricato">
  <header>
    <h1>Benvenuto Uploader {{ nomeAttoreAttualmenteAutenticato }} <!-- TODO : spostarlo in un componente padre, perché questa proprietà ci sarà anche negli altri Attori --> </h1>
  </header>
  <main class="listaConsumer">
    <p>Lista dei <i>Consumer</i></p>
    <Form @csrf-token-ricevuto="csrfToken = $event">
      <ol  v-for="consumer in Array.from(mappa_idConsumer_proprietaConsumer.entries())" :key="consumer[0]/*Id del consumer*/" >
        <li id="{{ consumer[0] }}">
          <ol>
            <li>{{ NOME_PROP_NOME_CONSUMER }}: <span class="nome">{{ consumer[1][NOME_PROP_NOME_CONSUMER] }}</span></li>
            <li>{{ NOME_PROP_EMAIL_CONSUMER }}: <span class="nome">{{ consumer[1][NOME_PROP_EMAIL_CONSUMER] }}</span></li>
            <li>{{ NOME_PROP_USERNAME_CONSUMER }}: <span class="nome">{{ consumer[1][NOME_PROP_USERNAME_CONSUMER] }}</span></li>
          </ol>
          <input type="button" value="Elimina" @click="eliminaConsumer(consumer[0])">
        </li>
      </ol>

      <p>
        Aggiungi un Consumer    <!-- TODO : parte di aggiunta nuovo consumer da implementare !!! Si potrebbe fare un menu a tendina cosicché i campi possano essere scelti tra quelli disponibili richiesti al server-->
        <input type="text" v-model="nominativoNuovoConsumer" name="{{ NOME_PROP_NOME_CONSUMER }}" placeholder="Nominativo" />
        <input type="email" v-model="emailNuovoConsumer" name="{{ NOME_PROP_EMAIL_CONSUMER }}" placeholder="Email" />
        <input type="text" v-model="usernameNuovoConsumer" name="{{ NOME_PROP_USERNAME_CONSUMER }}" placeholder="Username" />

        <!-- TODO : questo input dovrebbe in qualche modo restituire l'identificativo del consumer da aggiungere, così da passarlo al metodo di aggiunta
                altrimenti passare al metodo i parametri e poi che se la veda il server -->

        <input type="button" value="Aggiungi" @click="aggiungiConsumer( /* TODO  */ )" /><!-- TODO !!! -->
      </p>
    </Form>
  </main>
</template>

<script>
// TODO : componente Consumer interamente da implementare
import {richiestaDelete, richiestaGet, richiestaPost} from "../../utils/http";
import Form from "../../components/FormConCsrfToken";

export default {
  name: "Uploader",
  components: {Form},
  data() {
    return {

      /** Flag, true quando il layout è caricato.*/
      layoutCaricato: false,

      /** Nome dell'attore attualmente autenticato.*/
      nomeAttoreAttualmenteAutenticato: undefined,

      // Parametri in v-model col form per aggiunta nuovo Consumer
      nominativoNuovoConsumer: "",
      emailNuovoConsumer: "",
      usernameNuovoConsumer: "",

      /** Mappa { idConsumer => arrayConIdFilesPerConsumerDaQuestoUploader }.*/
      mappa_idConsumer_arrayIdFiles: new Map(),

      /** Mappa { idConsumer => oggettoConProprietaConsumer },
       * ordinata alfabeticamente rispetto al nome del Consumer.*/  // TODO : verificare ordinamento corretto
      mappa_idConsumer_proprietaConsumer: new Map(),

      /** Nell'oggetto restituito dal server contenente le informazioni di un
       * Consumer, il nome del Consumer è salvato nella proprietà con il nome
       * indicato in questa variabile.
       * Vedere valori di {@link #mappa_idConsumer_proprietaConsumer}*/
      NOME_PROP_NOME_CONSUMER: undefined,

      /** Nell'oggetto restituito dal server contenente le informazioni di un
       * Consumer, l'email del Consumer è salvata nella proprietà con il nome
       * indicato in questa variabile.
       * Vedere valori di {@link #mappa_idConsumer_proprietaConsumer}*/
      NOME_PROP_EMAIL_CONSUMER: undefined,

      /** Nell'oggetto restituito dal server contenente le informazioni di un
       * Consumer, lo username del Consumer è salvato nella proprietà con il nome
       * indicato in questa variabile.
       * Vedere valori di {@link #mappa_idConsumer_proprietaConsumer}*/
      NOME_PROP_USERNAME_CONSUMER: undefined,

      /** CSRF token per l'eliminazione di un Consumer.*/
      csrfToken: undefined

    }
  },
  created() {

    const caricaQuestoComponente = async () => {

      await getNomeUploaderAttualmenteAutenticato()
            .then( nomeUploader => this.nomeAttoreAttualmenteAutenticato = nomeUploader )

      await getNomePropNomeConsumer()           // richiede il nome della prop contenente il nome di un consumer nell'oggetto che sarà restituito dal server con le info di un Consumer
            .then( nomePropNomeConsumer => this.NOME_PROP_NOME_CONSUMER = nomePropNomeConsumer )

            .then( getNomePropEmailConsumer )   // richiede il nome della prop contenente l'email di un consumer nell'oggetto che sarà restituito dal server con le info di un Consumer
            .then( nomePropEmailConsumer => this.NOME_PROP_EMAIL_CONSUMER = nomePropEmailConsumer )

            .then( getNomePropUsernameConsumer )   // richiede il nome della prop contenente lo username di un consumer nell'oggetto che sarà restituito dal server con le info di un Consumer
            .then( nomePropUsernameConsumer => this.NOME_PROP_USERNAME_CONSUMER = nomePropUsernameConsumer )

            // Richiede la mappa { consumer => [filesPerQuestoConsumer] }
            .then( getMappa_idConsumer_arrayIdFiles )

            // Salva mappa { consumer => [filesPerQuestoConsumer] } e restituisce array con identificativi dei consumer
            .then( mappa_idConsumer_arrayIdFiles => {
              this.mappa_idConsumer_arrayIdFiles = mappa_idConsumer_arrayIdFiles
              return Array.from(mappa_idConsumer_arrayIdFiles.keys());
            })

            // Richiede le info di ogni Consumer e restituisce mappa { idConsumer => {proprietaQuestoConsumer} }
            .then( arrayIdConsumer => getMappa_idConsumer_proprietaConsumer( arrayIdConsumer ) )

            // Ordina la mappa dei Consumer (con relative proprietà) alfabeticamente e la salva nelle proprietà di questo componente
            .then( mappa_idConsumer_proprietaConsumer =>
                this.mappa_idConsumer_proprietaConsumer = new Map( [...mappa_idConsumer_proprietaConsumer.entries()]
                                                                      .sort((a,b) =>
                                                                          a[1][this.NOME_PROP_NOME_UPLOADER] - b[1][this.NOME_PROP_NOME_UPLOADER] ) ) );// TODO : verificare )

    }

    caricaQuestoComponente().then( () => this.layoutCaricato = true )
                            .catch( console.error );  // TODO : aggiungere gestione dell'errore in tutti i componenti che usano questo "pattern" di caricamento contenuti

  },
  methods: {

    /** Metodo per l'eliminazione del Consumer con
     * identificativo specificato nel parametro.*/
    eliminaConsumer( idConsumer ) {

      const parametriRichiestaDelete = {[process.env.VUE_APP_CSRF_INPUT_FIELD_NAME]: this.csrfToken};

      richiestaDelete( process.env.VUE_APP_DELETE_CONSUMER_PER_QUESTO_UPLOADER + "/" + idConsumer, parametriRichiestaDelete )
        .then( () => {
            alert("Consumer \"" + this.mappa_idConsumer_proprietaConsumer.values()[this.NOME_PROP_USERNAME_CONSUMER] + "\" eliminato." ); // TODO : fare qualcosa di più carino di un alert
            this.mappa_idConsumer_proprietaConsumer.delete(idConsumer);
            this.mappa_idConsumer_arrayIdFiles.delete(idConsumer);
          })
        .catch( rispostaErrore => {
          console.error("Errore durante l'eliminazione del consumer: " + rispostaErrore );
          // TODO : gestire l'errore (invio mail ai gestori?)
          // TODO : cercare tutti i catch nel progetto e fare un gestore di eccezioni unico
        });
    },

    /** Metodo per aggiungere un Consumer, dato il suo identificativo,
     * a quelli gestiti da questo Uploader.*/
    aggiungiConsumer( idConsumerDaAggiungere ) {

      const parametriRichiesta = {[process.env.VUE_APP_CSRF_INPUT_FIELD_NAME]: this.csrfToken};

      // Richiesta di aggiunta consumer
      richiestaPost( process.env.VUE_APP_ADD_CONSUMER_PER_QUESTO_UPLOADER + "/" + idConsumerDaAggiungere, parametriRichiesta )

          // TODO : il server potrebbe restituire le proprietà del consumer, evitando un ulteriore richiesta
          // TODO   oppure se l'idConsumer è stato ottenuto da un menu a tendina a completamento (quindi già
          // TODO   si conoscono le proprietà del consumer) si potrebbe passare a questo metodo l'intero consumer
          // TODO   anziché solo l'id   [ bisogna aggiungere le sue info alla mappa dei consumer in questo componente ]

          // Richiesta delle informazioni del consumer
          .then( () => getInfoConsumer(idConsumerDaAggiungere) )

          // Aggiungi le info del consumer alla mappa in questo componente
          .then( entry_idConsumer_oggettoConPropConsumer =>
              this.mappa_idConsumer_proprietaConsumer.set(entry_idConsumer_oggettoConPropConsumer[0], entry_idConsumer_oggettoConPropConsumer[1]) )

          // Aggiungi il consumer alla mappa dei file (per ora vuota, visto che il Consumer è appena stato creato)
          .then( () => this.mappa_idConsumer_arrayIdFiles.set(idConsumerDaAggiungere, [] ) )  // valore aggiunto è un array vuoto (non ci sono ancora file per questo Consumer)

          // Procedura completata
          .then( () => alert("Aggiunto " + this.mappa_idConsumer_proprietaConsumer.get(idConsumerDaAggiungere)[this.NOME_PROP_NOME_CONSUMER] + "!" ) )

          .catch( errore => console.error( "Errore durante la procedura di aggiunta nuovo Consumer: " + errore ) );

    }

  }
}

/** Restituisce il nome dell'uploader attualmente autenticato.*/
const getNomeUploaderAttualmenteAutenticato = async () => {
  // TODO aggiungerlo al token o a qualcosa che sia visibile dal client senza sprecare una richiesta al server (tra l'altro, se non c'è il nome nel token, il server dovrà cercarlo nel db e gli accessi costano).
  return richiestaGet(process.env.VUE_APP_GET_NOME_QUESTO_ATTORE_AUTENTICATO)
      .then(  risposta       =>  risposta.data )
      .catch( rispostaErrore => {
        console.error("Errore durante il caricamento delle informazioni: " + rispostaErrore );
        return Promise.reject(rispostaErrore);
        // TODO : gestire l'errore (invio mail ai gestori?)
        // TODO : cercare tutti i catch nel progetto e fare un gestore di eccezioni unico
      });
}


/** Richiede al server il nome della property contenente il nome di un
 * consumer nell'oggetto con le proprietà di un consumer quando viene
 * restituito dal server. Vedere {@link getMappa_idConsumer_proprietaConsumer}.
 */
const getNomePropNomeConsumer = async () => {

  // TODO : verificare correttezza

  return richiestaGet( process.env.VUE_APP_GET_NOME_PROP_NOME_CONSUMER )
      .then( risposta => risposta.data )
      .catch( rispostaErrore => {
        console.error("Errore durante il caricamento: " + rispostaErrore );
        return Promise.reject(rispostaErrore);
        // TODO : gestire l'errore (invio mail ai gestori?)
        // TODO : cercare tutti i catch nel progetto e fare un gestore di eccezioni unico
      });

}

/** Richiede al server il nome della property contenente l'email di un
 * consumer nell'oggetto con le proprietà di un consumer quando viene
 * restituito dal server. Vedere {@link getMappa_idConsumer_proprietaConsumer}.
 */
const getNomePropEmailConsumer = async () => {

  // TODO : verificare correttezza
  // TODO : refactor ? Per richiedere il nome della prop con il nome del consumer è uguale...
  return richiestaGet( process.env.VUE_APP_GET_NOME_PROP_EMAIL_CONSUMER )
      .then( risposta => risposta.data )
      .catch( rispostaErrore => {
        console.error("Errore durante il caricamento: " + rispostaErrore );
        return Promise.reject(rispostaErrore);
        // TODO : gestire l'errore (invio mail ai gestori?)
        // TODO : cercare tutti i catch nel progetto e fare un gestore di eccezioni unico
      });

}

/** Richiede al server il nome della property contenente lo username di un
 * consumer nell'oggetto con le proprietà di un consumer quando viene
 * restituito dal server. Vedere {@link getMappa_idConsumer_proprietaConsumer}.
 */
const getNomePropUsernameConsumer = async () => {

  // TODO : verificare correttezza
  // TODO : refactor ? Per richiedere il nome della prop con il nome del consumer è uguale...
  return richiestaGet( process.env.VUE_APP_GET_NOME_PROP_USERNAME_CONSUMER )
      .then( risposta => risposta.data )
      .catch( rispostaErrore => {
        console.error("Errore durante il caricamento: " + rispostaErrore );
        return Promise.reject(rispostaErrore);
        // TODO : gestire l'errore (invio mail ai gestori?)
        // TODO : cercare tutti i catch nel progetto e fare un gestore di eccezioni unico
      });

}


/** Richiede al server una mappa avente per chiavi gli identificativi dei
 * Consumer serviti da questo Uploader e per valori l'array contenente
 * gli identificativi dei file caricati da questo Uploader e destinati al
 * Consumer avente per identificativo il valore della chiave corrispondente.
 */
const getMappa_idConsumer_arrayIdFiles = async () => {

  // TODO : verificare correttezza

    return richiestaGet( process.env.VUE_APP_GET_ID_UPLOADER_FILE_PER_QUESTO_CONSUMER )
        .then( rispostaConMappa_idConsumer_arrayIdFiles => new Map(Object.entries(rispostaConMappa_idConsumer_arrayIdFiles.data)) )
        .catch( rispostaErrore => {
          console.error("Errore durante il caricamento della mappa Consumer-Files: " + rispostaErrore );
          return Promise.reject(rispostaErrore);
          // TODO : gestire l'errore (invio mail ai gestori?)
          // TODO : cercare tutti i catch nel progetto e fare un gestore di eccezioni unico
        });

}

/** Richiede le informazioni relative al consumer il cui identificativo
 * è passato come parametro. Se la richiesta va a buon fine, viene
 * restituito un array di due elementi, in cui il primo contiene
 * l'identificativo del Consumer ed il secondo contiene un oggetto
 * in cui ogni property è una proprietà del Consumer.*/
const getInfoConsumer = idConsumer =>
    richiestaGet( process.env.VUE_APP_GET_INFO_CONSUMER + "/" + idConsumer )
        .then( rispostaConProprietaConsumer => [ idConsumer, rispostaConProprietaConsumer.data ] ) ;


/** Dato l'array avente per elementi gli identificativi dei Consumer,
 * richiede al server una mappa avente per chiavi gli identificativi dei
 * Consumer e per valori l'oggetto con le proprietà del Consumer
 * indicato dalla chiave.
 */
const getMappa_idConsumer_proprietaConsumer = async arrayIdConsumer => {

  // TODO : verificare correttezza

  // Richiede al server info su ogni Consumer nell'array
  //  (una Promise per ogni Consumer).  // TODO : si può evitare duplicazione di codice ? Stesso pattern usato anche in Consumer
  return Promise.all( arrayIdConsumer.map( idConsumer => getInfoConsumer( idConsumer ) ))
                .then( arrayConEntriesDaTutteLePromise => new Map(arrayConEntriesDaTutteLePromise) ) // then() aspetta tutte le promise prima di eseguire
                .catch( rispostaErrore => {
                  console.error("Errore durante il caricamento delle informazioni sui Consumer: " + rispostaErrore );
                  return Promise.reject(rispostaErrore);
                  // TODO : gestire l'errore (invio mail ai gestori?)
                  // TODO : cercare tutti i catch nel progetto e fare un gestore di eccezioni unico
                });

}

</script>

<style scoped>

</style>