import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import {configurazioneAxios} from "../axios.config";
import firebase from "firebase/app";
import {firebaseConfig} from "@/firebase.config";

// Configurazione axios
configurazioneAxios();

// Inizializzazione Firebase
firebase.initializeApp(firebaseConfig);

// Creazione App Vue
createApp(App).use(router).mount('#app');