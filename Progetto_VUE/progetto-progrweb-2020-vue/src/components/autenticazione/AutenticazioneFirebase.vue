<template>

  <div id="firebaseui-auth-container"></div>
  <small class="mt-1 text-center d-block text-secondary">Accedere utilizzando l'indirizzo e-mail registrato nell'applicazione.</small>

  <Loader v-if="attendendoRispostaDaServerApplicazione"
          class="fullscreen-loader">
  </Loader>

</template>

<script>

// Script per gestire l'autenticazione tramite Firebase.
// Fonti: codice delle lezioni, https://medium.com/dailyjs/authenticating-a-vue-js-application-with-firebase-ui-8870a3a5cff8, https://firebase.google.com/docs/web/setup#node.js-apps

// Firebase App (the core Firebase SDK) is always required and
// must be listed before other Firebase SDKs
import firebase from "firebase/app";
import 'firebase/auth'; // Add the Firebase services that you want to use

import * as firebaseui from 'firebaseui'

import {richiestaPostContenutoTextPlain} from "@/utils/http";
import Loader from "@/components/layout/Loader";

export default {
  name: "AutenticazioneFirebase",
  components: {Loader},
  emits: [

    /** Evento emesso quando l'intera procedura di login, sia con Firebase sia
     * con il server dell'applicazione, vanno a buon fine. Il valore associato
     * a questo evento è il token di autenticazione emesso dal server
     * dell'applicazione.*/
    'login-riuscito',

    /** Evento emesso se la procedura di login <strong>non</strong> va a buon
     * fine. Il valore associato a questo evento è il messaggio d'errore.*/
    'login-fallito'

  ],

  data() {

    return {

      /** Flag: true quando il client si è già autenticato con Firebase e sta
       * attendendo risposta dal server dell'applicazione. */
      attendendoRispostaDaServerApplicazione: false,

      /** Configurazione interfaccia grafica */
      uiConfig: {

        /** Opzioni di login. */
        signInOptions: [
          firebase.auth.GoogleAuthProvider.PROVIDER_ID // login con account Google
        ],

        /** Permette di scegliere come mostrare le opzioni di autenticazione:
         * redirect (predefinito) o popup (apre una nuova finestra). */
        signInFlow: "popup",

        /** Funzioni di callbacks invocate quando cambia lo stato dell'autenticazione */
        callbacks: {

          /** Funzione eseguita se il login con Firebase va a buon fine. */
          signInSuccessWithAuthResult: authResult => {

            this.attendendoRispostaDaServerApplicazione = true; // login con Firebase ok,
            // ora chiederà autenticazione al server dell'applicazione

            authResult.user.getIdToken()
                .then( tokenJwtDaFirebase =>
                  richiestaPostContenutoTextPlain(
                      process.env.VUE_APP_URL_LOGIN_CON_FIREBASE,
                      tokenJwtDaFirebase    // è il token JWT ottenuto da Firebase con info sull'utente
                  )
                )
                .then( tokenAutenticazionePerQuestaApplicazione => { // il mio server risponde con il token di autenticazione da lui emesso se login va a buon fine
                  this.$emit('login-riuscito', tokenAutenticazionePerQuestaApplicazione);
                })
                .catch( errore => {
                  console.error(errore);
                  this.ui = this.creaEAvviaAuthUiFirebase();
                  this.$emit('login-fallito', errore);
                });

          }

        }

      }

    }

  },

  created() {

    // Quando viene creato questo componente, l'utente deve essere undefined
    // (non si è ancora autenticato)
    firebase.auth().signOut();

  },

  methods: {

    /** Funzione per creare ed avviare la Auth User Interface di Firebase. */
    creaEAvviaAuthUiFirebase() {

      this.attendendoRispostaDaServerApplicazione = false;

      const creaAuthUiFirebase = () => {

        // Creazione della AuthUI di Firebase
        const ui = new firebaseui.auth.AuthUI(firebase.auth());
        ui.start("#firebaseui-auth-container", this.uiConfig);

      }

      const firebaseUiSeEsisteGia = firebaseui.auth.AuthUI.getInstance();

      // se esiste gia AuthUi, prima si cancella la vecchia istanza
      if( firebaseUiSeEsisteGia ) {
        firebaseUiSeEsisteGia.delete()
                             .then( creaAuthUiFirebase )
                             .catch( console.error );
      } else {
        creaAuthUiFirebase();
      }

    }

  },

  mounted() {

    this.creaEAvviaAuthUiFirebase() ;

  }
}
</script>

<style scoped>

/* Stile Firebase, importato da node_module */
@import "../../../node_modules/firebaseui/dist/firebaseui.css";

.fullscreen-loader {
  /* Sfondo bianco, applicato all'intera finestra del browser*/
  background-color: white;
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100vh;
}
</style>