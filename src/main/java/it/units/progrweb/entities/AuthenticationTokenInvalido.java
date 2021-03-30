package it.units.progrweb.entities;

import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.VoidWork;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;
import it.units.progrweb.persistence.DatabaseHelper;
import it.units.progrweb.utils.Logger;
import it.units.progrweb.utils.datetime.DateTime;
import it.units.progrweb.utils.jwt.JwtToken;
import it.units.progrweb.utils.jwt.componenti.claims.JwtClaim;
import it.units.progrweb.utils.jwt.componenti.claims.JwtExpirationTimeClaim;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * Classe rappresentante un token di autenticazione invalido.
 * Quest'entità viene memorizzata per tenere traccia dei token
 * di autenticazione i quali, seppure formalmente validi (non
 * scaduti e con firma verificata) non devono essere accettati
 * da questo server (ad esempio, se l'utente richiede il logout,
 * allora il suo token non deve più essere accettato, anche se
 * ancora valido). Memorizzare i token non validi rappresenta
 * un ulteriore garanzia di sicurezza.
 * L'utilizzo di questa classe è molto oneroso in termini di
 * accessi al database, motivo per cui può essere disattivato
 * impostando il parametro {@link #USARE_CLASSE_TOKEN_INVALIDI}.
 * (se disattivato, la verifica di validità di un token avrà sempre
 * esito positivo).
 *
 * Periodicamente (grazie a {@link ScheduledExecutorService},
 * i token scaduti vengono rimossi dal database
 * (<a href="https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/ScheduledExecutorService.html">Fonte</a>).
 *
 * @author Matteo Ferfoglia
 */
@Entity
public class AuthenticationTokenInvalido {

    /** Flag per abilitare l'utilizzo di questa classe
     * L'utilizzo di questa classe comporta un livello di sicurezza in più,
     * in quanto si verifica che il token di autenticazione di ogni richiesta
     * sia ancora valido (e che non venga utilizzato dopo il logout dell'utente,
     * ad esempio). In realtà questo servizio è più generale e gestisce qualsiasi
     * token utilizzato dall'applicazione (non solo quelli di autenticazione).
     * Questo servizio risulta molto oneroso, perché verificare la validità di
     * un token richiede un accesso in lettura al database (ed invalidare un
     * token richiede un accesso in scrittura): per questo motivo, questo parametro,
     * se impostato a false, disattiva questa classe, nel senso che non usa alcun
     * database e che quando si richiede la verifica di validità di un token
     * risponde sempre affermativamente.
     */
    public final static boolean USARE_CLASSE_TOKEN_INVALIDI = true; // TODO : mettere false (molto oneroso in termini di accessi al db), possibilmente spostarlo in Environment Variables


    /** Id di un token invalido, assegnato dal DBMS. */
    @Id
    @SuppressWarnings("unused")             // valore generato da Datastore
    private Long idTokenInvalido;

    /** Token da considerare invalido. */
    @Index
    @SuppressWarnings("FieldCanBeLocal")    // deve poter essere salvata in DB
    private String tokenInvalido;

    /** Scadenza del token in secondi, rispetto a Epoch time (1 January 1970, 00:00:00 UTC). */
    @Index
    @SuppressWarnings("FieldCanBeLocal")    // deve poter essere salvata in DB
    private Long scadenzaTokenInSecondiDaEpoch;


    /** Salva nel database il token passato come parametro.
     * Tale token verrà considerato invalido (da subito). */
    private AuthenticationTokenInvalido(String jwtTokenComeStringa) {

        this.tokenInvalido = jwtTokenComeStringa;

        try {
            this.scadenzaTokenInSecondiDaEpoch = new JwtExpirationTimeClaim(
                    JwtToken.creaJwtTokenDaStringaCodificata(jwtTokenComeStringa)
                            .getPayload()
                            .getClaimByName(JwtClaim.NomeClaim.EXP)
            ).getValue();
        } catch (IllegalArgumentException ignored) {    // generato da JwtToken.creaJwtTokenDaStringaCodificata se stringa invalida
            this.scadenzaTokenInSecondiDaEpoch = Long.MAX_VALUE;
        }

    }

    @SuppressWarnings("unused") // usato da Objectify
    private AuthenticationTokenInvalido() {}

    public Long getIdTokenInvalido() {
        return idTokenInvalido;
    }

    /** Aggiunge un token JWT (rappresentato come stringa) al database di quelli invalidi. */
    public static void aggiungiATokenJwtInvalidi(String jwtTokenDaAggiungereAQuelliInvalidi) {

        if( USARE_CLASSE_TOKEN_INVALIDI &&
                JwtToken.isTokenValido(jwtTokenDaAggiungereAQuelliInvalidi) ) {
            // Se token già scaduto (o in generale invalido) non serve neanche salvarlo
            // tra i token invalidi (non verrebbe comunque accettato)

            AuthenticationTokenInvalido tokenInvalido =
                    new AuthenticationTokenInvalido(jwtTokenDaAggiungereAQuelliInvalidi);

            DatabaseHelper.salvaEntita(tokenInvalido);

        }

    }

    /** Restituisce true se il token dato è presente nel database dei token invalidi.
     * Restituisce sempre false se {@link #USARE_CLASSE_TOKEN_INVALIDI} è false.*/
    public static boolean isTokenInvalido( String token ) {

        if( USARE_CLASSE_TOKEN_INVALIDI ) {

            final String nomeAttributoContenenteToken = "tokenInvalido";

            try {

                AuthenticationTokenInvalido.class.getDeclaredField(nomeAttributoContenenteToken);   // check se attributo esiste, altrimenti genera NoSuchFieldException

                return DatabaseHelper
                        .query(AuthenticationTokenInvalido.class, nomeAttributoContenenteToken, DatabaseHelper.OperatoreQuery.UGUALE, token)
                        .size() > 0;    // se vero, significa che il token è presente nel db dei token invalidi

            } catch (NoSuchFieldException e) {
                Logger.scriviEccezioneNelLog(AuthenticationTokenInvalido.class,
                        "Attributo " + nomeAttributoContenenteToken +
                                " non trovato nella classe " + AuthenticationTokenInvalido.class.getName() +
                                ". Potrebbe essere stato modificato il nome.",
                        e);

                return true;    // errore del server, per sicurezza dice che il token è invalido
            }

        } else {
            return false;    // se si disattiva l'utilizzo della classe, la verifica restituisce sempre false.
        }

    }


    /** Classe che gestisce l'elimiminazione di token scaduti, che quindi verrebbero
     * in ogni caso rifiutati dal server, indipendentemente dalla loro presenza in
     * questo database
     * (<a href="https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/ScheduledExecutorService.html">Fonte</a>).
     */
    public static class EliminatoreTokenInvalidi {

        /** Specifica ogni quanti secondi deve essere eseguito il task che si occupa di
         * eliminare i token invalidi dal database associato a questa classe. */
//        public static final long INTERVALLO_ESECUZIONE_TASK_ELIMINATORE_TOKEN_INVALIDI_IN_SECONDI = 3600;
        public static final long INTERVALLO_ESECUZIONE_TASK_ELIMINATORE_TOKEN_INVALIDI_IN_SECONDI = 10;   // TODO : rimuovere quest riga e lasciare scommentata la precdente

        /** Scheduler per gestire la rimozione periodica dei token invalidi dal database. */
        private static final ScheduledExecutorService scheduler =
                Executors.newScheduledThreadPool(1);    // newScheduledThreadPool() creates a thread pool that can schedule commands to run after a given delay, or to execute periodically, Fonte: https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/Executors.html#newScheduledThreadPool-int-

        /** Task da eseguire periodicamente per eliminare i token scaduti. */
        private static ScheduledFuture<?> gestoreEliminatoreTokenInvalidi;

        /** Metodo invocabile dallo starter del servlet container e da eseguire periodicamente
         * (ogni quanti secondi è specificato dal parametro) per cercare nel database ed eliminare
         * da esso i token non più validi
         * (<a href="https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/ScheduledExecutorService.html">Fonte</a>).
         *
         * @param ogniQuantiSecondiEseguire Tempo in secondi ogni quanto eseguire questo metodo.*/
        public static void avviaSchedulazionePeriodicaEliminazioneTokenInvalidi(Long ogniQuantiSecondiEseguire ) {

            if( USARE_CLASSE_TOKEN_INVALIDI ) {

                if (gestoreEliminatoreTokenInvalidi == null) {   // se già stato inizializzato, significa che questo metodo è già stato invocato

                    final String nomeAttributoContenenteScadenzaToken = "scadenzaTokenInSecondiDaEpoch";

                    try {

                        // test se il nome dell'attributo esiste nella classe (altrimenti genera NoSuchFieldException)
                        AuthenticationTokenInvalido.class.getDeclaredField(nomeAttributoContenenteScadenzaToken);

                        // Task che si occupa di eliminare i token scaduti dal database dei token invalidi
                        final Runnable eliminatoreTokenScadutiDaDBTokenInvalidi = () -> {     // TODO : da testare


                            Logger.scriviEccezioneNelLog(AuthenticationTokenInvalido.class, DateTime.currentTimeInSecondi()+"   FUNZIONA", new Exception("FUNZIONA"));//TODO CANCELLA questa riga
                            // Necessario usare ObjectifyService perché questo codice NON sarà eseguito
                            //  nel contesto di una request (quindi Objectify Filter non verrà eseguito)
                            ObjectifyService.run(new VoidWork() {
                                // Fonte(usare Objectify fuori dal contesto di una request): https://stackoverflow.com/a/34484715
                                public void vrun() {

                                    Logger.scriviEccezioneNelLog(AuthenticationTokenInvalido.class, DateTime.currentTimeInSecondi()+"   FUNZIONA2", new Exception("FUNZIONA2"));//TODO CANCELLA questa riga

                                    DatabaseHelper
                                            .query(AuthenticationTokenInvalido.class,
                                                    nomeAttributoContenenteScadenzaToken,
                                                    DatabaseHelper.OperatoreQuery.MINORE,
                                                    DateTime.currentTimeInSecondi()) // restituisce i token scaduti al momento in cui viene eseguito // TODO : sicuro che fa riferimento il momento in cui viene eseguito e non quando viene instanziato?
                                            .forEach(unIstanzaDaEliminare -> {
                                                DatabaseHelper.cancellaEntitaById((
                                                        (AuthenticationTokenInvalido) unIstanzaDaEliminare).getIdTokenInvalido(),
                                                        AuthenticationTokenInvalido.class
                                                );
                                            });
                                }
                            });

                        };

                        // Schedula il task da eseguire ogniQuantiSecondiEseguire
                        gestoreEliminatoreTokenInvalidi =
                                scheduler.scheduleAtFixedRate(
                                        eliminatoreTokenScadutiDaDBTokenInvalidi,
                                        ogniQuantiSecondiEseguire,
                                        ogniQuantiSecondiEseguire,
                                        SECONDS
                                );   // scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit) Creates and executes a periodic action that becomes enabled first after the given initial delay, and subsequently with the given period; that is executions will commence after initialDelay then initialDelay+period, then initialDelay + 2 * period, and so on. Fonte: https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/ScheduledExecutorService.html#scheduleAtFixedRate-java.lang.Runnable-long-long-java.util.concurrent.TimeUnit-

                    } catch (NoSuchFieldException e) {
                        Logger.scriviEccezioneNelLog(AuthenticationTokenInvalido.class,
                                "Attributo " + nomeAttributoContenenteScadenzaToken +
                                        " non trovato nella classe " + AuthenticationTokenInvalido.class.getName() +
                                        ". Potrebbe essere stato modificato il nome.",
                                e);
                    }

                }

            }

        }

        /** Metodo invocabili dal servlet container per eliminare la schedulazione del task
         * periodico di rimozione dei token invalidi, avviato da {@link #avviaSchedulazionePeriodicaEliminazioneTokenInvalidi(Long)}.
         * @return false se il task non può essere cancellato (generalmente perché è già terminato), false altrimenti.*/
        public static boolean rimuoviSchedulazionePeriodicaEliminazioneTokenInvalidi() {
            if( USARE_CLASSE_TOKEN_INVALIDI )
                return gestoreEliminatoreTokenInvalidi.cancel(true);
            else
                return true;
        }
    }



}