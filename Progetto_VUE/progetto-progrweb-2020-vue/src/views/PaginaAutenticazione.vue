<template>

  <div>
    <nav id="navAutenticazione">
      <ul class="d-flex justify-content-center">
        <li class="nav-item"><router-link :to="PERCORSO_LOGIN" class="nav-link">Login</router-link></li>
        <li class="nav-item"><router-link :to="PERCORSO_REGISTRAZIONE_CONSUMER" class="nav-link">Registrazione Consumer</router-link></li>
      </ul>
    </nav>
    <router-view @csrf-token-ricevuto="$emit('csrf-token-ricevuto',$event)"
                 @login="$emit('login', $event)" />
  </div>

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

<style scoped>
div {
  max-width: 500px;
  margin: 5% auto;
}
#navAutenticazione>ul>li {
  list-style-type: none;
  margin: 0 1rem;
}
#navAutenticazione a.router-link-exact-active {
  color: #42b983;
  font-size: large;
  font-weight: bold;
}
#navAutenticazione .nav-link:hover {
  text-decoration: underline;
}
</style>