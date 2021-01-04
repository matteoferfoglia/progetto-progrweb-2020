package it.units.progrweb.listeners;

import it.units.progrweb.entities.AuthenticationDatabaseEntry;
import it.units.progrweb.entities.attori.Administrator;
import it.units.progrweb.entities.attori.Attore;
import it.units.progrweb.persistence.DatabaseHelper;

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
            Administrator.class,
            AuthenticationDatabaseEntry.class
    };

    /**
     * Registra tutte le classi che dovranno essere gestite dal database.
     * Vedere {@link DatabaseHelper#registraEntitaGestitaDaDatabase(Class)}.
     */
    public static void registraClassiDatabase() {
        Arrays.stream(enititaGestiteDaDatabase)
                .forEach(DatabaseHelper::registraEntitaGestitaDaDatabase);
    }

    public void contextInitialized(ServletContextEvent sce) {
        registraClassiDatabase();
    }

    public void contextDestroyed(ServletContextEvent sce) {

    }
}
