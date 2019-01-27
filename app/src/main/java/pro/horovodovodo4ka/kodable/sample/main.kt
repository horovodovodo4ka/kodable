package pro.horovodovodo4ka.kodable.sample

import pro.horovodovodo4ka.kodable.core.DefaultKodableForType
import pro.horovodovodo4ka.kodable.core.Kodable
import pro.horovodovodo4ka.kodable.core.IKodable
import pro.horovodovodo4ka.kodable.core.JSONReader
import pro.horovodovodo4ka.kodable.sample.anotherpackage.A
import pro.horovodovodo4ka.kodable.sample.generated.kodable
import java.text.SimpleDateFormat
import java.util.*

fun main(args: Array<String>) {
    val b = B1::class.kodable().dekode("""{"i": null, "a": "aaaa"}""")
    println(b.i)


    val test = Test::class.kodable().dekode(""" {"index": [[1, 2]], "format": "ae!", "a" : { "aee" : 1 } } """)
    println(test)

    val e = E::class.kodable().dekode(""" "a" """)
    println(e)
}

inline fun <reified T> IKodable<T>.dekode(string: String): T = readValue(JSONReader.build(string))

@Kodable
class Test(val index: List<List<Int>>?, val format: String, val a: A, date: Date)

@Kodable
data class DTO(val i: Int)

@Kodable
open class B(val i: Int)

@Kodable
class B1(i: Int?, val a: String) : B(i ?: 10)

@DefaultKodableForType(Date::class)
object DateKodable : IKodable<Date> {
    private val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.ENGLISH)
    override fun readValue(reader: JSONReader): Date = formatter.parse(reader.readString())
}

