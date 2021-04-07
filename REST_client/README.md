# Client REST

In questo progetto *Maven* è stato implementato un semplice client *REST*
in Java, con l'obiettivo di dimostrare il corretto funzionamento del 
web service *REST* implementato sul server.

Il funzionamento può essere verificato eseguendo il metodo *main* della
classe *Main*.

## Build ed esecuzione

Il seguente comando, eseguito dal terminale nella cartella 

### Clean and build
Il comando

    mvn clean package

permette l'eliminazione delle eventuali vecchie *build* e la creazione
della *build* di questo progetto nella cartella *target*.
Il file *JAR* sarà disponibile nella cartella radice del progetto.

Il comando

    mvn clean package site

oltre a creare il nuovo file *JAR* del progetto, genera anche la
documentazione (compreso *Javadoc*) nella cartella *target/site*.

### Esecuzione
Dopo aver creato il *package* del progetto (`mvn package`), è
possibile eseguire il programma Client REST eseguendo il comando.

    mvn exec:java

In questo modo, vengono passati al metodo main dei parametri
preconfigurati nel file *pom.xml*.


## Distribuzione
Il file di configurazione *pom.xml* è stato programmato per creare,
oltre al file *JAR* ottenuto dalla fase di *package*, un ulteriore
file di dimensione maggiori, all'interno di cui sono state assemblate
tutte le dipendenze richieste dal progetto. Tale file *JAR* può essere
direttamente distribuito agli Uploader che lo possono eseguire da
terminale (ammesso che abbiano installato Java 8) con il comando 

    java -jar *nomeFileJar.jar* *parametriInput*

Ad esempio, assumendo che il file *JAR* risultante si chiami
*ClientRest.jar*, il programma potrà essere eseguito sulla macchina
dell'Uploader con il seguente comando

    java -jar ClientREST_distribuibile-jar-with-dependencies.jar AB01;5678;PPPPLT80A01A952G;consumerprova@example.com;Consumer di Prova;file prova;fileProva.txt;rest, file, prova, primo file

Tale comando deve essere eseguito dal terminale posizionato nella
stessa cartella in cui è salvato il file *jar* e si assume che nella
medesima cartella sia presente anche il file (in questo esempio
chiamato *fileProva.txt*) da caricare.