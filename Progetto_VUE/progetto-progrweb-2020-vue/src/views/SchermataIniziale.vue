<template>
  <AreaRiservata v-if="isUtenteAutenticato()"></AreaRiservata>
  <Autenticazione v-else>
    <LoginUtenteGiaRegistrato><!-- componente da mostrare di default --></LoginUtenteGiaRegistrato>
  </Autenticazione>
</template>

<script>

import Autenticazione from '../components/Autenticazione'
import AreaRiservata from "../components/AreaRiservata";
import {isAutenticato} from "../utils/utils";
import LoginUtenteGiaRegistrato from "./autenticazione/LoginUtenteGiaRegistrato";

export default {
  name: 'SchermataIniziale',
  components: {
    LoginUtenteGiaRegistrato,
    AreaRiservata,
    Autenticazione
  },
  methods: {
    isUtenteAutenticato(){
      return isAutenticato();
    }
  }
}

</script>

<style>
</style>