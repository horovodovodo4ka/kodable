package pro.horovodovodo4ka.kodable.core.defaults

import pro.horovodovodo4ka.kodable.core.JSONReader
import pro.horovodovodo4ka.kodable.core.Kodable
import kotlin.reflect.KClass

object ShortKodable : Kodable<Short> {
    override fun readValue(reader: JSONReader): Short = reader.readShort()
    override fun readValueOrNull(reader: JSONReader): Short? = reader.readShortOrNull()

    override val list by lazy { super.list }
}

@JvmName("ShortKodable")
fun KClass<Short>.kodable(): Kodable<Short> = ShortKodable