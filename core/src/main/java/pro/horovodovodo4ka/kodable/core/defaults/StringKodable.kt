package pro.horovodovodo4ka.kodable.core.defaults

import pro.horovodovodo4ka.kodable.core.IKodable
import pro.horovodovodo4ka.kodable.core.JSONReader
import kotlin.reflect.KClass

object StringKodable : IKodable<String> {
    override fun readValue(reader: JSONReader): String = reader.readString()
    override fun readValueOrNull(reader: JSONReader): String? = reader.readStringOrNull()

    override val list by lazy { super.list }
}

@JvmName("StringKodable")
fun KClass<String>.kodable(): IKodable<String> = StringKodable
