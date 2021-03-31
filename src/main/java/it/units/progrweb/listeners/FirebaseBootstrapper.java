package it.units.progrweb.listeners;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import it.units.progrweb.utils.Logger;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.io.File;
import java.io.FileInputStream;

/**
 * WebListener utilizzato per predisporre l'ambiente
 * all'utilizzo di Firebase all'avvio dell'istanza
 * dell'applicazione nel servlet container.
 * @author Matteo Ferfoglia
 */
@WebListener
public class FirebaseBootstrapper implements ServletContextListener {


    @Override
    public void contextInitialized(ServletContextEvent sce) {
        try {
            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setCredentials(GoogleCredentials.fromStream(new FileInputStream(new File("WEB-INF/credenzialiFirebase.json"))))
                    .build();
            FirebaseApp.initializeApp(options);
            Logger.scriviInfoNelLog(FirebaseBootstrapper.class,"Firebase operativo");
        } catch (Exception e) {
            Logger.scriviEccezioneNelLog(FirebaseBootstrapper.class, "Errore inizializzazione Firebase: " + e, e);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {}
}
