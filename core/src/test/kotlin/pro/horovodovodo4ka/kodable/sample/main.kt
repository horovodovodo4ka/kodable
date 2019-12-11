package pro.horovodovodo4ka.kodable.sample

import pro.horovodovodo4ka.kodable.core.Default
import pro.horovodovodo4ka.kodable.core.DefaultKodableForType
import pro.horovodovodo4ka.kodable.core.Dekoder
import pro.horovodovodo4ka.kodable.core.Enkoder
import pro.horovodovodo4ka.kodable.core.IKodable
import pro.horovodovodo4ka.kodable.core.KodableName
import pro.horovodovodo4ka.kodable.core.Koder
import pro.horovodovodo4ka.kodable.core.json.JsonReader
import pro.horovodovodo4ka.kodable.core.json.JsonWriter
import pro.horovodovodo4ka.kodable.core.types.poly
import pro.horovodovodo4ka.kodable.core.types.withFallback
import pro.horovodovodo4ka.kodable.sample.anotherpackage.A
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

//
@Koder
data class DependencyTest(val dependency: Dependence)

class Test @Dekoder constructor(
    val index: List<List<Int>>?,
//    val i: List<Int>?,
    val map: Map<String, Int>,
    @KodableName("format") val format: String,
    val a: A,
    date: Date?,
    foo: Foo?,
    val `for`: Int?
) {

    val b = true

    @Koder
    enum class Foo {
        @Default
        a,
        b;
    }

    @Enkoder
    inner class Out {
        val a = this@Test.a
        val ints = this@Test.index
        val format = this@Test.format
        val `for` = this@Test.`for`
    }
}

//@Dekoder
//sealed class S {
//    object S1: S()
//    object S2: S()
//}

@Koder
data class DTO(val i: Int)

@Dekoder
open class B(val i: Int)

@Dekoder
class B1(i: Int?, val a: String) : B(i ?: 10)

@DefaultKodableForType(Date::class)
object DateKodable : IKodable<Date> {
    private val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.ENGLISH)
    override fun readValue(reader: JsonReader): Date = formatter.parse(reader.readString())
    override fun writeValue(writer: JsonWriter, instance: Date) = writer.writeString(formatter.format(instance))
}

// polymorphic type
interface Poly

object UndefinedPoly : Poly

@Koder
data class P1(val i: Int) : Poly

@Koder
data class P2(val s: String) : Poly

@DefaultKodableForType(Poly::class)
object PolySerializer : IKodable<Poly> by poly({
    propType("poly_type")
    P1::class named "p1" with P1Kodable
    P2::class named "p2" with P2Kodable
    withFallback(UndefinedPoly)
})