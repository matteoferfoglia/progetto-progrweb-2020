package it.units.progrweb.rest.client;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Scanner;

/**
 * @author Matteo Ferfoglia
 */
public class Main {

    public static void main(String[] args) {
        // Parametri per l'invio dei file
        String codiceFiscaleConsumer = "PPPPLT80A01A952G";
        String emailConsumer = "pippopluto@example.com";
        String nomeCognomeConsumer = "Pippo Pluto";
        String nomeFile = "file prova";
        String listaHashtag = "rest, file, prova, primo file";
        File file;  // da inizializzare

        {
            // Lettura del file
            String nomeFileLocale = "fileProva.txt";
            URL urlFileLocale = Main.class.getClassLoader().getResource(nomeFileLocale);
            if (urlFileLocale == null) {
                throw new IllegalArgumentException("File " + nomeFileLocale + " non trovato.");
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


        RestClient restClient = new RestClient("http://localhost:8910/api/nomeDiQuestoAttore", "AB01", "5678");
        restClient.inviaFileAConsumer(codiceFiscaleConsumer, emailConsumer, nomeCognomeConsumer, nomeFile, listaHashtag, file);
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
