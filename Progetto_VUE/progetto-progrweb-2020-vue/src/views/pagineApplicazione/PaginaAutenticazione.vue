<template>

  <header>
    <h1>Benvenuto nella pagina di autenticazione</h1>
    <nav>
      <ul>
        <li><router-link :to="PERCORSO_LOGIN">Login</router-link></li>
        <li><router-link :to="PERCORSO_REGISTRAZIONE_CONSUMER">Registrazione Consumer</router-link></li>
      </ul>
    </nav>
  </header>
  <main>

    <router-view @csrf-token-ricevuto="$emit('csrf-token-ricevuto',$event)"
                 @login="$emit('login', $event)"  />

  </main>
</template>

<script>

export default {
  inheritAttrs: false,  // Fonte (warning when using dynamic components and custom-events): https://stackoverflow.com/a/65555712
  name: 'PaginaAutenticazione',
  emits: [

    /** Evento emesso quando viene modificato il token CSRF.*/
    'csrf-token-ricevuto',

    /** Evento emesso quando l'utente ha completato il login.*/
    'login'

  ],
  data() {
    return {
      PERCORSO_LOGIN: process.env.VUE_APP_ROUTER_PATH_LOGIN,
      PERCORSO_REGISTRAZIONE_CONSUMER: process.env.VUE_APP_ROUTER_PATH_REGISTRAZIONE_CONSUMER
    }
  }
}

</script>

<style>
</style>