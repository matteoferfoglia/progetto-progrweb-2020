<template>

  <article :id="idSectionAggiuntaAttore" class="card">
    <p v-if="isAdministratorAttualmenteAutenticato()">
      Da qui è possibile aggiungere un nuovo <i>{{ tipoAttore_uploader }}</i>
      oppure un nuovo <i>{{ tipoAttore_administrator }}</i> alla piattaforma.
    </p>
    <p v-if="isUploaderAttualmenteAutenticato()">
      Da qui è possibile aggiungere un nuovo <i>{{ tipoAttore_consumer }}</i>.
    </p>
    <FormCampiAttore :flag_mostrareLabelCampiInput="false"
                     :urlInvioFormTramitePost="isUploaderAttualmenteAutenticato() ? urlAggiuntaNuovoConsumerPerUploader : urlAggiuntaNuovoAttorePerAdmin"
                     :flag_inviaDatiForm="flag_inviareDatiFormAggiuntaAttore"
                     :isQuestoFormRiferitoAConsumer="isUploaderAttualmenteAutenticato()"
                     :csrfToken="csrfToken_wrapper"
                     :datiAggiuntiviDaInviareAlServer="datiAggiuntiviDaInviareAlServer"
                     :isContentType_JSON="true"
                     :tuttiICampi_required="true"
                     @submit="flag_inviareDatiFormAggiuntaAttore = true"
                     @dati-form-inviati="formAggiuntaAttoreInviato($event)"
                     @csrf-token-ricevuto="$emit('csrf-token-ricevuto',$event)">

      <p v-if="isAdministratorAttualmenteAutenticato()" class="form-check">Qualifica:
        <!-- Sezione solo per administrator -->
        <label class="form-check-label">
          <input type="radio"
                 :name="nomeParametroForm_qualificaAttoreDaAggiungere"
                 :value="tipoAttore_uploader"
                 @click="impostaTipoAttoreDaCreare(tipoAttore_uploader)"
                 required>
          {{ tipoAttore_uploader }}
        </label>
        <label class="form-check-label">
          <input type="radio"
                 :name="nomeParametroForm_qualificaAttoreDaAggiungere"
                 :value="tipoAttore_administrator"
                 @click="impostaTipoAttoreDaCreare(tipoAttore_administrator)">
          {{ tipoAttore_administrator }}
        </label>
      </p>

      <input type="submit" value="Aggiungi" class="btn btn-primary">
    </FormCampiAttore>
  </article>

</template>

<script>
import FormCampiAttore from "../layout/FormCampiAttore";
import {unisciOggetti} from "../../utils/utilitaGenerale";
import {getHttpResponseStatus, HTTP_STATUS_UNAUTHORIZED} from "../../utils/http";
import {
  isAdministratorAttualmenteAutenticato,
  isConsumerAttualmenteAutenticato,
  isUploaderAttualmenteAutenticato
} from "../../utils/autenticazione";
export default {
  name: "AggiuntaAttore",
  components: {FormCampiAttore},
  props: ['csrfToken'],
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

      // Copia dalle variabili d'ambiente: bisogna dichiararle per usarle nel template
      tipoAttore_consumer: process.env.VUE_APP_TIPO_UTENTE__CONSUMER,
      tipoAttore_uploader: process.env.VUE_APP_TIPO_UTENTE__UPLOADER,
      tipoAttore_administrator: process.env.VUE_APP_TIPO_UTENTE__ADMINISTRATOR,

      // Import funzioni
      isConsumerAttualmenteAutenticato: isConsumerAttualmenteAutenticato,
      isUploaderAttualmenteAutenticato: isUploaderAttualmenteAutenticato,
      isAdministratorAttualmenteAutenticato: isAdministratorAttualmenteAutenticato,

      // Wrapper
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
            if(getHttpResponseStatus(rispostaServer) === HTTP_STATUS_UNAUTHORIZED) { // es. se è scaduto il token di autenticazione
              alert("Non autorizzato. Autenticarsi.");
            } else {
              const MSG_ERRORE = rispostaServer.data ? String(rispostaServer.data) : String(rispostaServer);
              alert( MSG_ERRORE );
            }
          })
          .finally( () => {
            this.flag_inviareDatiFormAggiuntaAttore = false;
          });
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
</script>

<style scoped>
label {
  display: block;
}
input[type=submit], button {
  margin: 1rem 0;
}
</style>