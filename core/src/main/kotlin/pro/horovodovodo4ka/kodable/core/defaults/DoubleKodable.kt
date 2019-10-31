package pro.horovodovodo4ka.kodable.core.defaults

import pro.horovodovodo4ka.kodable.core.IKodable
import pro.horovodovodo4ka.kodable.core.json.JsonReader
import pro.horovodovodo4ka.kodable.core.json.JsonWriter
import kotlin.reflect.KClass

object DoubleKodable : IKodable<Double> {
    override fun readValue(reader: JsonReader): Double = reader.readNumber().toDouble()

    override fun writeValue(writer: JsonWriter, instance: Double) = writer.writeNumber(instance)

    override val list by lazy { super.list }
}

@JvmName("DoubleKodable")
fun KClass<Double>.kodable(): IKodable<Double> = DoubleKodable