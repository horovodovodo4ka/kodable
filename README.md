![image](./kodable.jpg)

[![](https://jitpack.io/v/horovodovodo4ka/kodable.svg)](https://jitpack.io/#horovodovodo4ka/kodable)

Reflectionless simple json serialization/deserialization library for **kotlin-jvm**

## Features:

- Generated code does not use any kind of reflection in runtime
- It's fast in runtime, very fast
- Compile time check for kodables - if there is no kodable for type
  we'll get exception while compilation
- no type erasure in runtime - all kodables called directly without any runtime registry, can pass kodables as arguments anywhere
- simple way for getting kodable for list of elements

## Limitations:

- Kodable doesn't support default values, only nullability - if property
  is not exists in json object it becomes **null**.
- Kodable does not support generics, wildcards etc. Only `List<{Type}>` and `Map<String, {Type}>`
  including nested lists and maps. Used for present json arrays and dictionaries.
- Kodable does not support nullability in that collections.
  
## Installation
Add this dependencies to your `build.grale(.kts)`:
```groovy
kapt("pro.horovodovodo4ka.kodable:processor:2.0.8")
implementation("pro.horovodovodo4ka.kodable:core:2.0.8")
```

If you use library with Android Studio and IDE doesn't allow you use generated code, try to add this:
```groovy
android {
    sourceSets {
        main {
            java.srcDirs += [file("$buildDir/generated/source/kaptKotlin/debug")]
            // file("$buildDir/generated/source/kaptKotlin/release")
        }
    }
}
```

## Usage
Library gives two abilities to deal with json:

- autogenerated code
- manually write custom coders/decoder

### Autogenerated code
Simplest and fastest way - just annotate classes or it's constructors and voila!

`@Koder` generates both encoder and decoder, used for data classes and
enums.

`@Dekoder` generates **decoder** for trivial classes

`@Enkoder` generates **encoder** for trivial classes - can be applied
only for inner classes of types needed to be encoded.

### Data classes

```kotlin
@Koder
data class User(
    val name: String, // required property in json

    // This annotation says that in json property name is differ from kotlin class property
    @KodableName("surname")
    // nullable types are optional in json - there will not be exception if property is not decoded, it just sets to null
    val givenName: String?
)
```
Simplest case - Kodable generates decoder and encoder for all properties
in primary constructor. That it simple!

So now we can use generated *kodable*
```kotlin
val kodable = User::class.kodable()
val user = kodable.dekode(""" { "name": "John" } """)
// user.name = "John"
// user.givenName = null
```

### Enums
```kotlin
@Koder
enum class Gender {
    // If enum can not be decoded from json than uses that value.
    // If default is not specified than json decoding is failed with exception
    @Default
    unspecified,
    male,
    female;
}
```
Just assumes that enum's value name is equal to json enum value and
presented as string.

```kotlin
val female = Gender::class.kodable().dekode(""" "female" """)
// female = Gender.female
val unknown = Gender::class.kodable().dekode(""" "aaaaaa!!!" """)
// unknown = Gender.unspecified
```

### Trivial classes

```kotlin
@Dekoder
class User(name: String, givenName: String?)
```

In this example Kodable takes first constructor in class and uses it's
signature as Json fields description.

You are able use `val/var` in constructor if it's primary same as with
data classes.
```kotlin
@Dekoder
class User(val name: String,  @KodableName("surname") val givenName: String?, gender: Gender?)
// Last parameter is not `val/var` so it will be decoded from json but you must deal with it by yourself
```

If not using data class style primary constructor, than you can emulate
default values for example or do something else
```kotlin
@Dekoder
class User(val name: String?, @KodableName("surname") givenName: String?) {
    val givenName: String = givenName ?: "Doe"
}
```

You also can annotate not class itself but it's any constructor.
```kotlin
class User {
    val name: String
    val givenName: String

    constructor (name: String, givenName: String?) {
        this.name = name
        this.givenName = givenName
    }

    @Dekoder
    constructor(fullName: String?) {
        // splitting full name and assign name and givenName
        // ...
    }
}
```

Decoding of trivial classes is much more tricky: you must create inner
class and all properties of it will be encoded to json. All properties
names **are** json properties names in json object:
```kotlin
@Dekoder
class User(val name: String?, @KodableName("surname") givenName: String?) {
    val givenName: String = givenName ?: "Doe"

    // this field we don't want to encode
    var gender = Gender.unspecified

    @Enkoder
    // this class must have empty constructor! Others are not prohibited, but not used
    inner class Out {
        val name = this@User.name
        val surname = this@User.givenName
    }
}
```

This is tricky but gives total control over
serialization/deserialization, even asymmetrical.

### External classes
Often you need use external classes (`java.util.Date` as example or
third party library classes) in your models and you can't modify and
annotate that classes.

You have two options: use annotation
`@CustomKodable` for specific field/property/constructor parameter or
use `@DefaultKodableForType` annotation - mark with it your own kodable
realization and it will be used as default.

You can combine this methods - define default kodable, but sometimes use
another for specific properties.

Example - we define that all dates are ISO8601 in json:
```kotlin
@DefaultKodableForType(Date::class)
object DateKodable : IKodable<Date> {
    private val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.ENGLISH)
    override fun readValue(reader: JsonReader): Date = formatter.parse(reader.readString())
    override fun writeValue(writer: JsonWriter, instance: Date) = writer.writeString(formatter.format(instance))
}
```
**Important note: such kodables must be `object`s**

Example of `@CustomKodable`
```kotlin
@Koder
data class Event(val caption: String, @CustomKodable(DateKodable::class) val startDate: Date)
```

Note: you can use `@CustomKodable` also for overriding **ANY** default
kodables including generated and even for primitive types

### Collections
Just use kodable's property `list` to get kodable for list of elements of given type
```kotlin
val userList: List<User> = User::class.kodable().list.dekode("""[{"name": "Alice"},{"name": "Bob"}]""")
```
Also homogeneous (properties values has same type) JSON objects can be decoded to (or encoded from) Map<String, {Type}> with `dictionary` kodable:
```kotlin
val links: List<String, URL> = URL::class.kodable().dictionary.dekode("""{"Google Inc.": "https://google.com", "Wikipedia": "https://wikipedia.org"}""")
```

### Advanced usage
Sometimes data for decoding is nested in some JSON entities.
To decode them we need define nesting entities and get sub entity from them. It's boring:
```json
{
  "data": {
      "user": {
          "name": "Alice"
        }
    }
}
```
leads to
```kotlin
@Dekoder
class UserWrapper(val user: User)

@Dekoder
class DataWrapper(val data: UserWrapper)
```
For use with other model more wrappers needed :-(

So! Kodable has special class `KodablePath` describing such nesting - when model is decoded Kodable tries move forward to nested element and then decodes needed model (User in example above)

```kotlin
User::class.kodable().dekode("...", KodablePath(".data.user"))
```
That's it.
Notation is simple: object's properties are accessed via `.<property_name>` and collections elements are accessed via `[<index_in_collection>]`

Samples of paths:
```kotlin
".data.user"
".data.items[0]"
"[2]" // in that case root element is JSON array
"data" // root dot can be ommitted
```

### Polymorphic cases
Sometimes also needed to encode/decode model based on their types - they are all childs of base abstraction and should be encoded/decoded with it as anchor. 
In JSON, typically, it looks like:
```
[ 
    {
        "i" : 10, 
        "poly_type" : "p1"
    },
    {
        "poly_type" : "p2",
        "s" : "yay!"
    }
]
```
Here two objects which types marked with `poly_type` field. Here that types in kode:
```kotlin
interface Poly

@Koder
data class P1(val i: Int) : Poly

@Koder
data class P2(val s: String) : Poly
```
As you see - both has supertype `Poly`. So let's make `Poly` able to be decoded and/or encoded:
```kotlin
@DefaultKodableForType(Poly::class)
object PolySerializer : IKodable<Poly> by poly({
    propType("poly_type")
    P1::class named "p1" with P1Kodable
    P2::class named "p2" with P2Kodable
})
```
With this DSL we defines that `Poly` can be represented with two types `P1` and `P2` tagged (via field `poly_type`) with "p1" and "p2" accordingly. Other fields in json are their's own.

## TODO
- [x] add documentation for KodablePath - helper for skip to subelements
      without describing dummy models
- [x] maps as collections additionally to `List`
- [ ] polymorphysm for sealed classes
- [x] polymorphysm for trivial classes
- [ ] more strong type cheking in compile time
- [ ] simplify enkoders for trivial classes

## License
```
MIT License

Copyright (c) 2019 Anna Sidorova (@horovodovodo4ka)

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

