package pro.horovodovodo4ka.kodable.core.defaults

import pro.horovodovodo4ka.kodable.core.JSONReader
import pro.horovodovodo4ka.kodable.core.Kodable
import kotlin.reflect.KClass

object NumberKodable : Kodable<Number> {
    override fun readValue(reader: JSONReader): Number = reader.readNumber()
    override fun readValueOrNull(reader: JSONReader): Number? = reader.readNumberOrNull()

    override val list by lazy { super.list }
}

@JvmName("NumberKodable")
fun KClass<Number>.kodable(): Kodable<Number> = NumberKodable