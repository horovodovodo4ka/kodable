import io.kotlintest.matchers.collections.shouldBeOneOf
import io.kotlintest.matchers.types.shouldBeInstanceOf
import io.kotlintest.matchers.types.shouldBeNull
import io.kotlintest.shouldBe
import io.kotlintest.specs.FunSpec
import pro.horovodovodo4ka.kodable.core.Koder
import pro.horovodovodo4ka.kodable.core.defaults.DoubleKodable
import pro.horovodovodo4ka.kodable.core.json.JsonReader
import pro.horovodovodo4ka.kodable.core.json.JsonWriter
import pro.horovodovodo4ka.kodable.core.json.arrayElement
import pro.horovodovodo4ka.kodable.core.json.invoke
import pro.horovodovodo4ka.kodable.core.json.isNextNull
import pro.horovodovodo4ka.kodable.core.json.objectProperty
import pro.horovodovodo4ka.kodable.core.types.KodablePath
import pro.horovodovodo4ka.kodable.core.types.kodablePath
import pro.horovodovodo4ka.kodable.core.utils.dekode
import pro.horovodovodo4ka.kodable.core.utils.enkode
import pro.horovodovodo4ka.kodable.sample.B1
import pro.horovodovodo4ka.kodable.sample.Dependence
import pro.horovodovodo4ka.kodable.sample.DependencyTest
import pro.horovodovodo4ka.kodable.sample.DependencyTestKodable
import pro.horovodovodo4ka.kodable.sample.P1
import pro.horovodovodo4ka.kodable.sample.P2
import pro.horovodovodo4ka.kodable.sample.PolySerializer
import pro.horovodovodo4ka.kodable.sample.Test
import pro.horovodovodo4ka.kodable.sample.TestKodable
import pro.horovodovodo4ka.kodable.sample.UndefinedPoly
import pro.horovodovodo4ka.kodable.sample.kodable

class SerializersTest : FunSpec({
    lateinit var test: Test

    test("decoder: simple numbers") {
        val b = DoubleKodable.list.dekode("""[0, 1.4]""")
        b[0].shouldBe(0.0)
        b[1].shouldBe(1.4)
    }

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
        val str = TestKodable.enkode(test)
        str.shouldBe("{\"a\":{\"aee\":1},\"ints\":[[1,2]],\"format\":\"ae!\",\"for\":null}")
    }

    test("encoder: external kodable") {
        val str = DependencyTestKodable.enkode(DependencyTest(Dependence(1)))
        str.shouldBe("{\"dependency\":{\"some\":1}}")
    }

    test("polymorphic encode") {
        val poly = listOf(P1(10), P2("yay!"))
        val str = PolySerializer.list.enkode(poly)
        str.shouldBe("[{\"poly_type\":\"p1\",\"i\":10},{\"poly_type\":\"p2\",\"s\":\"yay!\"}]")
    }

    test("polymorphic decoder") {
        val str = """
[ 
    {
        "i" : 10, 
        "poly_type" : "p1"
    },
    {
        "poly_type": "Booooo"
    },
    {
        "poly_type" : "p2",
        "s" : "yay!"
    }
]
        """.trimIndent()
        val poly = PolySerializer.list.dekode(str)
        poly[0].shouldBeInstanceOf<P1>()
        poly[1].shouldBeInstanceOf<UndefinedPoly>()
        poly[2].shouldBeInstanceOf<P2>()
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

            val objectSnapshot = iterateObjectWithPrefetch {
                when (it) {
                    "type" -> type = readString()
                    else -> skipValue()
                }
            }

            objectSnapshot.iterateObject {
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

@Koder
data class SocialProvider(val id: Int, val name: String)

class HardBass : FunSpec({
    test("with urls") {
        val json = """
{
    "meta": [],
    "data": [
        {
            "id": 15,
            "name": "Password",
            "isActive": true,
            "authData": []
        },
        {
            "id": 16,
            "name": "VK",
            "isActive": true,
            "oauthUrl": "http://oauth.vk.com/authorize?client_id=7154623&scope=email&response_type=code&redirect_uri=https%3A%2F%2Fweb-stage.dev.more.tv%2Foauth%2Fcallback%2Fvk&state=16",
            "redirectUrl": "https://web-stage.dev.more.tv/oauth/callback/vk",
            "authData": {
                "scope": "email",
                "client_id": "7154623",
                "client_secret": "aMOEMJsOnurmMWQAs1MN"
            }
        },
        {
            "id": 17,
            "name": "Facebook",
            "isActive": true,
            "oauthUrl": "https://www.facebook.com/dialog/oauth?client_id=696017504207217&scope=email&response_type=code&redirect_uri=https%3A%2F%2Fweb-stage.dev.more.tv%2Foauth%2Fcallback%2Ffacebook&state=17",
            "redirectUrl": "https://web-stage.dev.more.tv/oauth/callback/facebook",
            "authData": {
                "scope": "email",
                "client_id": "696017504207217",
                "data_fields": "name,email",
                "client_secret": "9faa9638691fcf4cd47de2107dd29182"
            }
        },
        {
            "id": 18,
            "name": "OK",
            "isActive": true,
            "oauthUrl": "https://connect.ok.ru/oauth/authorize?client_id=512000052419&response_type=code&redirect_uri=https%3A%2F%2Fweb-stage.dev.more.tv%2Foauth%2Fcallback%2Fok&state=18&scope=GET_EMAIL",
            "redirectUrl": "https://web-stage.dev.more.tv/oauth/callback/ok",
            "authData": {
                "scope": "GET_EMAIL",
                "app_id": "512000052419",
                "client_public": "CEMNMFJGDIHBABABA",
                "client_secret": "B9574C02C4A752761DE76820"
            }
        },
        {
            "id": 19,
            "name": "PIN",
            "isActive": true,
            "authData": []
        },
        {
            "id": 20,
            "name": "VK_Mobile",
            "isActive": true,
            "authData": []
        },
        {
            "id": 21,
            "name": "Facebook_Mobile",
            "isActive": true,
            "authData": []
        },
        {
            "id": 22,
            "name": "OK_Mobile",
            "isActive": true,
            "authData": []
        },
        {
            "id": 23,
            "name": "DeviceId",
            "isActive": true,
            "authData": []
        },
        {
            "id": 24,
            "name": "PIN",
            "isActive": true,
            "authData": []
        }
    ]
}
""".trimIndent()

        val resp = SocialProviderKodable.list.dekode(json, KodablePath(".data"))
        print(resp)
    }
})