package pro.horovodovodo4ka.kodable.core.defaults

import io.fluidsonic.json.JsonReader
import io.fluidsonic.json.JsonWriter
import io.fluidsonic.json.readShortOrNull
import io.fluidsonic.json.writeShortOrNull
import pro.horovodovodo4ka.kodable.core.IKodable
import kotlin.reflect.KClass

object ShortKodable : IKodable<Short> {
    override fun readValue(reader: JsonReader): Short = reader.readShort()
    fun readValueOrNull(reader: JsonReader): Short? = reader.readShortOrNull()

    override fun writeValue(writer: JsonWriter, instance: Short) = writer.writeShort(instance)
    fun writeValueOrNull(writer: JsonWriter, instance: Short?) = writer.writeShortOrNull(instance)

    override val list by lazy { super.list }
}

@JvmName("ShortKodable")
fun KClass<Short>.kodable(): IKodable<Short> = ShortKodable