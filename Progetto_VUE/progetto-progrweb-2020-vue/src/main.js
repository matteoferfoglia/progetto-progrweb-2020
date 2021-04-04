import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import {configurazioneAxios} from "../axios.config";
import firebase from "firebase/app";
import {firebaseConfig} from "@/firebase.config";
import swal from 'sweetalert';  // custom alert box, Fonte: https://sweetalert.js.org/guides/


// Configurazione axios
configurazioneAxios();

// Inizializzazione Firebase
firebase.initializeApp(firebaseConfig);

// Creazione App Vue
createApp(App).use(router).mount('#app');

// Riscrittura della funzione window.alert
window.alert = msg => swal( msg );

// Riscrittura della funzione window.confirm
/** Restituisce una Promise risolta se l'utente conferma,
 * oppure una Promise rigettata se l'utente rifiuta. */
window.confirm = msg =>
    swal({
            title: "Richiesta conferma",
            text: msg,
            icon: "warning",
            buttons: true,
            dangerMode: true,
    }).then( esitoConfirm => {
            if(esitoConfirm)
                    return Promise.resolve();
            else
                    return Promise.reject();
    });