import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import {configurazioneAxios} from "../axios.config";


configurazioneAxios();

createApp(App).use(router).mount('#app');