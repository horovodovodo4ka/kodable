package pro.horovodovodo4ka.kodable.core.types

import pro.horovodovodo4ka.kodable.core.IKodable
import pro.horovodovodo4ka.kodable.core.json.JsonReader
import pro.horovodovodo4ka.kodable.core.json.JsonWriter
import pro.horovodovodo4ka.kodable.core.json.objectProperty
import kotlin.reflect.KClass

interface PolyKodableConfig<BaseType : Any> {
    fun propType(typeProperty: String)

    infix fun <T : Any> KClass<T>.named(string: String): Pair<String, KClass<T>>

    infix fun <T : BaseType> Pair<String, KClass<T>>.with(kodable: IKodable<T>)
}

fun <T : Any> poly(config: PolyKodableConfig<T>.() -> Unit): IKodable<T> = PolymorphicKodable(config)

////

private class PolymorphicDescription<BaseType : Any, ConcreteType : BaseType>(val type: String, val kclass: KClass<*>, val concreteKodable: IKodable<ConcreteType>) :
    IKodable<BaseType> {
    operator fun component1() = type
    operator fun component2() = kclass
    operator fun component3() = concreteKodable

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
    private val polymorphicKoders = mutableListOf<PolymorphicDescription<BaseType, *>>()

    init {
        config(Config())
    }

    inner class Config : PolyKodableConfig<BaseType> {

        override fun propType(typeProperty: String) {
            this@PolymorphicKodable.typeProperty = typeProperty
        }

        override infix fun <T : Any> KClass<T>.named(string: String): Pair<String, KClass<T>> {
            return string to this
        }

        override infix fun <T : BaseType> Pair<String, KClass<T>>.with(kodable: IKodable<T>) {
            val binding = PolymorphicDescription<BaseType, T>(
                this.first,
                this.second,
                kodable
            )

            polymorphicKoders.add(binding)
        }
    }

    override fun readValue(reader: JsonReader): BaseType {
        var polymorphicTag = ""

        val objectSnapshot = reader.iterateObjectWithPrefetch {
            if (it == typeProperty) polymorphicTag = readString()
            else skipValue()
        }

        val decoder = polymorphicKoders.firstOrNull { it.type == polymorphicTag }
            ?: throw Exception("Unknown polymorphic case '$polymorphicTag' in ${this::class}")

        return decoder.readValue(objectSnapshot)
    }

    override fun writeValue(writer: JsonWriter, instance: BaseType) {
        val encoder = polymorphicKoders.firstOrNull { it.kclass == instance::class }
            ?: throw Exception("Unknown polymorphic case '${instance::class}' in ${this::class}")

        val props = sequenceOf(
            objectProperty(typeProperty) { writeString(encoder.type) }
        )

        writer.prependObject(props)
        encoder.writeValue(writer, instance)
    }
}
