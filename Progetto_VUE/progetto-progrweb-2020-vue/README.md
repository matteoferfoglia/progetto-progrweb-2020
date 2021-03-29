# progetto-progrweb-2020-vue
Questo progetto *Vue* realizza una Single Page Application
che soddisfa i requisiti (client-side) richiesti dal
progetto d'esame del corso di Programmazione Web
dell'anno accademico 2019-2020.

## Variabili d'ambiente
Questo progetto fa uso di variabili d'ambiente.
L'utilizzo di variabili d'ambiente permette di
specificare in un unico punto delle "variabili globali"
evitando (all'occorrenza) di dover andare a modificare
manualmente i parametri sparsi nell'applicazione, i quali,
invece, con questo approccio, sono centralizzati nei file
*.env*, *.env.development* ed *.env.production*.

### .env
Nel file *.env* vi sono le variabili d'ambiente per
qualsiasi modalità (production/development). Potrebbe
essere necessario modificare, ad esempio, gli
URL dei servizi resi disponibili dal server.

### .env.development
Il file *.env.development* contiene le variabili
d'ambiente per la modalità di sviluppo.

### .env.production
Il file *.env.development* contiene le variabili
d'ambiente per la modalità di produzione.


## Comandi npm 

### Setup del progetto ed esecuzione
Per installare le dipendenze del progetto, utilizzare
il comando `npm install`.

### Compilazione ed Compiles and hot-reloads for development
Il comando `npm run serve` compila i file del progetto e
permette l'*hot-reload* dell'applicazione.

### Compilazione e minificazione dei file per la fase di produzione
Il comando `npm run build` si occupa di installare tutte le
dipendenze richieste dal progetto, compilare i file, eseguire
i test e produrre i file minificati da usare nella fase di
produzione (*production*). Il risultato finale viene salvato
nella cartella *dist*.

### Validazione *Lints*
Il comando `npm run lint` permette di validare i file del progetto
usando *Lints*.