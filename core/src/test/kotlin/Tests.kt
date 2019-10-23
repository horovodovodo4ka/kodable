import io.kotlintest.shouldBe
import io.kotlintest.specs.FunSpec
import pro.horovodovodo4ka.kodable.core.utils.dekode
import pro.horovodovodo4ka.kodable.sample.B1
import pro.horovodovodo4ka.kodable.sample.kodable

class Tests : FunSpec({

    test("defaults while decoding") {
        val b: B1 = B1::class.kodable().dekode("""{"i": null, "a": "aaaa"}""")
        b.i.shouldBe(10)
        b.a.shouldBe("aaaa")
    }
    
})


//    val test = Test::class.kodable().dekode(""" {"index": [[1, 2]], "map" : { "a" : 1, "b" : 2 }, "format": "ae!", "a" : { "aee" : 1 } } """)
//    println(test.a.iii)
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