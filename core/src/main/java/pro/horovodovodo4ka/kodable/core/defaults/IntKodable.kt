package pro.horovodovodo4ka.kodable.core.defaults

import pro.horovodovodo4ka.kodable.core.IKodable
import pro.horovodovodo4ka.kodable.core.KodableReader
import kotlin.reflect.KClass

object IntKodable : IKodable<Int> {
    override fun readValue(reader: KodableReader): Int = reader.readInt()
    override fun readValueOrNull(reader: KodableReader): Int? = reader.readIntOrNull()

    override val list by lazy { super.list }
}

@JvmName("IntKodable")
fun KClass<Int>.kodable(): IKodable<Int> = IntKodable
