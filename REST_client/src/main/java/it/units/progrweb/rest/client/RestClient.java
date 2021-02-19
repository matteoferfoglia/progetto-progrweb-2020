package it.units.progrweb.rest.client;

import com.sun.org.apache.bcel.internal.generic.RET;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.media.multipart.file.FileDataBodyPart;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;

/**
 * @author Matteo Ferfoglia
 */
public class RestClient {

    /** URL del web service REST.*/
    private final String restWebServiceUri;

    /** Istanza del client.*/
    private final Client client;

    public RestClient(String webServiceUri, String username, String password) {
        this.restWebServiceUri = webServiceUri;
        this.client =  ClientBuilder.newClient();
        this.client.property("username", username)
                   .property("password", password)
                   .register(MultiPartFeature.class);
    }

    public Response inviaFileAConsumer(String codiceFiscaleConsumer, String emailConsumer, String nomeCognomeConsumer,
                                       String nomeFile, String listaHashtag, File file) {

        FileDaCaricare fileDaCaricare = new FileDaCaricare(codiceFiscaleConsumer, emailConsumer, nomeCognomeConsumer,
                                                           nomeFile, listaHashtag, file);

        return client.target(restWebServiceUri)
                     .request(MediaType.TEXT_PLAIN)
                     .post(Entity.entity(fileDaCaricare.getFormDataMultiPart(), fileDaCaricare.getFormDataMultiPart().getMediaType()));

    }
    
}

/** Classe di supporto rappresentante un file da caricare, con tutti
 * gli attributi richiesti dal web service.*/
class FileDaCaricare {

    private final String codiceFiscaleConsumer;
    private final String emailConsumer;
    private final String nomeCognomeConsumer;
    private final String nomeFile;
    private final List<String> listaHashtag;
    private final File file;

    public FileDaCaricare(String codiceFiscaleConsumer, String emailConsumer, String nomeCognomeConsumer,
                          String nomeFile, String listaHashtag, File file) {
        this.codiceFiscaleConsumer = codiceFiscaleConsumer;
        this.emailConsumer = emailConsumer;
        this.nomeCognomeConsumer = nomeCognomeConsumer;
        this.nomeFile = nomeFile;
        this.listaHashtag = Arrays.asList(listaHashtag.split(", "));
        this.file = file;
    }

    /** Restituisce un'istanza della classe {@link FormDataMultiPart} avente
     * come campi gli attributi dell'istanza di questa classe.
     * Fonte (ispirato da): https://stackoverflow.com/q/24637038 */
    FormDataMultiPart getFormDataMultiPart() {

        FormDataMultiPart formDataMultiPart = new FormDataMultiPart();

        formDataMultiPart.field("codiceFiscale", codiceFiscaleConsumer);
        formDataMultiPart.field("emailConsumer", emailConsumer);
        formDataMultiPart.field("nomeCognomeConsumer", nomeCognomeConsumer);
        formDataMultiPart.field("nomeFile", nomeFile);
        formDataMultiPart.field("listaHashtag", listaHashtag, MediaType.APPLICATION_JSON_TYPE);
        formDataMultiPart.field("file", file, MediaType.APPLICATION_OCTET_STREAM_TYPE);

        return formDataMultiPart;

    }
}
