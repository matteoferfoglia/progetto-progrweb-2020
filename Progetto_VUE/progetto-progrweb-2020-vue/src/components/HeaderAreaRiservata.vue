<template>
  <header>

    <nav>
      <router-link :to="PERCORSO_ROOT_APPLICAZIONE">
        <img class="logo" :src="URL_LOGO_APPLICAZIONE" alt=""/>
        {{NOME_APPLICAZIONE}}
      </router-link>

      <div id="containerOpzioniUtente">

        <button id="buttonOpzioniUtente">
          <svg width="1em" height="1em" viewBox="0 0 16 16" class="bi bi-person-fill" fill="currentColor" xmlns="http://www.w3.org/2000/svg">
            <path fill-rule="evenodd" d="M3 14s-1 0-1-1 1-4 6-4 6 3 6 4-1 1-1 1H3zm5-6a3 3 0 1 0 0-6 3 3 0 0 0 0 6z"></path>
          </svg>
          {{ nomeUtenteAutenticato }}
        </button>

        <ul id="elencoOpzioniUtente">
          <li>
            <router-link to="{path:' '}"> <!-- TODO -->
              <svg width="1em" height="1em" viewBox="0 0 16 16" class="bi bi-gear-fill" fill="currentColor" xmlns="http://www.w3.org/2000/svg">
                <path fill-rule="evenodd" d="M9.405 1.05c-.413-1.4-2.397-1.4-2.81 0l-.1.34a1.464 1.464 0 0 1-2.105.872l-.31-.17c-1.283-.698-2.686.705-1.987 1.987l.169.311c.446.82.023 1.841-.872 2.105l-.34.1c-1.4.413-1.4 2.397 0 2.81l.34.1a1.464 1.464 0 0 1 .872 2.105l-.17.31c-.698 1.283.705 2.686 1.987 1.987l.311-.169a1.464 1.464 0 0 1 2.105.872l.1.34c.413 1.4 2.397 1.4 2.81 0l.1-.34a1.464 1.464 0 0 1 2.105-.872l.31.17c1.283.698 2.686-.705 1.987-1.987l-.169-.311a1.464 1.464 0 0 1 .872-2.105l.34-.1c1.4-.413 1.4-2.397 0-2.81l-.34-.1a1.464 1.464 0 0 1-.872-2.105l.17-.31c.698-1.283-.705-2.686-1.987-1.987l-.311.169a1.464 1.464 0 0 1-2.105-.872l-.1-.34zM8 10.93a2.929 2.929 0 1 0 0-5.86 2.929 2.929 0 0 0 0 5.858z"/>
              </svg>
              Impostazioni account
            </router-link>
          </li>
          <li>
            <FormConCsrfToken @csrf-token-ricevuto="$emit('csrf-token-modificato',$event)">
              <a @click.prevent="$emit('logout')">
                <svg width="1em" height="1em" viewBox="0 0 16 16" class="bi bi-door-open" fill="currentColor" xmlns="http://www.w3.org/2000/svg">
                  <path fill-rule="evenodd" d="M1 15.5a.5.5 0 0 1 .5-.5h13a.5.5 0 0 1 0 1h-13a.5.5 0 0 1-.5-.5zM11.5 2H11V1h.5A1.5 1.5 0 0 1 13 2.5V15h-1V2.5a.5.5 0 0 0-.5-.5z"/>
                  <path fill-rule="evenodd" d="M10.828.122A.5.5 0 0 1 11 .5V15h-1V1.077l-6 .857V15H3V1.5a.5.5 0 0 1 .43-.495l7-1a.5.5 0 0 1 .398.117z"/>
                  <path d="M8 9c0 .552.224 1 .5 1s.5-.448.5-1-.224-1-.5-1-.5.448-.5 1z"/>
                </svg>
                Logout
              </a>
            </FormConCsrfToken>
          </li>
        </ul>

      </div>

    </nav>


  </header>


</template>

<script>
import FormConCsrfToken from "./FormConCsrfToken";
export default {
  name: "HeaderAreaRiservata",
  components: {FormConCsrfToken},
  props: [
    "nomeUtenteAutenticato",
    "tipoUtenteAutenticato"
  ],
  data() {
    return {
      // Dati per header
      NOME_APPLICAZIONE          : process.env.VUE_APP_NOME_APPLICAZIONE,
      URL_LOGO_APPLICAZIONE      : process.env.VUE_APP_PERCORSO_LOGO,
      PERCORSO_ROOT_APPLICAZIONE : process.env.VUE_APP_ROUTER_ROOT_PATH,
    }
  }
}
</script>

<style scoped>

</style>