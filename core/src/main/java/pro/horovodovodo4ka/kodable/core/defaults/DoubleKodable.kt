package pro.horovodovodo4ka.kodable.core.defaults

import pro.horovodovodo4ka.kodable.core.IKodable
import pro.horovodovodo4ka.kodable.core.KodableReader
import kotlin.reflect.KClass

object DoubleKodable : IKodable<Double> {
    override fun readValue(reader: KodableReader): Double = reader.readDouble()
    override fun readValueOrNull(reader: KodableReader): Double? = reader.readDoubleOrNull()

    override val list by lazy { super.list }
}

@JvmName("DoubleKodable")
fun KClass<Double>.kodable(): IKodable<Double> = DoubleKodable