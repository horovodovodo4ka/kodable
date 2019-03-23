package pro.horovodovodo4ka.kodable.core.defaults

import com.github.fluidsonic.fluid.json.JSONReader
import com.github.fluidsonic.fluid.json.JSONWriter
import com.github.fluidsonic.fluid.json.readLongOrNull
import com.github.fluidsonic.fluid.json.writeLongOrNull
import pro.horovodovodo4ka.kodable.core.IKodable
import kotlin.reflect.KClass

object LongKodable : IKodable<Long> {
    override fun readValue(reader: JSONReader): Long = reader.readLong()
    fun readValueOrNull(reader: JSONReader): Long? = reader.readLongOrNull()

    override fun writeValue(writer: JSONWriter, instance: Long) = writer.writeLong(instance)
    fun writeValueOrNull(writer: JSONWriter, instance: Long?) = writer.writeLongOrNull(instance)

    override val list by lazy { super.list }
}

@JvmName("LongKodable")
fun KClass<Long>.kodable(): IKodable<Long> = LongKodable