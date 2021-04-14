package it.units.progrweb.persistence;

import it.units.progrweb.utils.ResponseHelper;

import javax.ws.rs.core.Response;

/**
 * Eccezione generata quando una risorsa cercata nel server o
 * nel database non viene trovata.
 *
 * @author Matteo Ferfoglia
 */
public class NotFoundException extends Exception{

    /** Metodo di utilità per generare un'appropriata
     * {@link Response} se una risorsa non viene trovata.*/
    public static Response creaResponseNotFound( String messaggioErrore ) {
        return ResponseHelper.creaResponseNotFound( messaggioErrore );
    }

}
