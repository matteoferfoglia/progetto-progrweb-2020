package it.units.progrweb.api;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * Generica classe ExceptionMapper per tenere traccia delle eccezioni
 * "non mappate" dovute a Jersey
 * (<a href="https://stackoverflow.com/a/45758691">Fonte</a>).
 * @author Matteo Ferfoglia
 */
@Provider
public class DebugMapper implements ExceptionMapper<Throwable> {
    @Override
    public Response toResponse(Throwable t) {
        t.printStackTrace();
        return Response.serverError()
                .entity(t.getMessage())
                .build();
    }
}
