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
import {richiestaGet} from "../utils/http";


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
        path: process.env.VUE_APP_ROUTER_PATH_LOGIN,
        alias: process.env.VUE_APP_ROUTER_AUTENTICAZIONE_PATH,  // route default
        name: process.env.VUE_APP_ROUTER_NOME_ROUTE_LOGIN,
        component: () => import('../views/autenticazione/LoginUtenteGiaRegistrato')
      },
      {
        path: process.env.VUE_APP_ROUTER_PATH_REGISTRAZIONE_CONSUMER,
        component: () => import('../views/autenticazione/RegistrazioneNuovoConsumer'),  // lazy-loading
      }
    ]
  },
  {
    path: process.env.VUE_APP_ROUTER_PATH_AREA_RISERVATA,
    name: process.env.VUE_APP_ROUTER_NOME_COMPONENTE_AREA_RISERVATA,
    // TODO : children : suddividere in Administrator / Uploader / Consumer
    component: () => import('../components/AreaRiservata'),                // lazy-loading
    meta: {
      requiresAuth: true
    },
    children: [
      {
        path: process.env.VUE_APP_ROUTER_PATH_LISTA_DOCUMENTI_VISTA_DA_UPLOADER + "/:" +
            process.env.VUE_APP_ROUTER_PARAMETRO_ID_CONSUMER_DI_CUI_MOSTRARE_DOCUMENTI_PER_UPLOADER,
        name: process.env.VUE_APP_ROUTER_NOME_LISTA_DOCUMENTI_VISTA_DA_UPLOADER,
        component: () => import('../views/listaDocumenti/ListaDocumentiDiUnConsumerVistaDaUploader'),
        meta: {
          requiresAuth: true,  // TODO: require essere uploader
          requiresIdConsumer: true,   // per sapere la lista di documenti destinata a quale consumer
          requiresNomeConsumer: true, // per sapere il nome del consumer
        }
      },
      {
        path: process.env.VUE_APP_ROUTER_PATH_LISTA_DOCUMENTI_VISTA_DA_CONSUMER + "/:" +
            process.env.VUE_APP_ROUTER_PARAMETRO_ID_UPLOADER_DI_CUI_MOSTRARE_DOCUMENTI_PER_CONSUMER,
        name: process.env.VUE_APP_ROUTER_NOME_LISTA_DOCUMENTI_VISTA_DA_CONSUMER,
        component: () => import('../views/listaDocumenti/ListaDocumentiVistaDaConsumer'),
        meta: {
          requiresAuth: true,  // TODO: require essere consumer
          requiresIdUploader: true, // per sapere la lista di documenti proveniente da quale Uploader
          requiresLogoUploader: true// logo Uploader
        }
      }
    ]
  },
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

  if( routeDestinazione.matched.some(route => route.meta.requiresAuth) ) {
    // Gestione instradamento per route che richiede autenticazione

    verificaAutenticazione(routeDestinazione)

        .then( isUtenteAutenticato => {

          if(isUtenteAutenticato) {
            // Autenticato

            if( routeDestinazione.matched.some(route => route.meta.requiresIdUploader) ) {

              if (! routeDestinazione.params[process.env.VUE_APP_ROUTER_PARAMETRO_ID_UPLOADER_DI_CUI_MOSTRARE_DOCUMENTI_PER_CONSUMER]) {
                // Se qui, nella route manca il parametro dell'uploader
                console.error("Parametro " +
                    process.env.VUE_APP_ROUTER_PARAMETRO_ID_UPLOADER_DI_CUI_MOSTRARE_DOCUMENTI_PER_CONSUMER +
                    " mancante nella route.");
                next({path: process.env.VUE_APP_ROUTER_NOME_COMPONENTE_AREA_RISERVATA}); // rimanda ad area riservata
              } else if ( ! routeDestinazione.params[process.env.VUE_APP_ROUTER_PARAMETRO_LOGO_UPLOADER_DI_CUI_MOSTRARE_DOCUMENTI_PER_CONSUMER] ) {
                // Se qui, nella route manca il logo dell'uploader

                richiestaGet(process.env.VUE_APP_GET_LOGO_UPLOADER + "/" +
                    routeDestinazione.params[process.env.VUE_APP_ROUTER_PARAMETRO_ID_UPLOADER_DI_CUI_MOSTRARE_DOCUMENTI_PER_CONSUMER] ) // Richiede il logo dell'Uploader al server
                    .then( logoBase64 => {
                      routeDestinazione.params[process.env.VUE_APP_ROUTER_PARAMETRO_LOGO_UPLOADER_DI_CUI_MOSTRARE_DOCUMENTI_PER_CONSUMER] = logoBase64 ;
                      next();
                    })
                    .catch( errore => {
                      console.error(errore);
                      next({path: process.env.VUE_APP_ROUTER_NOME_COMPONENTE_AREA_RISERVATA}); // rimanda ad area riservata
                    });

              } else {
                next(); // instrada senza problemi se ci sono tutti i parametri
              }

            } else if ( routeDestinazione.matched.some(route => route.meta.requiresIdConsumer) ) {    // TODO : da verificare

              const idConsumer = routeDestinazione.params[ process.env.VUE_APP_ROUTER_PARAMETRO_ID_CONSUMER_DI_CUI_MOSTRARE_DOCUMENTI_PER_UPLOADER ];

              if( ! idConsumer ) {

                // Se qui, nella route manca il parametro del consumer
                console.error("Parametro " +
                    process.env.VUE_APP_ROUTER_PARAMETRO_ID_CONSUMER_DI_CUI_MOSTRARE_DOCUMENTI_PER_UPLOADER +
                    " mancante nella route.");
                next({path: process.env.VUE_APP_ROUTER_NOME_COMPONENTE_AREA_RISERVATA}); // rimanda ad area riservata

              } else {

                if( ! routeDestinazione.params[ process.env.VUE_APP_ROUTER_PARAMETRO_NOME_CONSUMER_DI_CUI_MOSTRARE_DOCUMENTI_PER_UPLOADER ] ) {

                  // Se qui, nella route manca il parametro del consumer
                  richiestaGet( process.env.VUE_APP_GET_NOME_CONSUMER + "/" + idConsumer )
                      .then( nomeConsumer => {
                        // Salva il nome del consumer ed instrada
                        routeDestinazione.params[ process.env.VUE_APP_ROUTER_PARAMETRO_NOME_CONSUMER_DI_CUI_MOSTRARE_DOCUMENTI_PER_UPLOADER ] = nomeConsumer;
                        next(routeDestinazione);
                      })
                      .catch( errore => {
                        console.error("Parametro " +
                          process.env.VUE_APP_ROUTER_PARAMETRO_NOME_CONSUMER_DI_CUI_MOSTRARE_DOCUMENTI_PER_UPLOADER +
                          " mancante nella route.\n" + errore );
                        next({path: process.env.VUE_APP_ROUTER_NOME_COMPONENTE_AREA_RISERVATA}); // rimanda ad area riservata
                      });

                } else {

                  next(); // instrada senza problemi se ci sono tutti i parametri
                }

              }


            } else if(routeProvenienza.name === process.env.VUE_APP_ROUTER_NOME_ROUTE_LOGIN                 &&
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

            next( router.creaRouteAutenticazioneConInfoRichiesta( routeDestinazione ) );

          }

        })
        .catch(error => {
          console.error(error);
          next({path: process.env.VUE_APP_ROUTER_AUTENTICAZIONE_PATH}); // rimanda ad autenticazione
        })

  } else  {
    // SE route non richiede autorizzazione, ALLORA instrada senza problemi
    next();

  }

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
      // memorizzo la route richiesta e la passo al componente di login così può fare redirect dopo il login a ciò che aveva richiesto
      [process.env.VUE_APP_ROUTER_PARAMETRO_FULLPATH_ROUTE_RICHIESTA_PRIMA]: routeRichiesta.fullPath,
      [process.env.VUE_APP_ROUTER_PARAMETRO_PARAMS_ROUTE_RICHIESTA_PRIMA]: JSON.stringify(routeRichiesta.params),
      // JSON perché devono essere stringhe (altrimenti gli oggetti annidati vengono passati come "[Object object]")

      [NOME_PROPERTY_MOTIVO_REDIRECTION_VERSO_LOGIN]: MOTIVO_REDIRECTION_SE_RICHIESTA_SENZA_AUTENTICAZIONE
    }
  }
}

/** Si occupa del redirect verso la pagina di autenticazione.
 * @param routeRichiesta per poter proseguire la navigazione
 * dopo l'autenticazione.*/
router.redirectVersoPaginaAutenticazione = routeRichiesta => {

  if( router.currentRoute.value.path === process.env.VUE_APP_ROUTER_PATH_LOGIN ||
      router.currentRoute.value.path ===  process.env.VUE_APP_ROUTER_AUTENTICAZIONE_PATH ) {
    // aggiorna la pagina se sei già in login
    console.log("Aggiornamento della pagina.");
    router.go(0);
  }

  router.push( router.creaRouteAutenticazioneConInfoRichiesta( routeRichiesta ) ) ;

}


export default router;