<template id="app">  <!-- TODO Questa pagina è da adattare al progetto (è quella creata da Vue) -->
  <AreaRiservata v-if="isUtenteAutenticato()"></AreaRiservata>
  <Login v-else></Login>

  <router-view></router-view>

</template>

<script>

import Login from '@/components/Login'
import AreaRiservata from "@/components/AreaRiservata";

export default {
  name: 'App',
  components: {
    AreaRiservata,
    Login
  },
  methods: {
    isUtenteAutenticato(){
      //TODO verificare se l'utente è autenticato
      return false;
    }
  }
}

</script>


<style>
#app {
}

#nav a.router-link-exact-active {
  color: #42b983;
}
</style>
