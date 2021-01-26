package it.units.progrweb.entities.attori.nonAdministrator.uploader;

import com.googlecode.objectify.annotation.Subclass;
import it.units.progrweb.entities.attori.Attore;
import it.units.progrweb.entities.attori.FormatoUsernameInvalido;
import it.units.progrweb.entities.attori.nonAdministrator.UtenteNonAdministrator;
import it.units.progrweb.entities.file.File;
import it.units.progrweb.persistence.DatabaseHelper;
import it.units.progrweb.utils.datetime.PeriodoTemporale;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author Matteo Ferfoglia
 */
@Subclass(index = true)
public abstract class Uploader extends UtenteNonAdministrator {

    /** RegEx per validare lo username di un {@link Uploader}.*/
    private final static String REGEX_VALIDITA_USERNAME_UPLOADER = "^\\w{4}$";

    public Uploader() {
        super();
        setTipoAttore( Uploader.class.getSimpleName() );
    }

    /** Costruttore.
     * @param username
     * @param nominativo
     * @param email
     * @throws IllegalArgumentException Se lo username non è valido.
     */
    public Uploader(String username, String nominativo, String email) {
        // TODO
        super(username, nominativo, email);

        if( ! Pattern.matches(REGEX_VALIDITA_USERNAME_UPLOADER, username) ) {   // TODO : metodo da verificare
            throw new FormatoUsernameInvalido("Lo username deve rispettare la RegEx: \"" + REGEX_VALIDITA_USERNAME_UPLOADER + "\"");
        }

    }

    /** Copy-constructor.*/
    public Uploader(Uploader uploader) {
        this( uploader.username, uploader.nominativo, uploader.email );
    }

    /** Crea un attore di questa classe.*/
    public static Uploader creaAttore( String username, String nominativo, String email ) {
        return new UploaderStorage(username, nominativo, email, null, "");
    }

    public static Uploader getAttoreDaIdentificativo(Long identificativoAttore) {
        Attore a = Attore.getAttoreDaIdentificativo(identificativoAttore);
        return a instanceof Uploader ? (Uploader) a : null;
    }

    /** Restituisce la lista degli identificativi di tutti gli {@link Uploader}
     * registrati nel sistema.*/
    public static List<Long> getListaIdentificativiTuttiGliUploaderNelSistema() {

        return DatabaseHelper.listaEntitaNelDatabase(Uploader.class)
                             .stream()
                             .map( uploader -> ((Uploader)uploader).getIdentificativoAttore() )
                             .collect(Collectors.toList());

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
        return attoreTrovatoInDb instanceof UploaderStorage ?
                new UploaderProxy((UploaderStorage) attoreTrovatoInDb) : null;
    }

    /** Restituisce il nome del field contenente il logo
     * di un Uploader.*/
    public static String getNomeFieldLogoUploader() {
        return UploaderProxy.getNomeFieldLogoUploader();
    }


    /** Restituisce l'immagine logo dell'Uploader
     * codificata in Base64.*/  // TODO : refactoring (informazioni sono mal frammentate tra questa classe, la proxy e la storage)
    public String getImmagineLogoBase64(){
        if( this instanceof UploaderStorage )
            return ((UploaderStorage)this).getLogo_base64();
        return "";
    }

    public void setImmagineLogo(byte[] convertiInputStreamInByteArray, String estensioneDaNomeFile) {
        if ( this instanceof UploaderStorage ) {
            this.setImmagineLogo(convertiInputStreamInByteArray, estensioneDaNomeFile);
        }
    }

    abstract public byte[] getImmagineLogo();
    abstract public String getEstensioneImmagineLogo();


}