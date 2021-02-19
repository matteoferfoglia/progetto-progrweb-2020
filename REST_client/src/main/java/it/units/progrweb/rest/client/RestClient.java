package it.units.progrweb.rest.client;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
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
                   .property("password", password);
    }

    public Response inviaFileAConsumer(String codiceFiscaleConsumer, String emailConsumer, String nomeCognomeConsumer,
                                       String nomeFile, String listaHashtag, File file) {

        FileDaCaricare fileDaCaricare = new FileDaCaricare(codiceFiscaleConsumer, emailConsumer, nomeCognomeConsumer,
                                                           nomeFile, listaHashtag, file);

//        return client.target(restWebServiceUri)
//                     .request(MediaType.MULTIPART_FORM_DATA_TYPE)
//                     .post(Entity.entity(fileDaCaricare, MediaType.MULTIPART_FORM_DATA_TYPE));

        return client.target(restWebServiceUri)
                     .request(MediaType.APPLICATION_JSON)
                     //.buildPost(Entity.entity(fileDaCaricare, MediaType.APPLICATION_JSON))
                     .buildGet()
                     .invoke();
    }



}

/** Classe di supporto rappresentante un file da caricare, con tutti
 * gli attributi richiesti dal web service.*/
class FileDaCaricare {

    private String codiceFiscaleConsumer;
    private String emailConsumer;
    private String nomeCognomeConsumer;
    private String nomeFile;
    private List<String> listaHashtag;
    //private File file;

    public FileDaCaricare(String codiceFiscaleConsumer, String emailConsumer, String nomeCognomeConsumer,
                          String nomeFile, String listaHashtag, File file) {
        this.codiceFiscaleConsumer = codiceFiscaleConsumer;
        this.emailConsumer = emailConsumer;
        this.nomeCognomeConsumer = nomeCognomeConsumer;
        this.nomeFile = nomeFile;
        this.listaHashtag = Arrays.asList(listaHashtag.split(", "));
        //this.file = file;
    }

    // Standard getter/setter per JAX-RS

    public String getCodiceFiscaleConsumer() {
        return codiceFiscaleConsumer;
    }

    public void setCodiceFiscaleConsumer(String codiceFiscaleConsumer) {
        this.codiceFiscaleConsumer = codiceFiscaleConsumer;
    }

    public String getEmailConsumer() {
        return emailConsumer;
    }

    public void setEmailConsumer(String emailConsumer) {
        this.emailConsumer = emailConsumer;
    }

    public String getNomeCognomeConsumer() {
        return nomeCognomeConsumer;
    }

    public void setNomeCognomeConsumer(String nomeCognomeConsumer) {
        this.nomeCognomeConsumer = nomeCognomeConsumer;
    }

    public String getNomeFile() {
        return nomeFile;
    }

    public void setNomeFile(String nomeFile) {
        this.nomeFile = nomeFile;
    }

    public List<String> getListaHashtag() {
        return listaHashtag;
    }

    public void setListaHashtag(List<String> listaHashtag) {
        this.listaHashtag = listaHashtag;
    }

//    public File getFile() {
//        return file;
//    }
//
//    public void setFile(File file) {
//        this.file = file;
//    }
}
