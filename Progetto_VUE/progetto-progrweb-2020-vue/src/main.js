import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import axios from "axios";

createApp(App).use(router).mount('#app');
axios.interceptors.request.use(
    // Fonte: https://stackoverflow.com/a/55643460
    axiosRequestConfig => {
        axiosRequestConfig.withCredentials = true;
        return axiosRequestConfig;
    },
    error => new Promise.reject(error)
);
