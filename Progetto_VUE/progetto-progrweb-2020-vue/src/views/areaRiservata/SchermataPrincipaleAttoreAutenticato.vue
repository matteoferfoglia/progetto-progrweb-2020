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


  <router-view :tipoAttoreAutenticato="tipoAttoreAutenticato_wrapper"
               :csrfToken="csrfToken_wrapper"
               :NOME_PROP_NOMINATIVO ="NOME_PROP_NOME_ATTORE"
               :NOME_PROP_USERNAME   ="NOME_PROP_USERNAME_ATTORE"
               :NOME_PROP_EMAIL      ="NOME_PROP_EMAIL_ATTORE"
               @csrf-token-ricevuto="$emit('csrf-token-ricevuto',$event)"/>


</template>

<script>

// Questo componente mostra la schermata principale di un attore autenticato

import FormCampiAttore from "../../components/layout/FormCampiAttore";
import {richiestaGet} from "../../utils/http";

export default {
  name: "SchermataPrincipaleAttore",
  inheritAttrs: false,
  components: {FormCampiAttore},
  emits: ['csrf-token-ricevuto','nominativo-attore-modificato'],
  props: ['tipoAttoreAutenticato', 'csrfToken'],
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

      tipoAttoreAutenticato_wrapper: this.tipoAttoreAutenticato,  //wrapper (prop non è modificabile)
      csrfToken_wrapper: this.csrfToken,

      // Copia dalle variabili d'ambiente: bisogna dichiararle per usarle nel template  // TODO : serve ? C'è anche in ElencoAttori
      tipoAttore_consumer: process.env.VUE_APP_TIPO_UTENTE_AUTENTICATO_CONSUMER,
      tipoAttore_uploader: process.env.VUE_APP_TIPO_UTENTE_AUTENTICATO_UPLOADER,
      tipoAttore_administrator: process.env.VUE_APP_TIPO_UTENTE_AUTENTICATO_ADMINISTRATOR,

      /** Tipo degli attori di cui mostrare l'elenco.*/
      tipoAttoriDiCuiMostrareElenco: undefined, // valutato in base alle azioni dell'utente, NON alla creazione di questo componente

      // Parametro del form
      nomeParametroForm_qualificaAttoreDaAggiungere: process.env.VUE_APP_FORM_TIPO_ATTORE_INPUT_FIELD_NAME,

      /** ID dell'elemento HTML usato per contenere il form di aggiunta nuovo attore.*/
      idSectionAggiuntaUploaderOAdministrator: "aggiuntaUploaderOAdministrator",

      /** Dati aggiuntivi (oggetto) che il {@link FormCampiAttore} deve
       * inviare al server, oltre a quelli già specificati nel componente stesso.*/
      datiAggiuntiviDaInviareAlServer: {}, // default: oggetto vuoto

      /** Nell'oggetto restituito dal server contenente le informazioni di un
       * Attore, il nome dell'Attore è salvato nella proprietà con il nome
       * indicato in questa variabile.*/
      NOME_PROP_NOME_ATTORE: undefined,

      /** Nell'oggetto restituito dal server contenente le informazioni di un
       * Attore, l'email dell'Attore è salvata nella proprietà con il nome
       * indicato in questa variabile.*/
      NOME_PROP_EMAIL_ATTORE: undefined,

      /** Nell'oggetto restituito dal server contenente le informazioni di un
       * Attore, lo username dell'Attore è salvato nella proprietà con il nome
       * indicato in questa variabile.*/
      NOME_PROP_USERNAME_ATTORE: undefined,

    }

  },
  created() {

    // TODO : aggiungere una proprietà isLayoutCaricato (visto che created() non può avere comportamento asincrono)

    // richiede il nome della prop contenente il nominativo di un attore nell'oggetto
    // che sarà restituito dal server con le info di un attore
    richiestaGet( process.env.VUE_APP_URL_GET_NOME_PROP_NOME_ATTORE )
        .then( nomePropNomeAttore => this.NOME_PROP_NOME_ATTORE = nomePropNomeAttore )

        // richiede il nome della prop contenente l'email
        .then( () => richiestaGet( process.env.VUE_APP_URL_GET_NOME_PROP_EMAIL_ATTORE ) )
        .then( nomePropEmailAttore => this.NOME_PROP_EMAIL_ATTORE = nomePropEmailAttore )

        // richiede il nome della prop contenente lo username
        .then( () => richiestaGet( process.env.VUE_APP_URL_GET_NOME_PROP_USERNAME_ATTORE ) )
        .then( nomePropUsernameAttore => this.NOME_PROP_USERNAME_ATTORE = nomePropUsernameAttore )

        .then( this.instradaVersoElencoAttori )

        .catch( console.error );

  },
  methods: {

    /** Metodo per instradare (Vue-Router) all'elenco attori.*/
    instradaVersoElencoAttori() {
      this.$router.push({ name:   process.env.VUE_APP_ROUTER_NOME_ELENCO_ATTORI });
    },

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
          .then( rispostaServer => {

            let messaggioInformativo;
            if( this.datiAggiuntiviDaInviareAlServer[process.env.VUE_APP_FOM_PASSWORD_INPUT_FIELD_NAME] ) {
              // Administrator ha aggiunto un attore
              messaggioInformativo = "Attore " + oggetto.datiInviati[process.env.VUE_APP_FORM_USERNAME_INPUT_FIELD_NAME] +
                  " [" + rispostaServer + "] aggiunto, la sua password è: " +
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

    /** Watch per il tipo di attore attualmente autenticato (potrebbe
     * fare logout e potrebbe autenticarsi subito dopo un attore
     * con un'altra qualifica).*/
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