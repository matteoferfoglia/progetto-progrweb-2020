# Package `entities`

Questo package contiene tutte le classi che saranno salvate come
entità nel database.

## Utilizzo di _Objectify_

>Observe:
>
>* Entity classes must be annotated with `@Entity`.
>
>* Objectify persists fields and only fields.  It does not arbitrarily map fields to the
   > datastore; if you want to change the name of a property in the datastore, rename the
   > field.  Getters and setters are ignored so you can isolate the public interface of your
   > class (eg, `public String getVehicleIdentificationNumber() { return vin; `}).
>
>* Objectify will not persist `static` fields, `final` fields, or fields annotated with
   > `@Ignore`.  It **will** persist fields with the `transient` keyword, which only affects
   > serialization.
>
>* Entities must have one field annotated with `@Id`.  The actual name of the field is
   > irrelevant and can be renamed at any time, even after data is persisted.  This value
   > (along with the kind 'Car') becomes part of the `Key` which identifies an entity.
>
>* The `@Id` field can be of type `Long`, `long`, or `String`.  If you use `Long` and save an
   > entity with a null id, a numeric value will be generated for you using the standard GAE
   > allocator for this kind.  If you use `String` or the primitive `long` type, values will
   > never be autogenerated.
>
>* There must be a no-arg constructor (or no constructors - Java creates a default no-arg
   > constructor).  The no-arg constructor can have any protection level (private, public,
   > etc).
>
>* `String` fields which store more than 500 characters (the GAE limit) are automatically
   > converted to `Text` internally.  Note that `Text` fields, like `Blob` fields, are never
   > indexed.
>
>* `byte[]` fields are automatically converted to `Blob` internally.  However, `Byte[]` is
   > persisted "normally" as an array of (potentially indexed) `Byte` objects.  Note that
   > GAE natively stores all integer values as a 64-bit long.

<p align="right"><a href="https://github.com/objectify/objectify/wiki/Entities#the-basics">Fonte</a></p>