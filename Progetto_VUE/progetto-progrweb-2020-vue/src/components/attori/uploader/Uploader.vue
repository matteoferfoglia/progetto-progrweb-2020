<template v-if="layoutCaricato">

  <main>

    <Form @csrf-token-ricevuto="setCsrfToken( $event )"
          @submit.prevent="aggiungiConsumer">

      <p v-if="mappa_idConsumer_proprietaConsumer.size > 0">Lista dei <i>Consumer</i></p>
      <ol>
        <li id="{{ consumer[0] }}"
            v-for="consumer in Array.from(mappa_idConsumer_proprietaConsumer.entries())"
            :key="consumer[0]/*Id del consumer*/">
          <SchedaUnConsumer :consumer="consumer"
                            :NOME_PROP_NOME_CONSUMER    ="NOME_PROP_NOME_CONSUMER"
                            :NOME_PROP_USERNAME_CONSUMER="NOME_PROP_USERNAME_CONSUMER"
                            :NOME_PROP_EMAIL_CONSUMER   ="NOME_PROP_EMAIL_CONSUMER"    />
          <input type="button" value="Elimina" @click.stop="eliminaConsumer(consumer[0])">
        </li>
      </ol>

      <p>
        Aggiungi un <i>Consumer</i>
        <input type="text" v-model="usernameNuovoConsumer"
               :pattern="REGEX_CODICE_FISCALE"
               placeholder="Username"
               maxlength="100"
               autocomplete="on"
               required />
        <input type="text"
               v-model="nominativoNuovoConsumer"
               placeholder="Nominativo"
               maxlength="100"
               autocomplete="on"
               required/>
        <input type="email"
               v-model="emailNuovoConsumer"
               name="{{ NOME_PROP_EMAIL_CONSUMER }}"
               placeholder="xxxxxx@example.com"
               maxlength="100"
               :pattern="REGEX_EMAIL"
               autocomplete="on"
               required/>

        <input type="submit" value="Aggiungi"/>
      </p>
    </Form>

  </main>
</template>

<script>
import {richiestaDelete, richiestaGet, richiestaPost} from "../../../utils/http";
import Form from "../../FormConCsrfToken";
import SchedaUnConsumer from "./SchedaUnConsumer";
import {unisciOggetti} from "../../../utils/utilitaGenerale";

export default {
  name: "Uploader",
  components: {Form, SchedaUnConsumer},
  emits: ['csrf-token-modificato'], // Fonte: https://stackoverflow.com/a/64220977
  data() {
    return {

      /** CSRF token.*/
      csrfToken: undefined,

      /** RegEx codice fiscale.*/
      REGEX_CODICE_FISCALE: process.env.VUE_APP_REGEX_CODICE_FISCALE,

      /** RegEx email.*/
      REGEX_EMAIL: process.env.VUE_APP_REGEX_EMAIL,

      /** Flag, true quando il layout è caricato.*/
      layoutCaricato: false,

      /** Array con i suggerimenti degli username nel form di
       * aggiunta nuovo Consumer.*/
      suggerimentiAutocompletamentoUsernameConsumerDaAggiungere: [],

      /** Flag per mostrare le opzioni di autocompletamento
       * username del Consumer da aggiungere.*/
      mostraOpzioniAutocompletamento: false,

      // Parametri in v-model col form per aggiunta nuovo Consumer
      usernameNuovoConsumer: "",
      nominativoNuovoConsumer: "",
      emailNuovoConsumer: "",

      /** Mappa { idConsumer => oggettoConProprietaConsumer },
       * ordinata alfabeticamente rispetto al nome del Consumer.*/
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
      NOME_PROP_USERNAME_CONSUMER: undefined

    }
  },
  created() {

    const caricaQuestoComponente = async () => {

            // richiede il nome della prop contenente il nome di un consumer nell'oggetto che sarà restituito dal server con le info di un Consumer
      await richiestaGet( process.env.VUE_APP_URL_GET_NOME_PROP_NOME_CONSUMER )
            .then( nomePropNomeConsumer => this.NOME_PROP_NOME_CONSUMER = nomePropNomeConsumer )

            // richiede il nome della prop contenente l'email di un consumer nell'oggetto che sarà restituito dal server con le info di un Consumer
            .then( () => richiestaGet( process.env.VUE_APP_URL_GET_NOME_PROP_EMAIL_CONSUMER ) )
            .then( nomePropEmailConsumer => this.NOME_PROP_EMAIL_CONSUMER = nomePropEmailConsumer )

            // richiede il nome della prop contenente lo username di un consumer nell'oggetto che sarà restituito dal server con le info di un Consumer
            .then( () => richiestaGet( process.env.VUE_APP_URL_GET_NOME_PROP_USERNAME_CONSUMER ) )
            .then( nomePropUsernameConsumer => this.NOME_PROP_USERNAME_CONSUMER = nomePropUsernameConsumer )

            // Richiede l'elenco dei consumer associati con questo uploader
            .then( getElencoConsumer )

            // Richiede le info di ogni Consumer e restituisce mappa { idConsumer => {proprietaQuestoConsumer} }
            .then( arrayIdConsumer => getMappa_idConsumer_proprietaConsumer( arrayIdConsumer ) )

            // Ordina la mappa dei Consumer (con relative proprietà) alfabeticamente e la salva nelle proprietà di questo componente
            .then( mappa_idConsumer_proprietaConsumer =>
                this.mappa_idConsumer_proprietaConsumer =
                    new Map( [...mappa_idConsumer_proprietaConsumer.entries()]
                            .sort((a,b) =>
                                a[1][this.NOME_PROP_NOME_UPLOADER] - b[1][this.NOME_PROP_NOME_UPLOADER] ) ) )

            .catch( console.error ) ;

    }

    caricaQuestoComponente().then( () => this.layoutCaricato = true )
                            .catch( console.error );  // TODO : aggiungere gestione dell'errore in tutti i componenti che usano questo "pattern" di caricamento contenuti

  },
  methods: {

    /** Informa il componente padre del cambiamento del token CSRF,
     * oltre ad aggiornarlo in questo componente.*/
    setCsrfToken( nuovoCsrfToken ) {
      this.csrfToken = nuovoCsrfToken;
      this.$emit('csrf-token-modificato', nuovoCsrfToken);
    },

    /** Funzione per l'eliminazione del Consumer con
     * identificativo specificato nel parametro.*/
    eliminaConsumer( idConsumer ) {

      const parametriRichiestaDelete = {[process.env.VUE_APP_CSRF_INPUT_FIELD_NAME]: this.csrfToken};

      richiestaDelete( process.env.VUE_APP_URL_DELETE_CONSUMER_PER_QUESTO_UPLOADER + "/" + idConsumer, parametriRichiestaDelete )
        .then( () => {
            alert("Consumer \"" + this.mappa_idConsumer_proprietaConsumer.get(idConsumer)[this.NOME_PROP_NOME_CONSUMER] + "\" eliminato." );
            this.mappa_idConsumer_proprietaConsumer.delete(idConsumer);
          })
        .catch( rispostaErrore => {
          console.error("Errore durante l'eliminazione del consumer: " + rispostaErrore );
        });
    },

    /** Funzione per aggiungere un Consumer. I dati del consumer da
     * aggiungere sono presi dalle variabili di questo componente.*/
    aggiungiConsumer() {

      if( this.usernameNuovoConsumer   &&
          this.nominativoNuovoConsumer &&
          this.emailNuovoConsumer         ) {

        const proprietaConsumerDaAggiungere = {
          [this.NOME_PROP_USERNAME_CONSUMER]: this.usernameNuovoConsumer,
          [this.NOME_PROP_NOME_CONSUMER]: this.nominativoNuovoConsumer,
          [this.NOME_PROP_EMAIL_CONSUMER]: this.emailNuovoConsumer
        }

        // Richiesta di aggiunta consumer
        richiestaPost(process.env.VUE_APP_URL_AGGIUNGI_CONSUMER_PER_QUESTO_UPLOADER,
            unisciOggetti(proprietaConsumerDaAggiungere, {[process.env.VUE_APP_CSRF_INPUT_FIELD_NAME]: this.csrfToken}))
            .then( idConsumerAppenaCreato => {

              // Aggiungi le info del consumer alla mappa in questo componente
              this.mappa_idConsumer_proprietaConsumer.set(idConsumerAppenaCreato, proprietaConsumerDaAggiungere);

              // Procedura completata
              alert("Aggiunto " + this.mappa_idConsumer_proprietaConsumer.get(idConsumerAppenaCreato)[this.NOME_PROP_NOME_CONSUMER] + "!");

            })
            .catch(errore => {
              alert("Errore: impossibile aggiungere il Consumer " + this.usernameNuovoConsumer +
                  " perché " + errore.response.data.charAt(0).toLowerCase() + errore.response.data.substring(1));
              console.error("Errore durante la procedura di aggiunta nuovo Consumer.");
              console.error(errore);
            })
            .finally(() => {
              // Pulisci campi del form input
              this.usernameNuovoConsumer = "";
              this.nominativoNuovoConsumer = "";
              this.emailNuovoConsumer = "";
            });

      } else {
        alert("Riempire correttamente i campi di input.");
      }

    },

  }
}

/** Richiede al server l'elenco di tutti i Consumer associati
 * con questo Uploader. Se la richiesta va a buon fine, questa
 * funzione restituisce una promise risolta il cui valore è
 * l'array con i valori richiesti.
 */
const getElencoConsumer = async () => {

  // TODO : verificare correttezza

    return richiestaGet( process.env.VUE_APP_URL_GET_ELENCO_CONSUMER_PER_QUESTO_UPLOADER )
        .then( rispostaConListaConsumer => rispostaConListaConsumer )
        .catch( rispostaErrore => {
          console.error("Errore durante il caricamento dei Consumer: " + rispostaErrore );
          return Promise.reject(rispostaErrore);
        });

}

/** Richiede le informazioni relative al consumer il cui identificativo
 * è passato come parametro. Se la richiesta va a buon fine, viene
 * restituita una Promise risolta contenente un array di due elementi,
 * in cui il primo contiene l'identificativo del Consumer ed il secondo
 * contiene un oggetto in cui ogni property è una proprietà del Consumer.*/
const getInfoConsumer = idConsumer =>
    richiestaGet( process.env.VUE_APP_URL_GET_INFO_CONSUMER + "/" + idConsumer )
        .then( rispostaConProprietaConsumer => [ idConsumer, rispostaConProprietaConsumer ] ) ;


/** Dato l'array avente per elementi gli identificativi dei Consumer,
 * richiede al server una mappa avente per chiavi gli identificativi dei
 * Consumer e per valori l'oggetto con le proprietà del Consumer
 * indicato dalla chiave.
 */
const getMappa_idConsumer_proprietaConsumer = async arrayIdConsumer => {

  // TODO : verificare correttezza

  // Richiede al server info su ogni Consumer nell'array
  //  (una Promise per ogni Consumer).  // TODO : si può evitare duplicazione di codice ? Stesso pattern usato anche in Consumer
  return Promise.all( arrayIdConsumer.map( idConsumer => getInfoConsumer( idConsumer ) )) // TODO : verificare che cosa restituisce getInfoConsumer
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