package pro.horovodovodo4ka.kodable.core.defaults

import io.fluidsonic.json.JsonReader
import io.fluidsonic.json.JsonWriter
import io.fluidsonic.json.readDoubleOrNull
import io.fluidsonic.json.writeDoubleOrNull
import pro.horovodovodo4ka.kodable.core.IKodable
import kotlin.reflect.KClass

object DoubleKodable : IKodable<Double> {
    override fun readValue(reader: JsonReader): Double = reader.readDouble()
    fun readValueOrNull(reader: JsonReader): Double? = reader.readDoubleOrNull()

    override fun writeValue(writer: JsonWriter, instance: Double) = writer.writeDouble(instance)
    fun writeValueOrNull(writer: JsonWriter, instance: Double?) = writer.writeDoubleOrNull(instance)

    override val list by lazy { super.list }
}

@JvmName("DoubleKodable")
fun KClass<Double>.kodable(): IKodable<Double> = DoubleKodable