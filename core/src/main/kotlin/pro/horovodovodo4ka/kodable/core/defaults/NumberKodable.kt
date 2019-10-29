package pro.horovodovodo4ka.kodable.core.defaults

import io.fluidsonic.json.JsonReader
import io.fluidsonic.json.JsonWriter
import io.fluidsonic.json.readNumberOrNull
import io.fluidsonic.json.writeNumberOrNull
import pro.horovodovodo4ka.kodable.core.IKodable
import kotlin.reflect.KClass

object NumberKodable : IKodable<Number> {
    override fun readValue(reader: JsonReader): Number = reader.readNumber()
    fun readValueOrNull(reader: JsonReader): Number? = reader.readNumberOrNull()

    override fun writeValue(writer: JsonWriter, instance: Number) = writer.writeNumber(instance)
    fun writeValueOrNull(writer: JsonWriter, instance: Number?) = writer.writeNumberOrNull(instance)

    override val list by lazy { super.list }
}

@JvmName("NumberKodable")
fun KClass<Number>.kodable(): IKodable<Number> = NumberKodable