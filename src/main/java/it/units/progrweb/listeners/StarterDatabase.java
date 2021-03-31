package it.units.progrweb.listeners;

import com.google.appengine.api.utils.SystemProperty;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.VoidWork;
import it.units.progrweb.entities.AuthenticationDatabaseEntry;
import it.units.progrweb.entities.RelazioneUploaderConsumer;
import it.units.progrweb.entities.attori.Attore;
import it.units.progrweb.persistence.DatabaseHelper;
import it.units.progrweb.persistence.NotFoundException;
import it.units.progrweb.utils.Logger;
import it.units.progrweb.utils.UtilitaGenerale;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import static it.units.progrweb.entities.AuthenticationTokenInvalido.EliminatoreTokenInvalidi.*;

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
    private final static String[] nomiEntitaGestiteDalDatabase = {
            // TODO : non basta mettere gli storage?
            "it.units.progrweb.entities.attori.Attore",
            "it.units.progrweb.entities.attori.administrator.Administrator",
            "it.units.progrweb.entities.attori.administrator.AdministratorStorage",
            "it.units.progrweb.entities.attori.uploader.Uploader",
            "it.units.progrweb.entities.attori.uploader.UploaderStorage",
            "it.units.progrweb.entities.attori.consumer.Consumer",
            "it.units.progrweb.entities.attori.consumer.ConsumerStorage",
            "it.units.progrweb.entities.RelazioneUploaderConsumer",
            "it.units.progrweb.entities.AuthenticationDatabaseEntry",
            "it.units.progrweb.entities.AuthenticationTokenInvalido",
            "it.units.progrweb.entities.file.File",
            "it.units.progrweb.entities.file.FileStorage"
    };

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
                      Class<?> classeDaRegistrare = Class.forName(nomeClasse);
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
        avviaSchedulazionePeriodicaEliminazioneTokenInvalidi(INTERVALLO_ESECUZIONE_TASK_ELIMINATORE_TOKEN_INVALIDI_IN_SECONDI, sce.getServletContext());

        // Inizializzazione del database SOLO in ambiente di sviluppo
        // Fonte: https://cloud.google.com/appengine/docs/standard/java/tools/using-local-server#detecting_the_application_runtime_environment
        if (SystemProperty.environment.value() == SystemProperty.Environment.Value.Development) {
            // Local development server

            // Caricamento di alcune entità di prova

            try {

                // Creazione delle entità di test
                Object consumerTest, uploaderTest, administratorTest;
                AuthenticationDatabaseEntry authConsumerTest, authUploaderTest, authAdministratorTest;

                {
                    // Creazione delle entità (oggetti Java)

                    {
                        // Creazione consumer
                        Class<?> consumerStorage = Class.forName("it.units.progrweb.entities.attori.consumer.ConsumerStorage");
                        Constructor<?> constructorConsumerStorage = consumerStorage.getDeclaredConstructor(String.class, String.class, String.class);
                        constructorConsumerStorage.setAccessible(true);
                        consumerTest = constructorConsumerStorage.newInstance("PPPPLT80A01A952G", "Pippo Pluto", "matteoferfoglia3@gmail.com");
                        authConsumerTest = new AuthenticationDatabaseEntry("PPPPLT80A01A952G","1234");
                    }

                    {
                        // Creazione Uploader
                        Class<?> uploaderStorage = Class.forName("it.units.progrweb.entities.attori.uploader.UploaderStorage");
                        Constructor<?> constructorUploaderStorage = uploaderStorage.getDeclaredConstructor(String.class, String.class, String.class, byte[].class, String.class);
                        constructorUploaderStorage.setAccessible(true);
                        uploaderTest = constructorUploaderStorage.newInstance("AB01", "Banca Prova", "bancaprova@example.com",
                                UtilitaGenerale.convertiInputStreamInByteArray(sce.getServletContext().getResourceAsStream("/favicon.ico") ), "ico");
                        authUploaderTest = new AuthenticationDatabaseEntry("AB01","5678");
                    }

                    {
                        // Creazione Administrator
                        Class<?> administratorStorage = Class.forName("it.units.progrweb.entities.attori.administrator.AdministratorStorage");
                        Constructor<?> constructorAdministratorStorage = administratorStorage.getDeclaredConstructor(String.class, String.class, String.class);
                        constructorAdministratorStorage.setAccessible(true);
                        administratorTest = constructorAdministratorStorage.newInstance("AdminTest", "Mario l'Amministratore", "marioadmin@example.com");
                        authAdministratorTest = new AuthenticationDatabaseEntry("AdminTest","9012");
                    }

                }

                // Salvataggio nel database locale
                ObjectifyService.run(new VoidWork() {
                    // Fonte(usare Objectify fuori dal contesto di una request): https://stackoverflow.com/a/34484715
                    public void vrun() {
                        Long idConsumer = (Long)DatabaseHelper.salvaEntita(consumerTest);
                        Long idUploader = (Long)DatabaseHelper.salvaEntita(uploaderTest);

                        try {
                            // Creazione relazione consumer-uploader
                            Constructor<RelazioneUploaderConsumer> costruttoreRelazione;
                            costruttoreRelazione = RelazioneUploaderConsumer.class
                                    .getDeclaredConstructor(Long.class, Long.class, Long.class);
                            costruttoreRelazione.setAccessible(true);
                            RelazioneUploaderConsumer relazione = costruttoreRelazione.newInstance(idConsumer, idUploader, null);

                            Arrays.stream(new Object[]{administratorTest, relazione, authConsumerTest, authUploaderTest, authAdministratorTest})
                                .forEach( DatabaseHelper::salvaEntita );

                        } catch (InstantiationException|IllegalAccessException|
                                InvocationTargetException|NoSuchMethodException e) {
                            e.printStackTrace();
                        }
                    }
                });

            } catch (InvalidKeyException | NoSuchAlgorithmException |
                    ClassNotFoundException | NoSuchMethodException |
                    InstantiationException | IllegalAccessException |
                    InvocationTargetException e) {
                Logger.scriviEccezioneNelLog(StarterDatabase.class, e);
            }

        } else {
            // Production
            // which is: SystemProperty.Environment.Value.Production

            // Da requisiti: All’avvio della piattaforma per la prima volta, vi sarà un solo utente amministratore.
            // TODO : da testare in ambiente di produzione

            try {

                // Creazione Administrator e relativa entry dell'AuthDB
                Class<?> administratorStorage = Class.forName("it.units.progrweb.entities.attori.administrator.AdministratorStorage");
                Constructor<?> constructorAdministratorStorage = administratorStorage.getDeclaredConstructor(String.class, String.class, String.class);
                constructorAdministratorStorage.setAccessible(true);
                Attore administrator = (Attore) constructorAdministratorStorage.newInstance("AdminTest", "Mario l'Amministratore", "marioadmin@example.com");
                AuthenticationDatabaseEntry authAdministrator = new AuthenticationDatabaseEntry("AdminTest","9012", true);

                // Salvataggio nel database
                if( Attore.getAttoreDaUsername( administrator.getUsername() ) == null  ) {

                    DatabaseHelper.salvaEntita( administrator );

                    // Controllo se entry esiste già in AuthDB
                    try {
                        AuthenticationDatabaseEntry authenticationDatabaseEntry =
                                AuthenticationDatabaseEntry.cercaAttoreInAuthDb(administrator.getUsername());

                        // Se qui, significa che l'entry esisteva già in AuthDB, quindi lo si rimuove e sovrascrive
                        //  (incoerente che l'attore non era salvato ma la sua password sì)
                        AuthenticationDatabaseEntry.eliminaEntry( administrator.getUsername() );

                    } catch (NotFoundException ignored) {}

                    DatabaseHelper.salvaEntita( authAdministrator );

                } // altrimenti: attore già in DB, non serve reinserirlo


            }catch ( ClassNotFoundException | NoSuchMethodException | InstantiationException |
                      IllegalAccessException | InvocationTargetException | InvalidKeyException |
                      NoSuchAlgorithmException e ) {

                Logger.scriviEccezioneNelLog( StarterDatabase.class, e );

            }

        }
    }

    public void contextDestroyed(ServletContextEvent sce) {
        rimuoviSchedulazionePeriodicaEliminazioneTokenInvalidi(sce.getServletContext());
    }
}