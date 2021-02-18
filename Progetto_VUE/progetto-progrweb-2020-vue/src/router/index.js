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
import {getProprietaAttoreTarget, getTipoAttoreTarget} from "../utils/richiesteInfoSuAttori";


const routes = [

  // Route default (se non trovate altre)
  {
    // Route non trovata (url invalido) (Fonte: https://router.vuejs.org/guide/essentials/redirect-and-alias.html#redirect)
    // todo : si potrebbe mostrare un alert prima di effettuare redirect
    path: '/:pathMatch(.*)*',
    props: true,
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
      requiresAuth: true
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
        children: [
          {
            path: '',                                                 // percorso default per area riservata    // TODO : nei percorsi di default children bisogna usare percorso vuoto ('') perché questo è il pah relativo (appeso in coda a quello del padre)
            alias: process.env.VUE_APP_ROUTER_PATH_AREA_RISERVATA,
            name: process.env.VUE_APP_ROUTER_NOME_ELENCO_ATTORI,
            component: () => import('../components/attori/ElencoAttori')
          },
          {
            path: process.env.VUE_APP_ROUTER_PATH_SCHEDA_UN_ATTORE + '/:' +
                    process.env.VUE_APP_ROUTER_PARAMETRO_ID_ATTORE,
            name: process.env.VUE_APP_ROUTER_NOME_SCHEDA_UN_ATTORE,
            // TODO : aggiungere controllo: prima di instradare verificare che ci siano le property e se non ci sono richiederle al server
            component: () => import('../components/attori/SchedaDiUnAttore'),
            props: true,
            meta: {
              requiresInfoAttore: true  // questa route richiede di conoscere le informazioni sull'attore di cui mostrare la scheda
            }
          }
        ]
      }

    ],
  }

]

const router = createRouter({
  history: createWebHashHistory(),
  routes: routes
});


// --------- Gestione del routing ---------

// Permette di gestire il caso "utente richiede risorsa senza autenticazione,
// lo mando alla pagina di login, poi lo rimando alla pagina che stava visitando"
const MOTIVO_REDIRECTION_SE_RICHIESTA_SENZA_AUTENTICAZIONE = "NON AUTENTICATO";
const NOME_PROPERTY_MOTIVO_REDIRECTION_VERSO_LOGIN = "motivoRedirectionVersoLogin";


router.beforeEach((routeDestinazione, routeProvenienza, next) => {

  // TODO : rivedere bene i percorsi di instradamento

  (async () => {
    // funzione asincrona

    if( routeDestinazione.matched.some(route => route.meta.requiresAuth) ) {
      // Gestione instradamento per route che richiede autenticazione

      await verificaAutenticazione(routeDestinazione) // TODO: verifica autenticazione dovrebbe basarsi sul token JWT (implementare metodo in autenticazione.js)
        .then( async isUtenteAutenticato => {

          if(isUtenteAutenticato) {
            // Autenticato
            routeDestinazione.params[process.env.VUE_APP_ROUTER_PARAMETRO_IS_UTENTE_AUTENTICATO] = "true";

            if (routeDestinazione.matched.some(route => route.meta.requiresInfoAttore)) {

                if (!routeDestinazione.params[process.env.VUE_APP_ROUTER_PARAMETRO_ID_ATTORE]) {
                  // Se qui, nella route manca il parametro con l'id dell'attore di cui sono richieste le informazioni
                  console.error("Identificativo dell'attore non presente, selezionare un utente dall'elenco.");
                  router.push({name: process.env.VUE_APP_ROUTER_NOME_ELENCO_ATTORI}); // rimanda ad elenco attori
                }

                if (!routeDestinazione.params[process.env.VUE_APP_ROUTER_PARAMETRO_TIPO_ATTORE_CUI_SCHEDA_SI_RIFERISCE]) {
                  // Se qui, nella route manca il tipo attore
                  await getTipoAttoreTarget(routeDestinazione.params[process.env.VUE_APP_ROUTER_PARAMETRO_ID_ATTORE])
                      .then(tipoAttoreTarget =>
                          routeDestinazione.params[process.env.VUE_APP_ROUTER_PARAMETRO_TIPO_ATTORE_CUI_SCHEDA_SI_RIFERISCE] = String(tipoAttoreTarget)
                      )
                      .catch(errore => {
                        console.error(errore);
                        router.push({name: process.env.VUE_APP_ROUTER_NOME_ELENCO_ATTORI}); // rimanda ad elenco attori
                      });
                }

                if (!routeDestinazione.params[process.env.VUE_APP_ROUTER_PARAMETRO_PROPRIETA_ATTORE]) {
                  // Se qui, nella route manca il tipo attore
                  await getProprietaAttoreTarget(routeDestinazione.params[process.env.VUE_APP_ROUTER_PARAMETRO_ID_ATTORE])
                      .then(propAttoreTarget => {
                        routeDestinazione.params[process.env.VUE_APP_ROUTER_PARAMETRO_PROPRIETA_ATTORE] =
                            JSON.stringify(propAttoreTarget);
                      })
                      .catch(errore => {
                        console.error(errore);
                        router.push({name: process.env.VUE_APP_ROUTER_NOME_ELENCO_ATTORI}); // rimanda ad elenco attori
                      });
                }

            }

          } else {
            // Non autenticato
            next( router.creaRouteAutenticazioneConInfoRichiesta( routeDestinazione ) );
          }

        })
        .then( () => {

          if (routeProvenienza.name === process.env.VUE_APP_ROUTER_NOME_ROUTE_LOGIN &&
              routeDestinazione.params[process.env.VUE_APP_ROUTER_PARAMETRO_PARAMS_ROUTE_RICHIESTA_PRIMA] && // verifico non nulla ne undefined
              routeDestinazione.params[NOME_PROPERTY_MOTIVO_REDIRECTION_VERSO_LOGIN] === MOTIVO_REDIRECTION_SE_RICHIESTA_SENZA_AUTENTICAZIONE) {
            // TODO : questa parte deve essere ricontrollata
            // Se qui: l'utente aveva chiesto una risorsa senza essere autenticato
            // ed era stato mandato al login, ora redirect alla pagina che stava usando
            // con tutti i parametri che aveva prima del redirect
            let parametriVecchiaRoute = routeDestinazione.params[process.env.VUE_APP_ROUTER_PARAMETRO_PARAMS_ROUTE_RICHIESTA_PRIMA];
            parametriVecchiaRoute = JSON.parse(parametriVecchiaRoute.substring(1, parametriVecchiaRoute.length - 1));  // TODO : testare correttezza
            // substring() rimuove "" aggiunte all'inizio ed alla fine
            const routeRichiestaPrima = {
              fullPath: routeDestinazione.params[process.env.VUE_APP_ROUTER_PARAMETRO_FULLPATH_ROUTE_RICHIESTA_PRIMA],
              params: parametriVecchiaRoute
            };
            next(routeRichiestaPrima);

          } else {
            next();
          }

        })
        .catch(error => {
          console.error(error);
          router.push({path: process.env.VUE_APP_ROUTER_AUTENTICAZIONE_PATH}); // rimanda ad autenticazione
        })

    } else  {
      // SE route non richiede autorizzazione, ALLORA instrada senza problemi
      next();

    }

  })()
      .catch( errore => {
        console.error(errore);
        router.go(0); // ricarica la pagina
      });

});

/** Crea una route per la pagina di autenticazione dell'utente,
 * salvandosi le informazioni per poter reindirizzare (dopo
 * l'autenticazione) l'utente alla route che aveva chiesto in
 * precedenza.
 * @param routeRichiesta
 */
router.creaRouteAutenticazioneConInfoRichiesta = routeRichiesta => {

  // TODO : verificare che funzioni come da aspettative

  return {
    name: process.env.VUE_APP_ROUTER_NOME_ROUTE_LOGIN, // redirect a login
    params: {

      [process.env.VUE_APP_ROUTER_PARAMETRO_IS_UTENTE_AUTENTICATO]: "false",  // TODO : migliorare questo sistema che permette al componente App.vue di capire se l'utente è autenticato, per poi informare Header

      // memorizzo la route richiesta e la passo al componente di login così può fare redirect dopo il login a ciò che aveva richiesto
      [process.env.VUE_APP_ROUTER_PARAMETRO_FULLPATH_ROUTE_RICHIESTA_PRIMA]: routeRichiesta.fullPath,
      [process.env.VUE_APP_ROUTER_PARAMETRO_PARAMS_ROUTE_RICHIESTA_PRIMA]: JSON.stringify(routeRichiesta.params),
      // JSON perché devono essere stringhe (altrimenti gli oggetti annidati vengono passati come "[Object object]")

      [NOME_PROPERTY_MOTIVO_REDIRECTION_VERSO_LOGIN]: MOTIVO_REDIRECTION_SE_RICHIESTA_SENZA_AUTENTICAZIONE
    }
  }
}

/** Si occupa del redirect verso la pagina di autenticazione.
 * Non interviene se si è già nella pagina di autenticazione.
 * @param routeRichiesta per poter proseguire la navigazione
 * dopo l'autenticazione.*/
router.redirectVersoPaginaAutenticazione = routeRichiesta => {

  if( router.currentRoute.value.path !== process.env.VUE_APP_ROUTER_PATH_LOGIN          &&
      router.currentRoute.value.path !== process.env.VUE_APP_ROUTER_AUTENTICAZIONE_PATH    ) {
    console.log( "Redirection a pagina di autenticazione." );
    router.push( router.creaRouteAutenticazioneConInfoRichiesta( routeRichiesta ) ) ;
  }

}


export default router;