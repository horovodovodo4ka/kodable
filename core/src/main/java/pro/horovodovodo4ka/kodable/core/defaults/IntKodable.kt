package pro.horovodovodo4ka.kodable.core.defaults

import pro.horovodovodo4ka.kodable.core.JSONReader
import pro.horovodovodo4ka.kodable.core.IKodable
import kotlin.reflect.KClass

object IntKodable : IKodable<Int> {
    override fun readValue(reader: JSONReader): Int = reader.readInt()
    override fun readValueOrNull(reader: JSONReader): Int? = reader.readIntOrNull()

    override val list by lazy { super.list }
}

@JvmName("IntKodable")
fun KClass<Int>.kodable(): IKodable<Int> = IntKodable
