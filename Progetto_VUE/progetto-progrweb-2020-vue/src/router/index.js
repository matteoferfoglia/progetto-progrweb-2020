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

import { createRouter, createWebHashHistory } from 'vue-router'
import {isAutenticato} from "../utils/utils";

const routes = [
  {
    path: process.env.VUE_APP_ROUTER_ROOT_PATH,
    name: "SchermataIniziale",
    component: () => import('../views/SchermataIniziale.vue')
  },
  {
    // Route non trovata (url invalido) (Fonte: https://router.vuejs.org/guide/essentials/redirect-and-alias.html#redirect)
    // todo : si potrebbe mostrare un alert prima di effettuare redirect
    path: '/:pathMatch(.*)*',
    redirect: { name: "SchermataIniziale"}
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


// Gestione delle richieste in base alla property "meta"
router.beforeEach((to, from, next) => {
  if(to.matched.some(record => record.meta.requiresAuth)) {
    // route richiede autorizzazione
    if(isAutenticato()) {
      // per questa route: se autenticato, allora è autorizzato
      next({
        path: process.env.VUE_APP_ROUTER_AREA_RISERVATA_PATH
      });
    } else {
      // non autorizzato
      next({
        path: process.env.VUE_APP_ROUTER_ROOT_PATH, // responsabilità di root
        params: {
          urlRichiesto: to.fullPath
        }
      })
    }
  } else {
    // route non richiede autorizzazione
    next();
  }
});

export default router;