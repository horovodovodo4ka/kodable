package pro.horovodovodo4ka.kodable.core.defaults

import com.github.fluidsonic.fluid.json.JSONReader
import com.github.fluidsonic.fluid.json.JSONWriter
import com.github.fluidsonic.fluid.json.readByteOrNull
import com.github.fluidsonic.fluid.json.writeByteOrNull
import pro.horovodovodo4ka.kodable.core.IKodable
import kotlin.reflect.KClass

object ByteKodable : IKodable<Byte> {
    override fun readValue(reader: JSONReader): Byte = reader.readByte()
    fun readValueOrNull(reader: JSONReader): Byte? = reader.readByteOrNull()

    override fun writeValue(writer: JSONWriter, instance: Byte) = writer.writeByte(instance)
    fun writeValueOrNull(writer: JSONWriter, instance: Byte?) = writer.writeByteOrNull(instance)

    override val list by lazy { super.list }
}

@JvmName("ByteKodable")
fun KClass<Byte>.kodable(): IKodable<Byte> = ByteKodable