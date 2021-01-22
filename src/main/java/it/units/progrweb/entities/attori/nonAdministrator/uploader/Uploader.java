package it.units.progrweb.entities.attori.nonAdministrator.uploader;

import com.googlecode.objectify.annotation.Subclass;
import it.units.progrweb.entities.attori.Attore;
import it.units.progrweb.entities.attori.nonAdministrator.UtenteNonAdministrator;
import it.units.progrweb.entities.file.File;
import it.units.progrweb.utils.datetime.PeriodoTemporale;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Matteo Ferfoglia
 */
@Subclass
public abstract class Uploader extends UtenteNonAdministrator {

    protected LogoUploader immagineLogoUploader;

    final static String NOME_PROPRIETA_LOGO = "Immagine logo";

    public Uploader() {
        super();
        super.tipoAttore = Uploader.class.getSimpleName();
    }

    public static Uploader getAttoreDaIdentificativo(Long identificativoAttore) {
        Attore a = Attore.getAttoreDaIdentificativo(identificativoAttore);
        return a instanceof Uploader ? (Uploader) a : null;
    }

    /** Restituisce l'immagine logo dell'Uploader
     * codificata in Base64.*/
    public String getImmagineLogoBase64(){
        return immagineLogoUploader.getLogo_base64();
    }

    /** Modifica l'immagine logo dell'Uploader.*/
    public void setImmagineLogo(byte[] immagineLogo_bytes, String estensioneFileContenenteLogo){
        this.immagineLogoUploader.setLogo(immagineLogo_bytes, estensioneFileContenenteLogo);
    }

    /**

    /** Restituisce una mappa { "Nome attributo" -> "Valore attributo" }
     * di un'istanza di questa classe. L'attributo con l'immagine logo
     * deve avere come nome quello restituito da {@link #getNomeFieldLogoUploader()}
     * e come valore la codifica Base64 dell'immagine. L'attributo con
     * il nome deve avere come nome quello restituito da {@link #getNomeFieldNominativoAttore()}.*/
    @Override
    abstract public Map<String, ?> getMappaAttributi_Nome_Valore();

    /** Costruttore.
     * @param username
     * @param nominativo
     * @param email
     * @param immagineLogo  Array di bytes corrispondenti all'immagine logo di questo UploaderStorage.
     * @param estensioneFileContenenteImmagineLogo  Estensione dell'immagine (per sapere come interpretare l'array di bytes)
     */
    public Uploader(String username, String nominativo, String email,
                           byte[] immagineLogo, String estensioneFileContenenteImmagineLogo) {
        // TODO
        super(username, nominativo, email);
        this.immagineLogoUploader = new LogoUploader(immagineLogo, estensioneFileContenenteImmagineLogo) ;
        super.tipoAttore = Uploader.class.getSimpleName();
    }

    /** Restituisce la lista dei documenti caricati dall'{@link Uploader}
     * specificato nel periodo temporale specificato.*/
    public static List<File> getDocumentiCaricatiNelPeriodoDallUploader(PeriodoTemporale periodoTemporale, Uploader uploader) {
        // TODO : interroga db è restituisci il numero di documenti caricati da questo uploader nel periodo indicato
        return new ArrayList<>();   // TODO !!!
    }

    /** Restituisce l'entità {@link Uploader Uploader} cercata nel
     * database in base all'identificativo fornito.
     * @return L'uploader cercato, oppure null in caso di errore.*/
    public static Uploader cercaUploaderDaIdentificativo(Long identificativoUploader ) {
        Attore attoreTrovatoInDb = Attore.getAttoreDaIdentificativo(identificativoUploader);
        return attoreTrovatoInDb instanceof Uploader ? (Uploader) attoreTrovatoInDb : null;
    }

    /** Restituisce il nome del field contenente il logo
     * di un Uploader nell'oggetto nel valore della mappa
     * restituita da {@link #getMappaAttributi_Nome_Valore()}.*/
    public static String getNomeFieldLogoUploader() {
        return NOME_PROPRIETA_LOGO;
    }


}