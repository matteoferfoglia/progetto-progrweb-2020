package it.units.progrweb.entities.attori.nonAdministrator.uploader;

import com.googlecode.objectify.annotation.Entity;
import it.units.progrweb.entities.attori.Attore;
import it.units.progrweb.entities.attori.nonAdministrator.UtenteNonAdministrator;
import it.units.progrweb.entities.file.File;
import it.units.progrweb.persistence.DatabaseHelper;
import it.units.progrweb.persistence.NotFoundException;
import it.units.progrweb.utils.datetime.PeriodoTemporale;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Matteo Ferfoglia
 */
@Entity
public abstract class Uploader extends UtenteNonAdministrator {

    protected LogoUploader immagineLogoUploader;

    final static String NOME_PROPRIETA_LOGO = "Immagine logo";

    public Uploader() {
        super();
    }

    public static Uploader creaUploader(String username, String nomeCognome, String email, byte[] immagineLogo, String estensioneFileContenenteImmagineLogo) {
        return new UploaderStorage(username, nomeCognome, email, immagineLogo, estensioneFileContenenteImmagineLogo);
    }

    /** Restituisce l'immagine logo dell'Uploader
     * codificata in Base64.*/
    abstract public String getImmagineLogoBase64();

    /** Restituisce il nome dell'Uploader.*/
    abstract public String getNomeUploader();

    /** Restituisce una mappa { "Nome attributo" -> "Valore attributo" }
     * di un'istanza di questa classe. L'attributo con l'immagine logo
     * deve avere come nome quello restituito da {@link #getNomeFieldLogoUploader()}
     * e come valore la codifica Base64 dell'immagine. L'attributo con
     * il nome deve avere come nome quello restituito da {@link #getNomeFieldNomeUploader()}.
     * @return*/
    abstract public Map<String, ?> getMappaAttributi_Nome_Valore();

    /** Costruttore.
     * @param username
     * @param nomeCognome
     * @param email
     * @param immagineLogo  Array di bytes corrispondenti all'immagine logo di questo UploaderStorage.
     * @param estensioneFileContenenteImmagineLogo  Estensione dell'immagine (per sapere come interpretare l'array di bytes)
     */
    public Uploader(String username, String nomeCognome, String email,
                           byte[] immagineLogo, String estensioneFileContenenteImmagineLogo) {
        // TODO
        super(username, nomeCognome, email);
        this.immagineLogoUploader = new LogoUploader(immagineLogo, estensioneFileContenenteImmagineLogo) ;
    }

    /** Restituisce la lista dei documenti caricati dall'{@link Uploader}
     * specificato nel periodo temporale specificato.*/
    public static List<File> getDocumentiCaricatiNelPeriodoDallUploader(PeriodoTemporale periodoTemporale, Uploader uploader) {
        // TODO : interroga db è restituisci il numero di documenti caricati da questo uploader nel periodo indicato
        return new ArrayList<>();   // TODO !!!
    }

    /** Restituisce l'entità {@link Uploader
     * Uploader} cercata nel database in base all'identificativo fornito.
     * @return L'uploader cercato, oppure null in caso di errore.*/
    public static Uploader cercaUploaderById( Long identificativoUploader ) {
        try{
            return (Uploader) DatabaseHelper.getById(identificativoUploader, Uploader.class);
        } catch ( NotFoundException notFoundException) {
            return null;
        }
    }

    /** Restituisce il nome del field contenente l'identificativo
     * di un Uplaoder, per la ricerca nel database.*/
    public static String getNomeFieldIdentificativoUploader() {
        return Attore.getNomeFieldIdentificativoAttore();
    }

    /** Restituisce il nome del field contenente il nome
     * di un Uploader.
     * @return il nome del field richiesto, oppure null se non trovato.*/
    public static String getNomeFieldNomeUploader() {
        return Attore.getNomeFieldNomeAttore();
    }

    /** Restituisce il nome del field contenente il logo
     * di un Uploader nell'oggetto nel valore della mappa
     * restituita da {@link #getMappaAttributi_Nome_Valore()}.*/
    public static String getNomeFieldLogoUploader() {
        return NOME_PROPRIETA_LOGO;
    }


}