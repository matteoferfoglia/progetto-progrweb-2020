/**
 * Definizione delle routes per Vue-Router.
 * L'eventuale property "meta" è stata introdotta per gestire
 * comportamenti particolari (ad esempio, alcune pagine devono
 * poter essere accessibili solo se chi ne fa richiesta è
 * già autenticato).
 *
 * Fonti:
 *  https://www.digitalocean.com/community/tutorials/how-to-set-up-vue-js-authentication-and-route-handling-using-vue-router,
 *  https://router.vuejs.org/guide/advanced/navigation-guards.html#global-before-guards
 *  https://router.vuejs.org/guide/essentials/redirect-and-alias.html#redirect
 *  https://router.vuejs.org/guide/essentials/nested-routes.html
 */

// TODO : rivedere ed eventualmente ristrutturare questo script

import { createRouter, createWebHashHistory } from 'vue-router'

const routes = [
  {
    path: process.env.VUE_APP_ROUTER_ROOT_PATH,
    name: process.env.VUE_APP_ROUTER_NOME_COMPONENTE_SCHERMATA_INIZIALE,
    component: () => import('../views/SchermataIniziale.vue')
  },
  {
    // Route non trovata (url invalido) (Fonte: https://router.vuejs.org/guide/essentials/redirect-and-alias.html#redirect)
    // todo : si potrebbe mostrare un alert prima di effettuare redirect
    path: '/:pathMatch(.*)*',
    redirect: { name: process.env.VUE_APP_ROUTER_NOME_COMPONENTE_SCHERMATA_INIZIALE}
  },
  {
    path: process.env.VUE_APP_ROUTER_AUTENTICAZIONE_PATH,
    component: () => import('../components/Autenticazione'),
    meta: {
      requiresNotLoggedIn: true // pagina di autenticazione accessibile solo se non già loggato
    },
    children: [ // Nested routes (fonte: https://router.vuejs.org/guide/essentials/nested-routes.html)
      {
        path: process.env.VUE_APP_ROUTER_LOGIN_PATH,
        alias: "",  // questo è il default child
        component: () => import('../views/autenticazione/LoginUtenteGiaRegistrato')
      },
      {
        path: process.env.VUE_APP_ROUTER_REGISTRAZIONE_CONSUMER_PATH,
        component: () => import('../views/autenticazione/RegistrazioneNuovoConsumer'),  // lazy-loading
      }
    ]
  },
  {
    path: process.env.VUE_APP_ROUTER_AREA_RISERVATA_PATH,
    component: () => import('../components/AreaRiservata'),                // lazy-loading
    meta: {
      requiresAuth: true
    }
  }
]

const router = createRouter({
  history: createWebHashHistory(),
  routes: routes
});


/*
// Gestione del routing
router.beforeEach((to, from, next) => {

  if( to.matched.some(record => record.meta.requiresAuth) ) { // ture se la route richiesta ha la property "requiresAuth" in meta           // TODO rivedere
    // gestione instradamento per route che richiede autorizzazione

    const parametriRouter = {};  // oggetto con i parametri utilizzati da Vue Router per la prossima route di destinazione

    const tokenAutenticazione = to.params[process.env.VUE_APP_ROUTER_PARAMETRO_TOKEN_AUTENTICAZIONE]; // dai parametri di vue router

    if(tokenAutenticazione) { // truthy se token definito e non nullo
      // Imposta token di autenticazione come parametro della route prima di inoltrare nella route richiesta
      parametriRouter[process.env.VUE_APP_ROUTER_PARAMETRO_TOKEN_AUTENTICAZIONE] = tokenAutenticazione;
    } else {  // non autorizzato ad accedere alla route richiesta
      //parametriRouter[process.env.VUE_APP_ROUTER_PARAMETRO_TOKEN_AUTENTICAZIONE] = "";  // impostato ad undefined per "enforcement"  // TODO : serve ? Si può cancellare?
      parametriRouter["urlRichiestoMaNonAutorizzato"] = to.path;  // url per cui il client aveva fatto richiesta
    }

    next({params: parametriRouter});

  } else if( to.matched.some(record => record.meta.requiresNotLoggedIn) ) {
    // TODO : da implementare (se utente già loggato, evitare che possa tornare nel componente di autenticazione)
    next();
  } else {
    // SE route non richiede autorizzazione, ALLORA instrada senza problemi (invia comunque i parametri di route)
    next();
  }
});*/

export default router;