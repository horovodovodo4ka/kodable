package pro.horovodovodo4ka.kodable.core.defaults

import com.github.fluidsonic.fluid.json.JSONReader
import com.github.fluidsonic.fluid.json.readByteOrNull
import pro.horovodovodo4ka.kodable.core.IKodable
import kotlin.reflect.KClass

object ByteKodable : IKodable<Byte> {
    override fun readValue(reader: JSONReader): Byte = reader.readByte()
    override fun readValueOrNull(reader: JSONReader): Byte? = reader.readByteOrNull()

    override val list by lazy { super.list }
}

@JvmName("ByteKodable")
fun KClass<Byte>.kodable(): IKodable<Byte> = ByteKodable