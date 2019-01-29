package pro.horovodovodo4ka.kodable.core.defaults

import pro.horovodovodo4ka.kodable.core.IKodable
import pro.horovodovodo4ka.kodable.core.KodableReader
import kotlin.reflect.KClass

object NumberKodable : IKodable<Number> {
    override fun readValue(reader: KodableReader): Number = reader.readNumber()
    override fun readValueOrNull(reader: KodableReader): Number? = reader.readNumberOrNull()

    override val list by lazy { super.list }
}

@JvmName("NumberKodable")
fun KClass<Number>.kodable(): IKodable<Number> = NumberKodable