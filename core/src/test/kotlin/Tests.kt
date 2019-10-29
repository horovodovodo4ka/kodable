import io.kotlintest.matchers.collections.shouldBeOneOf
import io.kotlintest.matchers.types.shouldBeNull
import io.kotlintest.shouldBe
import io.kotlintest.shouldNotBe
import io.kotlintest.specs.FunSpec
import pro.horovodovodo4ka.kodable.core.json.JsonReader
import pro.horovodovodo4ka.kodable.core.json.isNextNull
import pro.horovodovodo4ka.kodable.core.json.record
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

    test("nullable") {
        val a: Int? = JsonReader("null".byteInputStream()).readNull()
        a.shouldBe(null as Int?)
    }

    test("object") {
        JsonReader("{\"a\": 1, \"b\" : null, \"c\": 1}".byteInputStream()).iterateObject {
            when (it) {
                "a" -> {
                    readNumber().toInt().shouldBe(1)
                }
                "b" -> {
                    val b = if (isNextNull()) readNull() else readNumber().toInt()
                    b.shouldBeNull()
                }
                "c" -> {
                    val c = if (isNextNull()) readNull() else readNumber().toInt()
                    c.shouldBe(1)
                }
                else -> skipValue()
            }
        }
    }

    test("array") {
        JsonReader("[1, 2, null]".byteInputStream())
            .iterateArray {
                val c = if (isNextNull()) readNull() else readNumber().toInt()
                c.shouldBeOneOf(1, 2, null)
            }
    }

    test("mixed") {
        JsonReader("[{\"a\":1, \"d\": \"a\"}, {\"b\":2, \"c\": null}]".byteInputStream())
            .iterateArray {
                iterateObject {
                    when (it) {
                        "a" -> {
                            val a = readNumber().toInt()
                            a.shouldBe(1)
                        }
                        "b" -> {
                            val b = readNumber().toInt()
                            b.shouldBe(2)
                        }
                        else -> skipValue()
                    }
                }
            }
    }

    test("recorded") {
        val reader = JsonReader("[{\"type\": \"int\", \"value\": 1}, {\"type\": \"string\", \"value\": \"aaa\"}]".byteInputStream())

        reader.iterateArray {
            var type: String = ""
            
            val recordedReader = record(reader) {
                iterateObject {
                    when(it) {
                        "type" -> type = readString()
                        else -> skipValue()
                    }
                }
                type.shouldNotBe("")
            }

            recordedReader.iterateObject {
                when(it) {
                    "value" -> {
                        when(type) {
                            "int" -> readNumber().toInt().shouldBe(1)
                            "string" -> readString().shouldBe("aaa")
                        }
                    }
                    else -> skipValue()
                }
            }
        }
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