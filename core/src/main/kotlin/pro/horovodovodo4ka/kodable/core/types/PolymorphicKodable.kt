package pro.horovodovodo4ka.kodable.core.types

import pro.horovodovodo4ka.kodable.core.IKodable
import pro.horovodovodo4ka.kodable.core.json.JsonReader
import pro.horovodovodo4ka.kodable.core.json.JsonWriter
import pro.horovodovodo4ka.kodable.core.json.objectProperty
import pro.horovodovodo4ka.kodable.core.json.record
import kotlin.reflect.KClass

typealias PolymorphicDescription<BaseType> = Pair<String, IKodable<BaseType>>

class PolymorphicKodable<BaseType: Any>(config: PolymorphicKodable<BaseType>.Config.() -> Unit) : IKodable<BaseType> {

    private var typeProperty: String = "type"
    private val polymorphicKoders = mutableMapOf<KClass<out BaseType>, IKodable<out BaseType>>()

    inner class Config {
        fun propType(typeProperty: String) {
            this@PolymorphicKodable.typeProperty = typeProperty
        }

        infix fun <T: BaseType> KClass<T>.with(koder: IKodable<T>) {
            polymorphicKoders[this] = koder
        }
    }

    override fun readValue(reader: JsonReader): BaseType {
        lateinit var polymorphicTag: String

        val snapshot = reader.record {
            iterateObject {
                if (it == typeProperty) polymorphicTag = readString()
                else skipValue()
            }
        }
//
//        val koder = polymorphicKoders[polymorphicTag] ?: throw Exception("Unknown polymorphic case '$polymorphicTag' in ${this::class}")
//
//        return koder.readValue(snapshot)
        TODO()
    }

    override fun writeValue(writer: JsonWriter, instance: BaseType) {
//        val (polymorpicTag, kodable) = kodersByClass[instance::class] ?: throw Exception("Unknown polymorphic case '${instance::class}' in ${this::class}")
//
//        val props = sequenceOf(
//            objectProperty(typeProperty) { writeString(polymorpicTag) },
//            objectProperty(valueProperty) { kodable.writeValue(this, instance) }
//        )
//
//        writer.iterateObject(props)
    }
}

//fun <BaseType: Any> KClass<BaseType>.polyKodable(typeProperty: String = "type",
//                              valueProperty: String = "value",
//                              firstPolymorphicKoder: PolymorphicDescription<BaseType>,
//                              vararg polymorphicKoders: PolymorphicDescription<BaseType>) : IKodable<BaseType> {
//    return PolymorphicKodable(this, typeProperty, valueProperty, (listOf(firstPolymorphicKoder) + polymorphicKoders).toMap())
//}

//inline fun <reified BaseType: Any> polymorphic(typeProperty: String = "type",
//                                        valueProperty: String = "value",
//                                        firstPolymorphicKoder: PolymorphicDescription<BaseType>,
//                                        vararg polymorphicKoders: PolymorphicDescription<BaseType>) : IKodable<BaseType> {
//    return PolymorphicKodable(BaseType::class, typeProperty, valueProperty, (listOf(firstPolymorphicKoder) + polymorphicKoders).toMap())
//}