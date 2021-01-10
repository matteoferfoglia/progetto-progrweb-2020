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
import {verificaAutenticazione} from "../utils/autenticazione";


const routes = [
  {
    // Route non trovata (url invalido) (Fonte: https://router.vuejs.org/guide/essentials/redirect-and-alias.html#redirect)
    // todo : si potrebbe mostrare un alert prima di effettuare redirect
    path: '/:pathMatch(.*)*',
    redirect: { name: process.env.VUE_APP_ROUTER_NOME_COMPONENTE_AREA_RISERVATA}
  },
  {
    path: process.env.VUE_APP_ROUTER_AUTENTICAZIONE_PATH,
    component: () => import('../components/Autenticazione'),
    children: [ // Nested routes (fonte: https://router.vuejs.org/guide/essentials/nested-routes.html)
      {
        path: process.env.VUE_APP_ROUTER_LOGIN_PATH,
        name: process.env.VUE_APP_ROUTER_NOME_COMPONENTE_LOGIN,
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
    name: process.env.VUE_APP_ROUTER_NOME_COMPONENTE_AREA_RISERVATA,
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


// Gestione del routing
router.beforeEach((routeDestinazione, routeProvenienza, next) => {

  if( routeDestinazione.matched.some(route => route.meta.requiresAuth) ) {
    // Gestione instradamento per route che richiede autenticazione

    verificaAutenticazione(routeDestinazione)
        .then( isUtenteAutenticato => {

          // Permette di gestire il caso "utente richiede risorsa senza autenticazione,
          // lo mando alla pagina di login, poi lo rimando alla pagina che stava visitando"
          const MOTIVO_REDIRECTION_SE_RICHIESTA_SENZA_AUTENTICAZIONE = "NON AUTENTICATO";
          const NOME_PROPERTY_MOTIVO_REDIRECTION_VERSO_LOGIN = "motivoRedirectionVersoLogin";

          if(isUtenteAutenticato) {
            // Autenticato

            if(routeProvenienza.name === process.env.VUE_APP_ROUTER_NOME_COMPONENTE_LOGIN                   &&
                routeDestinazione.params[process.env.VUE_APP_ROUTER_PARAMETRO_PARAMS_ROUTE_RICHIESTA_PRIMA] && // verifico non nulla ne undefined
                routeDestinazione.params[NOME_PROPERTY_MOTIVO_REDIRECTION_VERSO_LOGIN] === MOTIVO_REDIRECTION_SE_RICHIESTA_SENZA_AUTENTICAZIONE ) {
              // Se qui: l'utente aveva chiesto una risorsa senza essere autenticato
              // ed era stato mandato al login, ora redirect alla pagina che stava usando
              // con tutti i parametri che aveva prima del redirect
              let parametriVecchiaRoute = routeDestinazione.params[process.env.VUE_APP_ROUTER_PARAMETRO_PARAMS_ROUTE_RICHIESTA_PRIMA];
              parametriVecchiaRoute = JSON.parse(parametriVecchiaRoute.substring(1,parametriVecchiaRoute.length-1));  // TODO : testare correttezza
                                                          // substring() rimuove "" aggiunte all'inizio ed alla fine
              const routeRichiestaPrima = {
                fullPath: routeDestinazione.params[process.env.VUE_APP_ROUTER_PARAMETRO_FULLPATH_ROUTE_RICHIESTA_PRIMA],
                params: parametriVecchiaRoute
              };
              next(routeRichiestaPrima);

            } else {
              next();

            }
          } else {
            // Non autenticato

            next({
              name: process.env.VUE_APP_ROUTER_NOME_COMPONENTE_LOGIN, // redirect a login
              params: {
                // memorizzo la route richiesta e la passo al componente di login così può fare redirect dopo il login a ciò che aveva richiesto
                [process.env.VUE_APP_ROUTER_PARAMETRO_FULLPATH_ROUTE_RICHIESTA_PRIMA]: routeDestinazione.fullPath,
                [process.env.VUE_APP_ROUTER_PARAMETRO_PARAMS_ROUTE_RICHIESTA_PRIMA]: JSON.stringify(routeDestinazione.params),
                      // JSON perché devono essere stringhe (altrimenti gli oggetti annidati vengono passati come "[Object object]")

                [NOME_PROPERTY_MOTIVO_REDIRECTION_VERSO_LOGIN]: MOTIVO_REDIRECTION_SE_RICHIESTA_SENZA_AUTENTICAZIONE
              }
            });

          }

        })
        .catch(console.error);

  } else {
    // SE route non richiede autorizzazione, ALLORA instrada senza problemi
    next();

  }

});

export default router;