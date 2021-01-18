package it.units.progrweb.listeners;

import com.googlecode.objectify.ObjectifyService;
import it.units.progrweb.utils.Logger;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.util.Arrays;

/**
 * Starter del database per indicare quali classi
 * dovranno essere gestite come entità.
 *
 * @author Matteo Ferfoglia
 */
@WebListener
public class StarterDatabase implements ServletContextListener {

    /** Array dei nomi delle classi corrispondenti alle entità da
     * registrare nel database. Utilizzare il <i>fully-qualified
     * name</i> delle classi.*/
    private final static String[] nomiEntitaGestiteDalDatabase = {  // TODO : RENDERLA VARIABILE D'AMBIENTE
            "it.units.progrweb.entities.attori.administrator.Administrator",
            "it.units.progrweb.entities.attori.administrator.AdministratorStorage",
            "it.units.progrweb.entities.attori.nonAdministrator.uploader.Uploader",
            "it.units.progrweb.entities.attori.nonAdministrator.uploader.UploaderStorage",
            "it.units.progrweb.entities.attori.nonAdministrator.consumer.Consumer",
            "it.units.progrweb.entities.attori.nonAdministrator.consumer.ConsumerStorage",
            "it.units.progrweb.entities.RelazioneUploaderConsumerFile",
            "it.units.progrweb.entities.AuthenticationDatabaseEntry",
            "it.units.progrweb.entities.file.File",
            "it.units.progrweb.entities.file.FileStorage"
    };

    // Salvare direttamente la classe (es.: FileStorage.class) non
    // era possibile perché se il suo accesso è di tipo private-package
    // sarebbe inacessibile dall'esterno del package ed usare le
    // reflection era poco comodo, in quanto avrebbe richiesto un blocco
    // try-catch intorno all'inizializzazione di un parametro.
    // In caso di modifica del nome delle classi, bisognerà modificare
    // manualmente questo parametro: se ci si dimentica, l'errore sarà
    // subito evidente all'avvio del server. Con questa soluzione si
    // perde un po' in automazione, ma l'eventuale lavoro manuale è
    // concentrato nell'inizializzazione del parametro e facilmente
    // tracciabile.
    // Motivazione di tutto ciò: meglio rendere inaccessibili le classi
    // corrispondenti ad entità salvate nel database e rendere pubbliche
    // solo le entità "proxy" (ma le entità storage non sono accessibile
    // nemmeno da qua, se non che tramite Reflection).



    /**
     * Registra tutte le classi che dovranno essere gestite dal database.
     * Attenzione: questo metodo è specifico solamente per Objectify.
     * Vedere {@link ObjectifyService#register(Class)} e
     * {@link com.googlecode.objectify.ObjectifyFactory#register(Class)}.
     */
    public static void registraClassiDatabase() {
        Arrays.stream(nomiEntitaGestiteDalDatabase)
              .forEach(nomeClasse -> {  //mappatura da nome della classe alla classe
                  try {
                      Class classeDaRegistrare = Class.forName(nomeClasse);
                      ObjectifyService.register(classeDaRegistrare);
                  } catch (ClassNotFoundException e) {
                      Logger.scriviEccezioneNelLog(StarterDatabase.class,
                              "Classe " + nomeClasse + " non trovata durante il tentativo di individuazione tramite Reflection.",
                              e);
                  }
              });
    }

    public void contextInitialized(ServletContextEvent sce) {
        registraClassiDatabase();
    }

    public void contextDestroyed(ServletContextEvent sce) {}
}