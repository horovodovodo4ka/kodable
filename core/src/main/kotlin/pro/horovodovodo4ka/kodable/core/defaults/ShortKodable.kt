package pro.horovodovodo4ka.kodable.core.defaults

import pro.horovodovodo4ka.kodable.core.IKodable
import pro.horovodovodo4ka.kodable.core.json.JsonReader
import pro.horovodovodo4ka.kodable.core.json.JsonWriter
import kotlin.reflect.KClass

object ShortKodable : IKodable<Short> {
    override fun readValue(reader: JsonReader): Short = reader.readNumber().toShort()

    override fun writeValue(writer: JsonWriter, instance: Short) = writer.writeNumber(instance)

    override val list by lazy { super.list }
}

@JvmName("ShortKodable")
fun KClass<Short>.kodable(): IKodable<Short> = ShortKodable