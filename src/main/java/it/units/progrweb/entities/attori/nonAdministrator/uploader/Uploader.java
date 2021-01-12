package it.units.progrweb.entities.attori.nonAdministrator.uploader;

import com.googlecode.objectify.annotation.Entity;
import it.units.progrweb.entities.attori.nonAdministrator.UtenteNonAdministrator;
import it.units.progrweb.entities.file.File;
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
     * di un'istanza di questa classe.*/
    abstract public Map<String,Object> getMappaAttributi_Nome_Valore();

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
}