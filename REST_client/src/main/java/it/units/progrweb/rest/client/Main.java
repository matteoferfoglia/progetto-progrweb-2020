package it.units.progrweb.rest.client;

import javax.ws.rs.core.Response;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

/**
 * Classe main per dimostrare il funzionamento del web service: un
 * Uploader può usare il web service esposta dal sistema per caricare
 * tramite questa applicazione client un documento destinato ad un
 * Consumer. L'Uploader deve specificare qui le proprie credenziali
 * ed i parametri per il file da caricare, come richiesti dal web
 * service.
 * @author Matteo Ferfoglia
 */
public class Main {

    /** Uri base del server a cui eseguire le richieste.*/
    final static String URI_BASE_WEB_SERVICE = "https://progettoprogrweb2020.ew.r.appspot.com/api/webService";
//    final static String URI_BASE_WEB_SERVICE = "http://localhost:8910/api/webService"; // server di sviluppo

    /** Uri web service per upload di un documento. */
    final static String PATH_SERVIZIO_UPLOAD_FILE = URI_BASE_WEB_SERVICE + "/uploadDocumento";

    /** Uri web service per login di questo client. */
    final static String PATH_SERVIZIO_LOGIN_UPLOADER = URI_BASE_WEB_SERVICE + "/login";

    

    /**
     * Metodo main.
     * @param args Array con 2 elementi:
     *             <ol>
     *              <li>Nome dell'applicazione</li>
     *              <li>Parametri per il Web Service. Specificare i seguenti
     *                   valori separati dal punto e virgola (;):
     *              <ol>
     *                <li>Username dell'Uploader</li>
     *                <li>Password dell'Uploader</li>
     *                <li>Username del Consumer destinatario</li>
     *                <li>Email del Consumer destinatario</li>
     *                <li>Nominativo del Consumer destinatario</li>
     *                <li>Percorso file da caricare</li>
     *                <li>lista di hashtag separati da virgola senza spazi</li>
     *              </ol>
     *             </ol>
     * */
    public static void main(String[] args) {

        final String[] nomiParametriAttesi = {
                "Username dell'Uploader",
                "Password dell'Uploader",
                "Username del Consumer destinatario",
                "Email del Consumer destinatario",
                "Nominativo del Consumer destinatario",
                "Nome del file da caricare (visibile nella piattaforma)",
                "Percorso del file da caricare (estensione compresa)",
                "Lista di hashtag separati da virgola senza spazi"
        };

        String[] parametriInseriti =
                args.length>0 ?
                    String.join(" ", args).split(";") : //  Lo spazio è un separatore per args, ma per l'applicazione uno spazio può essere usato come valore per un parametro
                    new String[0];

        if(parametriInseriti.length!=8) {

            System.out.println("Sono stati ricevuti " + parametriInseriti.length + " parametri. Inserire: ");
            for (String s : nomiParametriAttesi)
                System.out.println(" - " + s);

        } else {

            System.out.println("Sono stati inseriti i seguenti parametri:");
            for(int i=0; i< parametriInseriti.length; i++)
                System.out.println(" - " + nomiParametriAttesi[i] + ": " + parametriInseriti[i]);

            // Credenziali Uploader
            String credenziali_username = parametriInseriti[0];
            String credenziali_password = parametriInseriti[1];

            // Consumer destinatario del file
            String codiceFiscaleConsumer = parametriInseriti[2];
            String emailConsumer = parametriInseriti[3];
            String nomeCognomeConsumer = parametriInseriti[4];

            // File da caricare
            String nomeFile = parametriInseriti[5];
            String percorsoFileLocale = parametriInseriti[6];
            String listaHashtag = parametriInseriti[7];

            // ---- FINE ACQUISIZIONE PARAMETRI ----


            System.out.println("\nUpload del file ...\n");

            File file = leggiFileLocale(percorsoFileLocale);

            // Creazione client REST ed invio del contenuto
            RestClient restClient = new RestClient(PATH_SERVIZIO_UPLOAD_FILE);
            try {

                // Login del client
                if( restClient.login(credenziali_username, credenziali_password, PATH_SERVIZIO_LOGIN_UPLOADER) ) {

                    // Upload del file e risposta dal server
                    Response risposta =
                            restClient.inviaFileAConsumer(
                                    codiceFiscaleConsumer, emailConsumer, nomeCognomeConsumer,
                                    nomeFile, listaHashtag, file);
                    System.out.println("--- RISPOSTA DAL SERVER: " +
                            risposta.getStatus() + " " + risposta.getStatusInfo() + " ---\n" +
                            risposta.readEntity(String.class) + "\n");

                    restClient.logout();

                } else {
                    System.out.println("Errore durante il login.");
                }

            } catch (Exception e) {
                System.err.println("Errore durante l'invio:\n\n" + e.getMessage() + "\n\n\tSTACK TRACE:\n");
                e.printStackTrace(System.err);
            }

            restClient.close();

        }


    }

    private static File leggiFileLocale(String percorsoFileLocale) {
        File file;

        // Lettura del file
        file = new File(percorsoFileLocale);
//        stampaFile(file);

        return file;
    }


    /** Metodo per stampare il contenuto di un file dato.
     * @param file File il cui contenuto sarà stampato.*/
    @SuppressWarnings("unused") // metodo mantenuto per comodità
    private static void stampaFile(File file) {

        Scanner scannerFile;
        try {
            scannerFile = new Scanner(file);
            while( scannerFile.hasNextLine() )
                System.out.println(scannerFile.nextLine());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }

}
