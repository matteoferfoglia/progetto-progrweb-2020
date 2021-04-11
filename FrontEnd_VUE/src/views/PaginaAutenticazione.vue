<template>

  <nav id="navAutenticazione">
    <ul class="d-flex justify-content-center flex-wrap p-0 mt-5">
      <li class="nav-item text-center"><router-link :to="PERCORSO_LOGIN" class="nav-link">Login</router-link></li>
      <li class="nav-item text-center"><router-link :to="PERCORSO_REGISTRAZIONE_CONSUMER" class="nav-link">Registrazione Consumer</router-link></li>
    </ul>
  </nav>
  <router-view @csrf-token-ricevuto="$emit('csrf-token-ricevuto',$event)"
               @login="$emit('login', $event)" />

</template>

<script>

export default {
  inheritAttrs: false,
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
div.card-autenticazione {
  max-width: 500px;
  margin: 0 auto;
}
div.card-autenticazione label {
  width: 100%;
}
div.card-autenticazione form p {
  display: flex;
  justify-content: center;
  padding: 0 5%;
}
div.card-autenticazione div>input {
  width: 20rem;
  margin: 0 5%;
}
div.card-autenticazione label {
  width: 100%;
}
div.card-autenticazione input[type=submit] {
  width: 10rem;
}
#navAutenticazione>ul>li {
  list-style-type: none;
  margin: 0 1rem;
}
#navAutenticazione a.router-link-exact-active {
  font-size: large;
}
#navAutenticazione .nav-link:hover {
  text-decoration: underline;
}
</style>