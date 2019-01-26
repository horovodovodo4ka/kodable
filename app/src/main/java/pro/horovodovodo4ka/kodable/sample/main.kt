package pro.horovodovodo4ka.kodable.sample

import pro.horovodovodo4ka.kodable.JsonKoder
import pro.horovodovodo4ka.kodable.ObjectDekoder
import pro.horovodovodo4ka.kodable.core.JSONReader
import pro.horovodovodo4ka.kodable.core.Kodable
import pro.horovodovodo4ka.kodable.sample.anotherpackage.A
import java.util.*

fun main(args: Array<String>) {
    val b = B1::class.kodable().dekode("""{"i": null, "a": "aaaa"}""")
    println(b.i)


    val test = Test::class.kodable().dekode(""" {"index": [[1, 2]], "format": "ae!", "a" : { "aee" : 1 } } """)
    println(test)

    val e = E::class.kodable().dekode(""" "a" """)
    println(e)
}

inline fun <reified T> Kodable<T>.dekode(string: String): T = readValue(JSONReader.build(string))

@ObjectDekoder
class Test(val index: List<List<Int>>?, val format: String, val a: A)//, @JsonKoder(DateKodable::class) date: Date)

@ObjectDekoder
open class B(val i: Int)

@ObjectDekoder
class B1(i: Int?, val a: String) : B(i ?: 10)

object DateKodable : Kodable<Date> {
    override fun readValue(reader: JSONReader): Date {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

