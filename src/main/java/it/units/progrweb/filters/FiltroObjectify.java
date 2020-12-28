package it.units.progrweb.filters;

import com.googlecode.objectify.ObjectifyFilter;

import javax.servlet.annotation.WebFilter;

/**
 * Filtro per Objectify:
 * <blockquote cite="https://github.com/objectify/objectify/wiki/Setup#enable-objectifyfilter-for-your-requests">
 *     Objectify requires a filter to clean up any thread-local
 *     transaction contexts and pending asynchronous operations
 *     that remain at the end of a request.
 * </blockquote>
 *
 * @author Matteo Ferfoglia
 */
@WebFilter(filterName = "FiltroObjectify", urlPatterns = {"/*"})
public class FiltroObjectify extends ObjectifyFilter {}
