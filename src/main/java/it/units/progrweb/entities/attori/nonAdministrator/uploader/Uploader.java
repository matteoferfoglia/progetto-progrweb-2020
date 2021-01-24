package it.units.progrweb.entities.attori.nonAdministrator.uploader;

import com.googlecode.objectify.annotation.Serialize;
import com.googlecode.objectify.annotation.Subclass;
import it.units.progrweb.entities.attori.Attore;
import it.units.progrweb.entities.attori.FormatoUsernameInvalido;
import it.units.progrweb.entities.attori.nonAdministrator.UtenteNonAdministrator;
import it.units.progrweb.entities.file.File;
import it.units.progrweb.utils.datetime.PeriodoTemporale;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * @author Matteo Ferfoglia
 */
@Subclass
public abstract class Uploader extends UtenteNonAdministrator {

    @Serialize
    protected LogoUploader immagineLogoUploader;

    /** Nome della proprietà che contiene il logo dell'{@link Uploader},
     * nelle risposte fornite ai client. Vedere {@link #getNomeFieldLogoUploader()}*/
    final static String NOME_PROPRIETA_LOGO = "Immagine logo";

    /** RegEx per validare lo username di un {@link Uploader}.*/
    private final static String REGEX_VALIDITA_USERNAME_UPLOADER = "^\\w{4}$";

    public Uploader() {
        super();
        super.tipoAttore = Uploader.class.getSimpleName();
    }

    /** Costruttore.
     * @param username
     * @param nominativo
     * @param email
     * @param immagineLogo  Array di bytes corrispondenti all'immagine logo di questo UploaderStorage.
     * @param estensioneFileContenenteImmagineLogo  Estensione dell'immagine (per sapere come interpretare l'array di bytes)
     * @throws IllegalArgumentException Se lo username non è valido.
     */
    public Uploader(String username, String nominativo, String email,
                    byte[] immagineLogo, String estensioneFileContenenteImmagineLogo) {
        // TODO
        super(username, nominativo, email);

        if( ! Pattern.matches(REGEX_VALIDITA_USERNAME_UPLOADER, username) ) {   // TODO : metodo da verificare
            throw new FormatoUsernameInvalido("Lo username deve rispettare la RegEx: \"" + REGEX_VALIDITA_USERNAME_UPLOADER + "\"");
        }

        this.immagineLogoUploader = new LogoUploader(immagineLogo, estensioneFileContenenteImmagineLogo) ;
        super.tipoAttore = Uploader.class.getSimpleName();
    }

    /** Crea un attore di questa classe.*/
    public static Uploader creaAttore( String username, String nominativo, String email ) {
        return new UploaderStorage(username, nominativo, email, null, "");
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