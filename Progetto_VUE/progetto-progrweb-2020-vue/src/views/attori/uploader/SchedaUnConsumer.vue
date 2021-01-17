<template>

  <!-- Componente per mostrare ad un Uploader la scheda di un Consumer -->

  <router-link :to="{ path: process.env.VUE_APP_ROUTER_PATH_LISTA_DOCUMENTI_VISTA_DA_CONSUMER_SCARICATI_DI_UN_CONSUMER + '/' + consumer[0] }"
               :idConsumer                  ="consumer[0]"
               :nomeConsumer                ="consumer[1][NOME_PROP_NOME_CONSUMER]" >

    <ol>
      <li>{{ this.camelCaseToHumanReadable(NOME_PROP_NOME_CONSUMER)     }}: <span class="nome">{{ consumer[1][NOME_PROP_NOME_CONSUMER] }}     </span></li>
      <li>{{ this.camelCaseToHumanReadable(NOME_PROP_EMAIL_CONSUMER)    }}: <span class="nome">{{ consumer[1][NOME_PROP_EMAIL_CONSUMER] }}    </span></li>
      <li>{{ this.camelCaseToHumanReadable(NOME_PROP_USERNAME_CONSUMER) }}: <span class="nome">{{ consumer[1][NOME_PROP_USERNAME_CONSUMER] }} </span></li>
    </ol>

  </router-link>

</template>

<script>

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

  ]
}

</script>

<style scoped>

</style>