<template>

  <!-- Componente per mostrare ad un Uploader la scheda di un Consumer -->

  <router-link :to="{ name: vueRouter_vistaDocumentiDelConsumer,
                      params: {
                        [vueRouter_idConsumer]:   consumer[0],
                        [vueRouter_nomeConsumer]: consumer[1][NOME_PROP_NOME_CONSUMER]
                      }
                    }" >

    <ul>
      <li>{{ camelCaseToHumanReadable(NOME_PROP_NOME_CONSUMER)     }}: <span class="nome">{{ consumer[1][NOME_PROP_NOME_CONSUMER] }}     </span></li>
      <li>{{ camelCaseToHumanReadable(NOME_PROP_EMAIL_CONSUMER)    }}: <span class="nome">{{ consumer[1][NOME_PROP_EMAIL_CONSUMER] }}    </span></li>
      <li>{{ camelCaseToHumanReadable(NOME_PROP_USERNAME_CONSUMER) }}: <span class="nome">{{ consumer[1][NOME_PROP_USERNAME_CONSUMER] }} </span></li>
    </ul>

  </router-link>

</template>

<script>

import {camelCaseToHumanReadable} from "../../../utils/utilitaGenerale";

export default {
  name: "SchedaUnConsumer",
  props: [

    /** Rappresentazione di un consumer:
     * consumer[0] è il suo identificativo,
     * consumer[1] è l'oggetto con tutte le sue properties.
     */
    "consumer",

    /** Nella rappresentazione di un Consumer, è il nome
     * della property con il suo nome.*/
    "NOME_PROP_NOME_CONSUMER",

    /** Nella rappresentazione di un Consumer, è il nome
     * della property con la sua email.*/
    "NOME_PROP_EMAIL_CONSUMER",

    /** Nella rappresentazione di un Consumer, è il nome
     * della property con il suo username.*/
    "NOME_PROP_USERNAME_CONSUMER",

  ],
  data() {
    return {

      // Parametri Vue-Router
      /** Nome della vista dei documenti caricati da questo Uploader per il Consumer specificato.*/
      vueRouter_vistaDocumentiDelConsumer: process.env.VUE_APP_ROUTER_NOME_LISTA_DOCUMENTI_VISTA_DA_UPLOADER,
      /** Identificativo del consumer di cui si vogliono visualizzare i documenti.*/
      vueRouter_idConsumer: process.env.VUE_APP_ROUTER_PARAMETRO_ID_CONSUMER_DI_CUI_MOSTRARE_DOCUMENTI_PER_UPLOADER,
      /** Nome del consumer di cui si vogliono visualizzare i documenti.*/
      vueRouter_nomeConsumer: process.env.VUE_APP_ROUTER_PARAMETRO_NOME_CONSUMER_DI_CUI_MOSTRARE_DOCUMENTI_PER_UPLOADER,

      // Copia della funzione importata, per usarla nel template
      camelCaseToHumanReadable: camelCaseToHumanReadable
    }
  }
}

</script>

<style scoped>

</style>