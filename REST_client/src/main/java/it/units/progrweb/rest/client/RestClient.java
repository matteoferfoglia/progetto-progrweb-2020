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
 * @author Matteo Ferfoglia
 */
public class RestClient {

    /** URL del web service REST.*/
    private final String restWebServiceUri;

    /** Istanza del client.*/
    private final Client client;

    public RestClient(String webServiceUri) {
        this.restWebServiceUri = webServiceUri;
        this.client =  ClientBuilder.newClient();
        this.client.register(MultiPartFeature.class);
    }

    /** Vedere {@link Client#close()}.*/
    public void close() {
        this.client.close();
    }

    public Response inviaFileAConsumer(String usernameUploader, String passwordUploader,
                                       String codiceFiscaleConsumer, String emailConsumer, String nomeCognomeConsumer,
                                       String nomeFile, String listaHashtag, File file) {

        FileDaCaricare fileDaCaricare = new FileDaCaricare(usernameUploader, passwordUploader,
                                                           codiceFiscaleConsumer, emailConsumer, nomeCognomeConsumer,
                                                           nomeFile, listaHashtag, file);

        return client.target(restWebServiceUri)
                     .request(MediaType.APPLICATION_JSON)
                     .post(Entity.entity(fileDaCaricare.getFormDataMultiPart(), fileDaCaricare.getFormDataMultiPart().getMediaType()));

    }

}

/** Classe di supporto rappresentante un file da caricare, con tutti
 * gli attributi richiesti dal web service.*/
class FileDaCaricare {

    private final String usernameUploader;
    private final String passwordUploader;
    private final String codiceFiscaleConsumer;
    private final String emailConsumer;
    private final String nomeCognomeConsumer;
    private final String nomeFile;
    private final String listaHashtag;
    private final File file;

    public FileDaCaricare(String usernameUploader, String passwordUploader,
                          String codiceFiscaleConsumer, String emailConsumer, String nomeCognomeConsumer,
                          String nomeFile, String listaHashtag, File file) {
        this.usernameUploader = usernameUploader;
        this.passwordUploader = passwordUploader;
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

        formDataMultiPart.field("usernameUploader", usernameUploader);
        formDataMultiPart.field("passwordUploader", passwordUploader);
        formDataMultiPart.field("codiceFiscaleConsumerDestinatario", codiceFiscaleConsumer);
        formDataMultiPart.field("emailConsumerDestinatario",         emailConsumer);
        formDataMultiPart.field("nomeCognomeConsumerDestinatario",   nomeCognomeConsumer);
        formDataMultiPart.field("nomeFile",     nomeFile);
        formDataMultiPart.field("listaHashtag", listaHashtag);
        formDataMultiPart.bodyPart(fileDataBodyPart);

        return formDataMultiPart;

    }
}
