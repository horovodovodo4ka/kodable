package pro.horovodovodo4ka.kodable.core.defaults

import pro.horovodovodo4ka.kodable.core.JSONReader
import pro.horovodovodo4ka.kodable.core.Kodable
import kotlin.reflect.KClass

object IntKodable : Kodable<Int> {
    override fun readValue(reader: JSONReader): Int = reader.readInt()
    override fun readValueOrNull(reader: JSONReader): Int? = reader.readIntOrNull()

    override val list by lazy { super.list }
}

@JvmName("IntKodable")
fun KClass<Int>.kodable(): Kodable<Int> = IntKodable
