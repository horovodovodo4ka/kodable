package pro.horovodovodo4ka.kodable.core.defaults

import pro.horovodovodo4ka.kodable.core.JSONReader
import pro.horovodovodo4ka.kodable.core.Kodable
import kotlin.reflect.KClass

object StringKodable : Kodable<String> {
    override fun readValue(reader: JSONReader): String = reader.readString()
    override fun readValueOrNull(reader: JSONReader): String? = reader.readStringOrNull()

    override val list by lazy { super.list }
}

@JvmName("StringKodable")
fun KClass<String>.kodable(): Kodable<String> = StringKodable
