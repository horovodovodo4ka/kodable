import io.kotlintest.matchers.collections.shouldBeOneOf
import io.kotlintest.matchers.types.shouldBeNull
import io.kotlintest.shouldBe
import io.kotlintest.shouldNotBe
import io.kotlintest.specs.FunSpec
import pro.horovodovodo4ka.kodable.core.json.JsonReader
import pro.horovodovodo4ka.kodable.core.json.JsonWriter
import pro.horovodovodo4ka.kodable.core.json.arrayElement
import pro.horovodovodo4ka.kodable.core.json.isNextNull
import pro.horovodovodo4ka.kodable.core.json.objectProperty
import pro.horovodovodo4ka.kodable.core.json.record
import pro.horovodovodo4ka.kodable.core.types.PolymorphicKodable
import pro.horovodovodo4ka.kodable.core.types.kodablePath
import pro.horovodovodo4ka.kodable.core.utils.dekode
import pro.horovodovodo4ka.kodable.core.utils.enkode
import pro.horovodovodo4ka.kodable.sample.B1
import pro.horovodovodo4ka.kodable.sample.Dependence
import pro.horovodovodo4ka.kodable.sample.DependencyTest
import pro.horovodovodo4ka.kodable.sample.Test
import pro.horovodovodo4ka.kodable.sample.kodable

class SerializersTest : FunSpec({
    lateinit var test: Test
    test("decoder: defaults while decoding") {
        val b: B1 = B1::class.kodable().dekode("""{"i": null, "a": "aaaa"}""")
        b.i.shouldBe(10)
        b.a.shouldBe("aaaa")
    }

    test("decoder: nesting decoding test") {
        test = Test::class.kodable()
            .dekode(""" {"index": [[1, 2]], "map" : { "a" : 1, "b" : 2 }, "format": "ae!", "a" : { "aee" : 1 } } """)
        test.a.iii.shouldBe(1)
    }

    test("decoder: kodable path") {
        val path = ".data.items".kodablePath()
        val e = E::class.kodable().list.dekode(""" { "data" : { "items" : [ "a", "ooooo" ] } } """, path)
        e[0].shouldBe(E.a)
        e[1].shouldBe(E.unknown)
    }

    test("encoder: nesting encoding") {
        val str = Test::class.kodable().enkode(test)
        str.shouldBe("{\"a\":{\"aee\":1},\"ints\":[[1,2]],\"format\":\"ae!\",\"for\":null}")
    }

    test("encoder: external kodable") {
        val str = DependencyTest::class.kodable().enkode(DependencyTest(Dependence(1)))
        str.shouldBe("{\"dependency\":{\"some\":1}}")
    }

    test("polymorphic") {

    }
})

class EncodingTests : FunSpec({
    test("string") {
        val ret = JsonWriter {
            writeString(" \n\t\rð\"\u001F ")
        }
        ret.shouldBe("\" \\n\\t\\rð\\\"\\u001F \"")
    }

    test("number") {
        val ret = JsonWriter {
            writeNumber(-123.423)
        }
        ret.shouldBe("-123.423")

        val ret2 = JsonWriter {
            writeNumber(0.00000000000000014)
        }
        ret2.shouldBe("1.4E-16")
    }

    test("bool") {
        val ret = JsonWriter {
            writeBoolean(true)
        }
        ret.shouldBe("true")
    }

    test("nullable") {
        val ret = JsonWriter {
            writeNull()
        }
        ret.shouldBe("null")
    }

    test("object") {
        val props = sequenceOf(
            objectProperty("a") { writeNumber(1) },
            objectProperty("b") { writeString("yay!") }
        )
        val ret = JsonWriter {
            iterateObject(props)
        }
        ret.shouldBe("{\"a\":1,\"b\":\"yay!\"}")
    }

    test("array") {
        val elements = sequenceOf(
            arrayElement { writeNumber(1) },
            arrayElement { writeString("yay!") }
        )
        val ret = JsonWriter {
            iterateArray(elements)
        }
        ret.shouldBe("[1,\"yay!\"]")
    }
})

class DecodingTests : FunSpec({
    test("string") {
        val a = JsonReader("\" \\n\\t\\r\\u00F0\\\" \"").readString()
        a.shouldBe(" \n\t\rð\" ")
    }

    test("number") {
        val a = JsonReader("-1234.23e-1").readNumber()
        a.shouldBe(-123.423)
    }

    test("bool") {
        val a = JsonReader("true").readBoolean()
        a.shouldBe(true)
    }

    test("nullable") {
        val a: Int? = JsonReader("null").readNull()
        a.shouldBe(null as Int?)
    }

    test("object") {
        JsonReader("{\"a\": 1, \"b\" : null, \"c\": 1}").iterateObject {
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
        JsonReader("[1, 2, null]")
            .iterateArray {
                val c = if (isNextNull()) readNull() else readNumber().toInt()
                c.shouldBeOneOf(1, 2, null)
            }
    }

    test("mixed") {
        JsonReader("[{\"a\":1, \"d\": \"a\"}, {\"b\":2, \"c\": null}]")
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
        val reader = JsonReader("[{\"type\": \"int\", \"value\": 1}, {\"type\": \"string\", \"value\": \"aaa\"}]")

        reader.iterateArray {
            var type = ""

            val recordedReader = record {
                iterateObject {
                    when (it) {
                        "type" -> type = readString()
                        else -> skipValue()
                    }
                }
                type.shouldNotBe("")
            }

            recordedReader.iterateObject {
                when (it) {
                    "value" -> {
                        when (type) {
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
