<template>

  <FormConCsrfToken :id="idHtml"
                    @csrf-token-ricevuto="$emit('csrf-token-ricevuto', $event)"
                    @submit="$emit('submit')/*Deve essere propagato*/">
    <div class="d-flex justify-content-around flex-wrap align-items-center form-items-container">
      <p v-if="username!==null">
        <label :for="idUsername" v-if="flag_mostrareLabelCampiInput">{{ usernameCodFisc }}</label>
        <!--suppress HtmlFormInputWithoutLabel --><!-- La label è associata tramite :for al campo input-->
        <input type="text"
               v-model="username_wrapper"
               maxlength="100"
               :pattern="usernameCodFisc==='Codice fiscale' ? REGEX_CODICE_FISCALE : '^.*$'"
               :id="idUsername"
               :placeholder="usernameCodFisc"
               :readonly="username_readOnly_wrapper || tuttiICampi_readOnly_wrapper"
               class="form-control"
               :aria-label="usernameCodFisc"
               :required="!!tuttiICampi_required"
               autocomplete="on">
      </p>
      <p v-if="nominativo!==null">
        <label :for="idNominativo" v-if="flag_mostrareLabelCampiInput">Nominativo</label>
        <input type="text"
               v-model="nominativo_wrapper"
               :id="idNominativo"
               placeholder="Nominativo"
               maxlength="100"
               :readonly="tuttiICampi_readOnly_wrapper"
               class="form-control"
               aria-label="Nominativo"
               :required="!!tuttiICampi_required"
               autocomplete="on">
      </p>
      <p v-if="email!==null">
        <label :for="idEmail" v-if="flag_mostrareLabelCampiInput">Email</label>
        <span class="input-group">
          <input type="email"
                 v-model="email_wrapper"
                 :id="idEmail"
                 :pattern="REGEX_EMAIL"
                 placeholder="xxxxxx@example.com"
                 maxlength="100"
                 :readonly="tuttiICampi_readOnly_wrapper"
                 class="form-control"
                 aria-label="Email"
                 :required="!!tuttiICampi_required"
                 autocomplete="email">
          <a :href="'mailto:' + email_wrapper" v-if="!!email_wrapper" class="input-group-append btn btn-link"></a>
        </span>
      </p>
    </div>
    <div class="d-flex justify-content-around align-items-center form-row row">
      <slot>
        <!-- Qui possono essere aggiunti altri campi input / contenuti dal componente
             padre, in particolare bisognerebbe mettere <input type="submit"> -->
      </slot>
    </div>
  </FormConCsrfToken>

</template>

<script>
import FormConCsrfToken from "./FormConCsrfToken";
import {generaIdUnivoco, isStringaNonNullaNonVuota} from "../../utils/utilitaGenerale";
import {richiestaPost, richiestaPostConFile} from "../../utils/http";
export default {
  name: "FormCampiAttore",
  components: {FormConCsrfToken},
  emits: [

    /** Emesso quando, a causa di questo componente o di un suo
     * figlio, viene modificato il token CSRF.*/
    'csrf-token-ricevuto',

    /** Evento generato quando un form viene inviato al server.
     * Il valore associato a queto evento è la Promise restituita
     * da {@link richiestaPost} a seguito dell'invio.*/
    'dati-form-inviati',

    /** Evento generato cliccando submit.*/
    'submit',

    /** Evento generato quando i valori dei campi di input
     * vengono ripristinati ai valori indicati nelle props.*/
    'ripristina-valori-prop'
  ],
  props: [

    /** Flag: true se questo form si riferisce ad un consumer.*/
    "isQuestoFormRiferitoAConsumer",

    /** Flag: true se a seguito dell'invio dei dati i campi del
     * form devono essere resettati. Se non specificato è true
     * di default.*/
    "resetCampiInputDopoInvioForm",

    /** Flag: quando diventa true nel componente padre, i campi
     * del form vengono ripristinati ai valori nelle prop.*/
    "ripristinaValoriProp",

    /** True quando si devono inviare i dati del form all'url
     * specificato nell'apposita proprietà.*/
    "flag_inviaDatiForm",

    /**True se si voglio inviare i dati in formato JSON, se non
     * specificato i dati del form saranno inviati come
     * multipart/form-data. Se si sceglie il formato JSON,
     * eventuali campi input[type=file] vengono ignorati.*/
    "isContentType_JSON",

    /** Flag: true se si vogliono mostrare le label vicino ai
     * campi di input.*/
    "flag_mostrareLabelCampiInput",

    /** Flag: true se il campo username <strong>non</strong>
     * può essere modificato. Se non specificato, si considera
     * che il campo può essere modficato.*/
    "username_readOnly",

    /** Flag: true se <strong>nessun</strong> campo di input
     * può essere modificato. Se non specificato, si considera
     * che il campo può essere modficato.*/
    "tuttiICampi_readOnly",

    /** Flag: true se tutti i campi di input sono devono avere
     * l'attributo "required". */
    "tuttiICampi_required",


    /** Proprietà che permette al componente padre
     * di specificare a quale url inviare i dati di
     * questo form tramite POST.*/
    "urlInvioFormTramitePost",

    /** Proprietà che permette al componente padre di aggiungere
     * dei dati a quelli che verranno inviati con il form. Questa
     * proprietà deve essere un oggetto.*/
    "datiAggiuntiviDaInviareAlServer",

    // Valori per il form (solo se specificati)
    // Se impostati a null, non viene mostrato il campo di input corrispondente.
    /** Valore per il campo username.*/
    "username",
    /** Valore per il campo nominativo.*/
    "nominativo",
    /** Valore per il campo email.*/
    "email",

    /**Token CSRF da usare.*/
    "csrfToken"
  ],
  data() {
    return {

      /** ID dell'elemento HTML contenente questo componente.*/
      idHtml : this.$options.name + generaIdUnivoco(),

      REGEX_EMAIL          : process.env.VUE_APP_REGEX_EMAIL,
      REGEX_CODICE_FISCALE : process.env.VUE_APP_REGEX_CODICE_FISCALE,

      isQuestoFormRiferitoAConsumer_wrapper: this.isQuestoFormRiferitoAConsumer, // wrapper per la prop
      username_readOnly_wrapper   : this.username_readOnly===undefined ?
                                      false : this.username_readOnly,
      tuttiICampi_readOnly_wrapper: this.tuttiICampi_readOnly===undefined ?
                                      false : this.tuttiICampi_readOnly,

      // Se è un consumer ha un codice fiscale, altrimenti ha uno username (gestito tramite watch)
      usernameCodFisc: undefined,

      // Dati v-model nel form
      username_wrapper  : "",
      nominativo_wrapper: "",
      email_wrapper     : "",

      // Wrapper necessario per specificare il comportamento di default se la property non è specificata
      resetCampiInputDopoInvioForm_wrapper: this.resetCampiInputDopoInvioForm===undefined ?
                                              true : this.resetCampiInputDopoInvioForm,

      // Valori ID per gli elementi input (univoci nel progetto)
      idUsername: generaIdUnivoco(),
      idNominativo: generaIdUnivoco(),
      idEmail: generaIdUnivoco(),

      datiAggiuntiviDaInviareAlServer_wrapper: {}, // vedere descrizione della prop corrispondente a questo wrapper


      csrfToken_wrapper: undefined  // token CSRF

    }
  },
  methods : {
    /** Metodo per inizializzare i valori dei campi di input.
     *  Se i valori sono definiti nelle prop usa quelli,
     *  altrimenti stringa vuota.*/
    inizializzaValoriCampiInput() {
      this.username_wrapper   = this.username   ? this.username   : "";
      this.nominativo_wrapper = this.nominativo ? this.nominativo : "";
      this.email_wrapper      = this.email      ? this.email      : "";
    },

    /** Questo metodo restituisce true se sono state apportate delle
     * modifiche ai campi del form, false altrimenti. */
    isFormModificato() {
      return ! ( this.username_wrapper===this.username     &&
                 this.nominativo_wrapper===this.nominativo &&
                 this.email_wrapper===this.email              ) ||
            !!this.filesDaForm() ;  // se è stato caricato un file, allora c'è stata una modifica
    },

    /** Restituisce la {@link FileList} degli eventuali file caricati nel form,
     * oppure null se non ce ne sono.*/
    filesDaForm() {
      const inputFile = document.querySelector('#' + this.idHtml + ' input[type=file]');
      return inputFile!==null && inputFile.files!==null && inputFile.files.length>0 ? inputFile.files : null;
    }

  },
  watch: {

    /** Watcher per gestire l'invio dei dati dal form al server.
     * I dati vengono inviati quando il flag osservato diventa true.
     * Restituisce un oggetto con due property: "datiInviati" contiene
     * l'oggetto coi dati inviati, "promiseRispostaServer" contiene
     * la promise che si occupa dell'invio della richiesta al server
     * (da quest'ultima è possibile leggere la risposta del server).
     * Questo metodo chiede la conferma all'utente prima di inviare
     * la richiesta di modifica dati al server: se l'utente non conferma,
     * allora "promiseRispostaServer" (nell'oggetto restituito) sarà
     * undefined.*/
    flag_inviaDatiForm: {
      immediate: true,
      deep: true,
      handler( flag_inviaDatiForm ) {

        /** Funzione per informare il componente padre che l'evento di
         * richiesta invio dati del form è stato gestito.
         * L'evento contiene come valore un oggetto con due propery:
         * <dl>
         *   <dt>datiInviati</dt>
         *   <dd>
         *     L'oggetto con i dati inviati.
         *   </dd>
         *   <dt>promiseRispostaServer</dt>
         *   <dd>
         *     Una promise la cui risoluzione conterrà la risposta ricevuta
         *     dal server, oppure direttamente una Promise rigettata se la
         *     richiesta non è stata proprio inviata al server.
         *   </dd>
         * </dl>
         * @param promiseRispostaServer Promise la cui risoluzione contiene
         * la risposta del server a seguito delle modifiche.
         * @param datiInviatiAlServer Dati eventualmente inviati al server.*/
        const informaComponentePadreEventoGestito = (promiseRispostaServer, datiInviatiAlServer={}) => {
          this.$emit('dati-form-inviati', {
            'datiInviati': datiInviatiAlServer,
            'promiseRispostaServer': promiseRispostaServer
          });
        };

        if( flag_inviaDatiForm ) {

          // Crea una promise rigettata nell'evenienza che non si effettui una richiesta
          // così da poter comunque seguire il flusso delle operazioni "promise"-based
          const creaERestituisciPromiseRigettataSeNonSiInvianoDatiAlServer = messaggioDaMostrare =>
              Promise.reject({['data']:messaggioDaMostrare}); // property 'data' come in una risposta HTTP fallita

          if (this.isFormModificato()) {

            const datiDaInviareAlServer = {
              [process.env.VUE_APP_FORM_CSRF_INPUT_FIELD_NAME]: this.csrfToken_wrapper,
              [process.env.VUE_APP_FORM_EMAIL_INPUT_FIELD_NAME]: this.email_wrapper,
              [process.env.VUE_APP_FORM_NOMINATIVO_INPUT_FIELD_NAME]: this.nominativo_wrapper,
              [process.env.VUE_APP_FORM_USERNAME_INPUT_FIELD_NAME]: this.username_wrapper
            };
            for (const prop in this.datiAggiuntiviDaInviareAlServer_wrapper) {
              datiDaInviareAlServer[prop] = this.datiAggiuntiviDaInviareAlServer_wrapper[prop];
            }

            // Check proprietà prima di inviare (rimuove quelle non valide)
            for (const prop in datiDaInviareAlServer) {
              if (!isStringaNonNullaNonVuota(datiDaInviareAlServer[prop]))
                delete isStringaNonNullaNonVuota[prop];
            }


            // Richiesta conferma modifiche

            let promise;  // attualmente è undefined

            // Creazione messaggio con le modifiche che risulteranno
            const msg = JSON.stringify(datiDaInviareAlServer)
                            .replaceAll(',', '\n')
                            .replaceAll(':', ': ')
                            .replaceAll('"', '')
                            .replaceAll('{', '')
                            .replaceAll('}', '')
                            .split('\n')
                            .map(unaLinea => unaLinea[0].toUpperCase() + unaLinea.substr(1))  // upper case del primo carattere
                            .filter(unaLinea => {
                              // rimuove le prop csrf token e id attore (all'utente che deve confermare i dati da modificare non interessa vedere i dettagli tecnici)

                              // Controllo che le proprietà da scartare esistano (magari è stato cambiato il nome, così ci si accorge)
                              const nomePropCsrfToken = 'csrfToken';
                              const nomePropIdAttore = 'identificativoAttore';
                              if (!datiDaInviareAlServer[nomePropCsrfToken])
                                throw (nomePropCsrfToken + ' non è una property');
                              // if (!datiDaInviareAlServer[nomePropIdAttore])  // commentato perché non tutti i componenti hanno questa proprietà
                              //   throw (nomePropIdAttore + ' non è una property');

                              const lineaContieneProp = (linea, propDaCercare) =>
                                  linea.toLowerCase().startsWith(propDaCercare.toLowerCase());

                              // escludo la prop
                              return ! ( lineaContieneProp(unaLinea, nomePropCsrfToken) ||
                                          ( !!datiDaInviareAlServer[nomePropIdAttore] &&
                                              lineaContieneProp(unaLinea, nomePropIdAttore) ) );
                            })
                            .sort() // ordine alfabetico
                            .join('\n') + String(this.filesDaForm() ? "\n Verrà  modificato il logo." : "");

            confirm("Riepilogo.\n\n" + msg)
                .then(() => {  // utente ha confermato

                  // Si procede con la richiesta di modifiche al server
                  if (this.isContentType_JSON) {
                    // Content-type application/json
                    promise = richiestaPost(this.urlInvioFormTramitePost, datiDaInviareAlServer);

                  } else {
                    // Content-type multipart/form-data
                    const datiDaInviareAlServer_comeFormData = new FormData();
                    for (const prop in datiDaInviareAlServer) {
                      datiDaInviareAlServer_comeFormData.append(prop, datiDaInviareAlServer[prop]);
                    }

                    // Se presente un file, lo aggiunge ai dati da inviare
                    const filesDaForm = this.filesDaForm();
                    if( filesDaForm ) { // se truthy, allora c'è un file caricato nel form
                      const fileDaForm = filesDaForm[0];
                      datiDaInviareAlServer_comeFormData.append(process.env.VUE_APP_FORM_LOGO_UPLOADER_INPUT_FIELD_NAME, fileDaForm);
                    }

                    promise = richiestaPostConFile(this.urlInvioFormTramitePost, datiDaInviareAlServer_comeFormData);

                  }

                  promise
                      .then( risposta => {

                        if (this.resetCampiInputDopoInvioForm_wrapper) {
                          // pulizia dei campi del form
                          this.username_wrapper = "";
                          this.nominativo_wrapper = "";
                          this.email_wrapper = "";
                          document.querySelector("#" + this.idHtml).reset();
                        }

                        return risposta
                      })
                      .catch(console.error);            // tiene traccia dell'eventuale errore

                })
                .catch( () => {

                  // utente non ha confermato le modifiche

                  promise = creaERestituisciPromiseRigettataSeNonSiInvianoDatiAlServer("Operazione interrotta, nessuna modifica effettuata.");
                  // come in una risposta http fallita, in cui c'è il campo 'data'

                })
                .finally( () => {

                  // In ogni caso, informare il componente padre
                  informaComponentePadreEventoGestito(promise, datiDaInviareAlServer);

                });

          } else {
            // In ogni caso, informare il componente padre
            informaComponentePadreEventoGestito(
              creaERestituisciPromiseRigettataSeNonSiInvianoDatiAlServer("Nessuna modifica rilevata."),
              {}
            );
          }
        }

      }
    },

    isQuestoFormRiferitoAConsumer : {
      immediate: true,
      deep: true,
      handler(nuovoValore) {
        this.isQuestoFormRiferitoAConsumer_wrapper = nuovoValore;
        this.usernameCodFisc = this.isQuestoFormRiferitoAConsumer ? "Codice fiscale" : "Username";
      }
    },

    datiAggiuntiviDaInviareAlServer : {
      immediate: true,
      deep: true,
      handler( oggettoConDatiAggiuntiviDaInviareNelForm ) {
        this.datiAggiuntiviDaInviareAlServer_wrapper = oggettoConDatiAggiuntiviDaInviareNelForm;
      }
    },

    csrfToken : {
      immediate: true,
      deep: true,
      handler(nuovoValore) {
        this.csrfToken_wrapper = nuovoValore;
      }
    },


    // Valori del form
    username : {
      immediate: true,
      deep: true,
      handler(nuovoValore) {
        this.username_wrapper = nuovoValore;
      }
    },
    nominativo : {
      immediate: true,
      deep: true,
      handler(nuovoValore) {
        this.nominativo_wrapper = nuovoValore;
      }
    },
    email : {
      immediate: true,
      deep: true,
      handler(nuovoValore) {
        this.email_wrapper = nuovoValore;
      }
    },

    ripristinaValoriProp : {
      immediate: true,
      deep: true,
      handler( flag_ripristinaValoriProp ) {
        if( flag_ripristinaValoriProp ) {
          this.inizializzaValoriCampiInput();
          this.$emit('ripristina-valori-prop');
        }
      }
    }

  }


}
</script>

<style scoped>
  a[href^="mailto"] {
    text-decoration: none;
  }
  a[href^="mailto"]::after {
    content: "✉";
  }
  .btn-link:hover {
    background: gray;
  }
</style>