# Package `persistence`

Questo package contiene le classi che si interfacciano con il database.
Riunire tali classi tutte in questo package semplifica l'eventuale
migrazione futura ad un altro database.

## Datastore ed Objectify
Attualmente, per memorizza i dati, si utilizza il
[Datastore](http://code.google.com/appengine/docs/java/datastore/)
a cui vi si accede con [Objectify](https://github.com/objectify/objectify/wiki).

### Annotation
> Entities are simple Java POJOs with a handful of special annotations.
> Objectify has its own annotations and does NOT use JPA or JDO annotations.
[Fonte](https://github.com/objectify/objectify/wiki/Entities#defining-entities)

Quindi, le annotation usate nelle classi rappresentanti delle entità, nel caso
in cui si decida di migrare ad un diverso database, dovranno essere modificate,
perché quelle attualmente in uso sono specifiche per Objectify.