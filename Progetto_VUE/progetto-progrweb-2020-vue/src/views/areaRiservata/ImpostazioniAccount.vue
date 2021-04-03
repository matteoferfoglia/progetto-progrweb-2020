<template>
  <h2 class="card-header card-title">Impostazioni account</h2>

  <article class="card">
    <h3>Informazioni personali</h3>
    <FormCampiAttore :flag_mostrareLabelCampiInput="true"
                     :isQuestoFormRiferitoAConsumer="isConsumer()"
                     :username_readOnly="true"
                     :tuttiICampi_readOnly="true"
                     :username="username"
                     :nominativo="nominativo"
                     :email="email">
    </FormCampiAttore>
  </article>

  <FormConCsrfToken
      :valoreQualsiasiPerAggiornareIComponenteSeModificato="valoreQualsiasiPerAggiornareIlComponenteSeModificato"
      :id="idForm_modificaInformazioniAttore"
      class="card"
      @change="isFormModificato = true"
      @submit="modificaInformazioni"
      @csrf-token-ricevuto="aggiornaCsrfToken($event)">

    <h3>Modifica informazioni personali</h3>

    <p>
      Da questa pagina è possibile modificare le informazioni personali.
      Riempire solo i campi da aggiornare.
    </p>

    <fieldset class="informazioni-personali">
      <legend>Modifica informazioni personali</legend>
      <label>Nuovo nominativo
        <input type="text"
               v-model="nuovoNominativo"
               class="form-control"
               autocomplete="on"
               maxlength="100">
      </label>

      <label>Nuova email
        <input type="text"
               v-model="nuovaEmail"
               class="form-control"
               autocomplete="on"
               maxlength="100"
               placeholder="xxxxxx@example.com"
               :pattern="REGEX_EMAIL">
      </label>
    </fieldset>

<!--    <fieldset v-if="!isConsumer()"> I requisiti non specificano se un Consumer possa modificare la sua password -->
    <fieldset>
      <legend>Modifica password</legend>
      <label>Vecchia password
        <input type="password"
               v-model="vecchiaPassword"
               class="form-control"
               maxlength="100"
               autocomplete="off">
      </label>
      <label>Nuova password
        <input type="password"
               v-model="nuovaPassword"
               class="form-control"
               maxlength="100"
               autocomplete="new-password">
      </label>
      <label>Conferma nuova password
        <input type="password"
               v-model="confermaNuovaPassword"
               class="form-control"
               maxlength="100"
               autocomplete="new-password">
      </label>
    </fieldset>

    <fieldset class="modifica-logo" v-if="isUploader()">
      <legend>Modifica immagine logo</legend>
      <label>Nuovo logo
        <input type="file"
               @change="isFileLogoCaricato=true"
               class="form-control-file">
      </label>
    </fieldset>

    <div class="d-flex justify-content-around flex-row-reverse flex-wrap mt-3">
      <button type="submit" class="check-icon btn btn-info d-block order-0"> Applica modifiche</button>
      <button @click="tornaAdAreaRiservata" class="x-circle btn btn-dark order-1"> Chiudi</button>
      <button type="reset" class="reset btn btn-secondary"> Reset form</button>
    </div>

  </FormConCsrfToken>

</template>

<script>
import FormConCsrfToken from "../../components/layout/FormConCsrfToken";
import {richiestaPostConFile} from "@/utils/http";
import {
  getEmailAttoreAttualmenteAutenticato,
  getNomeAttoreAttualmenteAutenticato,
  getUsernameAttoreAttualmenteAutenticato,
  setTokenAutenticazione
} from "@/utils/autenticazione";
import FormCampiAttore from "@/components/layout/FormCampiAttore";
export default {
  name: "ImpostazioniAccount",
  components: {FormCampiAttore, FormConCsrfToken},
  emits: [

    /** Evento generato se si riceve un nuovo token dal server.
     * Contiene il nuovo valore del token.*/
    'csrf-token-ricevuto',

    /** Evento generato se viene modificato il nominativo
     * dell'attore attualmente autenticato. Contiene il
     * nuovo nominativo come valore*/
    'nominativo-attore-modificato',

      /** Evento generato se viene modificato il logo
       * dell'attore attualmente autenticato*/
    'logo-attore-modificato'

],
  props: ['tipoAttoreAutenticato','nomeUtenteAutenticato', 'csrfToken'],
  data() {
    return {

      /** RegEx email.*/
      REGEX_EMAIL: process.env.VUE_APP_REGEX_EMAIL,

      /** Valore dell'attributo "id" del form per il caricamento dei documenti.*/
      idForm_modificaInformazioniAttore: "modificaInformazioniAttore",

      /** Email dell'utente attualmente autenticato */
      emailUtenteAutenticato: "",

      // Dati dell'utente (inizializzati in created)
      nominativo: "",
      username: "",
      email: "",

      // Dati per i campi di input
      nuovoNominativo       : "",
      nuovaEmail            : "",
      vecchiaPassword       : "",
      nuovaPassword         : "",
      confermaNuovaPassword : "",
      isFileLogoCaricato    : false,
      isFormModificato      : false,  // diventa true se l'utente modifica il form

      // NOMI DEI PARAMETRI ATTESI DAL SERVER
      nomeParametro_nominativo     : process.env.VUE_APP_FORM_NOMINATIVO_INPUT_FIELD_NAME,
      nomeParametro_email          : process.env.VUE_APP_FORM_EMAIL_INPUT_FIELD_NAME,
      nomeParametro_vecchiaPassword: process.env.VUE_APP_FORM_VECCHIA_PASSWORD_INPUT_FIELD_NAME,
      nomeParametro_nuovaPassword  : process.env.VUE_APP_FORM_PASSWORD_INPUT_FIELD_NAME,
      nomeParametro_nuovoLogo      : process.env.VUE_APP_FORM_LOGO_UPLOADER_INPUT_FIELD_NAME,



      // Chiave per forzare aggiornamento dei componenti (se cambia la chiave
      // c'è un listener nel componente da aggiornare che lo fa aggiornare)
      valoreQualsiasiPerAggiornareIlComponenteSeModificato: 0,

      csrfToken_wrapper: this.csrfToken

    }
  },
  created() {

    this.setupDatiComponente();

  },
  methods: {

    /** Setup del componente: carica i dati da mostrare. */
    setupDatiComponente() {
      this.nominativo = getNomeAttoreAttualmenteAutenticato();
      this.email      = getEmailAttoreAttualmenteAutenticato();
      this.username   = getUsernameAttoreAttualmenteAutenticato();
    },

    /** Restituisce true se è un uploader, false altrimenti.*/
    isUploader() {
      return this.tipoAttoreAutenticato ===
          process.env.VUE_APP_TIPO_UTENTE__UPLOADER;
    },

    /** Restituisce true se è un consumer, false altrimenti.*/
    isConsumer() {
      return this.tipoAttoreAutenticato ===
          process.env.VUE_APP_TIPO_UTENTE__CONSUMER;
    },

    aggiornaCsrfToken( nuovoValore ) {
      this.csrfToken_wrapper = nuovoValore;
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
          alert("Per modificare la password, la nuova password deve coincidere con quella inserita nel campo di conferma.");
        }

        if( informazioniCheSarannoModificate.length>0 ) {

          let stringaInformativaDelleModifiche = "Stanno per essere modificate le seguenti informazioni: " +
              informazioniCheSarannoModificate.join(", ").replace(/,([^,]+)$/,' e$1') + ".";
                                                          // $1 è il primo parenthesized substring match.
                                                          // Sostituisce ultima ', ' con ' e '

          const confermaModifiche = confirm(stringaInformativaDelleModifiche);

          if (confermaModifiche) {

            (async () => { // Richiesta di modifica al server

              // Salvo la variabile nella funzione perché, trattandosi di richieste
              // asincrone, intanto il valore della variabile potrebbe cambiare
              // (questo è il motivo di utilizzo di una funzione: closure)
              const nominativoModificato = this.nuovoNominativo;
              const isLogoCaricato = this.isFileLogoCaricato;

              await richiestaPostConFile(process.env.VUE_APP_URL_MODIFICA_INFORMAZIONI_ATTORI, formData)
                  .then(nuovoTokenAutenticazione => {  // Nella risposta c'è il nuovo token di autenticazione
                    setTokenAutenticazione(nuovoTokenAutenticazione);
                    this.valoreQualsiasiPerAggiornareIlComponenteSeModificato = 1 - this.valoreQualsiasiPerAggiornareIlComponenteSeModificato;  // vedere documentazione
                    this.setupDatiComponente();
                    if (nominativoModificato) {  // truthy se è stato modificato
                      // Avvisa se è stato modificato il nominativo
                      this.$emit('nominativo-attore-modificato', nominativoModificato);
                    }
                    if(isLogoCaricato) {
                      // SE è stato modificato il logo
                      this.$emit('logo-attore-modificato');
                      this.isFileLogoCaricato = false;  // ripristina il valore iniziale
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
        this.nuovoNominativo       = "";
        this.nuovaEmail            = "";
        this.vecchiaPassword       = "";
        this.nuovaPassword         = "";
        this.confermaNuovaPassword = "";
        this.isFileLogoCaricato    = false;
        this.isFormModificato      = false

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
fieldset {
  display: flex;
  justify-content: flex-start;
  flex-wrap: wrap;
  border: 2px solid rgba(0,0,0,.25);
  border-radius: .25rem;
  margin: 1% 0;
}
legend {
  width: unset;
  margin: 0 1%;
  padding: 0 2px;
}
fieldset label {
  padding: 0 2%;
}
fieldset.informazioni-personali p {
  min-width: 40%;
}
fieldset.modifica-logo legend {
  margin-bottom: 0;
}

</style>