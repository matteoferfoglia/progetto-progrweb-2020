Web Application
====================

Questo progetto realizza una *App Engine Standard Java application*,
utilizzando *Maven* come sistema di *build automation*.

Il progetto è stato inizialmente generato con *Google Cloud SDK*
(installabile da https://cloud.google.com/sdk/docs/install),
usando l'archetipo *appengine-standard-archetype*.

## Maven e Google Cloud SDK
Questo progetto richiede la versione 3.5 di Maven.
Di seguito vengono elencati alcuni comandi Maven (eseguibili nella forma
`mvn ...`) che possono essere eseguiti dal terminale posizionato nella
cartella principale del progetto (dove risiede il file *pom.xml*).

E' necessario installare *Google Cloud SDK*, con la seguente procedura:
1. installare [Google Cloud SDK](https://cloud.google.com/sdk/docs/install);
2. dal terminale, installare *app-engine-java* con il seguente comando
```
gcloud components install app-engine-java
```

### Esecuzione locale del web server
Il comando

    mvn clean validate compile test package site verify appengine:run

permette di eliminare il risultato di una precedente compilazione e
ricompilare l'intero progetto, seguendo le fasi di Maven.

Per evitare l'esecuzione dei test, eseguire il comando:

    mvn clean validate compile package -Dmaven.test.skip=true appengine:run

Assumendo che la web-application sia già stata compilata correttamente,
il comando

    mvn appengine:run

permette l'avvio del server di sviluppo (locale) di *Google App Engine*.

Si veda il plugin `com.google.cloud.tools » appengine-maven-plugin` nel
file *pom.xml*.

### Deploying
Per eseguire il *deploying* del progetto, creare il *package* del progetto
(`mvn package`), poi usare il comando

    mvn appengine:deploy

Per l'operazione di *deploying* dell'applicazione *Google AppEngine*,
l'account di fatturazione associato al progetto deve essere abilitato
e l'applicazione *AppEngine* sul server di Google deve essere abilitata
(quindi non in stato *stopped*, vedere la sezione
[*Impostazioni*](https://console.cloud.google.com/appengine/settings) di
*AppEngine* del progetto).

#### Indici del Datastore
Se viene modificato il file *WEB-INF/index.yaml* contenente la definizione
degli indici composti per l'esecuzione di query complesse, prima di eseguire
il deploying dell'applicazione (come soprascritto), bisogna eseguire dal
terminale posizionato nella cartella radice di questo progetto *Maven* il
seguente comando *gcloud*, per il *deploying* del file *index.yaml*
([Fonte](https://cloud.google.com/appengine/docs/standard/java/configuring-datastore-indexes-with-index-yaml)).

    gcloud app deploy src\main\webapp\WEB-INF\index.yaml

#### Deploying: procedura completa
Riepilogando, per eseguire il *deploying* dell’applicazione nei server di
*Google App Engine*, bisogna eseguire i seguenti comandi dal terminale
posizionato nella cartella radice del progetto *Maven*:
1) `gcloud app deploy src\main\webapp\WEB-INF\index.yaml`
    per eseguire il *deploying* del file *index.yaml.*;
2) `mvn clean validate compile test package site verify appengine:deploy`
    per creare il file *WAR* dell’applicazione ed eseguirne il deploying,
    seguendo le fasi *Maven* (*clean, validate, compile, test, package,
    site, verify*).

### Testing
Per creare i test Java è stato utilizzato il framework *JUnit 5*.
Il comando

    mvn test

permette di eseguire i test del progetto.

### Documentazione e Javadoc
La documentazione del progetto viene generata con il comando `mvn site`
ed è disponibile nella cartella `target/site` in formato HTML (vedere
*target/site/index.html*)


### Aggiornamento degli artefatti
Il plugin [versions-plugin](http://www.mojohaus.org/versions-maven-plugin/)
di Maven è stato utilizzato per controllare che le dipendenze utilizzate
(definite nel *pom.xml*) siano aggiornate.


## Servizi REST

### Jersey
Questo progetto realizza una *web application* basata su servizi di tipo *REST*
implementati sfruttando *Jersey*.

### Swagger
La documentazione delle API viene generata sfruttando *Swagger* (nel *pom.xml*)
ed è disponibile:
- in formato JSON, al request URI */api/openapi.json*;
- in formato YAML, al request URI */api/openapi.yaml*;
- in modo interattivo, direttamente dal browser, al request URI */swagger-ui/*.


## Front-end
Si è utilizzato *Vue 3* per la programmazione del *front-end*.

Il file *pom.xml* del progetto *Maven* è stato adattato in modo che,
quando si crea il *package* del progetto, venga automaticamente generata
la *release* Vue (creata con il comando `npm run build` eseguito
automaticamente dal terminale posizionato nella cartella contenente
il progetto *Vue*) e copiata tra le risorse del Web Server, il tutto in modo completamente
automatizzato (fatto nella fase Maven *prepare-package*).

## Web Service
Questo progetto implementa anche un *Web Service REST*,
come richiesto dalle specifiche.