package pro.horovodovodo4ka.kodable.core.defaults

import io.fluidsonic.json.JsonReader
import io.fluidsonic.json.JsonWriter
import io.fluidsonic.json.readLongOrNull
import io.fluidsonic.json.writeLongOrNull
import pro.horovodovodo4ka.kodable.core.IKodable
import kotlin.reflect.KClass

object LongKodable : IKodable<Long> {
    override fun readValue(reader: JsonReader): Long = reader.readLong()
    fun readValueOrNull(reader: JsonReader): Long? = reader.readLongOrNull()

    override fun writeValue(writer: JsonWriter, instance: Long) = writer.writeLong(instance)
    fun writeValueOrNull(writer: JsonWriter, instance: Long?) = writer.writeLongOrNull(instance)

    override val list by lazy { super.list }
}

@JvmName("LongKodable")
fun KClass<Long>.kodable(): IKodable<Long> = LongKodable