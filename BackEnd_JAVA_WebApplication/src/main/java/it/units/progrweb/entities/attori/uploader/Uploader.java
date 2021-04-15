package it.units.progrweb.entities.attori.uploader;

import com.googlecode.objectify.annotation.Subclass;
import it.units.progrweb.entities.attori.Attore;
import it.units.progrweb.utils.UtilitaGenerale;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;

import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @author Matteo Ferfoglia
 */
@Subclass(index = true)
public abstract class Uploader extends Attore {

    /** RegEx per validare lo username di un {@link Uploader}.*/
    private final static String REGEX_VALIDITA_USERNAME_UPLOADER = "^\\w{4}$";

    public Uploader() {
        super();
        setTipoAttore( Uploader.class.getSimpleName() );
    }

    /** Costruttore.
     * @throws FormatoUsernameInvalido Se lo username non è valido.
     */
    public Uploader(String username, String nominativo, String email) {
        super(username, nominativo, email, TipoAttore.Uploader);

        if( ! Pattern.matches(REGEX_VALIDITA_USERNAME_UPLOADER, username) ) {
            throw new FormatoUsernameInvalido("Lo username deve essere composto da quattro caratteri alfanumerici.");
        }

    }

    /** Copy-constructor.*/
    public Uploader(Uploader uploader) {
        this( uploader.username, uploader.nominativo, uploader.email );
    }

    /** Crea un attore di questa classe.*/
    public static Uploader creaAttore( String username, String nominativo, String email ) {
        try {
            return new UploaderStorage(username, nominativo, email, null, "");
        } catch (IOException e) {
            // logo impostato a null, non può generare un'eccezione a causa di dimensioni eccessive.
            return null;
        }
    }

    /** Restituisce l'entità {@link Uploader} cercata nel database
     * in base all'identificativo fornito.
     * @return L'{@link Uploader} cercato, oppure null in caso di errore.*/
    public static Uploader getAttoreDaIdentificativo(Long identificativoAttore) {
        Attore a = Attore.getAttoreDaIdentificativo(identificativoAttore);
        return a instanceof Uploader ? (Uploader) a : null;
    }

    /** Restituisce la lista degli identificativi di tutti gli {@link Uploader}
     * registrati nel sistema.*/
    public static List<Long> getListaIdentificativiTuttiGliUploaderNelSistema() {

        return Attore.getListaIdentificativiTuttiGliAttoriNelSistema( Uploader.class );

    }

    /** Aggiorna nel database gli attributi di un {@link Uploader}
     * con quelli forniti nei parametri (solo se validi).
     * @throws IOException Se il logo ha una dimensione eccessiva.*/
    public static void modificaInfoUploader(@NotNull Uploader uploaderDaModificare,
                                            InputStream nuovoLogo, FormDataContentDisposition dettagliNuovoLogo,
                                            String nuovoNominativo, String nuovaEmail)
            throws IOException {

        if( nuovoLogo!=null && dettagliNuovoLogo!=null && dettagliNuovoLogo.getFileName()!=null ) {
            // se queste condizioni sono true, allora un nuovo logo valido è stato caricato
            uploaderDaModificare
                    .setImmagineLogo(
                            UtilitaGenerale.convertiInputStreamInByteArray( nuovoLogo ),
                            UtilitaGenerale.getEstensioneDaNomeFile( dettagliNuovoLogo.getFileName() )
                    );
        }

        modificaInfoAttore( uploaderDaModificare, nuovoNominativo, nuovaEmail );

    }

    /** @throws IOException Se le dimensioni del logo sono eccessive. Vedere {@link LogoUploader#setLogo(byte[], String)}. */
    public void setImmagineLogo(byte[] convertiInputStreamInByteArray, String estensioneDaNomeFile)
            throws IOException {
        if ( this instanceof UploaderStorage ) {
            this.setImmagineLogo(convertiInputStreamInByteArray, estensioneDaNomeFile);
        }
    }

    /** Restituisce l'array di byte corrispondente all'immagine del logo. */
    abstract public byte[] getImmagineLogo();

    /** Restituisce il Mediatype dell'immagine del logo. */
    abstract public String getMediatypeImmagineLogo();

}