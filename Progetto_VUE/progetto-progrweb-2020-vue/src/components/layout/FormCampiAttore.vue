<template>

  <FormConCsrfToken @csrf-token-ricevuto="$emit('csrf-token-ricevuto', $event)"
                    @submit="$emit('submit')/*Deve essere propagato*/">
    <p>
      <label v-if="flag_mostrareLabelCampiInput"
             :for="idUsername">{{ usernameCodFisc }}</label>
      <input type="text"
             v-model="username_wrapper"
             maxlength="100"
             :pattern="usernameCodFisc==='Codice fiscale' ? REGEX_CODICE_FISCALE : '^.*$'"
             :id="idUsername"
             :placeholder="usernameCodFisc"
             :readonly="! username_readOnly"
             autocomplete="on">
    </p>
    <p>
      <label v-if="flag_mostrareLabelCampiInput"
             :for="idNominativo">Nominativo</label>
      <input type="text"
             v-model="nominativo_wrapper"
             :id="idNominativo"
             placeholder="Nominativo"
             maxlength="100"
             autocomplete="on">
    </p>
    <p>
      <label v-if="flag_mostrareLabelCampiInput"
             :for="idEmail">Email</label>
      <input type="email"
             v-model="email_wrapper"
             :id="idEmail"
             :pattern="REGEX_EMAIL"
             placeholder="xxxxxx@example.com"
             maxlength="100"
             autocomplete="email">
    </p>
    <slot>
      <!-- Qui possono essere aggiunti altri campi input / contenuti dal componente
           padre, in particolare bisognerebbe mettere <input type="submit"> -->
    </slot>
  </FormConCsrfToken>

</template>

<script>
import FormConCsrfToken from "./FormConCsrfToken";
import {generaIdUnivoco, unisciOggetti} from "../../utils/utilitaGenerale";
import {richiestaPost} from "../../utils/http";
export default {
  name: "FormCampiAttore",
  components: {FormConCsrfToken},
  emits: [

    /** Emesso quando, a causa di questo componente o di un suo
     * figlio, viene modificato il token CSRF.*/
    'csrf-token-ricevuto',

    /** Evento generato quando un form viene inviato al server.
     * Il valore associato a queto evento è la Promise restituita
     * da {@link richiestaPost} a seguito dell'invio.*/
    'dati-form-inviati',

    /** Evento generato cliccando submit.*/
    'submit'
  ],
  props: [

    /** Flag: true se questo form si riferisce ad un consumer.*/
    "isQuestoFormRiferitoAConsumer",

    /** True quando si devono inviare i dati del form all'url
     * specificato nell'apposita proprietà.*/
    "flag_inviaDatiForm",

    /** Flag: true se si vogliono mostrare le label vicino ai
     * campi di input.*/
    "flag_mostrareLabelCampiInput",

    /** Flag: true se il campo username <strong>non</strong>
     * può essere modificato. Se non specificato, si considera
     * che il campo può essere modficato.*/
    "username_readOnly",

    /** Proprietà che permette al componente padre
     * di specificare a quale url inviare i dati di
     * questo form tramite POST.*/
    "urlInvioFormTramitePost",

    /** Proprietà che permette al componente padre di aggiungere
     * dei dati a quelli che verranno inviati con il form. Questa
     * proprietà deve essere un oggetto.*/
    "datiAggiuntiviDaInviareAlServer",

    // Valori per il form (solo se specificati)
    /** Valore per il campo username.*/
    "username",
    /** Valore per il campo nominativo.*/
    "nominativo",
    /** Valore per il campo email.*/
    "email",

    /**Token CSRF da usare.*/
    "csrfToken"
  ],
  data() {
    return {

      REGEX_EMAIL          : process.env.VUE_APP_REGEX_EMAIL,
      REGEX_CODICE_FISCALE : process.env.VUE_APP_REGEX_CODICE_FISCALE,

      isQuestoFormRiferitoAConsumer_wrapper: this.isQuestoFormRiferitoAConsumer, // wrapper per la prop
      username_readOnly_wrapper: !! this.username_readOnly,  // wrapper per la prop (doppio '!' per considerare valori truthy/falsy)

      // Se è un consumer ha un codice fiscale, altrimenti ha uno username (gestito tramite watch)
      usernameCodFisc: undefined,

      // Dati v-model nel form: se definiti nelle prop usa quelli altrimenti stringa vuota
      username_wrapper  : this.username   ? this.username   : "",
      nominativo_wrapper: this.nominativo ? this.nominativo : "",
      email_wrapper     : this.email      ? this.email      : "",

      // Valori ID per gli elementi input (univoci nel progetto)
      idUsername: generaIdUnivoco(),
      idNominativo: generaIdUnivoco(),
      idEmail: generaIdUnivoco(),

      datiAggiuntiviDaInviareAlServer_wrapper: {}, // vedere descrizione della prop corrispondente a questo wrapper


      csrfToken_wrapper: undefined  // token CSRF

    }
  },
  watch: {

    /** Watcher per gestire l'invio dei dati dal form al server.
     * I dati vengono inviati quando il flag osservato diventa true.
     * Restituisce un oggetto con due property: "datiInviati" contiene
     * l'oggetto coi dati inviati, "promiseRispostaServer" contiene
     * la promise che si occupa dell'invio della richiesta al server
     * (da quest'ultima è possibile leggere la risposta del server).*/
    flag_inviaDatiForm: {
      immediate: true,
      deep: true,
      handler( flag_inviaDatiForm ) {

        if( flag_inviaDatiForm ) {
          const datiDaInviare = unisciOggetti(
            {
              [process.env.VUE_APP_FORM_USERNAME_INPUT_FIELD_NAME]  : this.username_wrapper,
              [process.env.VUE_APP_FORM_NOMINATIVO_INPUT_FIELD_NAME]: this.nominativo_wrapper,
              [process.env.VUE_APP_FORM_EMAIL_INPUT_FIELD_NAME]     : this.email_wrapper,
              [process.env.VUE_APP_FORM_CSRF_INPUT_FIELD_NAME]      : this.csrfToken_wrapper
            },
            this.datiAggiuntiviDaInviareAlServer
          );

          const promise = richiestaPost(this.urlInvioFormTramitePost, datiDaInviare);
          promise
            .then( risposta => {
              // pulizia dei campi del form
              this.username_wrapper   = "";
              this.nominativo_wrapper = "";
              this.email_wrapper      = "";
              return risposta
            })
            .catch( console.error );            // tiene traccia dell'eventuale errore

          this.$emit('dati-form-inviati', {
            'datiInviati'          : datiDaInviare,
            'promiseRispostaServer': promise
          });  // informa il componente padre

        }

      }
    },

    isQuestoFormRiferitoAConsumer : {
      immediate: true,
      deep: true,
      handler(nuovoValore) {
        this.isQuestoFormRiferitoAConsumer_wrapper = nuovoValore;
        this.usernameCodFisc = this.isQuestoFormRiferitoAConsumer ? "Codice fiscale" : "Username";
      }
    },

    datiAggiuntiviDaInviareAlServer : {
      immediate: true,
      deep: true,
      handler( oggettoConDatiAggiuntiviDaInviareNelForm ) {
        this.datiAggiuntiviDaInviareAlServer_wrapper = oggettoConDatiAggiuntiviDaInviareNelForm;
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
</script>

<style scoped>

</style>