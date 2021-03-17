package it.units.progrweb.rest.client;

import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.media.multipart.file.FileDataBodyPart;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;

/**
 * Classe rappresentante un client REST.
 * @author Matteo Ferfoglia
 */
public class RestClient {

    /** URL del web service REST.*/
    private final String restWebServiceUri;

    /** Istanza del client.*/
    private final Client client;

    /** Token di autenticazione per questo client */
    private String tokenAutenticazione;

    /** Costruttore. */
    public RestClient(String webServiceUri) {
        this.restWebServiceUri = webServiceUri;
        this.client =  ClientBuilder.newClient();
        this.client.register(MultiPartFeature.class);
        this.tokenAutenticazione = "";
    }

    /** Vedere {@link Client#close()}.*/
    public void close() {
        this.client.close();
    }

    public Response inviaFileAConsumer(String codiceFiscaleConsumer, String emailConsumer, String nomeCognomeConsumer,
                                       String nomeFile, String listaHashtag, File file) {

        /* Classe di supporto rappresentante un file da caricare, con tutti
          gli attributi richiesti dal web service.*/
        class FileDaCaricare {

            private final String codiceFiscaleConsumer;
            private final String emailConsumer;
            private final String nomeCognomeConsumer;
            private final String nomeFile;
            private final String listaHashtag;
            private final File file;

            public FileDaCaricare(String codiceFiscaleConsumer, String emailConsumer, String nomeCognomeConsumer,
                                  String nomeFile, String listaHashtag, File file) {
                this.codiceFiscaleConsumer = codiceFiscaleConsumer;
                this.emailConsumer = emailConsumer;
                this.nomeCognomeConsumer = nomeCognomeConsumer;
                this.nomeFile = nomeFile;
                this.listaHashtag = listaHashtag;
                this.file = file;
            }

            /** Restituisce un'istanza della classe {@link FormDataMultiPart} avente
             * come campi gli attributi dell'istanza di questa classe.
             * Fonte (ispirato da): https://stackoverflow.com/q/24637038 */
            FormDataMultiPart getFormDataMultiPart() {

                FormDataMultiPart formDataMultiPart = new FormDataMultiPart();
                FileDataBodyPart fileDataBodyPart = new FileDataBodyPart("contenutoFile", file,
                        MediaType.APPLICATION_OCTET_STREAM_TYPE);

                formDataMultiPart.field("codiceFiscaleConsumerDestinatario", codiceFiscaleConsumer);
                formDataMultiPart.field("emailConsumerDestinatario",         emailConsumer);
                formDataMultiPart.field("nomeCognomeConsumerDestinatario",   nomeCognomeConsumer);
                formDataMultiPart.field("nomeFile",     nomeFile);
                formDataMultiPart.field("listaHashtag", listaHashtag);
                formDataMultiPart.bodyPart(fileDataBodyPart);

                return formDataMultiPart;

            }
        }

        FileDaCaricare fileDaCaricare = new FileDaCaricare(codiceFiscaleConsumer, emailConsumer, nomeCognomeConsumer,
                                                           nomeFile, listaHashtag, file);

        return client.target(restWebServiceUri)
                     .request(MediaType.APPLICATION_JSON)
                     .header("Authorization", "Bearer " + this.tokenAutenticazione)
                     .post(Entity.entity(fileDaCaricare.getFormDataMultiPart(), fileDaCaricare.getFormDataMultiPart().getMediaType()));

    }

    /** Metodo per eseguire il login.
     * @return True se il login va a buon fine, false altrimenti. */
    public boolean login(String credenziali_username, String credenziali_password, String loginUri) {

        /* Classe di supporto utilizzata per la serializzazione dei dati da inviare al server. */
        @SuppressWarnings("unused")  // Alcuni metodi "unused" in realtà sono utilizzati per le operazioni di de/serializzazione
        class Login {
            private String username;
            private String password;

            private Login() {}

            public Login(String username, String password) {
                this.username = username;
                this.password = password;
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

        Response risposta = client.target( loginUri )
                                  .request( MediaType.TEXT_PLAIN_TYPE )
                                  .post( Entity.entity(new Login(credenziali_username, credenziali_password),
                                         MediaType.APPLICATION_JSON_TYPE) );

        if( 200<=risposta.getStatus() && risposta.getStatus()<300 ) {
            // Se login ok, attendo il token di autenticazione nella risposta
            this.tokenAutenticazione = risposta.readEntity( String.class );
            return true;
        } else {
            return false;
        }

    }

    /** Metodo per eseguire il logout. */
    public void logout() {
        this.tokenAutenticazione = "";
    }
}