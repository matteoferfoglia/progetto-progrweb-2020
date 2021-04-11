package it.units.progrweb.listeners;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import it.units.progrweb.utils.Logger;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.io.FileInputStream;

/**
 * WebListener utilizzato per predisporre l'ambiente
 * all'utilizzo di Firebase all'avvio dell'istanza
 * dell'applicazione nel servlet container.
 * @author Matteo Ferfoglia
 */
@WebListener
public class FirebaseBootstrapper implements ServletContextListener {

    /** Percorso del file contenente le credenziali Firebase. */
    private final static String percorsoFileCredenzialieFirebase = "WEB-INF/credenziali/credenzialiFirebase.json";


    @Override
    public void contextInitialized(ServletContextEvent sce) {
        try {
            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setCredentials(GoogleCredentials.fromStream(new FileInputStream(percorsoFileCredenzialieFirebase)))
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
