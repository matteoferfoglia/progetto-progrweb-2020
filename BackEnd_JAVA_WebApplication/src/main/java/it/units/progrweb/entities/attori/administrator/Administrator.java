package it.units.progrweb.entities.attori.administrator;

import com.googlecode.objectify.annotation.Subclass;
import it.units.progrweb.entities.attori.Attore;

import java.util.List;

/**
 * Rappresentazione di un attore con il ruolo di Administrator.
 * @author Matteo Ferfoglia
 */
@Subclass(index = true)
public abstract class Administrator extends Attore {

    protected Administrator() {
        super();
    }

    public Administrator(String username, String nominativo, String email) {
        super(username, nominativo, email, TipoAttore.Administrator);
        setTipoAttore(Administrator.class.getSimpleName());
    }

    /** Crea un attore di questa classe con le proprietà specificate nei parametri.*/
    public static Administrator creaAttore(String username, String nominativo, String email) {
        return new AdministratorStorage(username, nominativo, email);
    }

    /** Restituisce la lista degli identificativi di tutti gli {@link Administrator}
     * registrati nel sistema.*/
    public static List<Long> getListaIdentificativiTuttiGliAdministratorNelSistema() {
        return Attore.getListaIdentificativiTuttiGliAttoriNelSistema( Administrator.class );
    }

}

