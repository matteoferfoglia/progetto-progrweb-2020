package it.units.progrweb.utils;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;

/**
 * Classe con metodi di utilità che restituiscono degli
 * oggetti di tipo {@link javax.ws.rs.core.Response} con
 * {@link Response.Status} intuibile dal nome dei metodi
 * e messaggio (entity) atteso come parametro. Potrebbero
 * essere richiesti ulteriori parametri intuibili dalla
 * firma del metodo.
 *
 * @author Matteo Ferfoglia
 */
public class ResponseHelper {

    // 2xx

    public static<E> Response creaResponseOk(E messaggio, MediaType mediaType, HeaderResponse header, Cookie[] cookies) {
        return creaResponseMediaTypePersonalizzato(messaggio, Response.Status.OK, mediaType, header, cookies);
    }

    public static<E> Response creaResponseOk(E messaggio, MediaType mediaType, HeaderResponse header) {
        return creaResponseOk(messaggio, mediaType, header, new Cookie[0]/*no cookies*/);
    }

    public static<E> Response creaResponseOk(E messaggio, MediaType mediaType, Cookie[] cookies) {
        return creaResponseOk(messaggio, mediaType, null, cookies);
    }

    public static<E> Response creaResponseOk(E messaggio, MediaType mediaType) {
        return creaResponseOk(messaggio, mediaType, new Cookie[0]/*no cookies*/);
    }

    public static<E> Response creaResponseOk(E messaggio) {
        return creaResponseOk(messaggio, MediaType.TEXT_PLAIN_TYPE);
    }

    public static<E> Response creaResponseOk(E messaggio, Cookie[] cookies) {
        return creaResponseOk(messaggio, MediaType.TEXT_PLAIN_TYPE, cookies);
    }


    // 3xx

    public static Response creaResponseSeeOther(URI uriRedirection) {
        return Response.seeOther(uriRedirection).build();
    }

    public static Response creaResponseNotModified() {
        return Response.notModified().build();
    }


    // 4xx

    public static<E> Response creaResponseBadRequest(E messaggio) {
        return creaResponseText(messaggio, Response.Status.BAD_REQUEST);
    }

    public static<E> Response creaResponseUnauthorized(E messaggio, HeaderResponse header) {
        return creaResponseText(messaggio, Response.Status.UNAUTHORIZED, header);
    }

    public static<E> void creaResponseUnauthorized(E messaggio, HttpServletResponse httpServletResponse) throws IOException {
        httpServletResponse.sendError(
                HttpServletResponse.SC_UNAUTHORIZED,
                messaggio instanceof String ? (String) messaggio : JsonHelper.convertiOggettoInJSON(messaggio)
        );
    }

    public static<E> Response creaResponseForbidden(E messaggio) {
        return creaResponseText(messaggio, Response.Status.FORBIDDEN);
    }

    public static<E> Response creaResponseNotFound(E messaggio) {
        return creaResponseText(messaggio, Response.Status.NOT_FOUND);
    }

    public static<E> void creaResponseNotFound(E messaggio, HttpServletResponse httpServletResponse) throws IOException {
        httpServletResponse.sendError(
                HttpServletResponse.SC_NOT_FOUND,
                messaggio instanceof String ? (String)messaggio : JsonHelper.convertiOggettoInJSON(messaggio)
        );
    }

    public static<E> Response creaResponseConflict(E messaggio, MediaType mediaType) {
        return creaResponseMediaTypePersonalizzato(messaggio, Response.Status.CONFLICT, mediaType, null, new Cookie[0]);
    }

    public static<E> Response creaResponseRequestEntityTooLarge(E messaggio) {
        return creaResponseText(messaggio, Response.Status.REQUEST_ENTITY_TOO_LARGE);
    }

    public static<E> Response creaResponseUnprocessableEntity(E messaggio) {
        return creaResponseText(messaggio, Response.Status.BAD_REQUEST);    // Response.Status è di tipo enum e non ha lo stato "Unprocessable entity"
    }


    // 5xx

    public static<E> Response creaResponseServerError(E messaggio) {
        return creaResponseText(messaggio, Response.Status.INTERNAL_SERVER_ERROR);
    }

    public static<E> void creaResponseServerError(E messaggio, HttpServletResponse httpServletResponse) throws IOException {
        httpServletResponse.sendError(
                HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                messaggio instanceof String ? (String)messaggio : JsonHelper.convertiOggettoInJSON(messaggio)
        );
    }


    // Helper

    /** Dati messaggio {@link Response.Status} e cookie, restituisce l'oggetto
     * {@link Response} corrispondente, con {@link MediaType} {@link MediaType#TEXT_PLAIN}.*/
    private static<E> Response creaResponseText(E messaggio, Response.Status status, Cookie[] cookies, HeaderResponse headerResponse) {
        return creaResponseMediaTypePersonalizzato(messaggio, status, MediaType.TEXT_PLAIN_TYPE, headerResponse, cookies);
    }

    /** Dati messaggio e {@link Response.Status}, restituisce l'oggetto
     * {@link Response} corrispondente, con {@link MediaType} {@link MediaType#TEXT_PLAIN}.*/
    private static<E> Response creaResponseText(E messaggio, Response.Status status, HeaderResponse headerResponse) {
        return creaResponseText(messaggio, status, new Cookie[0], headerResponse);
    }

    /** Dati messaggio e {@link Response.Status}, restituisce l'oggetto
     * {@link Response} corrispondente, con {@link MediaType} {@link MediaType#TEXT_PLAIN}.*/
    private static<E> Response creaResponseText(E messaggio, Response.Status status) {
        return creaResponseText(messaggio, status, null);
    }


    /** Dati messaggio, {@link Response.Status}, {@link MediaType}, {@link HeaderResponse} e {@link Cookie Cookie[]},
     * restituisce l'oggetto {@link Response} corrispondente, con {@link MediaType} specificato.*/
    private static<E> Response creaResponseMediaTypePersonalizzato(E messaggio, Response.Status status, MediaType mediaType, HeaderResponse headerResponse, Cookie[] cookies) {
        Response.ResponseBuilder tmp = creaResponse_builder(messaggio, status, mediaType);
        if(headerResponse!=null)
            tmp.header(headerResponse.getNomeHeader(), headerResponse.getValoreHeader());
        return aggiungiCookieAResponseBuilder( cookies, tmp ).build();
    }

    private static Response.ResponseBuilder aggiungiCookieAResponseBuilder(Cookie[] cookies, Response.ResponseBuilder tmp) {
        for(Cookie c : cookies)
            tmp.cookie(c);
        return tmp;
    }

    /** Dati messaggio e {@link Response.Status}, restituisce l'oggetto
     * {@link Response.ResponseBuilder} corrispondente, impostando i campi
     * "entity", "type" e "status".*/
    private static<E> Response.ResponseBuilder creaResponse_builder(E messaggio, Response.Status status, MediaType mediaType) {
        return Response.status( status ).type( mediaType ).entity( messaggio );
    }

    /** Classe di comodo per creare un Header di una response. */
    public static class HeaderResponse {
        private final String nomeHeader;
        private final Object valoreHeader;

        public HeaderResponse(String nomeHeader, Object valoreHeader) {
            this.nomeHeader = nomeHeader;
            this.valoreHeader = valoreHeader;
        }

        public String getNomeHeader() {
            return nomeHeader;
        }

        public Object getValoreHeader() {
            return valoreHeader;
        }
    }

}