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
url dei servizi resi disponibili dal server.

### .env.development
Il file *.env.development* contiene le variabili
d'ambiente per la modalità di sviluppo.

### .env.production
Il file *.env.development* contiene le variabili
d'ambiente per la modalità di produzione.

## Project setup
```
npm install
```

### Compiles and hot-reloads for development
```
npm run serve
```

### Compiles and minifies for production
```
npm run build
```

### Lints and fixes files
```
npm run lint
```

### Customize configuration
See [Configuration Reference](https://cli.vuejs.org/config/).
