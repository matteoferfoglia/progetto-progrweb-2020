package it.units.progrweb.listeners;

import com.google.appengine.api.utils.SystemProperty;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.VoidWork;
import it.units.progrweb.entities.AuthenticationDatabaseEntry;
import it.units.progrweb.entities.ProprietaSistema;
import it.units.progrweb.entities.attori.Attore;
import it.units.progrweb.entities.attori.AttoreProxy;
import it.units.progrweb.entities.attori.administrator.Administrator;
import it.units.progrweb.entities.attori.consumer.Consumer;
import it.units.progrweb.entities.attori.uploader.Uploader;
import it.units.progrweb.persistence.DatabaseHelper;
import it.units.progrweb.persistence.NotFoundException;
import it.units.progrweb.utils.JsonHelper;
import it.units.progrweb.utils.Logger;
import it.units.progrweb.utils.UtilitaGenerale;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;

import static it.units.progrweb.entities.AuthenticationTokenInvalido.EliminatoreTokenInvalidi.avviaSchedulazionePeriodicaEliminazioneTokenInvalidi;
import static it.units.progrweb.entities.AuthenticationTokenInvalido.EliminatoreTokenInvalidi.rimuoviSchedulazionePeriodicaEliminazioneTokenInvalidi;
import static it.units.progrweb.entities.AuthenticationTokenInvalido.INTERVALLO_ESECUZIONE_TASK_ELIMINATORE_TOKEN_INVALIDI_IN_SECONDI;

/**
 * Starter del database per indicare quali classi
 * dovranno essere gestite come entità e verificare
 * che nel database siano presenti (altrimenti vengono
 * aggiunti) gli attori richiesti.
 *
 * @author Matteo Ferfoglia
 */
@WebListener
public class StarterDatabase implements ServletContextListener {

    /** Array di attori da creare e salvare nel DB in modalità di sviluppo.
     * In modalità di sviluppo verranno salvati anche quelli marcati per
     * la modalità di produzione.
     * Questi attori saranno giò presenti al primo accesso al sistema. */
    private final static AttoreConCredenziali[] attoriDaCreareInDevMod = {
            new AttoreConCredenziali("PPPPLT80A01A952G", "1234","consumerprova@example.com","Consumer di Prova", Attore.TipoAttore.Consumer),
            new AttoreConCredenziali("AB01", "5678","uploaderprova@example.com","Uploader di Prova", Attore.TipoAttore.Uploader),
            new AttoreConCredenziali("AdminTest", "9012","adminprova@example.com","Amministratore di Prova", Attore.TipoAttore.Administrator),
    };

    /** Percorso del file contenente le credenziali del primo utente amministratore
     * da inserire nel database, in Production mode (come da requisiti). */
    private final static String fileCredenzialiPrimoAdmin = "WEB-INF/credenziali/credenzialiPrimoAmministratoreAllAvvioDellaPiattaforma.json";

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
            "it.units.progrweb.entities.file.FileStorage",
            "it.units.progrweb.entities.ProprietaSistema"
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
            salvaAttoreInDB( attoriDaCreareInDevMod );
        }



        // Sia Production sia Development

        // Da requisiti: All’avvio della piattaforma per la prima volta, vi sarà un solo utente amministratore.
        // Proprietà attore caricate da file (password non esposta su repository pubblica)
        ObjectifyService.run(
                // Fonte (usare Objectify fuori dal contesto di una request): https://stackoverflow.com/a/34484715
                new VoidWork() {
                    @Override
                    public void vrun() {

                        ProprietaSistema.incrementaNumeroIstanze();

                        if( ProprietaSistema.getNumeroIstanze()==1 ) {
                            // Se qui, questo è il primo avvio della piattaforma
                            try {

                                AttoreConCredenziali[] attoriDaCreareInProdMod = new AttoreConCredenziali[]{
                                        new AttoreConCredenziali(fileCredenzialiPrimoAdmin)
                                };
                                salvaAttoreInDB(attoriDaCreareInProdMod);

                            } catch (IOException e) {
                                Logger.scriviEccezioneNelLog(
                                        StarterDatabase.class,
                                        "Errore nella lettura della credenziali.",
                                        e
                                );
                            }
                        }

                    }
                }
        );



    }

    /** Metodo per il salvataggio nel database delle entità contenute
     * nell'array passato come parametro. */
    private void salvaAttoreInDB(AttoreConCredenziali[] attoriDaSalvare ) {
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
    private Attore attore;

    /** L'istanza di {@link AuthenticationDatabaseEntry} rappresentante
     * le credenziali di {@link #attore}. */
    private AuthenticationDatabaseEntry authenticationDatabaseEntry;

    public AttoreConCredenziali(String username, String password,
                                String email, String nominativo, Attore.TipoAttore tipoAttore ) {

        creaAttore(username, password, email, nominativo, tipoAttore);

    }

    /** Da usare nel costruttore.*/
    private void creaAttore( String username, String password,
                             String email, String nominativo, Attore.TipoAttore tipoAttore ) {

        try {

            // Costruzione dell'attore
            final Attore attore;
            switch(tipoAttore) {
                case Consumer:
                    attore = Consumer.creaAttore(username, nominativo, email);
                    break;
                case Uploader:
                    attore = Uploader.creaAttore(username, nominativo, email);
                    break;
                case Administrator:
                    attore = Administrator.creaAttore(username, nominativo, email);
                    break;
                default:
                    throw new RuntimeException("Tipo attore non valido.");
            }
            this.attore = attore;

            // Costruzione del Database entry
            this.authenticationDatabaseEntry =
                    new AuthenticationDatabaseEntry(username, password, true);

        } catch (Exception e) {
            Logger.scriviEccezioneNelLog(StarterDatabase.class, "Errore nella creazione di un attore.", e);
            throw new RuntimeException(e);  // eventuale eccezione compare a runtime (unchecked exception)
        }

    }

    /** Dato il percorso del file JSON con le credenziali e le proprietà
     * di un {@link Attore} da creare, crea un'istanza di questa classe. */
    public AttoreConCredenziali(String percorsoFileJson)
            throws IOException {

        String contenutoJson = UtilitaGenerale.leggiContenutoFile(percorsoFileJson);

        // Stesso file contiene sia proprietà dell'attore sia le credenziali
        Attore attoreDaInput = JsonHelper.convertiOggettoDaJSON(contenutoJson, AttoreProxy.class);

        CredenzialiUsernamePassword credenzialiUsernamePassword =
                JsonHelper.convertiOggettoDaJSON(contenutoJson, CredenzialiUsernamePassword.class);

        creaAttore(
                credenzialiUsernamePassword.getUsername(),
                credenzialiUsernamePassword.getPassword(),
                attoreDaInput.getEmail(),
                attoreDaInput.getNominativo(),
                Attore.TipoAttore.valueOf(attoreDaInput.getTipoAttore())
        );

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

        ObjectifyService.run(

                new VoidWork() {
                    @Override
                    public void vrun() {

                        String usernameAttoreDaSalvare = getAttore().getUsername();

                        // Verifica se l'attore da salvare esiste già nel sistema
                        Attore attoreInDB = Attore.getAttoreDaUsername(usernameAttoreDaSalvare);
                        AuthenticationDatabaseEntry authEntry;
                        try {
                            authEntry = AuthenticationDatabaseEntry.cercaAttoreInAuthDb( usernameAttoreDaSalvare );
                            // Se non vengono generate eccezioni, significa che l'attore è stato trovato in AuthDB
                        } catch (NotFoundException ignored) {
                            // Se qui: attore non trovato in AuthDB
                            authEntry=null;
                        }

                        if( attoreInDB==null && authEntry==null ) {

                            DatabaseHelper.salvaEntita(getAttore());
                            DatabaseHelper.salvaEntita(getAuthenticationDatabaseEntry());

                        } else if( attoreInDB==null ^ authEntry==null ) {   // xor
                            String messaggioErrore = "Attore " + usernameAttoreDaSalvare +
                                    " o le sue credenziali sono già presenti nel Database," +
                                    " ma non entrambi.";

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

/** Classe di comodo per deserializzazione JSON (in Database credenziali salvate Hashed&Salted).*/
@SuppressWarnings("unused") // campi usati per de/serializzazione (serve costruttore vuoto e getter/setter)
class CredenzialiUsernamePassword {

    String username;
    String password;

    CredenzialiUsernamePassword() throws NoSuchFieldException {

        // Verifica nomi attributi corretti
        String nomeAttr_username = "username";
        String nomeAttr_password = "password";

        if( ! ( UtilitaGenerale.esisteAttributoInClasse(nomeAttr_username, this.getClass()) &&
                UtilitaGenerale.esisteAttributoInClasse(nomeAttr_password, this.getClass())    ) ) {

            throw new NoSuchFieldException("Verificare i nomi degli attributi (\""+nomeAttr_username+"\" e \""+ nomeAttr_password +"\")");

        }
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}