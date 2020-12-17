<template>
  <h2>Login > Accedi</h2>
  <form :action="`${LOGIN_SERVER_URL}`" method="post">
    <label>Username<input type="text" :name="`${USERNAME_INPUT_FIELD_NAME}`" autocomplete="off" required autofocus></label>
    <label>Password<input type="password" :name="`${PASSWORD_INPUT_FIELD_NAME}`" autocomplete="off" required></label>
    <input type="submit" value="Login">
  </form>
</template>

<!--suppress JSUnusedGlobalSymbols -->
<script>

export default {
  name: 'LoginUtenteGiaRegistrato',
  data() {
    return {
      LOGIN_SERVER_URL: process.env.VUE_APP_LOGIN_SERVER_URL,
      USERNAME_INPUT_FIELD_NAME: process.env.VUE_APP_LOGIN_USERNAME_INPUT_FIELD_NAME,
      PASSWORD_INPUT_FIELD_NAME: process.env.VUE_APP_LOGIN_PASSWORD_INPUT_FIELD_NAME
    }
  }
}

</script>

<style>
</style>
