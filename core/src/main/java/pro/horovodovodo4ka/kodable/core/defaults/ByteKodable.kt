package pro.horovodovodo4ka.kodable.core.defaults

import pro.horovodovodo4ka.kodable.core.JSONReader
import pro.horovodovodo4ka.kodable.core.Kodable
import kotlin.reflect.KClass

object ByteKodable : Kodable<Byte> {
    override fun readValue(reader: JSONReader): Byte = reader.readByte()
    override fun readValueOrNull(reader: JSONReader): Byte? = reader.readByteOrNull()

    override val list by lazy { super.list }
}

@JvmName("ByteKodable")
fun KClass<Byte>.kodable(): Kodable<Byte> = ByteKodable