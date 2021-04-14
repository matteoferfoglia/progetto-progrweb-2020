<template>

  <div id="firebaseui-auth-container"></div>
  <small class="mt-1 text-center d-block text-secondary">
    Accedere utilizzando l'indirizzo e-mail registrato nell'applicazione.
  </small>

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

import {HTTP_STATUS_CONFLICT, richiestaPostContenutoTextPlain} from "../../utils/http";
import Loader from "../../components/layout/Loader";

import swal from "sweetalert";
import {authConfig} from "../../../firebase.config"; // Sweet alert

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

      /** ID dell'elemento html container per Firebase. */
      idHtmlFirebaseContainer: 'firebaseui-auth-container',

      /** Configurazione interfaccia grafica */
      uiConfig: {

        /** Opzioni di login. */
        signInOptions: [

          // Sign-in con Google mostrando finestra di scelta account
          //   Fonte: https://stackoverflow.com/a/59744590
          { provider: firebase.auth.GoogleAuthProvider.PROVIDER_ID,
            customParameters: { prompt: 'select_account' }
          }

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

            let tokenAutenticazioneFirebase;  // memorizzerà il token di autenticazione ottenuto da Firebase

            // Da invocare DOPO aver ricevuto il token da Firebase e averlo salvato nella variabile 'tokenAutenticazioneFirebase'
            const richiestaAutenticazioneAServerApplicazione = urlRichiesta =>
              richiestaPostContenutoTextPlain(
                  urlRichiesta,
                  tokenAutenticazioneFirebase    // è il token JWT ottenuto da Firebase con info sull'utente
              );  // il mio server risponde con il token di autenticazione da lui emesso se login va a buon fine

            const handler_loginRiuscito = tokenAutenticazionePerQuestaApplicazione => {
              this.$emit('login-riuscito', tokenAutenticazionePerQuestaApplicazione);
            };

            const handler_loginFallito = errore => {
              console.error(errore);
              this.ui = this.creaEAvviaAuthUiFirebase();
              this.$emit('login-fallito', errore);
            }

            const MSG_LOGIN_RIUSCITO = "Login riuscito";  // usato per uscire dalla then-catch chain
            authResult.user.getIdToken()
                .then( tokenJwtDaFirebase => {
                  tokenAutenticazioneFirebase = tokenJwtDaFirebase;
                  return richiestaAutenticazioneAServerApplicazione(process.env.VUE_APP_URL_LOGIN_CON_FIREBASE);
                })
                .then( tokenAutenticazionePerQuestaApplicazione => {
                  handler_loginRiuscito(tokenAutenticazionePerQuestaApplicazione);
                  return Promise.reject(MSG_LOGIN_RIUSCITO);  // per uscire da questa chain di then-catch
                })
                .catch( errore => {

                  if (errore.status === HTTP_STATUS_CONFLICT) {
                    // più utenti con stessa email

                    // Oggetto rappresentante i button da mostrare, avente per valore il testo da mostrare
                    const __buttons = Object.fromEntries(errore.data.map(unUsername => [unUsername, unUsername])); // errore.data contiene la lista di username possibili

                    return swal("Sono stati trovati nel sistema più utenti " +
                        "associati all'account selezionato. Sceglierne uno:", {
                      buttons: __buttons
                    })

                  } else {

                    // Errore diverso non gestito da questo catch e rilanciato
                    return Promise.reject(errore);

                  }

                })
                .then( usernameDaAutenticare => {

                  const urlRichiestaAutenticazioneConUsername =
                      process.env.VUE_APP_URL_LOGIN_CON_FIREBASE +
                      '?' + process.env.VUE_APP_FORM_USERNAME_INPUT_FIELD_NAME + '=' + usernameDaAutenticare;

                  return richiestaAutenticazioneAServerApplicazione(urlRichiestaAutenticazioneConUsername);

                })
                .then( handler_loginRiuscito )
                .catch( errore => {
                  if( errore!==MSG_LOGIN_RIUSCITO )
                    handler_loginFallito(errore);
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
    firebase.auth().setPersistence(firebase.auth.Auth.Persistence[authConfig.persistence]); // imposta livello di persistenza delle credenziali Firebase

  },

  methods: {

    /** Funzione per creare ed avviare la Auth User Interface di Firebase. */
    creaEAvviaAuthUiFirebase() {

      this.attendendoRispostaDaServerApplicazione = false;

      const creaAuthUiFirebase = () => {

        // Creazione della AuthUI di Firebase
        const ui = new firebaseui.auth.AuthUI(firebase.auth());
        ui.start("#" + this.idHtmlFirebaseContainer, this.uiConfig);

      }

      const firebaseUiSeEsisteGia = firebaseui.auth.AuthUI.getInstance();

      // se esiste gia AuthUi, prima si cancella la vecchia istanza
      if( firebaseUiSeEsisteGia ) {
        firebaseUiSeEsisteGia.delete()    // cancella istanza di Firebase AuthUI
                             .then( creaAuthUiFirebase )
                             .catch( console.error );
      } else {
        creaAuthUiFirebase();
      }

    }

  },

  mounted() {

    // Elimina vecchi contenuti Firebase dal DOM se presenti
    const vecchioFirebaseContainer = document.getElementById(this.idHtmlFirebaseContainer);
    if( vecchioFirebaseContainer && typeof vecchioFirebaseContainer.remove === "function" ) {
      vecchioFirebaseContainer
          .querySelectorAll('*')
          .forEach(el => el.remove());
    }

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