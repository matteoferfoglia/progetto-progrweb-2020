<template>

  <!-- ###################   AGGIUNTA CONSUMER   ################### -->
  <section id="aggiuntaConsumer"
           v-if="isUploader()"><!-- TODO : creare componente "AggiuntaConsumer" ed "AggiuntaUploaderOAdmin" -->
    <p>
      Da questo form è possibile aggiungere un nuovo <i>{{ tipoAttore_consumer }}</i>
      tra quelli registrati nella piattaforma.
    </p>
    <FormCampiAttore :flag_mostrareLabelCampiInput="false"
                     :urlInvioFormTramitePost="urlAggiuntaNuovoConsumer"
                     :flag_inviaDatiForm="flag_inviareDatiFormAggiuntaConsumer"
                     :isQuestoFormRiferitoAConsumer="true"
                     :csrfToken="csrfToken_wrapper"
                     :isContentType_JSON="true"
                     @submit="flag_inviareDatiFormAggiuntaConsumer = true"
                     @dati-form-inviati="formAggiuntaAttoreInviato($event)"
                     @csrf-token-ricevuto="$emit('csrf-token-ricevuto',$event)">
      <input type="submit" value="Aggiungi">
    </FormCampiAttore>
  </section>


  <!-- ###################   AGGIUNTA UPLOADER/ADMINISTRATOR   ################### -->
  <section :id="idSectionAggiuntaUploaderOAdministrator"
           v-if="isAdministrator()">
    <p>
      Da questo form è possibile aggiungere un nuovo <i>{{ tipoAttore_uploader }}</i>
      oppure un nuovo <i>{{ tipoAttore_administrator }}</i> alla piattaforma.
    </p>
    <FormCampiAttore :flag_mostrareLabelCampiInput="false"
                     :urlInvioFormTramitePost="urlAggiuntaNuovoAttore"
                     :flag_inviaDatiForm="flag_inviareDatiFormAggiuntaAttoreNonConsumer"
                     :isQuestoFormRiferitoAConsumer="false"
                     :csrfToken="csrfToken_wrapper"
                     :datiAggiuntiviDaInviareAlServer="datiAggiuntiviDaInviareAlServer"
                     :isContentType_JSON="true"
                     @submit="flag_inviareDatiFormAggiuntaAttoreNonConsumer = true"
                     @dati-form-inviati="formAggiuntaAttoreInviato($event)"
                     @csrf-token-ricevuto="$emit('csrf-token-ricevuto',$event)">
      <p>Qualifica:
        <label>
          <input type="radio"
                 :name="nomeParametroForm_qualificaAttoreDaAggiungere"
                 :value="tipoAttore_uploader"
                 @click="impostaValoriDaInviareAlServer(tipoAttore_uploader)"
                 required>
          {{ tipoAttore_uploader }}
        </label>
        <label>
          <input type="radio"
                 :name="nomeParametroForm_qualificaAttoreDaAggiungere"
                 :value="tipoAttore_administrator"
                 @click="impostaValoriDaInviareAlServer(tipoAttore_administrator)">
          {{ tipoAttore_administrator }}
        </label>
      </p>
      <input type="submit" value="Aggiungi">
    </FormCampiAttore>
  </section>

</template>

<script>
import FormCampiAttore from "../layout/FormCampiAttore";
import {unisciOggetti} from "../../utils/utilitaGenerale";
export default {
  name: "AggiuntaAttore",
  components: {FormCampiAttore},
  props: ['tipoAttoreAutenticato', 'csrfToken'],
  emits: [
    'csrf-token-ricevuto',
    'nominativo-attore-modificato',

    /** Evento emesso quando viene aggiunto un nuovo attore (dopo
     * che il server ha confermato l'aggiunta. Il valore è un oggetto
     * rappresentante l'attore, con gli stessi attributi indicati nel
     * form per l'aggiunta dell'attore stesso ed anche l'identificativo
     * ricevuto dal server dopo aver confermato l'aggiunta.*/
    "aggiunto-nuovo-attore"
  ],
  data() {
    return {

      /** Url per richiedere al server di aggiungere un consumer.*/
      urlAggiuntaNuovoConsumer: process.env.VUE_APP_URL_AGGIUNGI_CONSUMER_PER_QUESTO_UPLOADER__RICHIESTA_DA_UPLOADER,

      /** Url per richiedere al server di aggiungere un attore,
       * senza eventuali parametri aggiunti in coda.*/
      urlAggiuntaNuovoAttore: process.env.VUE_APP_URL_AGGIUNGI_ATTORE__RICHIESTA_DA_ADMIN,

      /** Flag: se impostato a true, i dati del form di aggiunta nuovo consumer vengono inviati al server.*/
      flag_inviareDatiFormAggiuntaConsumer: false,

      /** Flag: se impostato a true, i dati del form di aggiunta nuovo attore non-Consumer vengono inviati al server.*/
      flag_inviareDatiFormAggiuntaAttoreNonConsumer: false,


      // Parametro del form
      nomeParametroForm_qualificaAttoreDaAggiungere: process.env.VUE_APP_FORM_TIPO_ATTORE_INPUT_FIELD_NAME,

      /** ID dell'elemento HTML usato per contenere il form di aggiunta nuovo attore.*/
      idSectionAggiuntaUploaderOAdministrator: "aggiuntaUploaderOAdministrator",

      /** Dati aggiuntivi (oggetto) che il {@link FormCampiAttore} deve
       * inviare al server, oltre a quelli già specificati nel componente stesso.*/
      datiAggiuntiviDaInviareAlServer: {}, // default: oggetto vuoto


      // Copia dalle variabili d'ambiente: bisogna dichiararle per usarle nel template  // TODO : serve ? C'è anche in ElencoAttori
      tipoAttore_consumer: process.env.VUE_APP_TIPO_UTENTE_AUTENTICATO_CONSUMER,
      tipoAttore_uploader: process.env.VUE_APP_TIPO_UTENTE_AUTENTICATO_UPLOADER,
      tipoAttore_administrator: process.env.VUE_APP_TIPO_UTENTE_AUTENTICATO_ADMINISTRATOR,

      // Wrapper
      tipoAttoreAutenticato_wrapper: this.tipoAttoreAutenticato,
      csrfToken_wrapper: this.csrfToken
    }
  },
  methods: {

    /** Crea l'oggetto coi dati aggiuntivi da inviare al server
     * per l'aggiunta di un nuovo attore, oltre a quelli normalmente
     * gestiti da {@link FormCampiAttore}.
     * @param tipoAttore Il tipo di attore che si vuole aggiungere.*/
    impostaValoriDaInviareAlServer( tipoAttore ) {
      this.datiAggiuntiviDaInviareAlServer = {
        [process.env.VUE_APP_FOM_PASSWORD_INPUT_FIELD_NAME]: Math.trunc((Math.random()*10e6))+1234,  // genera una password casuale
        // (l'attore appena creato dovrebbe cambiarla al primo login...)
        [process.env.VUE_APP_FORM_TIPO_ATTORE_INPUT_FIELD_NAME]: tipoAttore
      }
    },

    /** Metodo invocato quando il form per l'aggiunta di un attore viene
     * inviato.
     * @param oggetto Oggetto restituito dal gestore di {@link FormCampiAttore.script.default.watch.flag_inviaDatiForm}.
     */
    formAggiuntaAttoreInviato ( oggetto ) {
      oggetto.promiseRispostaServer
          .then( identificativoAttore => {

            let messaggioInformativo;
            if( this.datiAggiuntiviDaInviareAlServer[process.env.VUE_APP_FOM_PASSWORD_INPUT_FIELD_NAME] ) {
              // Administrator ha aggiunto un attore
              messaggioInformativo = "Attore " + oggetto.datiInviati[process.env.VUE_APP_FORM_USERNAME_INPUT_FIELD_NAME] +
                  " [" + identificativoAttore + "] aggiunto, la sua password è: " +
                  this.datiAggiuntiviDaInviareAlServer[process.env.VUE_APP_FOM_PASSWORD_INPUT_FIELD_NAME] +
                  ". Si consiglia di modificare la password al primo accesso.";

              const formAggiuntaUploaderOAdministrator =
                  document.querySelector("#"+this.idSectionAggiuntaUploaderOAdministrator+" form");
              formAggiuntaUploaderOAdministrator.reset();

            } else {
              // Uploader ha aggiunto un Consumer
              messaggioInformativo = "Consumer " + oggetto.datiInviati[process.env.VUE_APP_FORM_USERNAME_INPUT_FIELD_NAME] +
                  " aggiunto.";
            }

            this.$emit("aggiunto-nuovo-attore", unisciOggetti({
              [process.env.VUE_APP_FORM_IDENTIFICATIVO_ATTORE_INPUT_FIELD]: identificativoAttore
            },oggetto.datiInviati));

            alert( messaggioInformativo );

          })
          .catch( rispostaServer => {
            console.error( rispostaServer );
            alert( "ERRORE: "+ rispostaServer.data );
          })
          .finally( () => {
            this.flag_inviareDatiFormAggiuntaConsumer = false;
            this.flag_inviareDatiFormAggiuntaAttoreNonConsumer = false;
          });
    },

    isConsumer() {
      return this.tipoAttoreAutenticato_wrapper ===
          process.env.VUE_APP_TIPO_UTENTE_AUTENTICATO_CONSUMER;
    },
    isUploader() {
      return this.tipoAttoreAutenticato_wrapper ===
          process.env.VUE_APP_TIPO_UTENTE_AUTENTICATO_UPLOADER;
    },
    isAdministrator() {
      return this.tipoAttoreAutenticato_wrapper ===
          process.env.VUE_APP_TIPO_UTENTE_AUTENTICATO_ADMINISTRATOR;
    }
  },
  watch: {

    tipoAttoreAutenticato: {
      immediate: true,
      deep: true,
      handler( nuovoValore ) {
        this.tipoAttoreAutenticato_wrapper = nuovoValore;
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