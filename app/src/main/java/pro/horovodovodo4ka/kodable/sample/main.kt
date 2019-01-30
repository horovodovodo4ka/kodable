package pro.horovodovodo4ka.kodable.sample

import com.github.fluidsonic.fluid.json.JSONReader
import pro.horovodovodo4ka.kodable.core.*
import pro.horovodovodo4ka.kodable.core.utils.dekode
import pro.horovodovodo4ka.kodable.sample.anotherpackage.A
import pro.horovodovodo4ka.kodable.sample.generated.kodable
import java.text.SimpleDateFormat
import java.util.*

fun main(args: Array<String>) {
    val b = B1::class.kodable().dekode("""{"i": null, "a": "aaaa"}""")
    println(b.i)


    val test = Test::class.kodable().dekode(""" {"index": [[1, 2]], "format": "ae!", "a" : { "aee" : 1 } } """)
    println(test.index)

    val path = ".data.items".kodablePath()
    val e = E::class.kodable().list.dekode(""" { "data" : { "items" : [ "a", "ooooo" ] } } """, path)
    println(e)
}

@Dekoder
class Test(
    val index: List<List<Int>>?,
    @KodableName("format") val format: String,
    val a: A,
    date: Date?,
    foo: Foo?
) {

    val b = true

    @Dekoder
    enum class Foo {
        @Default
        a,
        b;
    }

    @Enkoder
    inner class Out {
        val index = this@Test.index
        val format = this@Test.format
    }
}

@Dekoder
data class DTO(val i: Int)

@Dekoder
open class B(val i: Int)

@Dekoder
class B1(i: Int?, val a: String) : B(i ?: 10)

@DefaultKodableForType(Date::class)
object DateKodable : IKodable<Date> {
    private val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.ENGLISH)
    override fun readValue(reader: JSONReader): Date = formatter.parse(reader.readString())
}


