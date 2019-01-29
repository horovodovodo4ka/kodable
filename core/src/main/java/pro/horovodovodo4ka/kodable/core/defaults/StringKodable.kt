package pro.horovodovodo4ka.kodable.core.defaults

import pro.horovodovodo4ka.kodable.core.IKodable
import pro.horovodovodo4ka.kodable.core.KodableReader
import kotlin.reflect.KClass

object StringKodable : IKodable<String> {
    override fun readValue(reader: KodableReader): String = reader.readString()
    override fun readValueOrNull(reader: KodableReader): String? = reader.readStringOrNull()

    override val list by lazy { super.list }
}

@JvmName("StringKodable")
fun KClass<String>.kodable(): IKodable<String> = StringKodable
