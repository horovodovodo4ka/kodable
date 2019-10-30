package pro.horovodovodo4ka.kodable.core.defaults

import pro.horovodovodo4ka.kodable.core.IKodable
import pro.horovodovodo4ka.kodable.core.json.JsonReader
import pro.horovodovodo4ka.kodable.core.json.JsonWriter
import kotlin.reflect.KClass

object LongKodable : IKodable<Long> {
    override fun readValue(reader: JsonReader): Long = reader.readNumber().toLong()

    override fun writeValue(writer: JsonWriter, instance: Long) = writer.writeNumber(instance)

    override val list by lazy { super.list }
}

@JvmName("LongKodable")
fun KClass<Long>.kodable(): IKodable<Long> = LongKodable