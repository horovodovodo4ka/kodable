package pro.horovodovodo4ka.kodable.core.defaults

import pro.horovodovodo4ka.kodable.core.IKodable
import pro.horovodovodo4ka.kodable.core.json.JsonReader
import pro.horovodovodo4ka.kodable.core.json.JsonWriter
import kotlin.reflect.KClass

object ByteKodable : IKodable<Byte> {
    override fun readValue(reader: JsonReader): Byte = reader.readNumber().toByte()

    override fun writeValue(writer: JsonWriter, instance: Byte) = writer.writeNumber(instance)

    override val list by lazy { super.list }
}

@JvmName("ByteKodable")
fun KClass<Byte>.kodable(): IKodable<Byte> = ByteKodable