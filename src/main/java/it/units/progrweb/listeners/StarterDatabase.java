package it.units.progrweb.listeners;

import com.googlecode.objectify.ObjectifyService;
import it.units.progrweb.entities.AuthenticationDatabaseEntry;
import it.units.progrweb.entities.attori.Attore;
import it.units.progrweb.entities.file.FileStorage;

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

    /** Array delle classi corrispondenti alle entità registrate nel database.*/
    private final static Class<?>[] enititaGestiteDaDatabase = {
            Attore.class,
            //Administrator.class,
            //Uploader.class,
            //Consumer.class,
            FileStorage.class,
            AuthenticationDatabaseEntry.class
    };

    /**
     * Registra tutte le classi che dovranno essere gestite dal database.
     * Attenzione: questo metodo è specifico solamente per Objectify.
     * Vedere {@link ObjectifyService#register(Class)} e
     * {@link com.googlecode.objectify.ObjectifyFactory#register(Class)}.
     */
    public static void registraClassiDatabase() {
        Arrays.stream(enititaGestiteDaDatabase)
              .forEach(ObjectifyService::register);
    }

    public void contextInitialized(ServletContextEvent sce) {
        registraClassiDatabase();
    }

    public void contextDestroyed(ServletContextEvent sce) {}
}