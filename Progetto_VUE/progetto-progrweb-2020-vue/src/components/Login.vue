<template>

  <header>
    <router-link to="/">
      <img class="logo" :src="`${PERCORSO_LOGO}`" alt=""/>
      {{NOME_APPLICAZIONE}}
    </router-link>
    <nav>
      <ul>
        <li><router-link to="/Login">Login</router-link></li>
        <li><router-link to="/Registrazione">Registrazione</router-link></li>
      </ul>
    </nav>

    <h1>Benvenuto nella pagina di login</h1>
  </header>

  <form :action="`${LOGIN_SERVER_URL}`" method="post">
    <label>Username<input type="text" name="username" autocomplete="off" required autofocus></label>
    <label>Password<input type="password" name="password" autocomplete="off" required autofocus></label>
    <input type="submit" value="Login">
  </form>

</template>

<script>

export default {
  name: 'Login',
  data() {
    return {
      NOME_APPLICAZIONE: process.env.VUE_APP_NOME_APPLICAZIONE,
      PERCORSO_LOGO: process.env.VUE_APP_PERCORSO_LOGO,
      LOGIN_SERVER_URL: process.env.VUE_APP_LOGIN_SERVER_URL
    }
  }
}
</script>

<style>
</style>
