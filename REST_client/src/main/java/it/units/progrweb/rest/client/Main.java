package it.units.progrweb.rest.client;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Scanner;

/**
 * Classe main per dimostrare il funzionamento del web service: un
 * Uploader può usare il web service esposta dal sistema per caricare
 * tramite questa applicazione client un documento destinato ad un
 * Consumer. L'uploader deve specificare qui le proprie credenziali
 * ed i parametri per il file da caricare, come richiesti dal web
 * service.
 * @author Matteo Ferfoglia
 */
public class Main {

    public static void main(String[] args) {

        // Uri web service
        String uri_webService = "http://localhost:8910/api/webService/uploadDocumento";

        // Credenziali uploader
        String credenziali_username = "AB01";
        String credenziali_password = "5678";

        // Parametri per l'invio dei file
//        String codiceFiscaleConsumer = "PPPPLT80A01A952G";
//        String emailConsumer = "pippopluto@example.com";
//        String nomeCognomeConsumer = "Pippo Pluto";
        String codiceFiscaleConsumer = "RSSMRA80A01F205X";
        String emailConsumer = "mariorossi@example.com";
        String nomeCognomeConsumer = "Mario Rossi";
        String nomeFile = "file prova";
        String listaHashtag = "rest, file, prova, primo file";
        String percorsoFileLocale = "fileProva.txt";


        // ---- FINE PARAMETRI ----


        File file;  // da inizializzare
        {
            // Lettura del file
            URL urlFileLocale = Main.class.getClassLoader().getResource(percorsoFileLocale);
            if (urlFileLocale == null) {
                throw new IllegalArgumentException("File " + percorsoFileLocale + " non trovato.");
            } else {
                try {
                    file = new File(urlFileLocale.toURI());
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                    file = null;
                }
//                stampaFile(file);
            }
        }

        // Creazione client REST ed invio del contenuto
        RestClient restClient = new RestClient(uri_webService);
        try {

            String responseBody =
                    restClient.inviaFileAConsumer(credenziali_username, credenziali_password,
                                                  codiceFiscaleConsumer, emailConsumer, nomeCognomeConsumer,
                                                  nomeFile, listaHashtag, file)
                              .readEntity(String.class);
            System.out.println("--- RISPOSTA DAL SERVER ---\n" + responseBody);

        } catch (Exception e) {
            System.err.println("Errore durante l'invio:\n\n" + e.getMessage() + "\n\n\tSTACK TRACE:\n");
            e.printStackTrace(System.err);
        }

    }


    /** Metodo per stampare il contenuto di un file dato.*/
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
