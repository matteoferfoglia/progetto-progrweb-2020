package it.units.progrweb.api;

import it.units.progrweb.api.administrator.GestioneAttori;
import it.units.progrweb.entities.AuthenticationDatabaseEntry;
import it.units.progrweb.entities.attori.Attore;
import it.units.progrweb.entities.attori.FormatoUsernameInvalido;
import it.units.progrweb.entities.attori.administrator.Administrator;
import it.units.progrweb.entities.attori.nonAdministrator.consumer.Consumer;
import it.units.progrweb.entities.attori.nonAdministrator.uploader.Uploader;
import it.units.progrweb.persistence.DatabaseHelper;
import it.units.progrweb.utils.Autenticazione;
import it.units.progrweb.utils.EncoderPrevenzioneXSS;
import it.units.progrweb.utils.GeneratoreTokenCasuali;
import it.units.progrweb.utils.Logger;
import it.units.progrweb.utils.mail.MailSender;

import javax.mail.MessagingException;
import javax.validation.constraints.NotNull;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Objects;

/**
 * Classe per l'aggiunta di un nuovo attore nel sistema.
 * @author Matteo Ferfoglia */
public class CreazioneAttore {

    /**
     * Metodo per la creazione di un nuovo attore non presente nel database.
     * Quando crea un nuovo attore, invia a quest'ultimo una mail con le credenziali
     * per accedere al sistema.
     * @return null se l'attore che si vuole aggiungere esiste già, altrimenti restituisce
     *          l'attore appena creato.
     * @param campiFormAggiuntaAttore Istanza coi valori di input a partire da cui creare il
     *                                nuovo attore.
     * @param tipoAttoreRichiedenteCreazione Tipo di attore che richiede la creazione di un
     *                                       nuovo attore.
     * @throws NotAuthorizedException Se l'attore che sta richiedendo l'operazione
     *                                non è autorizzato.
     * @throws IllegalArgumentException Se i valori di input forniti non sono validi.
     * @throws NoSuchAlgorithmException Errore durante la scrittura in AuthDB.
     * @throws InvalidKeyException Errore durante la scrittura in AuthDB.
     * @throws UnsupportedEncodingException Errore durante l'invio della mail
     */
    public static Attore creaNuovoAttore(CampiFormAggiuntaAttore campiFormAggiuntaAttore,
                                         Attore.TipoAttore tipoAttoreRichiedenteCreazione)
            throws NotAuthorizedException, IllegalArgumentException,
                   NoSuchAlgorithmException, InvalidKeyException,
                   UnsupportedEncodingException, MessagingException {

        String username   = campiFormAggiuntaAttore.getUsername();
        String password   = campiFormAggiuntaAttore.getPassword();
        String nominativo = campiFormAggiuntaAttore.getNominativo();
        String email      = campiFormAggiuntaAttore.getEmail();
        String tipoAttoreDaCreare = campiFormAggiuntaAttore.getTipoAttore();

        boolean parametriInputValidi = Arrays.stream(new String[]{username, nominativo, email, tipoAttoreDaCreare})
                .noneMatch( Objects::isNull );

        if( parametriInputValidi ) {

            // Verifica che NON esista già l'attore nel sistema
            boolean isAttoreGiaPresente = Attore.getAttoreDaUsername(username) != null;
            Attore attoreDaCreare;

            if ( ! isAttoreGiaPresente ) {

                try {

                    if( tipoAttoreRichiedenteCreazione.equals(Attore.TipoAttore.Administrator) ) {

                        switch (Attore.TipoAttore.valueOf(tipoAttoreDaCreare)) {
                            case Uploader:
                                attoreDaCreare = Uploader.creaAttore(username, nominativo, email);
                                break;

                            case Administrator:
                                attoreDaCreare = Administrator.creaAttore(username, nominativo, email);
                                break;

                            default:
                                throw new IllegalArgumentException("Questo non dovrebbe mai succedere");
                        }
                    } else if ( tipoAttoreRichiedenteCreazione.equals(Attore.TipoAttore.Uploader) &&
                            tipoAttoreDaCreare.equals(Attore.TipoAttore.Consumer.getTipoAttore()) ) {

                        attoreDaCreare = Consumer.creaConsumer(username, nominativo, email);

                    } else {
                        throw new NotAuthorizedException("Autorizzazione alla creazione di attori negata.");
                    }

                    Long identificativoNuovoAttore = (Long) DatabaseHelper.salvaEntita(attoreDaCreare);
                    attoreDaCreare.setIdentificativoAttore(identificativoNuovoAttore);
                    AuthenticationDatabaseEntry authenticationDatabaseEntry =
                            new AuthenticationDatabaseEntry(username, password);
                    DatabaseHelper.salvaEntita(authenticationDatabaseEntry);

                    MailSender mailSender = new MailSender();
                    mailSender.inviaEmail(email, nominativo, "Creazione nuovo account",
                            "E' stato creato un nuovo account nella piattaforma." +
                                    "Sarà possibile accedervi con le seguenti credenziali: username:" + username +
                                    ", password:" + password + ".");

                    return attoreDaCreare;


                } catch( FormatoUsernameInvalido e ) {
                    throw new FormatoUsernameInvalido("Formato per il campo username non valido. " + e.getMessage());
                } catch (IllegalArgumentException e) {
                    // Tipo attore ricevuto non valido
                    throw new IllegalArgumentException("Valori di input inseriti non validi: " + e.getMessage());
                }

            } else {
                return null;
            }

        } else {
            throw new IllegalArgumentException("Parametri del form non validi.");
        }
    }



    public static Response creaNuovoAttoreECreaResponse(CampiFormAggiuntaAttore campiFormAggiuntaAttore,
                                                        Attore.TipoAttore tipoAttoreRichiedenteCreazione) {

        Attore attoreDaCreare;

        try {

            attoreDaCreare = creaNuovoAttore(campiFormAggiuntaAttore, tipoAttoreRichiedenteCreazione);

            if(attoreDaCreare==null)
                return Response
                        .status(Response.Status.BAD_REQUEST)
                        .entity("L'attore \"" + campiFormAggiuntaAttore.getUsername() + "\" è già presente nel sistema.")
                        .build();

            return Response
                    .ok()
                    .entity(attoreDaCreare.getIdentificativoAttore())
                    .type(MediaType.TEXT_PLAIN)
                    .build();


        } catch(NotAuthorizedException e) {
            return Autenticazione.creaResponseForbidden("Autorizzazione alla creazione di attori negata.");
        } catch( FormatoUsernameInvalido e ) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(e.getMessage())
                    .build();
        } catch (IllegalArgumentException e) {
            // Tipo attore ricevuto non valido
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Valori di input inseriti non validi.")
                    .build();
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            Logger.scriviEccezioneNelLog(GestioneAttori.class,
                    "Errore durante la scrittura in AuthDB",
                    e);
            return Response.serverError().build();
        } catch (UnsupportedEncodingException | MessagingException e) {
            Logger.scriviEccezioneNelLog(GestioneAttori.class,
                    "Errore durante l'invio della mail'",
                    e);
            return Response.serverError().build();
        }

    }


    /** Classe di comodo per la deserializzazione dei dati JSON
     * ricevuti dal client per l'aggiunta di un attore.*/
    @SuppressWarnings("unused") // metodi usati per la deserializzazione da JAX-RS
    public static class CampiFormAggiuntaAttore {

        private String username;
        private String password;
        private String nominativo;
        private String email;
        private String tipoAttore;  // Consumer, Uploader, Administrator
        private Long identificativoAttore;

        /** Crea un'istanza di questa classe a partire da un'istanza di {@link Attore}.*/
        public CampiFormAggiuntaAttore(Attore attore) {
            this.setUsername(attore.getUsername());
            this.setNominativo(attore.getNominativo());
            this.setPassword(String.valueOf((int)Math.floor(Math.random()*10e6)));
            this.setEmail(attore.getEmail());
            this.setTipoAttore(attore.getTipoAttore());
            this.setIdentificativoAttore(attore.getIdentificativoAttore());
        }

        /** Crea un'istanza di questa classe coi parametri dati.*/
        public CampiFormAggiuntaAttore(@NotNull String username, @NotNull String password,
                                       @NotNull String nominativo, @NotNull String email,
                                       @NotNull String tipoAttore, Long identificativoAttore) {
            this.username = username;
            this.password = password;
            this.nominativo = nominativo;
            this.email = email;
            this.tipoAttore = tipoAttore;
            this.identificativoAttore = identificativoAttore;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = EncoderPrevenzioneXSS.encodeForJava(username);
        }

        /** Se la password è nulla, come prima cosa viene generata una password
         * alfanumerica, che poi viene restituita, altrimenti viene semplicemente
         * restituita la password di quest'istanza.*/
        public String getPassword() {
            if(password==null)
                this.setPassword( GeneratoreTokenCasuali.generaTokenAlfanumerico(15) );
            return password;
        }

        /** Se il parametro è nullo, viene creata una semplice password numerica,
         * altrimenti si utilizza la password data come parametro.*/
        public void setPassword(String password) {
            this.password = EncoderPrevenzioneXSS.encodeForJava(password);
        }

        public String getNominativo() {
            return nominativo;
        }

        public void setNominativo(String nominativo) {
            this.nominativo = EncoderPrevenzioneXSS.encodeForJava(nominativo);
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = EncoderPrevenzioneXSS.encodeForJava(email);
        }

        public String getTipoAttore() {
            return tipoAttore;
        }

        public void setTipoAttore(String tipoAttore) {
            this.tipoAttore = EncoderPrevenzioneXSS.encodeForJava(tipoAttore);
        }

        public Long getIdentificativoAttore() {
            return identificativoAttore;
        }

        public void setIdentificativoAttore(Long identificativoAttore) {
            this.identificativoAttore = identificativoAttore;
        }
    }

}