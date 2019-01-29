package pro.horovodovodo4ka.kodable.core.defaults

import pro.horovodovodo4ka.kodable.core.IKodable
import pro.horovodovodo4ka.kodable.core.KodableReader
import kotlin.reflect.KClass

object ByteKodable : IKodable<Byte> {
    override fun readValue(reader: KodableReader): Byte = reader.readByte()
    override fun readValueOrNull(reader: KodableReader): Byte? = reader.readByteOrNull()

    override val list by lazy { super.list }
}

@JvmName("ByteKodable")
fun KClass<Byte>.kodable(): IKodable<Byte> = ByteKodable