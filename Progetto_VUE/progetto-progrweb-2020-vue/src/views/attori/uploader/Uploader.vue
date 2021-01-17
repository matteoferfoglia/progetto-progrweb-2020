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
                            :nomeUploader="nomeUploader"
                            :NOME_PROP_NOME_CONSUMER    ="NOME_PROP_NOME_CONSUMER"
                            :NOME_PROP_USERNAME_CONSUMER="NOME_PROP_USERNAME_CONSUMER"
                            :NOME_PROP_EMAIL_CONSUMER   ="NOME_PROP_EMAIL_CONSUMER"    />
          <input type="button" value="Elimina" @click.stop="eliminaConsumer(consumer[0])">
        </li>
      </ol>

      <p>
        Aggiungi un <i>Consumer</i>
        <input type="text" v-model="usernameNuovoConsumer"
               name="{{ NOME_PROP_USERNAME_CONSUMER }}"
               placeholder="Username"
               @click.stop=" mostraOpzioniAutocompletamento = true "
               required />
        <datalist @keyup.stop="autocompletamentoUsername(usernameNuovoConsumer)"> <!-- TODO : menu a tendina DA VERIFICARE IL FUNZIONAMENTO -->
          <option @input="usernameDaAggiungereScelto(usernameNuovoConsumer)"
                  v-for="usernameDaAggiungere in suggerimentiAutocompletamentoUsernameConsumerDaAggiungere"
                  :key="usernameDaAggiungere">
            {{ usernameDaAggiungere }}
          </option>
        </datalist>

        <input type="text" v-model="nominativoNuovoConsumer" name="{{ NOME_PROP_NOME_CONSUMER }}" placeholder="Nominativo" required/>
        <input type="email" v-model="emailNuovoConsumer" name="{{ NOME_PROP_EMAIL_CONSUMER }}" placeholder="Email" required/>

        <input type="submit" value="Aggiungi"/>
      </p>
    </Form>

  </main>
</template>

<script>
// TODO : componente Consumer interamente da implementare
import {richiestaDelete, richiestaGet, richiestaPost} from "../../../utils/http";
import Form from "../../../components/FormConCsrfToken";
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
      NOME_PROP_USERNAME_CONSUMER: undefined

    }
  },
  created() {

    const caricaQuestoComponente = async () => {

            // richiede il nome della prop contenente il nome di un consumer nell'oggetto che sarà restituito dal server con le info di un Consumer
      await getNomePropNomeConsumer()
            .then( nomePropNomeConsumer => this.NOME_PROP_NOME_CONSUMER = nomePropNomeConsumer )

            // richiede il nome della prop contenente l'email di un consumer nell'oggetto che sarà restituito dal server con le info di un Consumer
            .then( getNomePropEmailConsumer )
            .then( nomePropEmailConsumer => this.NOME_PROP_EMAIL_CONSUMER = nomePropEmailConsumer )

            // richiede il nome della prop contenente lo username di un consumer nell'oggetto che sarà restituito dal server con le info di un Consumer
            .then( getNomePropUsernameConsumer )
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
                                a[1][this.NOME_PROP_NOME_UPLOADER] - b[1][this.NOME_PROP_NOME_UPLOADER] ) ) ) // TODO : verificare

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

      richiestaDelete( process.env.VUE_APP_DELETE_CONSUMER_PER_QUESTO_UPLOADER + "/" + idConsumer, parametriRichiestaDelete )
        .then( () => {
            alert("Consumer \"" + this.mappa_idConsumer_proprietaConsumer.values()[this.NOME_PROP_USERNAME_CONSUMER] + "\" eliminato." ); // TODO : fare qualcosa di più carino di un alert
            this.mappa_idConsumer_proprietaConsumer.delete(idConsumer);
          })
        .catch( rispostaErrore => {
          console.error("Errore durante l'eliminazione del consumer: " + rispostaErrore );
          // TODO : gestire l'errore (invio mail ai gestori?)
          // TODO : cercare tutti i catch nel progetto e fare un gestore di eccezioni unico
        });
    },

    /** Funzione per aggiungere un Consumer. I dati del consumer da
     * aggiungere sono presi dalle variabili di questo componente.*/
    aggiungiConsumer() {

      const proprietaConsumerDaAggiungere = {
        [this.NOME_PROP_USERNAME_CONSUMER]:          this.usernameNuovoConsumer,
        [this.NOME_PROP_NOME_CONSUMER]:              this.nominativoNuovoConsumer,
        [this.NOME_PROP_EMAIL_CONSUMER]:             this.emailNuovoConsumer
      }

      // Richiesta di aggiunta consumer
      richiestaPost( process.env.VUE_APP_ADD_CONSUMER_PER_QUESTO_UPLOADER,
                     unisciOggetti(proprietaConsumerDaAggiungere, {[process.env.VUE_APP_CSRF_INPUT_FIELD_NAME]: this.csrfToken}) )
        .then( () => {

            // Aggiungi le info del consumer alla mappa in questo componente
            this.mappa_idConsumer_proprietaConsumer.set(this.usernameNuovoConsumer, proprietaConsumerDaAggiungere);

            // Procedura completata
            alert("Aggiunto " + this.mappa_idConsumer_proprietaConsumer.get(this.usernameNuovoConsumer)[this.NOME_PROP_NOME_CONSUMER] + "!" );

        })
        .catch( errore => {
          alert( "Errore: impossibile aggiungere il Consumer " + this.usernameNuovoConsumer + ", " + errore.data.toLowerCase() );
          console.error( "Errore durante la procedura di aggiunta nuovo Consumer.");
          console.error( errore );
        })
        .finally( () => {
          // Pulisci campi del form input
          this.usernameNuovoConsumer = "";
          this.nominativoNuovoConsumer = "";
          this.emailNuovoConsumer = "";
        });

    },

    /** Funzione di autocompletamento dell'identificativo di
     * un Consumer da aggiungere.
     * @param caratteriDigitati Caratteri digitati dall'utente.
     */
    autocompletamentoUsername( caratteriDigitati ) {

      // Chiedere al server la lista degli identificativi dei consumer il cui username inizia con i caratteri digitati
      richiestaGet(process.env.VUE_APP_GET_ID_CONSUMER_INIZIANTE_CON + "/" + caratteriDigitati)
        .then( risposta => {
          this.suggerimentiAutocompletamentoUsernameConsumerDaAggiungere = risposta.data;
        })
        .catch( console.error );

    },

    /** Funzione da eseguire quando l'utente clicca su una delle option
     * di autocompletamento proposte, nel form di aggiunta nuovo consumer.*/
    usernameDaAggiungereScelto( usernameSelezionato ) {

      // Autocompleta gli altri input del form
      getInfoConsumer( usernameSelezionato )
        .then( risposta => {
          const infoConsumer = risposta[1];
          this.usernameNuovoConsumer   = infoConsumer[this.NOME_PROP_USERNAME_CONSUMER];
          this.emailNuovoConsumer      = infoConsumer[this.NOME_PROP_EMAIL_CONSUMER];
          this.nominativoNuovoConsumer = infoConsumer[this.NOME_PROP_NOME_CONSUMER];

          this.mostraOpzioniAutocompletamento = false;  // TODO : verificare correttezza (che sparisca il datalist)
        })
        .catch(console.error);

    }

  }
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


/** Richiede al server l'elenco di tutti i Consumer associati
 * con questo Uploader. Se la richiesta va a buon fine, questa
 * funzione restituisce una promise risolta il cui valore è
 * l'array con i valori richiesti.
 */
const getElencoConsumer = async () => {

  // TODO : verificare correttezza

    return richiestaGet( process.env.VUE_APP_GET_ELENCO_CONSUMER_PER_QUESTO_UPLOADER )
        .then( rispostaConListaConsumer => rispostaConListaConsumer.data )  // TODO : verificare che arrivi correttamente un array
        .catch( rispostaErrore => {
          console.error("Errore durante il caricamento deiConsumer: " + rispostaErrore );
          return Promise.reject(rispostaErrore);
        });

}

/** Richiede le informazioni relative al consumer il cui identificativo
 * è passato come parametro. Se la richiesta va a buon fine, viene
 * restituita una Promise risolta contenente un array di due elementi,
 * in cui il primo contiene l'identificativo del Consumer ed il secondo
 * contiene un oggetto in cui ogni property è una proprietà del Consumer.*/
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