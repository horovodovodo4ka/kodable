import io.kotlintest.shouldBe
import io.kotlintest.specs.FunSpec
import pro.horovodovodo4ka.kodable.core.json.JsonReader
import pro.horovodovodo4ka.kodable.core.utils.dekode
import pro.horovodovodo4ka.kodable.sample.B1
import pro.horovodovodo4ka.kodable.sample.Test
import pro.horovodovodo4ka.kodable.sample.kodable

class Tests : FunSpec({

    test("defaults while decoding") {
        val b: B1 = B1::class.kodable().dekode("""{"i": null, "a": "aaaa"}""")
        b.i.shouldBe(10)
        b.a.shouldBe("aaaa")
    }

    test("nesting decoding test") {
        val test = Test::class.kodable()
            .dekode(""" {"index": [[1, 2]], "map" : { "a" : 1, "b" : 2 }, "format": "ae!", "a" : { "aee" : 1 } } """)
        test.a.iii.shouldBe(1)
    }
})

class DecodingTests : FunSpec({
    test("strings") {
        val a = JsonReader("\" \\n\\t\\r\\u00F0\\\" \"".byteInputStream()).readString()
        a.shouldBe(" \n\t\rð\" ")
    }

    test("numbers") {
        val a = JsonReader("-1234.23e-1".byteInputStream()).readNumber()
        a.shouldBe(-123.423)
    }

    test("bools") {
        val a = JsonReader("true".byteInputStream()).readBoolean()
        a.shouldBe(true)
    }
})


//
//    val path = ".data.items".kodablePath()
//    val e = E::class.kodable().list.dekode(""" { "data" : { "items" : [ "a", "ooooo" ] } } """, path)
//    println(e)
//
//    println("case_abc-damn".toCamelCase())
//    println("aSimpleValue".toSnakeCase())
//
//    println(Test::class.kodable().enkode(test))
//
//    println(DependencyTest::class.kodable().enkode(DependencyTest(Dependence(1))))