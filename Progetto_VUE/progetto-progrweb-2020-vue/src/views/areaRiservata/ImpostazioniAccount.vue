<template>
  <h2>Impostazioni account</h2>

  <FormConCsrfToken
      :valoreQualsiasiPerAggiornareIComponenteSeModificato="valoreQualsiasiPerAggiornareIlComponenteSeModificato"
      :id="idForm_modificaInformazioniAttore"
      @change="isFormModificato = true"
      @submit="modificaInformazioni"
      @csrf-token-ricevuto="aggiornaCsrfToken($event)"><!-- TODO : è necessario che questo evento venga raccolta dal padre? Dovrebbe andare direttamente in area riservata perché è li il <router-view> -->

    <small>Riempire solo i campi da aggiornare.</small>

    <p>
      <label>Nuovo nominativo
        <input type="text"
               v-model="nuovoNominativo"
               autocomplete="on"
               maxlength="100">
      </label>
    </p>

    <p>
      <label>Nuova email
        <input type="text"
               v-model="nuovaEmail"
               autocomplete="on"
               maxlength="100"
               placeholder="xxxxxx@example.com"
               :pattern="REGEX_EMAIL">
      </label>
    </p>

    <fieldset v-if="!isConsumer()">

      <p>Modifica della password
        <label>Vecchia password
          <input type="password"
                 v-model="vecchiaPassword"
                 maxlength="100"
                 autocomplete="current-password">
        </label>
        <label>Nuova password
          <input type="password"
                 v-model="nuovaPassword"
                 maxlength="100"
                 autocomplete="new-password">
        </label>
        <label>Conferma nuova password
          <input type="password"
                 v-model="confermaNuovaPassword"
                 maxlength="100"
                 autocomplete="new-password">
        </label>
      </p>

    </fieldset>

    <p v-if="isUploader()">Modifica immagine logo
      <label>Nuovo logo
        <input type="file"
               @change="isFileLogoCaricato=true"
               maxlength="100"><!-- TODO : testare -->
      </label>
    </p>

    <input type="submit" value="Applica modifiche">
    <input type="reset" value="Reset form">

  </FormConCsrfToken>

  <button @click="tornaAdAreaRiservata">Chiudi</button>

</template>

<script>
import FormConCsrfToken from "../../components/layout/FormConCsrfToken";
import {richiestaPostConFile} from "../../utils/http";
import {setTokenAutenticazione} from "../../utils/autenticazione";
export default {
  name: "ImpostazioniAccount",
  components: {FormConCsrfToken},
  emits: [

    /** Evento generato se si riceve un nuovo token dal server.
     * Contiene il nuovo valore del token.*/
    'csrf-token-ricevuto',

    /** Evento generato se viene modificato il nominativo
     * dell'attore attualmente autenticato. Contiene il
     * nuovo nominativo come valore*/
    'nominativo-attore-modificato'

  ],
  props: ['tipoAttoreAutenticato','nomeUtenteAutenticato', 'csrfToken'],
  data() {
    return {

      /** RegEx email.*/
      REGEX_EMAIL: process.env.VUE_APP_REGEX_EMAIL,

      /** Valore dell'attributo "id" del form per il caricamento dei documenti.*/
      idForm_modificaInformazioniAttore: "modificaInformazioniAttore",

      // Dati per i campi di input
      nuovoNominativo       : "",
      nuovaEmail            : "",
      vecchiaPassword       : "",
      nuovaPassword         : "",
      confermaNuovaPassword : "",
      isFileLogoCaricato    : false,
      isFormModificato      : false,  // diventa true se l'utente modifica il form

      // NOMI DEI PARAMETRI ATTESI DAL SERVER // TODO : variabili d'ambiente sia per client sia per server
      nomeParametro_nominativo     : "nuovoNominativo",
      nomeParametro_email          : "nuovaEmail",
      nomeParametro_vecchiaPassword: "vecchiaPassword",
      nomeParametro_nuovaPassword  : "nuovaPassword",
      nomeParametro_nuovoLogo      : "nuovoLogo",



      // Chiave per forzare aggiornamento dei componenti (se cambia la chiave
      // c'è un listener nel componente da aggiornare che lo fa aggiornare)
      valoreQualsiasiPerAggiornareIlComponenteSeModificato: 0,

      csrfToken_wrapper: this.csrfToken

    }
  },
  methods: {

    /** Restituisce true se è un uploader, false altrimenti.*/
    isUploader() {
      return this.tipoAttoreAutenticato ===
          process.env.VUE_APP_TIPO_UTENTE_AUTENTICATO_UPLOADER;
    },

    /** Restituisce true se è un consumer, false altrimenti.*/
    isConsumer() {
      return this.tipoAttoreAutenticato ===
          process.env.VUE_APP_TIPO_UTENTE_AUTENTICATO_CONSUMER;
    },

    aggiornaCsrfToken( nuovoValore ) {
      this.csrfToken_wrapper = nuovoValore,
      this.$emit('csrf-token-ricevuto', nuovoValore);
    },

    modificaInformazioni() {

      if( this.isFormModificato ) {

        const informazioniCheSarannoModificate = [];

        // Costruzione dei parametri da inviare con i soli campi del form che sono stati modificati
        const formData = new FormData();

        formData.append(process.env.VUE_APP_FORM_CSRF_INPUT_FIELD_NAME, this.csrfToken_wrapper);

        if( this.isFileLogoCaricato ) {
          const inputLogo = document.querySelector('#' + this.idForm_modificaInformazioniAttore + ' input[type=file]').files[0];
          formData.append( this.nomeParametro_nuovoLogo, inputLogo );
          informazioniCheSarannoModificate.push("il logo");
        }

        if( this.nuovoNominativo ) {
          formData.append( this.nomeParametro_nominativo, this.nuovoNominativo );
          informazioniCheSarannoModificate.push("il nominativo (in \"" + this.nuovoNominativo + "\")");
        }

        if( this.nuovaEmail ) {
          if( RegExp(this.REGEX_EMAIL).test(this.nuovaEmail) ) {
            formData.append( this.nomeParametro_email, this.nuovaEmail );
            informazioniCheSarannoModificate.push("l'indirizzo email (in \"" + this.nuovaEmail + "\")");
          } else {
            alert("La nuova email fornita non sembra avere un formato valido.");
          }
        }

        if( this.nuovaPassword && this.nuovaPassword === this.confermaNuovaPassword ) {
          formData.append( this.nomeParametro_vecchiaPassword, this.vecchiaPassword );
          formData.append( this.nomeParametro_nuovaPassword, this.nuovaPassword );
          informazioniCheSarannoModificate.push("la password");
        } else if( this.nuovaPassword || this.confermaNuovaPassword ) {
          alert("Per modificare la password, la nuova password con quella inserita nel campo di conferma.");
        }

        if( informazioniCheSarannoModificate.length>0 ) {

          let stringaInformativaDelleModifiche = "Stanno per essere modificate le seguenti informazioni: " +
              informazioniCheSarannoModificate.join(", ").replace(/,([^,]+)$/,' e$1') + ".";
          const confermaModifiche = confirm(stringaInformativaDelleModifiche);

          if (confermaModifiche) {

            (() => { // Richiesta di modifica al server

              // Salvo la variabile nella funzione perché, trattandosi di richieste
              // asincrone, intanto il valore della variabile potrebbe cambiare
              // (questo è il motivo di utilizzo di una funzione: closure)
              const nominativoModificato = this.nuovoNominativo;

              richiestaPostConFile(process.env.VUE_APP_URL_MODIFICA_INFORMAZIONI_ATTORI, formData)
                  .then(nuovoTokenAutenticazione => {  // Nella risposta c'è il nuovo token di autenticazione
                    setTokenAutenticazione(nuovoTokenAutenticazione);
                    this.valoreQualsiasiPerAggiornareIlComponenteSeModificato = 1 - this.valoreQualsiasiPerAggiornareIlComponenteSeModificato;  // serve per aggiornare il componente
                    if (nominativoModificato) {  // truthy se è stato modificato
                      // Avvisa se è stato modificato il nominativo
                      this.$emit('nominativo-attore-modificato', nominativoModificato);
                    }
                    alert("Modifiche salvate!");
                  })
                  .catch(errore => {
                    alert("Si è verificato un errore durante l'aggiornamento delle informazioni. " + errore.data );
                    console.error(errore);
                  })

            })();

          }
        }


        // Reset dei campi input
        document.getElementById(this.idForm_modificaInformazioniAttore).reset();
        this.nuovoNominativo   = "";
        this.nuovaEmail        = "";
        this.vecchiaPassword   = "";
        this.nuovaPassword     = "";
        this.isFileLogoCaricato= false;
        this.isFormModificato  = false

      }

    },

    tornaAdAreaRiservata() {
      this.$router.push({path: process.env.VUE_APP_ROUTER_PATH_AREA_RISERVATA});
    }

  },
  watch: {
    csrfToken : {
      immediate: true,
      deep: true,
      handler(nuovoValore) {
        this.csrfToken_wrapper = nuovoValore;
      }
    }
  }
}

</script>

<style scoped>

</style>