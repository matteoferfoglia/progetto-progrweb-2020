package it.units.progrweb.persistence;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.googlecode.objectify.ObjectifyFactory;
import com.googlecode.objectify.ObjectifyService;
import it.units.progrweb.UtilsInTest;
import it.units.progrweb.entities.AuthenticationDatabaseEntry;
import it.units.progrweb.entities.attori.Attore;
import it.units.progrweb.listeners.StarterDatabase;
import it.units.progrweb.utils.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static it.units.progrweb.UtilsInTest.fallisciTestACausaDiEccezioneNonAttesa;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.params.provider.Arguments.arguments;

/**
 * Classe di test per lettura/scrittura nel database.
 * Test per AppEngine tratti dalla
 * <a href="https://cloud.google.com/appengine/docs/standard/java/tools/localunittesting#datastore-memcache">
 *   documentazione di AppEngine
 * </a> e da <a href="https://stackoverflow.com/a/36247734">Stackoverflow</a>.
 * @author Matteo Ferfoglia
 */
public class DatabaseTest {

    /** Fattore di replicazione delle istanze di test create in {@link #generaEntitaDaSalvareNelDatabase()}.*/
    final static int NUMERO_RIPETIZIONI_OGNI_TEST = 1000;

    // ------------ SETUP APPENGINE e OBJECTIFY ------------

    protected Closeable session;

    private final LocalServiceTestHelper helper =
            new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());

    /** Starter del database.*/
    @BeforeEach
    public void starterDatabase() {
        ObjectifyService.setFactory(new ObjectifyFactory());    // Specifico per Objectify (Fonte: https://stackoverflow.com/a/36247734)
        this.session = ObjectifyService.begin();    // Specifico per Objectify (Fonte: https://stackoverflow.com/a/36247734)
        try {
            Method registrazioneClassiDatabase = StarterDatabase.class.getDeclaredMethod("registraClassiDatabase");
            registrazioneClassiDatabase.setAccessible(true);
            registrazioneClassiDatabase.invoke(null);
            this.helper.setUp();
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            UtilsInTest.fallisciTestACausaDiEccezioneNonAttesa(e);
        }
    }

    /** Chiusura. */
    @AfterEach
    public void tearDown() {
        try {
            this.session.close();
        } catch (IOException e) {
       //     UtilsInTest.fallisciTestACausaDiEccezioneNonAttesa(e);
        }
        this.helper.tearDown();
    }

    // ------------ Fine setup ------------


    /** Valore usato in {@link #generaEntitaDaSalvareNelDatabase()} per creare
     * gli id delle entità.*/
    private static long valoreIdIncrementale = 0;

    /**
     * Genera un oggetto per ognuna delle entità registrate nel database.
     * @return Stream in cui ogni elemento è un argomento, che ha come
     *          primo elemento la classe dell'oggetto e come secondo
     *          elemento l'oggetto generato da questo metodo.
     */
    private static Stream<Arguments> generaEntitaDaSalvareNelDatabase() {

        try {
            Field nomiEntitaGestiteDalDatabase_field = StarterDatabase.class.getDeclaredField("nomiEntitaGestiteDalDatabase");
            nomiEntitaGestiteDalDatabase_field.setAccessible(true);
            String[] nomiClassiGestiteDalDatabase = (String[]) nomiEntitaGestiteDalDatabase_field.get(null); // null perché la classe è statica

            Class[] classiGestiteDalDatabase = Arrays.stream(nomiClassiGestiteDalDatabase)
                                                               .map(nomeClasse -> {
                                                                   try { return Class.forName(nomeClasse); }
                                                                   catch (ClassNotFoundException e) {
                                                                       Logger.scriviEccezioneNelLog(DatabaseTest.class,e);
                                                                       fail("Nome classe \"" + nomeClasse + "\" non trovato", e);
                                                                       return null;
                                                                   }
                                                               }).toArray(Class[]::new);

            return Arrays.stream(classiGestiteDalDatabase)
                         .flatMap(classeEntita -> IntStream.range(0, NUMERO_RIPETIZIONI_OGNI_TEST)   // replica istanze
                                                           .mapToObj(contatoreRipetizioni -> classeEntita) )
                         .map(classeEntita -> {
                             try {

                                 Constructor costruttore = classeEntita.getDeclaredConstructor();
                                 costruttore.setAccessible(true);
                                 Object nuovaEntita = costruttore.newInstance();

                                 // Per gli attori, bisogna impostare ID (String non automaticamente impostato da Objectify)
                                 if( classeEntita.getName().contains("attori") ) {
                                     Field id_field = Attore.class.getDeclaredField("username");
                                     id_field.setAccessible(true);
                                     id_field.set(nuovaEntita, String.valueOf(valoreIdIncrementale++));
                                 }
                                 if( classeEntita.getName().contains("AuthenticationDatabaseEntry") ) {
                                     Field id_field = AuthenticationDatabaseEntry.class.getDeclaredField("username");
                                     id_field.setAccessible(true);
                                     id_field.set(nuovaEntita, String.valueOf(valoreIdIncrementale++));
                                 }


                                 return arguments(classeEntita, nuovaEntita);

                             } catch (NoSuchMethodException | NoSuchFieldException |
                                     IllegalAccessException | InvocationTargetException e) {
                                 return (Arguments)fallisciTestACausaDiEccezioneNonAttesa(e);
                             } catch (InstantiationException e) {
                                 // Se qui, la classe era astratta (quindi non poteva istanziare nuovi oggetti)
                                 return arguments(Object.class, new Object());
                             }
                         });

        } catch (NoSuchFieldException | IllegalAccessException e) {
            return (Stream<Arguments>) fallisciTestACausaDiEccezioneNonAttesa(e);
        }

    }

    /** Test per {@link DatabaseHelper#salvaEntita(java.lang.Object)}.*/
    @ParameterizedTest
    @MethodSource("generaEntitaDaSalvareNelDatabase")
    void testSalvataggioEntita(Class classeEntita, Object entita){

        if(! classeEntita.equals(Object.class)) {   // il @MethodSource genera Object in caso di classi astratte, ma non sono entità (evitate con questo if)

            assertEquals(0, DatabaseHelper.contaEntitaNelDatabase());   // 0 entità salvate all'inizializzazione del datastore

            DatabaseHelper.salvaEntita(entita);
            DatabaseHelper.completaOra(); // svuota transazioni in corso  // TODO : non funziona: a volte questo test fallisce

            assertEquals(1, DatabaseHelper.contaEntitaNelDatabase());

        }

    }


    /** Test per {@link DatabaseHelper#salvaEntita(Object)}.*/
    @ParameterizedTest
    @MethodSource("generaEntitaDaSalvareNelDatabase")
    void testSalvataggioImmediatoEntita(Class classeEntita, Object entita){

        if(! classeEntita.equals(Object.class)) {   // il @MethodSource genera Object in caso di classi astratte, ma non sono entità (evitate con questo if)

            assertEquals(0, DatabaseHelper.contaEntitaNelDatabase());   // 0 entita salvate all'inizializzazione del datastore

            DatabaseHelper.salvaEntita(entita);

            assertEquals(1, DatabaseHelper.contaEntitaNelDatabase());

        }

    }

}
