<!--suppress ALL -->
<template>

  <header>
    <router-link :to="PERCOROS_ROOT">
      <img class="logo" :src="PERCORSO_LOGO" alt=""/>
      {{NOME_APPLICAZIONE}}
    </router-link>
    <nav>
      <ul>
        <li><router-link :to="PERCORSO_LOGIN">Login</router-link></li>
        <li><router-link :to="PERCORSO_REGISTRAZIONE_CONSUMER">Registrazione Consumer</router-link></li>
      </ul>
    </nav>

    <h1>Benvenuto nella pagina di login</h1>
  </header>

</template>

<script>

export default {
  name: 'Login',
  data() {
    return {
      NOME_APPLICAZIONE: process.env.VUE_APP_NOME_APPLICAZIONE,
      PERCORSO_LOGO: process.env.VUE_APP_PERCORSO_LOGO,
      PERCOROS_ROOT: process.env.VUE_APP_ROUTER_ROOT_PATH,
      PERCORSO_LOGIN: process.env.VUE_APP_ROUTER_LOGIN_PATH,
      PERCORSO_REGISTRAZIONE_CONSUMER: process.env.VUE_APP_ROUTER_REGISTRAZIONE_CONSUMER_PATH
    }
  }
}
</script>

<style>
</style>
