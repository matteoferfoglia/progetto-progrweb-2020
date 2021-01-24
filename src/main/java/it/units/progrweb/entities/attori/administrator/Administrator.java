package it.units.progrweb.entities.attori.administrator;

import com.googlecode.objectify.annotation.Subclass;
import it.units.progrweb.entities.attori.Attore;
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


    /** Crea un attore di questa classe con le proprietà specificate nei parametri.*/
    public static Administrator creaAttore(String username, String nominativo, String email) {
        return new AdministratorStorage(username, nominativo, email);    // TODO
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

