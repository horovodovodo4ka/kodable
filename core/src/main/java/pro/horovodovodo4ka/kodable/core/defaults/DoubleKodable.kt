package pro.horovodovodo4ka.kodable.core.defaults

import pro.horovodovodo4ka.kodable.core.JSONReader
import pro.horovodovodo4ka.kodable.core.Kodable
import kotlin.reflect.KClass

object DoubleKodable : Kodable<Double> {
    override fun readValue(reader: JSONReader): Double = reader.readDouble()
    override fun readValueOrNull(reader: JSONReader): Double? = reader.readDoubleOrNull()

    override val list by lazy { super.list }
}

@JvmName("DoubleKodable")
fun KClass<Double>.kodable(): Kodable<Double> = DoubleKodable