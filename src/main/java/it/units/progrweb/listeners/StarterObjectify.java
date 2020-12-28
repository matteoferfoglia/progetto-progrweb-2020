package it.units.progrweb.listeners;

import com.googlecode.objectify.ObjectifyService;
import it.units.progrweb.entities.Administrator;
import it.units.progrweb.entities.Attore;
import it.units.progrweb.entities.AuthenticationDatabaseEntry;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.util.Arrays;

/**
 * Starter di Objectify per indicare quali classi
 * dovranno essere gestite come entità.
 *
 * @author Matteo Ferfoglia
 */
@WebListener
public class StarterObjectify implements ServletContextListener {

    private final static Class<?>[] enitityClassesGestiteDaObjectify = {
            Attore.class,
            Administrator.class,
            AuthenticationDatabaseEntry.class
    };

    /**
     * Registra tutte le classi che dovranno essere gestite da Objectify.
     * Vedere {@link ObjectifyService#register(Class)} e
     * {@link com.googlecode.objectify.ObjectifyFactory#register(Class) ObjectifyFactory#register(Class)}.
     */
    public void contextInitialized(ServletContextEvent sce) {
        Arrays.stream(enitityClassesGestiteDaObjectify)
                .forEach(ObjectifyService::register);
    }

    public void contextDestroyed(ServletContextEvent sce) {

    }
}
