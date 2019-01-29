package pro.horovodovodo4ka.kodable.core.defaults

import pro.horovodovodo4ka.kodable.core.IKodable
import pro.horovodovodo4ka.kodable.core.KodableReader
import kotlin.reflect.KClass

object ShortKodable : IKodable<Short> {
    override fun readValue(reader: KodableReader): Short = reader.readShort()
    override fun readValueOrNull(reader: KodableReader): Short? = reader.readShortOrNull()

    override val list by lazy { super.list }
}

@JvmName("ShortKodable")
fun KClass<Short>.kodable(): IKodable<Short> = ShortKodable