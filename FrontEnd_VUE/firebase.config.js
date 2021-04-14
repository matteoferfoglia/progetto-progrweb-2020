// Script di configurazione per Firebase

// Your web app's Firebase configuration

// noinspection JSUnusedGlobalSymbols   // usato da altri moduli
export const firebaseConfig = {
    apiKey: "AIzaSyBDWeuSii3vJeNw0mD8lnOej2aoAzWB36w",
    authDomain: "progettoprogrweb2020.firebaseapp.com",
    projectId: "progettoprogrweb2020",
    storageBucket: "progettoprogrweb2020.appspot.com",
    messagingSenderId: "579459811437",
    appId: "1:579459811437:web:530daa81892009a2b45764"
};

export const authConfig = {
    persistence: "NONE" // LOCAL, SESSION, NONE, Fonte: https://firebase.google.com/docs/auth/web/auth-state-persistence#supported_types_of_auth_state_persistence
}