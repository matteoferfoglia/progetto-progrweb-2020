package it.units.progrweb.api.uploader;

import it.units.progrweb.api.CreazioneAttore;
import it.units.progrweb.entities.RelazioneUploaderConsumer;
import it.units.progrweb.entities.attori.Attore;
import it.units.progrweb.entities.attori.consumer.Consumer;
import it.units.progrweb.entities.attori.consumer.ConsumerProxy;
import it.units.progrweb.entities.attori.uploader.Uploader;
import it.units.progrweb.utils.Autenticazione;
import it.units.progrweb.utils.Logger;
import it.units.progrweb.utils.ResponseHelper;
import org.glassfish.jersey.media.multipart.FormDataParam;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.InputMismatchException;
import java.util.List;

/**
 * Classe per la gestione dei {@link Consumer} da parte
 * degli {@link Uploader}.
 * @author Matteo Ferfoglia
 */
@Path("/uploader")
public class GestioneConsumer {

    /** Riconosce l'{@link Uploader} da cui proviene la richiesta
     * e cerca nel database i {@link Consumer} per i quali questo
     * {@link Uploader} ha caricato dei file, quindi restituisce
     * l'array dei Consumer associati.
     */
    @Path("/elencoConsumer")
    @GET
    @Produces( MediaType.APPLICATION_JSON )
    public List<Long> getElencoConsumerDiUploader(@Context HttpServletRequest httpServletRequest) {

        Long identificativoUploader = Autenticazione.getIdentificativoAttoreDaHttpServletRequest(httpServletRequest);
        return RelazioneUploaderConsumer.getListaConsumerDiUploader( identificativoUploader );

    }

    /** Dato l'identificativo di un {@link Consumer} come @PathParam
     * restituisce l'oggetto JSON in cui ogni proprietà dell'oggetto
     * rappresenta un attributo di quel {@link Consumer}.
     */
    @Path("/proprietaConsumer/{identificativoConsumer}")
    @GET
    @Produces( MediaType.APPLICATION_JSON )
    public Consumer getConsumer(@PathParam("identificativoConsumer") Long identificativoConsumer) {

        return Consumer.getAttoreDaIdentificativo(identificativoConsumer);

    }

    /** Associa il {@link ConsumerProxy} passato come parametro all'{@link Uploader}
     * che ha fatto la richiesta. Se il {@link ConsumerProxy} non esiste nel sistema
     * come prima cosa viene creato.
     * Se la richiesta va a buon fine, restituisce l'identificativo del {@link Consumer}
     * creato.*/
    @Path("/aggiungiConsumerPerQuestoUploader")
    @POST
    @Consumes( MediaType.APPLICATION_JSON )
    public Response associaConsumerAdUploader( CreazioneAttore.CampiFormAggiuntaAttore campiFormAggiuntaAttore ,
                                               @Context HttpServletRequest httpServletRequest) {

        Long identificativoUploader = Autenticazione.getIdentificativoAttoreDaHttpServletRequest( httpServletRequest );

        try {
            CreazioneAttore.CampiFormAggiuntaAttore consumerAppenaAggiunto
                    = associaConsumerAdUploader(httpServletRequest, campiFormAggiuntaAttore, identificativoUploader);
            return ResponseHelper.creaResponseOk(consumerAppenaAggiunto.getIdentificativoAttore());
        } catch ( NoSuchAlgorithmException | InvalidKeyException  |
                  MessagingException | UnsupportedEncodingException e) {
            return ResponseHelper.creaResponseServerError("Errore durante la creazione del consumer.");
        } catch (InputMismatchException e) {
            return ResponseHelper.creaResponseBadRequest(e.getMessage());
        }


    }

    /** Dato un {@link Consumer} e l'identificativo di un {@link Uploader}, questo metodo
     * associa il {@link Consumer} all'{@link Uploader}. Se il {@link Consumer} è già associato
     * all'{@link Uploader} allora questo metodo non fa nulla.
     * Se il {@link Consumer} specificato non esiste nella piattaforma, come prima cosa lo crea,
     * sfruttando il metodo {@link CreazioneAttore#creaNuovoAttoreECreaResponse(HttpServletRequest, CreazioneAttore.CampiFormAggiuntaAttore, Attore.TipoAttore)}
     * @param httpServletRequest La richiesta HTTP che ha richiesto l'associazione di un {@link Consumer} ad un {@link Uploader}.
     * @param consumerDaCampiForm Istanza di CreazioneAttore.CampiFormAggiuntaAttore rappresentante un {@link Consumer}.
     * @param identificativoUploader Identificativo dell'{@link Uploader}.
     * @throws InputMismatchException Se il {@link Consumer} da associare viene trovato nel database
     *          ma i valori per i suoi attributi specificati tramite CreazioneAttore.CampiFormAggiuntaAttore
     *          non corrispondono con quelli salvati nel sistema.
     * @return L'istanza di CreazioneAttore.CampiFormAggiuntaAttore rappresentante il
     *          {@link Consumer} appena creato, se l'operazione va a buon fine. */
    public static CreazioneAttore.CampiFormAggiuntaAttore
    associaConsumerAdUploader(HttpServletRequest httpServletRequest,
                              CreazioneAttore.CampiFormAggiuntaAttore consumerDaCampiForm,
                              Long identificativoUploader)
            throws MessagingException, NoSuchAlgorithmException,
                   InvalidKeyException, UnsupportedEncodingException {

        // Verifica se il Consumer esiste nella piattaforma
        Consumer consumerDalDB = Consumer.getAttoreDaUsername(consumerDaCampiForm.getUsername());

        if( consumerDalDB==null ) {
            // Creazione del consumer se non esiste già
            try {
                consumerDalDB = (Consumer) CreazioneAttore.creaNuovoAttore( httpServletRequest,
                                                                            consumerDaCampiForm,
                                                                            Attore.TipoAttore.Uploader );
                if( consumerDalDB==null )
                    throw new NullPointerException("Non dovrebbe mai essere null.");
            } catch ( NoSuchAlgorithmException | InvalidKeyException |
                      UnsupportedEncodingException | MessagingException e ) {
                Logger.scriviEccezioneNelLog(GestioneConsumer.class,
                        "Eccezione durante l'aggiunta di un consumer",
                        e);
                throw e;
            } catch( NullPointerException e ) {
                Logger.scriviEccezioneNelLog(GestioneConsumer.class, e);
                throw e;
            }
        } else { // Consumer trovato nella piattaforma

            // Verifica che gli attributi del Consumer nel sistema coincidano con quelli
            //  del Consumer che si sta aggiungendo
            if( !consumerDaCampiForm.equals( consumerDalDB ) ) {
                // username ok, ma altri campi non corrispondono a quelli inseriti
                throw new InputMismatchException("Username trovato nel sistema, forse si intende: \n" + consumerDalDB);
            }

        }

        consumerDaCampiForm.setIdentificativoAttore(consumerDalDB.getIdentificativoAttore());

        // Verifica che il Consumer NON sia già associato all'Uploader della richiesta (altrimenti non serve aggiungerlo di nuovo)
        if (!RelazioneUploaderConsumer.isConsumerServitoDaUploader(identificativoUploader, consumerDalDB.getIdentificativoAttore())) {
            // SE precedenti controlli ok, ALLORA aggiungi il consumer
            RelazioneUploaderConsumer.aggiungiConsumerAdUploader(consumerDalDB.getIdentificativoAttore(), identificativoUploader);
        }

        return consumerDaCampiForm;
    }


    /** Eliminazione di un {@link Consumer} dalla lista di quelli serviti
     * dall'Uploader che ne ha fatto richiesta. */
    @Path("/cancellaConsumerPerQuestoUploader/{identificativoConsumerDaEliminare}")
    @DELETE
    public Response eliminaConsumer(@PathParam("identificativoConsumerDaEliminare") Long identificativoConsumerDaEliminare,
                                    @Context HttpServletRequest httpServletRequest) {

        Long identificativoUploader = Autenticazione.getIdentificativoAttoreDaHttpServletRequest(httpServletRequest);
        RelazioneUploaderConsumer.dissociaConsumerDaUploader(identificativoConsumerDaEliminare, identificativoUploader);

        return ResponseHelper.creaResponseOk("Consumer eliminato");


    }

    /** Modifica di un {@link Consumer} dalla lista di quelli serviti
     * dall'Uploader che ne ha fatto richiesta. */
    @Path("/modificaConsumer")
    @POST
    @Consumes( MediaType.MULTIPART_FORM_DATA )
    public Response modificaConsumer(@FormDataParam("nominativo") String nuovoNominativo,
                                     @FormDataParam("email")      String nuovaEmail,
                                     @FormDataParam("identificativoAttore") Long identificativoConsumerDaModificare,
                                     @Context HttpServletRequest  httpServletRequest) {

        if( identificativoConsumerDaModificare != null ) {
            Long identificativoUploaderRichiedenteModifica =
                    Autenticazione.getIdentificativoAttoreDaHttpServletRequest(httpServletRequest);

            if( RelazioneUploaderConsumer
                    .isConsumerServitoDaUploader(identificativoUploaderRichiedenteModifica,
                                                 identificativoConsumerDaModificare) ) {

                return Attore.modificaAttore(
                        nuovoNominativo,
                        nuovaEmail,
                        identificativoConsumerDaModificare,
                        null,
                        null,
                        true
                );

            } else {

                return ResponseHelper.creaResponseBadRequest("Consumer non gestito dall'Uploader che ne ha richiesto la modifica.");

            }

        } else {
            return ResponseHelper.creaResponseBadRequest( "Identificativo del consumer da modificare non può essere null." );
        }

    }

}
