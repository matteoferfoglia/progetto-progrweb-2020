<template>
  <form @submit.prevent="richiediEMostraResocontoNelPeriodo(dataInizio, dataFine)">
    <!-- Token CSRF non usato perché questo form non modifica lo stato nel sistema -->
    <fieldset class="fieldset-resoconto">
      <legend>Periodo di riferimento:</legend>
      <div class="d-flex justify-content-around align-items-end flex-wrap form-items-container">
        <label>da
          <input type="date"
                 :value="dataInizio/*v-bind non funziona correttamente con input[type=date]*/"
                 :id="idDataInizio"
                 class="form-control"
                 @input.prevent="setDataInizio($event.target.value)"
                 required>
        </label>
        <label>a
          <input type="date"
                 :value="dataFine"
                 :min="valoreMinimoDataFineRispettoAData(dataInizio)"
                 :id="idDataFine"
                 class="form-control"
                 @input.prevent="setDataFine($event.target.value)"
                 required>
        </label>
        <button type="submit" class="btn btn-primary">Invia</button>
      </div>
    </fieldset>
  </form>
  <dl v-for="(valoreProp, nomeProp) in resoconto" :key="nomeProp">
    <!-- Fonte: https://developer.mozilla.org/en-US/docs/Web/HTML/Element/dl#wrapping_name-value_groups_in_htmlelementdiv_elements -->
    <dt>{{camelcaseToHumanReadable( nomeProp )}}</dt>
    <dd>{{ valoreProp }}</dd>
  </dl>
</template>

<script>
import {camelCaseToHumanReadable, generaIdUnivoco} from "../../../utils/utilitaGenerale";
import {richiestaGet} from "../../../utils/http";

export default {
  name: "ResocontoDiUnAttore",
  props: [
      /**Nome dell'Uploader a cui questo resoconto si riferisce.*/
      "nomeUploaderCuiQuestoResocontoSiRiferisce",

      /**Identificativo dell'Uploader a cui questo resoconto si riferisce.*/
      "identificativoUploader"
  ],
  data() {

    return {
      /** Data iniziale del periodo di riferimento.*/
      dataInizio: undefined,

      /** Data finale del periodo di riferimento.*/
      dataFine: undefined,

      /** Valore dell'ID dell'elemento HTML di input per la data iniziale.*/
      idDataInizio: "input-data-inizio-" + generaIdUnivoco(),

      /** Valore dell'ID dell'elemento HTML di input per la data iniziale.*/
      idDataFine: "input-data-fine-" + generaIdUnivoco(),

      /** Resoconto, così com'è stato inviato dal server.*/
      resoconto: {}, // inizializzato ad oggetto vuoto

      camelcaseToHumanReadable: camelCaseToHumanReadable  // import della funzione per usarla nel template

    }

  },

  /** Imposta di defualt il periodo temporale dal primo giorno
   * del mese precedente all'ultimo giorno del mese precedente
   * (<a href="">Fonte</a>).*/
  created() {

    const adesso = new Date();
    let anno = adesso.getFullYear();
    let mese = adesso.getMonth();

    const dataPrimoGiornoMesePrecedente  = new Date();
    const dataUltimoGiornoMesePrecedente = new Date();
    dataPrimoGiornoMesePrecedente .setFullYear(anno, mese-1, 1) ;
    dataUltimoGiornoMesePrecedente.setFullYear(anno, mese, 0);

    this.dataInizio = restituisciArrayDaDate_yyyy_mm_dd(dataPrimoGiornoMesePrecedente) .join("-");
    this.dataFine   = restituisciArrayDaDate_yyyy_mm_dd(dataUltimoGiornoMesePrecedente).join("-");

    this.richiediEMostraResocontoNelPeriodo(this.dataInizio, this.dataFine);

  },

  methods: {

    /** Setter per la data finale (v-model non funzionava correttamente).
     * Controlla la validità della data inserita.
     * @param nuovoValoreDataFinale Nuovo valore per la data finale nel formato yyyy-mm-dd.*/
    setDataFine( nuovoValoreDataFinale ) {

      if(  Number( this.dataInizio.replaceAll("-","")) >
           Number( nuovoValoreDataFinale.replaceAll("-","") )  ) {
        // In realtà non dovrebbe mai succedere che venga impostata una data sbagliata
        // perché è stato impostato l'attributo "min" del campo di input (ma non si sa mai)
        alert("La data iniziale non può essere posteriore alla data finale.");
        this.dataFine = this.valoreMinimoDataFineRispettoAData( this.dataInizio );
      } else {
        this.dataFine = nuovoValoreDataFinale;
      }

      const inputDataFine = document.getElementById( this.idDataFine );
      inputDataFine.value = this.dataFine;
      inputDataFine.setAttribute( "min", this.valoreMinimoDataFineRispettoAData( this.dataInizio ) );

    },

    /** Setter per la data iniziale. Eventualmente aggiorna la data finale, se
     * dovesse risultare invalida a seguito della modifica apportata sulla data iniziale.
     * @param nuovoValoreDataIniziale Nuovo valore per la data iniziale nel formato yyyy-mm-dd.*/
    setDataInizio( nuovoValoreDataIniziale ) {


      this.dataInizio = nuovoValoreDataIniziale;

      if(  Number( nuovoValoreDataIniziale.replaceAll("-","")) >
           Number( this.dataFine.replaceAll("-","") )  ) {
        this.setDataFine( this.valoreMinimoDataFineRispettoAData( this.dataInizio ) );
      }

      document.getElementById( this.idDataInizio ).value = this.dataInizio;

    },

    /** Restituisce il giorno successivo alla data indicata nel parametro.
     * @param dataIniziale Stringa rappresentante la data iniziale (formato: yyyy-mm-dd).
     * @return il giorno successivo alla data rappresentata dal parametro, restituito
     *         come stringa (formato: yyyy-mm-dd)*/
    valoreMinimoDataFineRispettoAData( dataIniziale ) {  // TODO : verificare come si comporta con input errati (es.: "0")
      // Prima converto in Date() poi riporto in stringa (lascio che sia Date
      // a gestire la somma di giorni, per ottenere il giorno successivo)

      // Note: Parsing of date strings with the Date constructor (and Date.parse(),
      // which works the same way) is strongly discouraged due to browser differences
      // and inconsistencies. Fonte: https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Date/Date
      const dataComeArray_yyyy_mm_dd = dataIniziale.split("-");
      const anno   = Number( dataComeArray_yyyy_mm_dd[0] );
      const mese   = Number( dataComeArray_yyyy_mm_dd[1] ) - 1; // mesi: in [1-12] nel formato "umano", in [0-11] (monthIndex) nel costruttore Date()
      const giorno = Number( dataComeArray_yyyy_mm_dd[2] );

      const data_giornoSuccessivo = new Date();
      data_giornoSuccessivo.setFullYear(anno, mese, giorno+1 ); // giorno successivo // TODO : verificare output corretto

      return restituisciArrayDaDate_yyyy_mm_dd(data_giornoSuccessivo).join("-");
    },

    /** Richiede il resoconto al server nel periodo scelto.
     * @param dataIniziale Data iniziale del periodo (Stringa, formato: yyyy-mm-dd).
     * @param dataFinale Data finale del periodo (Stringa, formato: yyyy-mm-dd).
     * @return La Promise associata alla richiesta asincrona effettuata al server.*/
    richiediEMostraResocontoNelPeriodo(dataIniziale, dataFinale ) {

      const parametriRequest = {
        [process.env.VUE_APP_FORM_DATA_INIZIALE_INPUT_FIELD_NAME] : dataIniziale,
        [process.env.VUE_APP_FORM_DATA_FINALE_INPUT_FIELD_NAME]   : dataFinale,
      };

      const urlRichiestaResocontoPerQuestoUploader =
          process.env.VUE_APP_URL_RICHIESTA_RESOCONTO_DI_UN_UPLOADER__RICHIESTA_DA_ADMIN + '/' + this.identificativoUploader;

      return richiestaGet( urlRichiestaResocontoPerQuestoUploader, parametriRequest )
              .then( resoconto => this.resoconto = resoconto )
              .catch( console.error );

    }

  }
}

/** Data una data (tipo Date) restituisce un array di Stringhe con tre
 * elementi rappresentanti la data: ['yyyy','mm','dd'], con 0<mm<=12 e
 * 0<dd<=31.
 * Se la data non è valida, utilizza la data odierna.*/
const restituisciArrayDaDate_yyyy_mm_dd = data => {

  if( ! ( data instanceof Date) ) {
    data = new Date();
  }

  let anno, mese, giorno;

  anno = data.getFullYear();
  anno = String( anno ).length === 4  ?
          String(anno)                :
          String( new Date().getFullYear() );  // se l'anno è invalido, usa l'anno corrente

  mese = data.getMonth() + 1 ;                 // getMonth() restituisce il numero del mese   in [0-11], quindi sommo 1 (tipo Number)
  mese = ( '0' + mese ).slice(-2);             // gestisce i mesi con una sola cifra (e converte in String)

  giorno = data.getDate();                     // getDate()  restituisce il numero del giorno in [1-31]
  giorno = ( '0' + giorno ).slice(-2);         // gestisce i mesi con una sola cifra (e converte in String)

  return [anno, mese, giorno];                 // stringhe con rispettivamente 4, 2 e 2 cifre

}
</script>

<style scoped>
  .fieldset-resoconto legend {
    font-size: 1.25rem;
  }
  dd {
    margin-left: 1em;
  }
  dl {
    margin-top: 1em;
  }
</style>