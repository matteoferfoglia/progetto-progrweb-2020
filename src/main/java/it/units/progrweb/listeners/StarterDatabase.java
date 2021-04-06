package it.units.progrweb.listeners;

import com.google.appengine.api.utils.SystemProperty;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.VoidWork;
import it.units.progrweb.entities.AuthenticationDatabaseEntry;
import it.units.progrweb.entities.attori.Attore;
import it.units.progrweb.entities.attori.administrator.Administrator;
import it.units.progrweb.entities.attori.consumer.Consumer;
import it.units.progrweb.entities.attori.uploader.Uploader;
import it.units.progrweb.persistence.DatabaseHelper;
import it.units.progrweb.persistence.NotFoundException;
import it.units.progrweb.utils.Logger;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.Arrays;

import static it.units.progrweb.entities.AuthenticationTokenInvalido.EliminatoreTokenInvalidi.avviaSchedulazionePeriodicaEliminazioneTokenInvalidi;
import static it.units.progrweb.entities.AuthenticationTokenInvalido.EliminatoreTokenInvalidi.rimuoviSchedulazionePeriodicaEliminazioneTokenInvalidi;
import static it.units.progrweb.entities.AuthenticationTokenInvalido.INTERVALLO_ESECUZIONE_TASK_ELIMINATORE_TOKEN_INVALIDI_IN_SECONDI;

/**
 * Starter del database per indicare quali classi
 * dovranno essere gestite come entità.
 *
 * @author Matteo Ferfoglia
 */
@WebListener
public class StarterDatabase implements ServletContextListener {

    /** Array di attori da creare e salvare nel DB in modalità di sviluppo.
     * Questi attori saranno giò presenti al primo accesso al sistema. */
    private final static AttoreConCredenziali[] attoriDaCreareInDevMod = {
            new AttoreConCredenziali("PPPPLT80A01A952G", "1234","consumerprova@example.com","Consumer di Prova", Attore.TipoAttore.Consumer),
            new AttoreConCredenziali("AB01", "5678","uploaderprova@example.com","Uploader di Prova", Attore.TipoAttore.Uploader),
            new AttoreConCredenziali("AdminTest", "9012","adminprova@example.com","Amministratore di Prova", Attore.TipoAttore.Administrator),
            new AttoreConCredenziali("admin", " 4famefo9p$#eMkw", "admin@example.com","Primo Admin", Attore.TipoAttore.Administrator)
    };

    /** Come {@link #attoriDaCreareInDevMod}, ma per la modalità di produzione. */
    private final static AttoreConCredenziali[] attoriDaCreareInProdMod = {
            new AttoreConCredenziali("admin", " 4famefo9p$#eMkw", "admin@example.com","Primo Admin", Attore.TipoAttore.Administrator)
    };


    // -------------- FINE PARAMETRI CONFIGURABILI --------------




    /** Array dei nomi delle classi corrispondenti alle entità da
     * registrare nel database. Utilizzare il <i>fully-qualified
     * name</i> delle classi.*/
    private final static String[] nomiEntitaGestiteDalDatabase = {
            "it.units.progrweb.entities.attori.Attore",
            "it.units.progrweb.entities.attori.administrator.AdministratorStorage",
            "it.units.progrweb.entities.attori.uploader.UploaderStorage",
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
            caricaEntitaInDB( attoriDaCreareInDevMod );
        } else {
            // Production
            // which is: SystemProperty.Environment.Value.Production

            // Da requisiti: All’avvio della piattaforma per la prima volta, vi sarà un solo utente amministratore.
            caricaEntitaInDB( attoriDaCreareInProdMod );

        }
    }

    /** Metodo per il salvataggio nel database delle entità contenute
     * nell'array passato come parametro. */
    private void caricaEntitaInDB( AttoreConCredenziali[] attoriDaSalvare ) {
        for( AttoreConCredenziali attore : attoriDaSalvare )
            attore.salvaInDB();
    }

    public void contextDestroyed(ServletContextEvent sce) {
        rimuoviSchedulazionePeriodicaEliminazioneTokenInvalidi(sce.getServletContext());
    }
}

/** Rappresentazione sintetica di un {@link Attore} e del corrispondente {@link AuthenticationDatabaseEntry}. */
class AttoreConCredenziali {

    /** L'{@link Attore} associato a quest'istanza. */
    private final Attore attore;

    /** L'istanza di {@link AuthenticationDatabaseEntry} rappresentante
     * le credenziali di {@link #attore}. */
    private final AuthenticationDatabaseEntry authenticationDatabaseEntry;

    public AttoreConCredenziali(String username, String password,
                                String email, String nominativo, Attore.TipoAttore tipoAttore ) {

        switch( tipoAttore ) {
            case Consumer:
                this.attore = Consumer.creaAttore(username, nominativo, email);
                break;
            case Uploader:
                this.attore = Uploader.creaAttore(username, nominativo, email);
                break;
            case Administrator:
                this.attore = Administrator.creaAttore(username, nominativo, email);
                break;
            default:
                throw new RuntimeException("Tipo attore non valido.");
        }

        try {
            this.authenticationDatabaseEntry =
                    new AuthenticationDatabaseEntry(username, password, true);
        } catch (InvalidKeyException|NoSuchAlgorithmException e) {
            throw new RuntimeException(e);  // eventuale eccezione compare a runtime (unchecked exception)
        }

    }

    public Attore getAttore() {
        return attore;
    }

    public AuthenticationDatabaseEntry getAuthenticationDatabaseEntry() {
        return authenticationDatabaseEntry;
    }

    /** Salva nel database l'{@link Attore} ed il corrispondente {@link AuthenticationDatabaseEntry}
     * rappresentato da quest'istanza oppure, se l'{@link Attore} o il corrispondente
     * {@link AuthenticationDatabaseEntry} sono già presente nel database, scrive l'eccezione
     * usando la classe {@link Logger}.*/
    public void salvaInDB() {

        // Fonte (usare Objectify fuori dal contesto di una request): https://stackoverflow.com/a/34484715
        ObjectifyService.run(

            new VoidWork() {
                @Override
                public void vrun() {

                    String usernameAttoreDaSalvare = getAttore().getUsername();

                    // Verifica se l'attore da salvare esiste già nel sistema
                    boolean attoreGiaNelSistema;
                    try {
                        AuthenticationDatabaseEntry.cercaAttoreInAuthDb( usernameAttoreDaSalvare );

                        // Se non vengono generate eccezioni, significa che l'attore è stato trovato in AuthDB
                        attoreGiaNelSistema = true;
                    } catch (NotFoundException notFoundException) {
                        // Se qui: attore non trovato in AuthDB
                        attoreGiaNelSistema = Attore.getAttoreDaUsername(usernameAttoreDaSalvare) != null;
                    }

                    if( !attoreGiaNelSistema ) {

                        DatabaseHelper.salvaEntita(getAttore());
                        DatabaseHelper.salvaEntita(getAuthenticationDatabaseEntry());

                    } else {
                        String messaggioErrore = "Attore " + usernameAttoreDaSalvare +
                                " o le sue credenziali sono già presenti nel Database";

                        Logger.scriviEccezioneNelLog(
                                StarterDatabase.class,
                                "Errore durante lo starter del database: " + messaggioErrore,
                                new SQLException( messaggioErrore )
                        );
                    }

                }
            }

        );

    }

}