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
 */

import { createRouter, createWebHashHistory } from 'vue-router'
import Login from "../views/login/LoginUtenteGiaRegistrato";
import App from "../App";
import {isAutenticato} from "../utils/utils";

const routes = [
  {
    path: process.env.VUE_APP_ROUTER_ROOT_PATH,
    component: App
  },
  {
    path: process.env.VUE_APP_ROUTER_LOGIN_PATH,
    component: Login,
    meta: {
      requiresNotLoggedIn: true
    }
  },
  {
    path: process.env.VUE_APP_ROUTER_REGISTRAZIONE_CONSUMER_PATH,
    component: () => import('../views/login/RegistrazioneNuovoConsumer.vue'),  // lazy-loading
    meta: {
      requiresNotLoggedIn: true
    }
  },
  {
    path: process.env.VUE_APP_ROUTER_AREA_RISERVATA_PATH,
    component: () => import('../components/AreaRiservata.vue'),                // lazy-loading
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
      // autorizzato
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