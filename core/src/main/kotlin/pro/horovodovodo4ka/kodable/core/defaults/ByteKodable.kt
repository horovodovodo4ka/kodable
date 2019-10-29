package pro.horovodovodo4ka.kodable.core.defaults

import io.fluidsonic.json.JsonReader
import io.fluidsonic.json.JsonWriter
import io.fluidsonic.json.readByteOrNull
import io.fluidsonic.json.writeByteOrNull
import pro.horovodovodo4ka.kodable.core.IKodable
import kotlin.reflect.KClass

object ByteKodable : IKodable<Byte> {
    override fun readValue(reader: JsonReader): Byte = reader.readByte()
    fun readValueOrNull(reader: JsonReader): Byte? = reader.readByteOrNull()

    override fun writeValue(writer: JsonWriter, instance: Byte) = writer.writeByte(instance)
    fun writeValueOrNull(writer: JsonWriter, instance: Byte?) = writer.writeByteOrNull(instance)

    override val list by lazy { super.list }
}

@JvmName("ByteKodable")
fun KClass<Byte>.kodable(): IKodable<Byte> = ByteKodable