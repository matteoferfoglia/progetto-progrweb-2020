<template id="app">  <!-- TODO Questa pagina è da adattare al progetto (è quella creata da Vue) -->

  <AreaRiservata v-if="isUtenteAutenticato()"></AreaRiservata>
  <Login v-else></Login>

  <router-view></router-view>

</template>

<script>

import Login from './components/Login'
import AreaRiservata from "./components/AreaRiservata";
import {isAutenticato} from "./utils/utils";

export default {
  name: 'App',
  components: {
    AreaRiservata,
    Login
  },
  methods: {
    isUtenteAutenticato(){
      return isAutenticato();
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
