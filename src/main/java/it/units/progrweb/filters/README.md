# Package `filters`

Questo package contiene tutti i filtri (WebFilter) del progetto.

## Filtro Objectify
Serve a istanziare il contesto di Objectify.

## Filtro CORS
Permette le richieste "Cross-Origin".

## Filtro Autenticazione
In base ad una whitelist per gli url accessibili senza autenticazione,
blocca o permette il regolare flusso della richiesta ricevuta in base
che questa richieda o meno l'autenticazione.

## Filtro CSRF
Controlla il token CSRF, quando presente.