import io.kotlintest.matchers.collections.shouldBeOneOf
import io.kotlintest.matchers.types.shouldBeInstanceOf
import io.kotlintest.matchers.types.shouldBeNull
import io.kotlintest.shouldBe
import io.kotlintest.specs.FunSpec
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import pro.horovodovodo4ka.kodable.core.Dekoder
import pro.horovodovodo4ka.kodable.core.defaults.DoubleKodable
import pro.horovodovodo4ka.kodable.core.defaults.StringKodable
import pro.horovodovodo4ka.kodable.core.json.JsonReader
import pro.horovodovodo4ka.kodable.core.json.JsonWriter
import pro.horovodovodo4ka.kodable.core.json.JsonWriterOption.SkipObjectNullProperties
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

@Dekoder
class TestDates(val startTime: String)

@Dekoder
class ComplexTest(
    val iconMin: String,
    val images: List<String>,
    val attributes: List<String>
)

class ConcurrencyTest : FunSpec({

    test("decoder: concurrency") {
        val r1 = async {
            delay(1)
            TestDatesKodable.list.dekode(largeText, KodablePath(".content[0].widgets"))
        }
        val r2 = async {
            delay(10)
            TestDatesKodable.list.dekode(largeText, KodablePath(".content[0].widgets"))
        }
        val r3 = async {
            delay(100)
            TestDatesKodable.list.dekode(largeText, KodablePath(".content[0].widgets"))
        }
        val r4 = async {
            TestDatesKodable.list.dekode(largeText, KodablePath(".content[0].widgets"))
        }

        runBlocking {
            val rr1 = r1.await().joinToString("\n") { it.startTime }
            val rr2 = r2.await().joinToString("\n") { it.startTime }
            val rr3 = r3.await().joinToString("\n") { it.startTime }
            val rr4 = r4.await().joinToString("\n") { it.startTime }
            println(rr1)
            println(rr2)
            println(rr3)
            println(rr4)
        }
    }
})

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
        str.shouldBe("""{"a":{"aee":1},"ints":[[1,2]],"format":"ae!","for":null}""")
    }

    test("encoder: nesting encoding, skips nulls") {
        val str = TestKodable.enkode(test, SkipObjectNullProperties)
        str.shouldBe("""{"a":{"aee":1},"ints":[[1,2]],"format":"ae!"}""")
    }

    test("encoder: external kodable") {
        val str = DependencyTestKodable.enkode(DependencyTest(Dependence(1)))
        str.shouldBe("""{"dependency":{"some":1}}""")
    }

    test("polymorphic encode") {
        val poly = listOf(P1(10), P2("yay!"))
        val str = PolySerializer.list.enkode(poly)
        str.shouldBe("""[{"poly_type":"p1","i":10},{"poly_type":"p2","s":"yay!"}]""")
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
        ret.shouldBe("""" \n\t\rð\"\u001F """")
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
            objectProperty("a", 1) { writeNumber(1) },
            objectProperty("b", "yay!") { writeString("yay!") }
        )
        val ret = JsonWriter {
            iterateObject(props)
        }
        ret.shouldBe("""{"a":1,"b":"yay!"}""")
    }

    test("array") {
        val elements = sequenceOf(
            arrayElement { writeNumber(1) },
            arrayElement { writeString("yay!") }
        )
        val ret = JsonWriter {
            iterateArray(elements)
        }
        ret.shouldBe("""[1,"yay!"]""")
    }

    test("dictionary") {
        val ret = StringKodable.dictionary.enkode(mapOf("camping_id" to "321", "distribution_id" to "123", "target_user_id" to "2", "Deeplink" to "more://subscription/discount"))

        ret.shouldBe("""{"camping_id":"321","distribution_id":"123","target_user_id":"2","Deeplink":"more://subscription/discount"}""")
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
        JsonReader("""{"a": 1, "b" : null, "c": 1}""").iterateObject {
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
        JsonReader("""[{"a":1, "d": "a"}, {"b":2, "c": null}]""")
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
        val reader = JsonReader("""[{"type": "int", "value": 1}, {"type": "string", "value": "aaa"}]""")

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

val largeText = """
    {"url":"/live/","menu":null,"content":[{"widgets":[{"widgets":[],"bubbleUrl":"/mobile/Pritvoris-moei-zhenoi/","bubbleImage":[{"url":"http://94.140.201.90/ctc/images/5de92fbc6ee53.png","width":60,"height":60},{"url":"http://94.140.201.90/ctc/images/5de92fbca9831.png","width":125,"height":125}],"bubbleTitle":"Притворись моей женой","bubbleSubTitle":null,"trackUrl":null,"onAirUrl":"https://player.mediavitrina.ru/ctc_ext/moretv_web/player.html","ageLimit":16,"startTime":"2019-12-11T22:15:00+03:00","isPremiere":false,"isOnAir":true,"viewProgress":null,"imageUrl":[{"url":"http://94.140.201.90/ctc/images/5dc29f61ca808.jpeg","width":150,"height":84},{"url":"http://94.140.201.90/ctc/images/5dc29f629d6c6.jpeg","width":280,"height":157},{"url":"http://94.140.201.90/ctc/images/5dc29f633e365.jpeg","width":400,"height":225},{"url":"http://94.140.201.90/ctc/images/5dc29f63d0db1.jpeg","width":550,"height":309},{"url":"http://94.140.201.90/ctc/images/5dc29f64cd311.jpeg","width":800,"height":450},{"url":"http://94.140.201.90/ctc/images/5dc29f660e91a.jpeg","width":960,"height":540},{"url":"http://94.140.201.90/ctc/images/5dc29f66b4ce6.jpeg","width":1226,"height":689},{"url":"http://94.140.201.90/ctc/images/5dc29f6819125.jpeg","width":1536,"height":864}],"hasPlayButton":false,"videoUrl":null,"type":"tv-slot"},{"widgets":[],"bubbleUrl":"/mobile/Politseiskii-iz-Beverli-Hillz-2/","bubbleImage":[{"url":"http://94.140.201.90/ctc/images/5de92fbc6ee53.png","width":60,"height":60},{"url":"http://94.140.201.90/ctc/images/5de92fbca9831.png","width":125,"height":125}],"bubbleTitle":"Полицейский из Беверли-Хиллз 2","bubbleSubTitle":"","trackUrl":null,"onAirUrl":null,"ageLimit":16,"startTime":"2019-12-12T00:35:00+03:00","isPremiere":false,"isOnAir":false,"viewProgress":0,"imageUrl":[],"hasPlayButton":true,"videoUrl":"http://siren-preprod.dev.more.tv/player?partner_id=1837","type":"tv-slot"},{"widgets":[],"bubbleUrl":"/mobile/Supermamochka/","bubbleImage":[{"url":"http://94.140.201.90/ctc/images/5dcad505c5398.jpeg","width":60,"height":60},{"url":"http://94.140.201.90/ctc/images/5dcad50796879.jpeg","width":125,"height":125},{"url":"http://94.140.201.90/ctc/images/5dcad508c3f85.jpeg","width":200,"height":200},{"url":"http://94.140.201.90/ctc/images/5dcad509ebb58.jpeg","width":300,"height":300},{"url":"http://94.140.201.90/ctc/images/5dcad50bde1f0.jpeg","width":400,"height":400}],"bubbleTitle":"Супермамочка","bubbleSubTitle":"Выпуск 20 Сезон 1","trackUrl":null,"onAirUrl":null,"ageLimit":16,"startTime":"2019-12-12T02:30:00+03:00","isPremiere":false,"isOnAir":false,"viewProgress":0,"imageUrl":[{"url":"http://94.140.201.90/ctc/images/5dcad520ec5d4.jpeg","width":150,"height":84},{"url":"http://94.140.201.90/ctc/images/5dcad52302d2f.jpeg","width":280,"height":157},{"url":"http://94.140.201.90/ctc/images/5dcad525916e8.jpeg","width":400,"height":225},{"url":"http://94.140.201.90/ctc/images/5dcad5272910d.jpeg","width":550,"height":309}],"hasPlayButton":true,"videoUrl":"http://siren-preprod.dev.more.tv/player?track_id=632885&partner_id=1837","type":"tv-slot"},{"widgets":[],"bubbleUrl":"/mobile/6-kadrov/","bubbleImage":[{"url":"http://94.140.201.90/ctc/images/5dcd880974e85.jpeg","width":125,"height":125}],"bubbleTitle":"6 кадров 123","bubbleSubTitle":"Серия 31 Сезон 7","trackUrl":null,"onAirUrl":null,"ageLimit":16,"startTime":"2019-12-12T03:20:00+03:00","isPremiere":false,"isOnAir":false,"viewProgress":0,"imageUrl":[{"url":"http://94.140.201.90/ctc/images/5dcd880e90c98.png","width":90,"height":130}],"hasPlayButton":true,"videoUrl":"http://siren-preprod.dev.more.tv/player?track_id=75501&partner_id=1837","type":"tv-slot"},{"widgets":[],"bubbleUrl":"/mobile/6-kadrov/","bubbleImage":[{"url":"http://94.140.201.90/ctc/images/5dcd880974e85.jpeg","width":125,"height":125}],"bubbleTitle":"6 кадров 123","bubbleSubTitle":"Серия 32 Сезон 7","trackUrl":null,"onAirUrl":null,"ageLimit":16,"startTime":"2019-12-12T03:40:00+03:00","isPremiere":false,"isOnAir":false,"viewProgress":0,"imageUrl":[{"url":"http://94.140.201.90/ctc/images/5dcd880e90c98.png","width":90,"height":130}],"hasPlayButton":true,"videoUrl":"http://siren-preprod.dev.more.tv/player?track_id=75503&partner_id=1837","type":"tv-slot"},{"widgets":[],"bubbleUrl":"/mobile/Molodezhka/","bubbleImage":[{"url":"http://94.140.201.90/ctc/images/5dcac889533d3.jpeg","width":60,"height":60},{"url":"http://94.140.201.90/ctc/images/5dcac889b63e2.jpeg","width":125,"height":125},{"url":"http://94.140.201.90/ctc/images/5dcac88aa4d04.jpeg","width":200,"height":200},{"url":"http://94.140.201.90/ctc/images/5dcac88b35024.jpeg","width":300,"height":300},{"url":"http://94.140.201.90/ctc/images/5dcac88c54d52.jpeg","width":400,"height":400}],"bubbleTitle":"Молодежка","bubbleSubTitle":"Серия 27 Сезон 6","trackUrl":null,"onAirUrl":null,"ageLimit":16,"startTime":"2019-12-12T03:50:00+03:00","isPremiere":false,"isOnAir":false,"viewProgress":0,"imageUrl":[{"url":"http://94.140.201.90/ctc/images/5dcacb2c33c36.jpeg","width":150,"height":84},{"url":"http://94.140.201.90/ctc/images/5dcacb2cd734c.jpeg","width":280,"height":157},{"url":"http://94.140.201.90/ctc/images/5dcacb2d9fefc.jpeg","width":400,"height":225},{"url":"http://94.140.201.90/ctc/images/5dcacb2e121ca.jpeg","width":550,"height":309}],"hasPlayButton":true,"videoUrl":"http://siren-preprod.dev.more.tv/player?track_id=735501&partner_id=1837","type":"tv-slot"},{"widgets":[],"bubbleUrl":"/mobile/Vy-vse-menya-besite/","bubbleImage":[{"url":"http://94.140.201.90/ctc/images/5dcada53b736b.jpeg","width":60,"height":60},{"url":"http://94.140.201.90/ctc/images/5dcada55e30f2.jpeg","width":125,"height":125},{"url":"http://94.140.201.90/ctc/images/5dcada5717329.jpeg","width":200,"height":200},{"url":"http://94.140.201.90/ctc/images/5dcada590424c.jpeg","width":300,"height":300},{"url":"http://94.140.201.90/ctc/images/5dcada5ac0c92.jpeg","width":400,"height":400}],"bubbleTitle":"Вы все меня бесите","bubbleSubTitle":"Серия 8 Сезон 1","trackUrl":null,"onAirUrl":null,"ageLimit":16,"startTime":"2019-12-12T04:35:00+03:00","isPremiere":false,"isOnAir":false,"viewProgress":0,"imageUrl":[{"url":"http://94.140.201.90/ctc/images/5dcada5d6ae76.jpeg","width":150,"height":84},{"url":"http://94.140.201.90/ctc/images/5dcada5ea53db.jpeg","width":280,"height":157},{"url":"http://94.140.201.90/ctc/images/5dcada603966a.jpeg","width":400,"height":225},{"url":"http://94.140.201.90/ctc/images/5dcada626a86e.jpeg","width":550,"height":309}],"hasPlayButton":true,"videoUrl":"http://siren-preprod.dev.more.tv/player?track_id=507902&partner_id=1837","type":"tv-slot"},{"widgets":[],"bubbleUrl":"/mobile/Eralash/","bubbleImage":[{"url":"http://94.140.201.90/ctc/images/5dc2ca0c518ee.png","width":60,"height":60},{"url":"http://94.140.201.90/ctc/images/5dc2ca0df389f.png","width":125,"height":125},{"url":"http://94.140.201.90/ctc/images/5dc2ca0fd97cc.png","width":200,"height":200},{"url":"http://94.140.201.90/ctc/images/5dc2ca1318f7e.png","width":300,"height":300},{"url":"http://94.140.201.90/ctc/images/5dc2ca14ccfa8.png","width":400,"height":400}],"bubbleTitle":"Ералаш","bubbleSubTitle":"Серия 91 Сезон 1","trackUrl":null,"onAirUrl":null,"ageLimit":0,"startTime":"2019-12-12T05:00:00+03:00","isPremiere":false,"isOnAir":false,"viewProgress":0,"imageUrl":[{"url":"http://94.140.201.90/ctc/images/5dcd880e90c98.png","width":90,"height":130}],"hasPlayButton":true,"videoUrl":"http://siren-preprod.dev.more.tv/player?track_id=14383&partner_id=1837","type":"tv-slot"},{"widgets":[],"bubbleUrl":"/mobile/Eralash/","bubbleImage":[{"url":"http://94.140.201.90/ctc/images/5dc2ca0c518ee.png","width":60,"height":60},{"url":"http://94.140.201.90/ctc/images/5dc2ca0df389f.png","width":125,"height":125},{"url":"http://94.140.201.90/ctc/images/5dc2ca0fd97cc.png","width":200,"height":200},{"url":"http://94.140.201.90/ctc/images/5dc2ca1318f7e.png","width":300,"height":300},{"url":"http://94.140.201.90/ctc/images/5dc2ca14ccfa8.png","width":400,"height":400}],"bubbleTitle":"Ералаш","bubbleSubTitle":null,"trackUrl":null,"onAirUrl":null,"ageLimit":0,"startTime":"2019-12-12T05:05:00+03:00","isPremiere":false,"isOnAir":false,"viewProgress":null,"imageUrl":[{"url":"http://94.140.201.90/ctc/images/5dcd880943bf7.jpeg","width":1536,"height":864}],"hasPlayButton":false,"videoUrl":null,"type":"tv-slot"},{"widgets":[],"bubbleUrl":"/mobile/Eralash/","bubbleImage":[{"url":"http://94.140.201.90/ctc/images/5dc2ca0c518ee.png","width":60,"height":60},{"url":"http://94.140.201.90/ctc/images/5dc2ca0df389f.png","width":125,"height":125},{"url":"http://94.140.201.90/ctc/images/5dc2ca0fd97cc.png","width":200,"height":200},{"url":"http://94.140.201.90/ctc/images/5dc2ca1318f7e.png","width":300,"height":300},{"url":"http://94.140.201.90/ctc/images/5dc2ca14ccfa8.png","width":400,"height":400}],"bubbleTitle":"Ералаш","bubbleSubTitle":null,"trackUrl":null,"onAirUrl":null,"ageLimit":0,"startTime":"2019-12-12T05:15:00+03:00","isPremiere":false,"isOnAir":false,"viewProgress":null,"imageUrl":[{"url":"http://94.140.201.90/ctc/images/5dcd880943bf7.jpeg","width":1536,"height":864}],"hasPlayButton":false,"videoUrl":null,"type":"tv-slot"},{"widgets":[],"bubbleUrl":"/mobile/Eralash/","bubbleImage":[{"url":"http://94.140.201.90/ctc/images/5dc2ca0c518ee.png","width":60,"height":60},{"url":"http://94.140.201.90/ctc/images/5dc2ca0df389f.png","width":125,"height":125},{"url":"http://94.140.201.90/ctc/images/5dc2ca0fd97cc.png","width":200,"height":200},{"url":"http://94.140.201.90/ctc/images/5dc2ca1318f7e.png","width":300,"height":300},{"url":"http://94.140.201.90/ctc/images/5dc2ca14ccfa8.png","width":400,"height":400}],"bubbleTitle":"Ералаш","bubbleSubTitle":null,"trackUrl":null,"onAirUrl":null,"ageLimit":0,"startTime":"2019-12-12T05:20:00+03:00","isPremiere":false,"isOnAir":false,"viewProgress":null,"imageUrl":[{"url":"http://94.140.201.90/ctc/images/5dcd880943bf7.jpeg","width":1536,"height":864}],"hasPlayButton":false,"videoUrl":null,"type":"tv-slot"},{"widgets":[],"bubbleUrl":"/mobile/Eralash/","bubbleImage":[{"url":"http://94.140.201.90/ctc/images/5dc2ca0c518ee.png","width":60,"height":60},{"url":"http://94.140.201.90/ctc/images/5dc2ca0df389f.png","width":125,"height":125},{"url":"http://94.140.201.90/ctc/images/5dc2ca0fd97cc.png","width":200,"height":200},{"url":"http://94.140.201.90/ctc/images/5dc2ca1318f7e.png","width":300,"height":300},{"url":"http://94.140.201.90/ctc/images/5dc2ca14ccfa8.png","width":400,"height":400}],"bubbleTitle":"Ералаш","bubbleSubTitle":null,"trackUrl":null,"onAirUrl":null,"ageLimit":0,"startTime":"2019-12-12T05:30:00+03:00","isPremiere":false,"isOnAir":false,"viewProgress":null,"imageUrl":[{"url":"http://94.140.201.90/ctc/images/5dcd880943bf7.jpeg","width":1536,"height":864}],"hasPlayButton":false,"videoUrl":null,"type":"tv-slot"},{"widgets":[],"bubbleUrl":"/mobile/Eralash/","bubbleImage":[{"url":"http://94.140.201.90/ctc/images/5dc2ca0c518ee.png","width":60,"height":60},{"url":"http://94.140.201.90/ctc/images/5dc2ca0df389f.png","width":125,"height":125},{"url":"http://94.140.201.90/ctc/images/5dc2ca0fd97cc.png","width":200,"height":200},{"url":"http://94.140.201.90/ctc/images/5dc2ca1318f7e.png","width":300,"height":300},{"url":"http://94.140.201.90/ctc/images/5dc2ca14ccfa8.png","width":400,"height":400}],"bubbleTitle":"Ералаш","bubbleSubTitle":null,"trackUrl":null,"onAirUrl":null,"ageLimit":0,"startTime":"2019-12-12T05:35:00+03:00","isPremiere":false,"isOnAir":false,"viewProgress":null,"imageUrl":[{"url":"http://94.140.201.90/ctc/images/5dcd880943bf7.jpeg","width":1536,"height":864}],"hasPlayButton":false,"videoUrl":null,"type":"tv-slot"},{"widgets":[],"bubbleUrl":"/mobile/Eralash/","bubbleImage":[{"url":"http://94.140.201.90/ctc/images/5dc2ca0c518ee.png","width":60,"height":60},{"url":"http://94.140.201.90/ctc/images/5dc2ca0df389f.png","width":125,"height":125},{"url":"http://94.140.201.90/ctc/images/5dc2ca0fd97cc.png","width":200,"height":200},{"url":"http://94.140.201.90/ctc/images/5dc2ca1318f7e.png","width":300,"height":300},{"url":"http://94.140.201.90/ctc/images/5dc2ca14ccfa8.png","width":400,"height":400}],"bubbleTitle":"Ералаш","bubbleSubTitle":null,"trackUrl":null,"onAirUrl":null,"ageLimit":0,"startTime":"2019-12-12T05:45:00+03:00","isPremiere":false,"isOnAir":false,"viewProgress":null,"imageUrl":[{"url":"http://94.140.201.90/ctc/images/5dcd880943bf7.jpeg","width":1536,"height":864}],"hasPlayButton":false,"videoUrl":null,"type":"tv-slot"},{"widgets":[],"bubbleUrl":"/mobile/Eralash/","bubbleImage":[{"url":"http://94.140.201.90/ctc/images/5dc2ca0c518ee.png","width":60,"height":60},{"url":"http://94.140.201.90/ctc/images/5dc2ca0df389f.png","width":125,"height":125},{"url":"http://94.140.201.90/ctc/images/5dc2ca0fd97cc.png","width":200,"height":200},{"url":"http://94.140.201.90/ctc/images/5dc2ca1318f7e.png","width":300,"height":300},{"url":"http://94.140.201.90/ctc/images/5dc2ca14ccfa8.png","width":400,"height":400}],"bubbleTitle":"Ералаш","bubbleSubTitle":null,"trackUrl":null,"onAirUrl":null,"ageLimit":0,"startTime":"2019-12-12T05:50:00+03:00","isPremiere":false,"isOnAir":false,"viewProgress":null,"imageUrl":[{"url":"http://94.140.201.90/ctc/images/5dcd880943bf7.jpeg","width":1536,"height":864}],"hasPlayButton":false,"videoUrl":null,"type":"tv-slot"}],"title":"Прямой эфир","onAirLink":"https://staticmv.mediavitrina.ru/dist/eump-ctcmedia/current/ctc-ctc.html?zoom=no&muted=yes&disable_com=pre","tvProgramPageLink":"/tvprogram/","description":null,"type":"on-air"},{"widgets":[],"title":"Проекты","tabs":[{"title":"Шоу","code":"show","priority":"50","url":null,"widgets":[{"widgets":[],"title":"Папины дочки","projectVerticalCoverUrl":[{"url":"http://94.140.201.90/ctc/images/5dcada6e262e8.jpeg","width":90,"height":127},{"url":"http://94.140.201.90/ctc/images/5dcada70174b6.jpeg","width":175,"height":248},{"url":"http://94.140.201.90/ctc/images/5dcada715ec88.jpeg","width":250,"height":355},{"url":"http://94.140.201.90/ctc/images/5dcada728fa69.jpeg","width":350,"height":497},{"url":"http://94.140.201.90/ctc/images/5dcada742aea5.jpeg","width":490,"height":695}],"projectPageUrl":"/mobile/Papiny-dochki/","hasTrack":true,"type":"content-project-vertical"},{"widgets":[],"title":"Ранетки","projectVerticalCoverUrl":[{"url":"http://94.140.201.90/ctc/images/5dc294bb0cc18.jpeg","width":90,"height":127},{"url":"http://94.140.201.90/ctc/images/5dc294bbc152b.jpeg","width":175,"height":248},{"url":"http://94.140.201.90/ctc/images/5dc294bd113b8.jpeg","width":250,"height":355},{"url":"http://94.140.201.90/ctc/images/5dc294bdad214.jpeg","width":350,"height":497},{"url":"http://94.140.201.90/ctc/images/5dc294be51787.jpeg","width":490,"height":695}],"projectPageUrl":"/mobile/Ranetki/","hasTrack":true,"type":"content-project-vertical"},{"widgets":[],"title":"Закрытая школа","projectVerticalCoverUrl":[{"url":"http://94.140.201.90/ctc/images/5dcad8d10b1ea.jpeg","width":90,"height":127},{"url":"http://94.140.201.90/ctc/images/5dcad8d24e906.jpeg","width":175,"height":248},{"url":"http://94.140.201.90/ctc/images/5dcad8d390114.jpeg","width":250,"height":355},{"url":"http://94.140.201.90/ctc/images/5dcad8d4dcafa.jpeg","width":350,"height":497},{"url":"http://94.140.201.90/ctc/images/5dcad8d64fdb3.jpeg","width":490,"height":695}],"projectPageUrl":"/mobile/Zakrytaya-shkola/","hasTrack":true,"type":"content-project-vertical"},{"widgets":[],"title":"Восьмидесятые","projectVerticalCoverUrl":[{"url":"http://94.140.201.90/ctc/images/5dcad69e98562.jpeg","width":90,"height":127},{"url":"http://94.140.201.90/ctc/images/5dcad6a0635f0.jpeg","width":175,"height":248},{"url":"http://94.140.201.90/ctc/images/5dcad6a18af0d.jpeg","width":250,"height":355},{"url":"http://94.140.201.90/ctc/images/5dcad6a25777c.jpeg","width":350,"height":497},{"url":"http://94.140.201.90/ctc/images/5dcad6a4c7379.jpeg","width":490,"height":695}],"projectPageUrl":"/mobile/Vosmidesyatye/","hasTrack":true,"type":"content-project-vertical"},{"widgets":[],"title":"Воронины","projectVerticalCoverUrl":[{"url":"http://94.140.201.90/ctc/images/5dcacd7ec7cf5.jpeg","width":90,"height":127},{"url":"http://94.140.201.90/ctc/images/5dcacd7f0b864.jpeg","width":175,"height":248},{"url":"http://94.140.201.90/ctc/images/5dcacd7f530da.jpeg","width":250,"height":355},{"url":"http://94.140.201.90/ctc/images/5dcacd7fd6cd6.jpeg","width":350,"height":497},{"url":"http://94.140.201.90/ctc/images/5dcacd80677d4.jpeg","width":490,"height":695}],"projectPageUrl":"/mobile/Voroniny/","hasTrack":true,"type":"content-project-vertical"},{"widgets":[],"title":"Не родись красивой","projectVerticalCoverUrl":[{"url":"http://94.140.201.90/ctc/images/5dc2944f0d6ad.jpeg","width":90,"height":127},{"url":"http://94.140.201.90/ctc/images/5dc294515fd14.jpeg","width":175,"height":248},{"url":"http://94.140.201.90/ctc/images/5dc2945257348.jpeg","width":250,"height":355},{"url":"http://94.140.201.90/ctc/images/5dc29452b44f8.jpeg","width":350,"height":497},{"url":"http://94.140.201.90/ctc/images/5dc2945317bde.jpeg","width":490,"height":695}],"projectPageUrl":"/mobile/Ne-rodis-krasivoi/","hasTrack":true,"type":"content-project-vertical"},{"widgets":[],"title":"Кухня","projectVerticalCoverUrl":[{"url":"http://94.140.201.90/ctc/images/5dcad18b92936.jpeg","width":90,"height":127},{"url":"http://94.140.201.90/ctc/images/5dcad18c80047.jpeg","width":175,"height":248},{"url":"http://94.140.201.90/ctc/images/5dcad18d41c22.jpeg","width":250,"height":355},{"url":"http://94.140.201.90/ctc/images/5dcad18df058d.jpeg","width":350,"height":497},{"url":"http://94.140.201.90/ctc/images/5dcad18e698b3.jpeg","width":490,"height":695}],"projectPageUrl":"/mobile/Kuhnya/","hasTrack":true,"type":"content-project-vertical"},{"widgets":[],"title":"Молодежка","projectVerticalCoverUrl":[{"url":"http://94.140.201.90/ctc/images/5dcac887f2dd9.jpeg","width":90,"height":127},{"url":"http://94.140.201.90/ctc/images/5dcac888b004c.jpeg","width":175,"height":248},{"url":"http://94.140.201.90/ctc/images/5dcac88a44715.jpeg","width":250,"height":355},{"url":"http://94.140.201.90/ctc/images/5dcac88aa08ca.jpeg","width":350,"height":497},{"url":"http://94.140.201.90/ctc/images/5dcac88b4dd3d.jpeg","width":490,"height":695}],"projectPageUrl":"/mobile/Molodezhka/","hasTrack":true,"type":"content-project-vertical"},{"widgets":[],"title":"Выжить после","projectVerticalCoverUrl":[{"url":"http://94.140.201.90/ctc/images/5dcad7dd59919.jpeg","width":90,"height":127},{"url":"http://94.140.201.90/ctc/images/5dcad7ddd425d.jpeg","width":175,"height":248},{"url":"http://94.140.201.90/ctc/images/5dcad7de8206c.jpeg","width":250,"height":355},{"url":"http://94.140.201.90/ctc/images/5dcad7df8a06b.jpeg","width":350,"height":497},{"url":"http://94.140.201.90/ctc/images/5dcad7e042f50.jpeg","width":490,"height":695}],"projectPageUrl":"/mobile/Vyzhit-posle/","hasTrack":true,"type":"content-project-vertical"},{"widgets":[],"title":"Отель Элеон","projectVerticalCoverUrl":[{"url":"http://94.140.201.90/ctc/images/5dcad4a579cb0.jpeg","width":90,"height":127},{"url":"http://94.140.201.90/ctc/images/5dcad4a741811.jpeg","width":175,"height":248},{"url":"http://94.140.201.90/ctc/images/5dcad4a960590.jpeg","width":250,"height":355},{"url":"http://94.140.201.90/ctc/images/5dcad4ab615c8.jpeg","width":350,"height":497},{"url":"http://94.140.201.90/ctc/images/5dcad4acc5e20.jpeg","width":490,"height":695}],"projectPageUrl":"/mobile/Otel-Eleon/","hasTrack":true,"type":"content-project-vertical"},{"widgets":[],"title":"Ивановы-Ивановы","projectVerticalCoverUrl":[{"url":"http://94.140.201.90/ctc/images/5dcac5fdcabe1.jpeg","width":90,"height":127},{"url":"http://94.140.201.90/ctc/images/5dcac5fe15533.jpeg","width":175,"height":248},{"url":"http://94.140.201.90/ctc/images/5dcac5fe7b82d.jpeg","width":250,"height":355},{"url":"http://94.140.201.90/ctc/images/5dcac5ffa203a.jpeg","width":350,"height":497},{"url":"http://94.140.201.90/ctc/images/5dcac6008bf5f.jpeg","width":490,"height":695}],"projectPageUrl":"/mobile/Ivanovy-Ivanovy/","hasTrack":true,"type":"content-project-vertical"},{"widgets":[],"title":"Психологини","projectVerticalCoverUrl":[{"url":"http://94.140.201.90/ctc/images/5dcad0cf2aa0b.jpeg","width":90,"height":127},{"url":"http://94.140.201.90/ctc/images/5dcad0cf87d2b.jpeg","width":175,"height":248},{"url":"http://94.140.201.90/ctc/images/5dcad0d0252c3.jpeg","width":250,"height":355},{"url":"http://94.140.201.90/ctc/images/5dcad0d0b977c.jpeg","width":350,"height":497},{"url":"http://94.140.201.90/ctc/images/5dcad0d14a4b5.jpeg","width":490,"height":695}],"projectPageUrl":"/mobile/Psihologini/","hasTrack":true,"type":"content-project-vertical"},{"widgets":[],"title":"Улетный экипаж","projectVerticalCoverUrl":[{"url":"http://94.140.201.90/ctc/images/5dcac7a1c9f5e.jpeg","width":90,"height":127},{"url":"http://94.140.201.90/ctc/images/5dcac7a2804a6.jpeg","width":175,"height":248},{"url":"http://94.140.201.90/ctc/images/5dcac7a38e3fd.jpeg","width":250,"height":355},{"url":"http://94.140.201.90/ctc/images/5dcac7a472efd.jpeg","width":350,"height":497},{"url":"http://94.140.201.90/ctc/images/5dcac7a4c0330.jpeg","width":490,"height":695}],"projectPageUrl":"/mobile/Uletnyi-ekipazh/","hasTrack":true,"type":"content-project-vertical"},{"widgets":[],"title":"#СеняФедя","projectVerticalCoverUrl":[{"url":"http://94.140.201.90/ctc/images/5dcac5052a050.jpeg","width":90,"height":127},{"url":"http://94.140.201.90/ctc/images/5dcac505b0c21.jpeg","width":175,"height":248},{"url":"http://94.140.201.90/ctc/images/5dcac506afcda.jpeg","width":250,"height":355},{"url":"http://94.140.201.90/ctc/images/5dcac507374d8.jpeg","width":350,"height":497},{"url":"http://94.140.201.90/ctc/images/5dcac509b9b18.jpeg","width":490,"height":695}],"projectPageUrl":"/mobile/-SenyaFedya/","hasTrack":true,"type":"content-project-vertical"},{"widgets":[],"title":"Кухня. Война за отель","projectVerticalCoverUrl":[{"url":"http://94.140.201.90/ctc/images/5de50b2e0f851.jpeg","width":90,"height":127},{"url":"http://94.140.201.90/ctc/images/5de50b2ee620c.jpeg","width":175,"height":248},{"url":"http://94.140.201.90/ctc/images/5de50b2f799ac.jpeg","width":250,"height":355},{"url":"http://94.140.201.90/ctc/images/5de50b3026e1a.jpeg","width":350,"height":497},{"url":"http://94.140.201.90/ctc/images/5de50b30945d1.jpeg","width":490,"height":695}],"projectPageUrl":"/mobile/Kuhnya-Voina-za-otel/","hasTrack":true,"type":"content-project-vertical"}]},{"title":"Сериалы","code":"serial","priority":"50","url":null,"widgets":[]},{"title":"Фильмы","code":"film","priority":"50","url":null,"widgets":[]},{"title":"Мультфильмы","code":"cartoon","priority":"50","url":null,"widgets":[]},{"title":"все","code":"all","priority":"50","url":null,"widgets":[{"widgets":[],"title":"Папины дочки","projectVerticalCoverUrl":[{"url":"http://94.140.201.90/ctc/images/5dcada6e262e8.jpeg","width":90,"height":127},{"url":"http://94.140.201.90/ctc/images/5dcada70174b6.jpeg","width":175,"height":248},{"url":"http://94.140.201.90/ctc/images/5dcada715ec88.jpeg","width":250,"height":355},{"url":"http://94.140.201.90/ctc/images/5dcada728fa69.jpeg","width":350,"height":497},{"url":"http://94.140.201.90/ctc/images/5dcada742aea5.jpeg","width":490,"height":695}],"projectPageUrl":"/mobile/Papiny-dochki/","hasTrack":true,"type":"content-project-vertical"},{"widgets":[],"title":"Ранетки","projectVerticalCoverUrl":[{"url":"http://94.140.201.90/ctc/images/5dc294bb0cc18.jpeg","width":90,"height":127},{"url":"http://94.140.201.90/ctc/images/5dc294bbc152b.jpeg","width":175,"height":248},{"url":"http://94.140.201.90/ctc/images/5dc294bd113b8.jpeg","width":250,"height":355},{"url":"http://94.140.201.90/ctc/images/5dc294bdad214.jpeg","width":350,"height":497},{"url":"http://94.140.201.90/ctc/images/5dc294be51787.jpeg","width":490,"height":695}],"projectPageUrl":"/mobile/Ranetki/","hasTrack":true,"type":"content-project-vertical"},{"widgets":[],"title":"Закрытая школа","projectVerticalCoverUrl":[{"url":"http://94.140.201.90/ctc/images/5dcad8d10b1ea.jpeg","width":90,"height":127},{"url":"http://94.140.201.90/ctc/images/5dcad8d24e906.jpeg","width":175,"height":248},{"url":"http://94.140.201.90/ctc/images/5dcad8d390114.jpeg","width":250,"height":355},{"url":"http://94.140.201.90/ctc/images/5dcad8d4dcafa.jpeg","width":350,"height":497},{"url":"http://94.140.201.90/ctc/images/5dcad8d64fdb3.jpeg","width":490,"height":695}],"projectPageUrl":"/mobile/Zakrytaya-shkola/","hasTrack":true,"type":"content-project-vertical"},{"widgets":[],"title":"Восьмидесятые","projectVerticalCoverUrl":[{"url":"http://94.140.201.90/ctc/images/5dcad69e98562.jpeg","width":90,"height":127},{"url":"http://94.140.201.90/ctc/images/5dcad6a0635f0.jpeg","width":175,"height":248},{"url":"http://94.140.201.90/ctc/images/5dcad6a18af0d.jpeg","width":250,"height":355},{"url":"http://94.140.201.90/ctc/images/5dcad6a25777c.jpeg","width":350,"height":497},{"url":"http://94.140.201.90/ctc/images/5dcad6a4c7379.jpeg","width":490,"height":695}],"projectPageUrl":"/mobile/Vosmidesyatye/","hasTrack":true,"type":"content-project-vertical"},{"widgets":[],"title":"Воронины","projectVerticalCoverUrl":[{"url":"http://94.140.201.90/ctc/images/5dcacd7ec7cf5.jpeg","width":90,"height":127},{"url":"http://94.140.201.90/ctc/images/5dcacd7f0b864.jpeg","width":175,"height":248},{"url":"http://94.140.201.90/ctc/images/5dcacd7f530da.jpeg","width":250,"height":355},{"url":"http://94.140.201.90/ctc/images/5dcacd7fd6cd6.jpeg","width":350,"height":497},{"url":"http://94.140.201.90/ctc/images/5dcacd80677d4.jpeg","width":490,"height":695}],"projectPageUrl":"/mobile/Voroniny/","hasTrack":true,"type":"content-project-vertical"},{"widgets":[],"title":"Не родись красивой","projectVerticalCoverUrl":[{"url":"http://94.140.201.90/ctc/images/5dc2944f0d6ad.jpeg","width":90,"height":127},{"url":"http://94.140.201.90/ctc/images/5dc294515fd14.jpeg","width":175,"height":248},{"url":"http://94.140.201.90/ctc/images/5dc2945257348.jpeg","width":250,"height":355},{"url":"http://94.140.201.90/ctc/images/5dc29452b44f8.jpeg","width":350,"height":497},{"url":"http://94.140.201.90/ctc/images/5dc2945317bde.jpeg","width":490,"height":695}],"projectPageUrl":"/mobile/Ne-rodis-krasivoi/","hasTrack":true,"type":"content-project-vertical"},{"widgets":[],"title":"Кухня","projectVerticalCoverUrl":[{"url":"http://94.140.201.90/ctc/images/5dcad18b92936.jpeg","width":90,"height":127},{"url":"http://94.140.201.90/ctc/images/5dcad18c80047.jpeg","width":175,"height":248},{"url":"http://94.140.201.90/ctc/images/5dcad18d41c22.jpeg","width":250,"height":355},{"url":"http://94.140.201.90/ctc/images/5dcad18df058d.jpeg","width":350,"height":497},{"url":"http://94.140.201.90/ctc/images/5dcad18e698b3.jpeg","width":490,"height":695}],"projectPageUrl":"/mobile/Kuhnya/","hasTrack":true,"type":"content-project-vertical"},{"widgets":[],"title":"Молодежка","projectVerticalCoverUrl":[{"url":"http://94.140.201.90/ctc/images/5dcac887f2dd9.jpeg","width":90,"height":127},{"url":"http://94.140.201.90/ctc/images/5dcac888b004c.jpeg","width":175,"height":248},{"url":"http://94.140.201.90/ctc/images/5dcac88a44715.jpeg","width":250,"height":355},{"url":"http://94.140.201.90/ctc/images/5dcac88aa08ca.jpeg","width":350,"height":497},{"url":"http://94.140.201.90/ctc/images/5dcac88b4dd3d.jpeg","width":490,"height":695}],"projectPageUrl":"/mobile/Molodezhka/","hasTrack":true,"type":"content-project-vertical"},{"widgets":[],"title":"Выжить после","projectVerticalCoverUrl":[{"url":"http://94.140.201.90/ctc/images/5dcad7dd59919.jpeg","width":90,"height":127},{"url":"http://94.140.201.90/ctc/images/5dcad7ddd425d.jpeg","width":175,"height":248},{"url":"http://94.140.201.90/ctc/images/5dcad7de8206c.jpeg","width":250,"height":355},{"url":"http://94.140.201.90/ctc/images/5dcad7df8a06b.jpeg","width":350,"height":497},{"url":"http://94.140.201.90/ctc/images/5dcad7e042f50.jpeg","width":490,"height":695}],"projectPageUrl":"/mobile/Vyzhit-posle/","hasTrack":true,"type":"content-project-vertical"},{"widgets":[],"title":"Три кота","projectVerticalCoverUrl":[{"url":"http://94.140.201.90/ctc/images/5dcacf7a7078e.jpeg","width":90,"height":127},{"url":"http://94.140.201.90/ctc/images/5dcacf7b0c53f.jpeg","width":175,"height":248},{"url":"http://94.140.201.90/ctc/images/5dcacf7bcbea3.jpeg","width":250,"height":355},{"url":"http://94.140.201.90/ctc/images/5dcacf7cc9892.jpeg","width":350,"height":497},{"url":"http://94.140.201.90/ctc/images/5dcacf7d84a6e.jpeg","width":490,"height":695}],"projectPageUrl":"/mobile/Tri-kota/","hasTrack":true,"type":"content-project-vertical"},{"widgets":[],"title":"Отель Элеон","projectVerticalCoverUrl":[{"url":"http://94.140.201.90/ctc/images/5dcad4a579cb0.jpeg","width":90,"height":127},{"url":"http://94.140.201.90/ctc/images/5dcad4a741811.jpeg","width":175,"height":248},{"url":"http://94.140.201.90/ctc/images/5dcad4a960590.jpeg","width":250,"height":355},{"url":"http://94.140.201.90/ctc/images/5dcad4ab615c8.jpeg","width":350,"height":497},{"url":"http://94.140.201.90/ctc/images/5dcad4acc5e20.jpeg","width":490,"height":695}],"projectPageUrl":"/mobile/Otel-Eleon/","hasTrack":true,"type":"content-project-vertical"},{"widgets":[],"title":"Ивановы-Ивановы","projectVerticalCoverUrl":[{"url":"http://94.140.201.90/ctc/images/5dcac5fdcabe1.jpeg","width":90,"height":127},{"url":"http://94.140.201.90/ctc/images/5dcac5fe15533.jpeg","width":175,"height":248},{"url":"http://94.140.201.90/ctc/images/5dcac5fe7b82d.jpeg","width":250,"height":355},{"url":"http://94.140.201.90/ctc/images/5dcac5ffa203a.jpeg","width":350,"height":497},{"url":"http://94.140.201.90/ctc/images/5dcac6008bf5f.jpeg","width":490,"height":695}],"projectPageUrl":"/mobile/Ivanovy-Ivanovy/","hasTrack":true,"type":"content-project-vertical"},{"widgets":[],"title":"Психологини","projectVerticalCoverUrl":[{"url":"http://94.140.201.90/ctc/images/5dcad0cf2aa0b.jpeg","width":90,"height":127},{"url":"http://94.140.201.90/ctc/images/5dcad0cf87d2b.jpeg","width":175,"height":248},{"url":"http://94.140.201.90/ctc/images/5dcad0d0252c3.jpeg","width":250,"height":355},{"url":"http://94.140.201.90/ctc/images/5dcad0d0b977c.jpeg","width":350,"height":497},{"url":"http://94.140.201.90/ctc/images/5dcad0d14a4b5.jpeg","width":490,"height":695}],"projectPageUrl":"/mobile/Psihologini/","hasTrack":true,"type":"content-project-vertical"},{"widgets":[],"title":"Улетный экипаж","projectVerticalCoverUrl":[{"url":"http://94.140.201.90/ctc/images/5dcac7a1c9f5e.jpeg","width":90,"height":127},{"url":"http://94.140.201.90/ctc/images/5dcac7a2804a6.jpeg","width":175,"height":248},{"url":"http://94.140.201.90/ctc/images/5dcac7a38e3fd.jpeg","width":250,"height":355},{"url":"http://94.140.201.90/ctc/images/5dcac7a472efd.jpeg","width":350,"height":497},{"url":"http://94.140.201.90/ctc/images/5dcac7a4c0330.jpeg","width":490,"height":695}],"projectPageUrl":"/mobile/Uletnyi-ekipazh/","hasTrack":true,"type":"content-project-vertical"},{"widgets":[],"title":"#СеняФедя","projectVerticalCoverUrl":[{"url":"http://94.140.201.90/ctc/images/5dcac5052a050.jpeg","width":90,"height":127},{"url":"http://94.140.201.90/ctc/images/5dcac505b0c21.jpeg","width":175,"height":248},{"url":"http://94.140.201.90/ctc/images/5dcac506afcda.jpeg","width":250,"height":355},{"url":"http://94.140.201.90/ctc/images/5dcac507374d8.jpeg","width":350,"height":497},{"url":"http://94.140.201.90/ctc/images/5dcac509b9b18.jpeg","width":490,"height":695}],"projectPageUrl":"/mobile/-SenyaFedya/","hasTrack":true,"type":"content-project-vertical"},{"widgets":[],"title":"Форт Боярд","projectVerticalCoverUrl":[{"url":"http://94.140.201.90/ctc/images/5de50a4795b5e.jpeg","width":90,"height":127},{"url":"http://94.140.201.90/ctc/images/5de50a47d8b5b.jpeg","width":175,"height":248},{"url":"http://94.140.201.90/ctc/images/5de50a48cb424.jpeg","width":250,"height":355},{"url":"http://94.140.201.90/ctc/images/5de50a4907cf9.jpeg","width":350,"height":497},{"url":"http://94.140.201.90/ctc/images/5de50a4a01c48.jpeg","width":490,"height":695}],"projectPageUrl":"/mobile/Fort-Boyard/","hasTrack":true,"type":"content-project-vertical"},{"widgets":[],"title":"Кухня. Война за отель","projectVerticalCoverUrl":[{"url":"http://94.140.201.90/ctc/images/5de50b2e0f851.jpeg","width":90,"height":127},{"url":"http://94.140.201.90/ctc/images/5de50b2ee620c.jpeg","width":175,"height":248},{"url":"http://94.140.201.90/ctc/images/5de50b2f799ac.jpeg","width":250,"height":355},{"url":"http://94.140.201.90/ctc/images/5de50b3026e1a.jpeg","width":350,"height":497},{"url":"http://94.140.201.90/ctc/images/5de50b30945d1.jpeg","width":490,"height":695}],"projectPageUrl":"/mobile/Kuhnya-Voina-za-otel/","hasTrack":true,"type":"content-project-vertical"}]}],"type":"mobile-popular-project-by-category"}],"seoTags":{"title":null,"h1":null,"description":null},"ogMarkup":{"type":null,"title":null,"description":null,"url":null,"imageUrl":null},"userGeo":{"selectedRegion":{"id":4270372,"cityName":"Москва","subdivisionName":"Москва"},"autoDetectedRegion":{"id":4270372,"cityName":"Москва","subdivisionName":"Москва"}},"userData":[],"meta":[],"activeFrom":null,"activeTo":null,"type":"mobile-live-page"}
""".trimIndent()
