package it.units.progrweb.entities.attori.administrator;

import com.googlecode.objectify.annotation.Subclass;
import it.units.progrweb.entities.attori.Attore;
import it.units.progrweb.entities.attori.nonAdministrator.uploader.Uploader;
import it.units.progrweb.utils.datetime.DateTime;

/**
 * Rappresentazione di un attore con il ruolo di Administrator.
 * @author Matteo Ferfoglia
 */
@Subclass
public abstract class Administrator extends Attore {

    // TODO : implementare questa classe

    protected Administrator() {
        super();    // TODO
    }

    public Administrator(String username, String nominativo, String email) {
        super(username, nominativo, email);  // TODO
        super.tipoAttore = Administrator.class.getSimpleName();
    }


    /** Crea un uploader con le proprietà specificate nei parametri.
     * @param administrator L'{@link Administrator} che esegue l'operazione.
     * @param username
     * @param nominativo
     * @param email
     * @param immagineLogo  Immagine logo dell'uploader.
     * @param estensioneFileContenenteImmagineLogo Estensione dell'immagine logo (es.: "jpeg").
     * @return L'uploader appena creato.
     */
    public static Uploader creaUploader(Administrator administrator, String username, String nominativo, String email,
                                 byte[] immagineLogo, String estensioneFileContenenteImmagineLogo) {
        return Uploader.creaUploader(username, nominativo, email, immagineLogo, estensioneFileContenenteImmagineLogo);    // TODO
    }

    /** Restituisce true se la modifica dell'uploader va a buon fine, false altrimenti.
     * @param administrator L'{@link Administrator} che esegue l'operazione.*/
    public static boolean modificaUploader(Administrator administrator, Uploader UploaderDaModificare, Uploader UploaderModificato) {
        // TODO
        return true;    // todo
    }

    /** Restituisce true se l'eliminazione dell'uploader va a buon fine, false altrimenti.
     * @param administrator L'{@link Administrator} che esegue l'operazione.*/
    public static boolean eliminaUploader(Administrator administrator, Uploader UploaderDaEliminare) {
        // TODO
        return true;
    }



    /** Crea un Administrator con le proprietà specificate nei parametri.
     * @param administrator L'{@link Administrator} che esegue l'operazione.*/
    public static Administrator creaAdministrator(Administrator administrator, String username, String nominativo, String email) {
        return new AdministratorProxy(username, nominativo, email);    // TODO
    }

    /** Restituisce true se la modifica dell'administrator va a buon fine, false altrimenti.
     * @param administratorEsecutore L'{@link Administrator} che esegue l'operazione.*/
    public static boolean modificaAdministrator(Administrator administratorEsecutore, Administrator administratorDaModificare, Administrator administratorModificato) {
        // TODO
        return true;    // todo
    }

    /** Restituisce true se l'eliminazione dell'administrator va a buon fine, false altrimenti.
     * @param administratorEsecutore L'{@link Administrator} che esegue l'operazione.*/
    public static boolean eliminaAdministrator(Administrator administratorEsecutore, Administrator administratorDaEliminare) {
        // TODO
        return true;
    }



    /** Restituisce il {@link Resoconto resoconto} del mese precedente*/
    public static Resoconto resocontoUploaders() {
        // TODO : metodo da implementare
        return new Resoconto();
    }

    /** Restituisce il {@link Resoconto resoconto} nel periodo temporale
     * specificato nei parametri (estremi inclusi).*/
    public static Resoconto resocontoUploaders(DateTime dataIniziale, DateTime dataFinale) {
        // TODO : metodo da implementare
        return new Resoconto(dataIniziale, dataFinale);
    }
}

