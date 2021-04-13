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

import {createRouter, createWebHashHistory} from 'vue-router'
import {eliminaInfoAutenticazione, verificaAutenticazione} from "../utils/autenticazione";
import {getProprietaAttoreTarget, getTipoAttoreTarget} from "../utils/richiesteSuAttori";

/** Oggetto contenente i possibili campi "meta" usati nelle
 * route, qua definiti in stile "enum".*/
const metaProps_enum = {
  requiresNonAuth:    0,    // se una route richiede di non essere autenticato
  requiresAuth:       1,    // se una route richiede di essere autenticato
  requiresInfoAttore: 2,    // se una route richiede info su un attore
};
Object.freeze(metaProps_enum); // A frozen object can no longer be changed. Fonte: https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Object/freeze


const routes = [

  // Route default (se non trovate altre)
  {
    // Route non trovata (url invalido) (Fonte: https://router.vuejs.org/guide/essentials/redirect-and-alias.html#redirect)
    path: '/:pathMatch(.*)*',
    props: true,  // When props is set to true, the route.params will be set as the component props. Fonte: https://router.vuejs.org/guide/essentials/passing-props.html#boolean-mode
    redirect: { name: process.env.VUE_APP_ROUTER_ROOT_NOME }
  },

  {
    // Componente root
    path: process.env.VUE_APP_ROUTER_ROOT_PATH,
    name: process.env.VUE_APP_ROUTER_ROOT_NOME,
    redirect: {name: process.env.VUE_APP_ROUTER_NOME_COMPONENTE_AREA_RISERVATA},
  },


  {
    // Autenticazione

    path: process.env.VUE_APP_ROUTER_AUTENTICAZIONE_PATH,
    component: () => import('../views/PaginaAutenticazione'),
    meta: {
      [metaProps_enum.requiresNonAuth]: true
    },
    children: [
      {
        path: process.env.VUE_APP_ROUTER_PATH_LOGIN,
        alias: process.env.VUE_APP_ROUTER_AUTENTICAZIONE_PATH,  // route default
        name: process.env.VUE_APP_ROUTER_NOME_ROUTE_LOGIN,
        component: () => import('../views/autenticazione/LoginUtenteGiaRegistrato')
      },
      {
        path: process.env.VUE_APP_ROUTER_PATH_REGISTRAZIONE_CONSUMER,
        component: () => import('../views/autenticazione/RegistrazioneNuovoConsumer'),
      }
    ]
  },


  {
    // Area riservata

    path: process.env.VUE_APP_ROUTER_PATH_AREA_RISERVATA,
    component: () => import('../views/AreaRiservata'),
    props: true,
    meta: {
      [metaProps_enum.requiresAuth]: true
    },
    children: [
      {
        path: process.env.VUE_APP_ROUTER_PATH_IMPOSTAZIONI_ACCOUNT,
        component: () => import('../views/areaRiservata/ImpostazioniAccount'),
      },

      {
        // Schermata principale dell'area riservata
        path: process.env.VUE_APP_ROUTER_PATH_AREA_RISERVATA,             // percorso default per area riservata
        name: process.env.VUE_APP_ROUTER_NOME_COMPONENTE_AREA_RISERVATA,
        component: () => import('../views/areaRiservata/SchermataPrincipaleAttoreAutenticato'),
        props: true,
        redirect: { name: process.env.VUE_APP_ROUTER_NOME_ELENCO_ATTORI },
        children: [
          {
            path: '',                                                     // percorso default per area riservata
            alias: process.env.VUE_APP_ROUTER_PATH_AREA_RISERVATA,
            name: process.env.VUE_APP_ROUTER_NOME_ELENCO_ATTORI,
            component: () => import('../components/attori/ElencoAttori')
          },
          {
            path: process.env.VUE_APP_ROUTER_PATH_SCHEDA_UN_ATTORE + '/:' +
                    process.env.VUE_APP_ROUTER_PARAMETRO_ID_ATTORE,
            name: process.env.VUE_APP_ROUTER_NOME_SCHEDA_UN_ATTORE,
            component: () => import('../components/attori/SchedaDiUnAttore'),
            props: true,
            meta: {
              [metaProps_enum.requiresInfoAttore]: true  // questa route richiede di conoscere le informazioni sull'attore di cui mostrare la scheda
            },
            children: [
              {
                path: process.env.VUE_APP_ROUTER_PATH_CARICAMENTO_DOCUMENTI,
                name: process.env.VUE_APP_ROUTER_NOME_CARICAMENTO_DOCUMENTI,
                component: () => import('../components/attori/uploader/CaricamentoDocumentoPerConsumer')
              },
              {
                path: process.env.VUE_APP_ROUTER_PATH_TABELLA_DOCUMENTI,
                alias: '',  // percorso default
                name: process.env.VUE_APP_ROUTER_NOME_TABELLA_DOCUMENTI,
                component: () => import('../components/attori/TabellaDocumenti')
              }
            ]
          }
        ]
      }

    ],
  }

]

// noinspection JSCheckFunctionSignatures // corretto
const router = createRouter({
  history: createWebHashHistory(),
  routes: routes
});


// --------- Gestione del routing ---------

router.beforeEach((routeDestinazione, routeProvenienza, next) => {

  if( routeDestinazione.matched.some(route => route.meta[metaProps_enum.requiresNonAuth]) ){
    // SE route richiede di non essere autenticato, allora logout
    eliminaInfoAutenticazione();
  }

  if( routeDestinazione.matched.some(route => route.meta[metaProps_enum.requiresAuth]) ) {
    // Gestione instradamento per route che richiede autenticazione


    verificaAutenticazione(routeDestinazione)
        .then( isUtenteAutenticato => isUtenteAutenticato ? Promise.resolve() : Promise.reject() )
        .catch( () => router.push(router.creaRouteAutenticazione()) )  // non autenticato
        .then( () => {                                                 // autenticato

          routeDestinazione.params[process.env.VUE_APP_ROUTER_PARAMETRO_IS_UTENTE_AUTENTICATO] = "true";  // salvata come String

          if (routeDestinazione.matched.some(route => route.meta[metaProps_enum.requiresInfoAttore])) {

            // Promise chain per (eventualmente) recuperare le informazioni mancanti
            ( () => {

              if ( !routeDestinazione.params[process.env.VUE_APP_ROUTER_PARAMETRO_ID_ATTORE] ) {

                // Se qui, nella route manca il parametro con l'id dell'attore di cui sono richieste le informazioni
                console.error("Identificativo dell'attore non presente, selezionare un utente dall'elenco.");
                return Promise.reject();

              } else {
                const tipoAttoreTarget = routeDestinazione.params[process.env.VUE_APP_ROUTER_PARAMETRO_TIPO_ATTORE_CUI_SCHEDA_SI_RIFERISCE];
                if ( tipoAttoreTarget ) {
                  // Se qui, nella route manca il tipo attore "target" (per mostrare scheda di un attore)
                  return getTipoAttoreTarget(routeDestinazione.params[process.env.VUE_APP_ROUTER_PARAMETRO_ID_ATTORE]);
                } else {
                  return Promise.resolve(tipoAttoreTarget);
                }
              }

            })()
                .then( tipoAttoreTarget =>
                    routeDestinazione.params[process.env.VUE_APP_ROUTER_PARAMETRO_TIPO_ATTORE_CUI_SCHEDA_SI_RIFERISCE] = String(tipoAttoreTarget)
                )
                .then( () => {
                  const proprietaAttoreTarget = routeDestinazione.params[process.env.VUE_APP_ROUTER_PARAMETRO_PROPRIETA_ATTORE];
                  if (!proprietaAttoreTarget) {
                    // Se qui, nella route mancano le proprieta dell'attore "target"
                    return getProprietaAttoreTarget(routeDestinazione.params[process.env.VUE_APP_ROUTER_PARAMETRO_ID_ATTORE])
                  } else {
                    return Promise.resolve(proprietaAttoreTarget);
                  }
                })
                .then(proprietaAttoreTarget => {
                  routeDestinazione.params[process.env.VUE_APP_ROUTER_PARAMETRO_PROPRIETA_ATTORE] =
                      JSON.stringify(proprietaAttoreTarget);
                })
                .then( next )
                .catch(errore => {
                  console.error(errore);
                  return router.push({name: process.env.VUE_APP_ROUTER_NOME_ELENCO_ATTORI}); // rimanda ad elenco attori
                });

          } else {
            next();
          }

        })

  } else  {
    // SE route non richiede autorizzazione, ALLORA instrada senza problemi
    next();
  }

});

/** Crea una route per la pagina di autenticazione dell'utente.
 */
router.creaRouteAutenticazione = () => {
  return {
    name: process.env.VUE_APP_ROUTER_NOME_ROUTE_LOGIN // redirect a login
  }
}

/** Si occupa del redirect verso la pagina di autenticazione.
 * Non interviene se si è già nella pagina di autenticazione.*/
router.redirectVersoPaginaAutenticazione = async () => {

  if( router.currentRoute.value.path !== process.env.VUE_APP_ROUTER_PATH_LOGIN          &&
      router.currentRoute.value.path !== process.env.VUE_APP_ROUTER_AUTENTICAZIONE_PATH    ) {
    console.log( "Redirection a pagina di autenticazione." );
    await router.push(router.creaRouteAutenticazione()) ;
  }

}


export default router;