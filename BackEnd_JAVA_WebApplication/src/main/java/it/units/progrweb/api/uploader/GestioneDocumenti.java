package it.units.progrweb.api.uploader;

import com.google.apphosting.api.ApiProxy;
import it.units.progrweb.EnvironmentVariables;
import it.units.progrweb.api.consumer.RichiestaDocumenti;
import it.units.progrweb.entities.attori.consumer.Consumer;
import it.units.progrweb.entities.attori.uploader.Uploader;
import it.units.progrweb.entities.file.File;
import it.units.progrweb.utils.Autenticazione;
import it.units.progrweb.utils.Logger;
import it.units.progrweb.utils.ResponseHelper;
import it.units.progrweb.utils.UtilitaGenerale;
import it.units.progrweb.utils.mail.MailSender;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static it.units.progrweb.EnvironmentVariables.API_CONTEXT_ROOT;
import static it.units.progrweb.EnvironmentVariables.API_NOAUTH_SERVLET_PATH;

/**
 * Classe per permettere ad un {@link Uploader} di gestire
 * i documenti inviati ad un {@link Consumer}.
 * @author Matteo Ferfoglia
 */
@Path("/uploader/documenti")
public class GestioneDocumenti {

    /** Il path (seguente al context root e al servlet path in un URL)
     * per le richieste di download di un documento. */
    public final static String URL_PATH_DOWNLOAD_DOCUMENTO = "/downloadDocumento";

    /** Restituisce il documento il cui identificativo è
     * nel @PathParam.*/
    @Path(URL_PATH_DOWNLOAD_DOCUMENTO+"/{identificativoFile}")
    @GET
    // Mediatype indicato nella response
    public Response downloadFile(@PathParam("identificativoFile") Long identificativoFile,
                                 @Context HttpServletRequest httpServletRequest) {

        return File.creaResponseConFile(identificativoFile, httpServletRequest, null, false);

    }


    /** Eliminazione del {@link File} dalla lista di quelli serviti
     * dall'Uploader che ne ha fatto richiesta. */
    @Path("/eliminaDocumento/{idFileDaEliminare}")
    @DELETE
    public Response eliminaFile(@PathParam("idFileDaEliminare") Long idFileDaEliminare,
                                @Context HttpServletRequest httpServletRequest) {

        Long identificativoUploader = Autenticazione.getIdentificativoAttoreDaHttpServletRequest(httpServletRequest);

        if( File.eliminaFileDiUploader(idFileDaEliminare, identificativoUploader) )
            return ResponseHelper.creaResponseOk("File " + idFileDaEliminare + " eliminato");

        else return ResponseHelper.creaResponseForbidden("Non autorizzato a cancellare il file.");

    }

    /** Caricamento di un {@link File}. Questo metodo attende dal
     * client un file, il cui nome è specificato come @FormParam,
     * e destinato al {@link Consumer} il cui identificativo è
     * specificato come @FormParam. L'uploader da cui proviene la
     * richiesta viene determinato dagli header della richiesta
     * stessa
     * (<a href="https://docs.oracle.com/javaee/7/api/javax/ws/rs/FormParam.html">Fonte</a>,
     * <a href="https://stackoverflow.com/a/25889454">Fonte</a>).
     * Se il caricamento va a buon fine, questo servizio risponde
     * con l'entry ( {chiave: {proprietà documento}} ) relativa
     * al documento appena creato.
     *
     * Inoltre: dopo aver eseguito il caricamento del documento,
     * questo servizio provvede ad inviare una notifica via
     * emil al {@link Consumer} destinatario del documento.*/
    @Path("/uploadDocumento")
    @POST
    @Consumes( MediaType.MULTIPART_FORM_DATA )
    public Response uploadFile(@Context HttpServletRequest httpServletRequest,
                               @FormDataParam("contenutoFile")                      InputStream contenuto,
                               @FormDataParam("contenutoFile")                      FormDataContentDisposition dettagliFile,
                               @FormDataParam("identificativoConsumerDestinatario") Long identificativoConsumerDestinatario,
                               @FormDataParam("nomeFile")                           String nomeFile,
                               @FormDataParam("listaHashtag")                       String listaHashtag ) {


        Long identificativoUploader = Autenticazione.getIdentificativoAttoreDaHttpServletRequest( httpServletRequest );

        return uploadFile(httpServletRequest, contenuto, dettagliFile, nomeFile, listaHashtag,
                   identificativoUploader, identificativoConsumerDestinatario);

    }

    /** Si occupa dell'upload di file destinati ad un {@link Consumer}.
     * Questo metodo è di supporto per
     * {@link #uploadFile(HttpServletRequest, InputStream, FormDataContentDisposition, Long, String, String)}.*/
    public static Response uploadFile( HttpServletRequest httpServletRequest,
                                       InputStream contenuto,
                                       FormDataContentDisposition dettagliFile,
                                       String nomeFile,
                                       String listaHashtag,
                                       Long identificativoUploaderMittente,
                                       Long identificativoConsumerDestinatario ) {

        if( contenuto != null && dettagliFile != null ) {
            List<String> listaHashtag_list = Arrays.stream( listaHashtag.trim().split(",") )
                                                   .map(String::trim)
                                                   .filter(unHashtag -> unHashtag.length()>0)
                                                   .distinct()
                                                   .collect(Collectors.toList());

            String estensioneFile = UtilitaGenerale.getEstensioneDaNomeFile(dettagliFile.getFileName());

            // Prima di caricare il file, verifica che il destinatario esista.
            Consumer destinatario = Consumer.getAttoreDaIdentificativo( identificativoConsumerDestinatario );
            if( destinatario != null ) {

                // Se qui, allora destinatario esiste

                try {

                    File fileAggiunto = File.aggiungiFile( contenuto,nomeFile + "." + estensioneFile, listaHashtag_list,
                            identificativoUploaderMittente, identificativoConsumerDestinatario );

                    Uploader mittente = Uploader.getAttoreDaIdentificativo(identificativoUploaderMittente);

                    if( mittente==null )
                        return ResponseHelper.creaResponseBadRequest( "Uploader " +
                                identificativoUploaderMittente + " non trovato nel sistema." );

                    inviaNotificaDocumentoCaricatoAlConsumerDestinatario(fileAggiunto, mittente, destinatario, httpServletRequest);

                    // Restituisce il file nella sua rappresentazione { chiave => {proprietà del file} }
                    Map<String, String> mappa_idFile_propFile = File.getMappa_idFile_propFile(Collections.singletonList(fileAggiunto), true);
                        // Collections.singletonList(fileAggiunto) anziché Collections.singletonList(fileAggiunto) suggerito dall'IDE per performance

                    return UtilitaGenerale.rispostaJsonConMappaConValoriJSON(mappa_idFile_propFile);

                } catch( ApiProxy.RequestTooLargeException e) {
                    return ResponseHelper.creaResponseBadRequest("Il file che si è cercato di caricare è troppo grande. " +
                                    "Dimensioni massime consentite: " + (int)Math.floor(File.MAX_SIZE_FILE/1024.0) + " KB.");
                }

            } else {

                return ResponseHelper.creaResponseBadRequest( "Consumer " + identificativoConsumerDestinatario + " non trovato nel sistema." );

            }
        } else {
            return ResponseHelper.creaResponseUnprocessableEntity("Caricare un file valido.");
        }

    }


    /** Invia una notifica via emil al {@link Consumer} destinatario del documento.
     * @param fileAggiunto Il file da notificare.
     * @param mittenteFile Il mittente del file.
     * @param destinatarioFile destinatario del documento.
     * @param httpServletRequest Utilizzato per creare l'url di download del documento.*/
    private static void inviaNotificaDocumentoCaricatoAlConsumerDestinatario(@NotNull File fileAggiunto,
                                                                             @NotNull Uploader mittenteFile,
                                                                             @NotNull Consumer destinatarioFile,
                                                                             @Context HttpServletRequest httpServletRequest) {

        String indirizzoServer  = UtilitaGenerale.getIndirizzoServer(httpServletRequest);
        String urlDownloadFile =  indirizzoServer +
                API_CONTEXT_ROOT + API_NOAUTH_SERVLET_PATH + URL_PATH_DOWNLOAD_DOCUMENTO +          // request URI
                "/" + fileAggiunto.getIdentificativoFile() + "/" + fileAggiunto.getTokenCasuale();  // query string

        String oggettoNotifica   = EnvironmentVariables.NOME_APPLICAZIONE + " - Nuovo documento disponibile";
        String emailDestinatario = destinatarioFile.getEmail();
        String nomeDestinatario  = destinatarioFile.getNominativo();

        String messaggioHtmlNotifica;
        {

            // Creazione del corpo del messaggio in formato HTML
            messaggioHtmlNotifica =
                    "<!DOCTYPE html>"                                                       +
                    "<html lang=\"it\">"                                                    +
                        "<head>"                                                            +
                            "<title>" + oggettoNotifica + "</title>"                        +
                        "</head>"                                                           +
                        "<body>"                                                            +
                            "<h1>" + oggettoNotifica + "</h1>"                              +
                            "<p>" + mittenteFile.getNominativo() + " ha caricato il file "  +
                                "&ldquo;" + fileAggiunto.getNomeDocumento() + "&rdquo;. "   +
                                "<a href=\"" + indirizzoServer + "\">Accedi</a> al sistema" +
                                " oppure <a href=\"" + urlDownloadFile + "\">scarica</a> " +
                                "direttamente il file."                                     +
                            "</p>"                                                          +
                        "</body>"                                                           +
                    "</html>";
        }

        MailSender mailSender = new MailSender();
        try {
            mailSender.inviaEmailMultiPart( emailDestinatario, nomeDestinatario,
                                            oggettoNotifica, "", messaggioHtmlNotifica,
                                            null, null, null );
        } catch (UnsupportedEncodingException|MessagingException e) {
            Logger.scriviEccezioneNelLog( GestioneDocumenti.class,
                            "Problemi durante l'invio dell'e-mail", e );
        }

    }

    /** Restituisce una mappa in cui ogni entry ha per chiave l'identificativo
     * di un {@link File} e come valore corrispondente l'oggetto che rappresenta
     * il {@link File} in termini delle sue proprietà. Nella mappa sono presenti
     * tutti i file caricati dall'{@link Uploader} attualmente autenticato e
     * destinati al {@link Consumer} il cui identificativo è specificato come
     * PathParam.
     * Nel caso in cui venga specificato il PathParam numeroElementiAttualmenteNotiAlClient,
     * questo metodo restituisce Response.Status#NOT_MODIFIED nel caso in cui il numero
     * di documenti noti al client sia lo stesso del numero dei documenti noti al server, altrimenti
     * restituisce l'oggetto con tutti i documenti, come sopra descritto.*/
    @Path("/mappa-idFile-propFile/{identificativoConsumer}")
    @GET
    public Response getElencoFile(@Context HttpServletRequest httpServletRequest,
                                  @PathParam("identificativoConsumer") Long identificativoConsumer,
                                  @QueryParam("numeroElementiAttualmenteNotiAlClient") Integer numeroDocumentiAttualmenteNotiAlClient) {


        Long identificativoUploader = Autenticazione.getIdentificativoAttoreDaHttpServletRequest( httpServletRequest );

        List<File> fileDaRestituire =
                File.getOccorrenzeFiltrataPerUploaderEConsumer( identificativoUploader, identificativoConsumer );

        return RichiestaDocumenti.creaResponseElencoDocumenti(numeroDocumentiAttualmenteNotiAlClient,
                                                              fileDaRestituire,
                                                              true,
                                                              "uploader/documenti/mappa-idFile-propFile/" + identificativoConsumer);
    }


}
