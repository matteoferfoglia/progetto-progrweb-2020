<template>

  <section :id="idSectionAggiuntaAttore" >
    <p v-if="isAdministrator()">
      Da questo form è possibile aggiungere un nuovo <i>{{ tipoAttore_uploader }}</i>
      oppure un nuovo <i>{{ tipoAttore_administrator }}</i> alla piattaforma.
    </p>
    <p v-if="isUploader()">
      Da questo form è possibile aggiungere un nuovo <i>{{ tipoAttore_consumer }}</i>.
    </p>
    <FormCampiAttore :flag_mostrareLabelCampiInput="false"
                     :urlInvioFormTramitePost="isUploader() ? urlAggiuntaNuovoConsumerPerUploader : urlAggiuntaNuovoAttorePerAdmin"
                     :flag_inviaDatiForm="flag_inviareDatiFormAggiuntaAttore"
                     :isQuestoFormRiferitoAConsumer="isUploader()"
                     :csrfToken="csrfToken_wrapper"
                     :datiAggiuntiviDaInviareAlServer="datiAggiuntiviDaInviareAlServer"
                     :isContentType_JSON="true"
                     @submit="flag_inviareDatiFormAggiuntaAttore = true"
                     @dati-form-inviati="formAggiuntaAttoreInviato($event)"
                     @csrf-token-ricevuto="$emit('csrf-token-ricevuto',$event)">

      <div v-if="isAdministrator()">
        <!-- Sezione solo per administrator -->
        <p>Qualifica:
          <label>
            <input type="radio"
                   :name="nomeParametroForm_qualificaAttoreDaAggiungere"
                   :value="tipoAttore_uploader"
                   @click="impostaTipoAttoreDaCreare(tipoAttore_uploader)"
                   required>
            {{ tipoAttore_uploader }}
          </label>
          <label>
            <input type="radio"
                   :name="nomeParametroForm_qualificaAttoreDaAggiungere"
                   :value="tipoAttore_administrator"
                   @click="impostaTipoAttoreDaCreare(tipoAttore_administrator)">
            {{ tipoAttore_administrator }}
          </label>
        </p>
      </div>

      <input type="submit" value="Aggiungi">
    </FormCampiAttore>
  </section>

</template>

<script>
import FormCampiAttore from "../layout/FormCampiAttore";
import {unisciOggetti} from "../../utils/utilitaGenerale";
import {getHttpResponseStatus, HTTP_STATUS_UNAUTHORIZED} from "../../utils/http";
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
      urlAggiuntaNuovoConsumerPerUploader: process.env.VUE_APP_URL_AGGIUNGI_CONSUMER_PER_QUESTO_UPLOADER__RICHIESTA_DA_UPLOADER,

      /** Url per richiedere al server di aggiungere un attore,
       * senza eventuali parametri aggiunti in coda, aggiunto da
       * un Administrator.*/
      urlAggiuntaNuovoAttorePerAdmin: process.env.VUE_APP_URL_AGGIUNGI_ATTORE__RICHIESTA_DA_ADMIN,

      /** Flag: se impostato a true, i dati del form di aggiunta nuovo attore vengono inviati al server.*/
      flag_inviareDatiFormAggiuntaAttore: false,


      // Parametro del form
      nomeParametroForm_qualificaAttoreDaAggiungere: process.env.VUE_APP_FORM_TIPO_ATTORE_INPUT_FIELD_NAME,

      /** ID dell'elemento HTML usato per contenere il form di aggiunta nuovo attore.*/
      idSectionAggiuntaAttore: "aggiuntaUploaderOAdministrator",

      /** Dati aggiuntivi (oggetto) che il {@link FormCampiAttore} deve
       * inviare al server, oltre a quelli già specificati nel componente stesso.*/
      datiAggiuntiviDaInviareAlServer: {
        [process.env.VUE_APP_FORM_TIPO_ATTORE_INPUT_FIELD_NAME]: process.env.VUE_APP_TIPO_UTENTE__CONSUMER  // default
      },


      // Copia dalle variabili d'ambiente: bisogna dichiararle per usarle nel template  // TODO : serve ? C'è anche in ElencoAttori
      tipoAttore_consumer: process.env.VUE_APP_TIPO_UTENTE__CONSUMER,
      tipoAttore_uploader: process.env.VUE_APP_TIPO_UTENTE__UPLOADER,
      tipoAttore_administrator: process.env.VUE_APP_TIPO_UTENTE__ADMINISTRATOR,

      // Wrapper
      tipoAttoreAutenticato_wrapper: this.tipoAttoreAutenticato,
      csrfToken_wrapper: this.csrfToken
    }
  },
  methods: {

    /** Crea l'oggetto coi dati aggiuntivi da inviare al server
     * per l'aggiunta di un nuovo attore, oltre a quelli normalmente
     * gestiti da {@link FormCampiAttore}.
     * @param tipoAttoreDaCreare Il tipo di attore che si vuole aggiungere.*/
    impostaTipoAttoreDaCreare(tipoAttoreDaCreare ) {
      this.datiAggiuntiviDaInviareAlServer[process.env.VUE_APP_FORM_TIPO_ATTORE_INPUT_FIELD_NAME] = tipoAttoreDaCreare
    },

    /** Metodo invocato quando il form per l'aggiunta di un attore viene
     * inviato.
     * @param oggetto Oggetto restituito dal gestore di {@link FormCampiAttore.script.default.watch.flag_inviaDatiForm}.
     */
    formAggiuntaAttoreInviato ( oggetto ) {
      oggetto.promiseRispostaServer
          .then( identificativoAttore => {

            // Attore aggiunto

            let messaggioInformativo =
                oggetto.datiInviati[process.env.VUE_APP_FORM_USERNAME_INPUT_FIELD_NAME] + " aggiunto.";

            const formAggiuntaUploaderOAdministrator =
                document.querySelector("#" + this.idSectionAggiuntaAttore + " form");
            formAggiuntaUploaderOAdministrator.reset();


            this.$emit("aggiunto-nuovo-attore", unisciOggetti({
              [process.env.VUE_APP_FORM_IDENTIFICATIVO_ATTORE_INPUT_FIELD]: identificativoAttore
            },oggetto.datiInviati));

            alert( messaggioInformativo );

          })
          .catch( rispostaServer => {
            console.error( rispostaServer );
            if(getHttpResponseStatus(rispostaServer) === HTTP_STATUS_UNAUTHORIZED) // es. se è scaduto il token di autenticazione
              alert("Non autorizzato. Autenticarsi.");
            else
              alert( "ERRORE: "+ rispostaServer.data );
          })
          .finally( () => {
            this.flag_inviareDatiFormAggiuntaAttore = false;
          });
    },

    isConsumer() {
      return this.tipoAttoreAutenticato_wrapper ===
          process.env.VUE_APP_TIPO_UTENTE__CONSUMER;
    },
    isUploader() {
      return this.tipoAttoreAutenticato_wrapper ===
          process.env.VUE_APP_TIPO_UTENTE__UPLOADER;
    },
    isAdministrator() {
      return this.tipoAttoreAutenticato_wrapper ===
          process.env.VUE_APP_TIPO_UTENTE__ADMINISTRATOR;
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