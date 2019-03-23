package pro.horovodovodo4ka.kodable.core.defaults

import com.github.fluidsonic.fluid.json.JSONReader
import com.github.fluidsonic.fluid.json.JSONWriter
import com.github.fluidsonic.fluid.json.readShortOrNull
import com.github.fluidsonic.fluid.json.writeShortOrNull
import pro.horovodovodo4ka.kodable.core.IKodable
import kotlin.reflect.KClass

object ShortKodable : IKodable<Short> {
    override fun readValue(reader: JSONReader): Short = reader.readShort()
    fun readValueOrNull(reader: JSONReader): Short? = reader.readShortOrNull()

    override fun writeValue(writer: JSONWriter, instance: Short) = writer.writeShort(instance)
    fun writeValueOrNull(writer: JSONWriter, instance: Short?) = writer.writeShortOrNull(instance)

    override val list by lazy { super.list }
}

@JvmName("ShortKodable")
fun KClass<Short>.kodable(): IKodable<Short> = ShortKodable