package pro.horovodovodo4ka.kodable.core.defaults

import pro.horovodovodo4ka.kodable.core.JSONReader
import pro.horovodovodo4ka.kodable.core.IKodable
import kotlin.reflect.KClass

object NumberKodable : IKodable<Number> {
    override fun readValue(reader: JSONReader): Number = reader.readNumber()
    override fun readValueOrNull(reader: JSONReader): Number? = reader.readNumberOrNull()

    override val list by lazy { super.list }
}

@JvmName("NumberKodable")
fun KClass<Number>.kodable(): IKodable<Number> = NumberKodable