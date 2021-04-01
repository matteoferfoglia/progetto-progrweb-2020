<template>

  <div id="firebaseui-auth-container"></div>
  <small class="mt-1 text-center d-block text-secondary">Accedere utilizzando l'indirizzo e-mail registrato nell'applicazione.</small>

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

export default {
  name: "AutenticazioneFirebase",

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

      /** Configurazione interfaccia grafica */
      uiConfig: {

        /** Opzioni di login. */
        signInOptions: [
          firebase.auth.GoogleAuthProvider.PROVIDER_ID // login con account Google
        ],

        /** Funzioni di callbacks invocate quando cambia lo stato dell'autenticazione */
        callbacks: {

          /** Funzione eseguita se il login con Firebase va a buon fine. */
          signInSuccessWithAuthResult: authResult => {

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

<!-- Stile Firebase, importato da node_module -->
<style src="firebaseui/dist/firebaseui.css"></style>