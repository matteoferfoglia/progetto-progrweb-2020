package it.units.progrweb.api;

import it.units.progrweb.api.administrator.GestioneAttori;
import it.units.progrweb.entities.attori.Attore;
import it.units.progrweb.entities.attori.administrator.Administrator;
import it.units.progrweb.entities.attori.consumer.Consumer;
import it.units.progrweb.entities.attori.uploader.Uploader;
import it.units.progrweb.utils.*;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;
import javax.ws.rs.NotAuthorizedException;
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
     * @param httpServletRequest La richiesta HTTP che richiede la creazione del nuovo attore.
     * @param campiFormAggiuntaAttore Istanza coi valori di input a partire da cui creare il
     *                                nuovo attore.
     * @param tipoAttoreRichiedenteCreazione Tipo di attore che richiede la creazione di un
     *                                       nuovo attore.
     * @throws NotAuthorizedException Se l'attore che sta richiedendo l'operazione
     *                                non è autorizzato.
     * @throws IllegalArgumentException Se i valori di input forniti non sono validi.
     * @throws NoSuchAlgorithmException Errore durante la scrittura in AuthDB.
     * @throws InvalidKeyException Errore durante la scrittura in AuthDB.
     * @throws UnsupportedEncodingException Errore durante l'invio della mail.
     * @throws NullPointerException Se si verificano errori durante la creazione dell'attore e
     *                              l'attore appena creato dovessere risultare null a causa di
     *                              errori interni.
     */
    public static Attore creaNuovoAttore(HttpServletRequest httpServletRequest,
                                         CampiFormAggiuntaAttore campiFormAggiuntaAttore,
                                         Attore.TipoAttore tipoAttoreRichiedenteCreazione)
            throws NotAuthorizedException, IllegalArgumentException,
                   NoSuchAlgorithmException, InvalidKeyException,
                   NullPointerException,
                   UnsupportedEncodingException, MessagingException {

        String username   = campiFormAggiuntaAttore.getUsername();
        String password   = campiFormAggiuntaAttore.getPassword();
        String nominativo = campiFormAggiuntaAttore.getNominativo();
        String email      = campiFormAggiuntaAttore.getEmail();
        Attore.TipoAttore tipoAttoreDaCreare = Attore.TipoAttore.valueOf(campiFormAggiuntaAttore.getTipoAttore());

        boolean parametriInputValidi = Arrays.stream(new Object[]{username, nominativo, email, tipoAttoreDaCreare})
                .noneMatch( Objects::isNull );

        if( parametriInputValidi ) {

            // Verifica che NON esista già l'attore nel sistema
            boolean isAttoreGiaPresente = Attore.getAttoreDaUsername(username) != null;
            Attore attoreDaCreare;

            if ( ! isAttoreGiaPresente ) {

                try {

                    if( tipoAttoreRichiedenteCreazione.equals(Attore.TipoAttore.Administrator) ) {

                        switch ( tipoAttoreDaCreare ) {
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
                            tipoAttoreDaCreare.equals(Attore.TipoAttore.Consumer) ) {

                        attoreDaCreare = Consumer.creaAttore(username, nominativo, email);

                    } else {
                        throw new NotAuthorizedException("Autorizzazione alla creazione di attori negata.");
                    }

                    if( attoreDaCreare == null )
                        throw new NullPointerException("L'attore da creare non dovrebbe mai essere null. Questo non dovrebbe mai succedere.");

                    Long identificativoNuovoAttore = Attore.salvaNuovoAttoreInDatabase(attoreDaCreare, password, true, httpServletRequest);
                    attoreDaCreare.setIdentificativoAttore(identificativoNuovoAttore);

                    return attoreDaCreare;


                } catch( Attore.FormatoUsernameInvalido e ) {
                    throw new Attore.FormatoUsernameInvalido("Formato per il campo username non valido. " + e.getMessage());
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



    /** Metodo per la creazione di un nuovo attore.
     * @return L'oggetto {@link Response} derivante dalla creazione dell'attore.
     * @param httpServletRequest La richiesta HTTP per la quale si sta creando l'attore.
     * @param campiFormAggiuntaAttore L'oggetto {@link CampiFormAggiuntaAttore} a partire
     *                                da cui si crea un nuovo attore.
     * @param tipoAttoreRichiedenteCreazione Tipo di attore che richiede la creazione.*/
    public static Response creaNuovoAttoreECreaResponse(HttpServletRequest httpServletRequest,
                                                        CampiFormAggiuntaAttore campiFormAggiuntaAttore,
                                                        Attore.TipoAttore tipoAttoreRichiedenteCreazione) {

        Attore attoreDaCreare;

        try {

            attoreDaCreare = creaNuovoAttore(httpServletRequest, campiFormAggiuntaAttore, tipoAttoreRichiedenteCreazione);

            if(attoreDaCreare==null)
                return ResponseHelper.creaResponseBadRequest("L'attore \"" + campiFormAggiuntaAttore.getUsername() + "\" è già presente nel sistema.");

            return ResponseHelper.creaResponseOk( attoreDaCreare.getIdentificativoAttore() );

        } catch(NotAuthorizedException e) {
            return ResponseHelper.creaResponseForbidden("Autorizzazione alla creazione di attori negata.");
        } catch( Attore.FormatoUsernameInvalido e ) {
            return ResponseHelper.creaResponseBadRequest(e.getMessage());
        } catch (IllegalArgumentException e) {
            // Tipo attore ricevuto non valido
            return ResponseHelper.creaResponseBadRequest("Valori di input inseriti non validi.");
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            Logger.scriviEccezioneNelLog(GestioneAttori.class,
                    "Errore durante la scrittura in AuthDB",
                    e);
            return ResponseHelper.creaResponseServerError("");
        } catch (UnsupportedEncodingException | MessagingException e) {
            Logger.scriviEccezioneNelLog(GestioneAttori.class,
                    "Errore durante l'invio della mail'",
                    e);
            return ResponseHelper.creaResponseServerError("");
        } catch (NullPointerException e) {
            Logger.scriviEccezioneNelLog(GestioneAttori.class, e);
            return ResponseHelper.creaResponseServerError("");
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
        private Attore.TipoAttore tipoAttore;  // Consumer, Uploader, Administrator
        private Long identificativoAttore;

        /** Zero argument constructor necessario per JAX-RS.*/
        private CampiFormAggiuntaAttore() {}

        /** Crea un'istanza di questa classe a partire da un'istanza di {@link Attore}.*/
        public CampiFormAggiuntaAttore(Attore attore) {
            this.setUsername(attore.getUsername());
            this.setNominativo(attore.getNominativo());
            this.setPassword(String.valueOf((int)Math.floor(Math.random()*10e6)));
            this.setEmail(attore.getEmail());
            this.setTipoAttore(attore.getTipoAttore());
            this.setIdentificativoAttore(attore.getIdentificativoAttore());
        }

        /** Restituisce true se quest'istanza è equivalente all'{@link Attore}
         * dato come parametro, verificando username, nominativo, tipoAttore ed email.*/
        public boolean equals(Attore attore) {
            return this.getUsername().equals(attore.getUsername()) &&
                    this.getNominativo().equals(attore.getNominativo()) &&
                    this.getTipoAttore().equals(attore.getTipoAttore()) &&
                    this.getEmail().equals(attore.getEmail());
        }

        /** Crea un'istanza di questa classe coi parametri dati.*/
        public CampiFormAggiuntaAttore(@NotNull String username, @NotNull String password,
                                       @NotNull String nominativo, @NotNull String email,
                                       @NotNull Attore.TipoAttore tipoAttore,
                                       Long identificativoAttore) {
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
            return tipoAttore.name();
        }

        /** @throws IllegalArgumentException Se il parametro non corrisponde ad un valido valore di TipoAttore. */
        public void setTipoAttore(String tipoAttoreStringa) throws IllegalArgumentException {
            this.tipoAttore = Attore.TipoAttore.valueOf(EncoderPrevenzioneXSS.encodeForJava(tipoAttoreStringa));
        }

        public Long getIdentificativoAttore() {
            return identificativoAttore;
        }

        public void setIdentificativoAttore(Long identificativoAttore) {
            this.identificativoAttore = identificativoAttore;
        }
    }

}