package pro.horovodovodo4ka.kodable.core.types

import pro.horovodovodo4ka.kodable.core.IKodable
import pro.horovodovodo4ka.kodable.core.json.JsonReader
import pro.horovodovodo4ka.kodable.core.json.JsonWriter
import pro.horovodovodo4ka.kodable.core.json.objectProperty
import kotlin.reflect.KClass

const val polymorphicDefaultMarker = ""

interface PolyKodableConfig<BaseType : Any> {
    fun propType(typeProperty: String)

    infix fun <T : BaseType> KClass<T>.named(string: String): Pair<String, KClass<T>>

    infix fun <T : BaseType> Pair<String, KClass<T>>.with(kodable: IKodable<T>)
}

inline fun <reified BaseType : Any> PolyKodableConfig<BaseType>.withFallback(kodable: IKodable<BaseType>) {
    BaseType::class named polymorphicDefaultMarker with kodable
}

inline fun <reified BaseType : Any> PolyKodableConfig<BaseType>.withFallback(default: BaseType) {
    val kodable = object : IKodable<BaseType> {
        override fun readValue(reader: JsonReader): BaseType = default
    }

    withFallback(kodable)
}

fun <T : Any> poly(config: PolyKodableConfig<T>.() -> Unit): IKodable<T> = PolymorphicKodable(config)

////

private class PolymorphicDescription<BaseType : Any, ConcreteType : BaseType>(val kclass: KClass<out BaseType>, val concreteKodable: IKodable<ConcreteType>) : IKodable<BaseType> {
    override fun readValue(reader: JsonReader): BaseType {
        return concreteKodable.readValue(reader)
    }

    @Suppress("UNCHECKED_CAST")
    override fun writeValue(writer: JsonWriter, instance: BaseType) {
        concreteKodable.writeValue(writer, instance as ConcreteType)
    }
}

private class PolymorphicKodable<BaseType : Any>(config: PolyKodableConfig<BaseType>.() -> Unit) : IKodable<BaseType> {

    private var typeProperty: String = "type"
    private val polymorphicKoders = mutableMapOf<String, PolymorphicDescription<BaseType, *>>()

    private val default
        get() = polymorphicKoders[polymorphicDefaultMarker]

    init {
        config(Config())
    }

    inner class Config : PolyKodableConfig<BaseType> {

        override fun propType(typeProperty: String) {
            this@PolymorphicKodable.typeProperty = typeProperty
        }

        override infix fun <T : BaseType> KClass<T>.named(string: String): Pair<String, KClass<T>> {
            return string to this
        }

        override infix fun <T : BaseType> Pair<String, KClass<T>>.with(kodable: IKodable<T>) {
            polymorphicKoders[first] = PolymorphicDescription<BaseType, T>(this.second, kodable)
        }
    }

    override fun readValue(reader: JsonReader): BaseType {
        var polymorphicTag = ""

        val objectSnapshot = reader.iterateObjectWithPrefetch {
            if (it == typeProperty) polymorphicTag = readString()
            else skipValue()
        }

        val decoder =
            polymorphicKoders[polymorphicTag]
                ?: default
                ?: throw KodableException("Unknown polymorphic case '$polymorphicTag' in ${this::class}")

        return decoder.readValue(objectSnapshot)
    }

    override fun writeValue(writer: JsonWriter, instance: BaseType) {
        val (type, encoder) =
            polymorphicKoders.toList().firstOrNull { it.second.kclass == instance::class }
                ?: throw KodableException("Unknown polymorphic case '${instance::class}' in ${this::class}")

        val props = sequenceOf(
            objectProperty(typeProperty, type) { writeString(type) }
        )

        writer.prependObject(props)
        encoder.writeValue(writer, instance)
    }
}
