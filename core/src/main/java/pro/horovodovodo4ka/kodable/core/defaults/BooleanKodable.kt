package pro.horovodovodo4ka.kodable.core.defaults

import pro.horovodovodo4ka.kodable.core.JSONReader
import pro.horovodovodo4ka.kodable.core.IKodable
import kotlin.reflect.KClass

object BooleanKodable : IKodable<Boolean> {
    override fun readValue(reader: JSONReader): Boolean = reader.readBoolean()
    override fun readValueOrNull(reader: JSONReader): Boolean? = reader.readBooleanOrNull()

    override val list by lazy { super.list }
}

@JvmName("BooleanKodable")
fun KClass<Boolean>.kodable(): IKodable<Boolean> = BooleanKodable