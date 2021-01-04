<template>

  <header>
    <router-link :to="PERCORSO_ROOT">
      <img class="logo" :src="PERCORSO_LOGO" alt=""/>
      {{NOME_APPLICAZIONE}}
    </router-link>
    <nav>
      <ul>
        <li><router-link :to="PERCORSO_LOGIN">Login</router-link></li>
        <li><router-link :to="PERCORSO_REGISTRAZIONE_CONSUMER">Registrazione Consumer</router-link></li>
      </ul>
    </nav>
  </header>
  <main>
    <h1>Benvenuto nella pagina di autenticazione</h1>
    <slot></slot>
    <router-view/> <!-- Nested router -->
  </main>
</template>

<script>

export default {
  name: 'Autenticazione',
  data() {
    return {
      NOME_APPLICAZIONE: process.env.VUE_APP_NOME_APPLICAZIONE,
      PERCORSO_LOGO: process.env.VUE_APP_PERCORSO_LOGO,
      PERCORSO_ROOT: process.env.VUE_APP_ROUTER_ROOT_PATH,
      PERCORSO_LOGIN: process.env.VUE_APP_ROUTER_LOGIN_PATH,
      PERCORSO_REGISTRAZIONE_CONSUMER: process.env.VUE_APP_ROUTER_REGISTRAZIONE_CONSUMER_PATH
    }
  }
}
</script>

<style>
main {
  /* TODO */
  height: 200px;  /* impostando altezza fissa, si evita la modifica estetica della pagina dopo aver ricevuto csrf token*/
}
</style>
