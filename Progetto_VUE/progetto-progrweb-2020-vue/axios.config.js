// Configurazione axios per inviare automaticamente credenziali e cookie
// Fonte: https://stackoverflow.com/a/55643460
import axios from "axios";

/** Funzione da modificare per configurare Axios.*/
export const configurazioneAxios = () => {
    axios.interceptors.request.use(
        axiosRequestConfig => {
            axiosRequestConfig.withCredentials = true;
            return axiosRequestConfig;
        }
    );
}