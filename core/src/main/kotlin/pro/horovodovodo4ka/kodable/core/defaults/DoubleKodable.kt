package pro.horovodovodo4ka.kodable.core.defaults

import com.github.fluidsonic.fluid.json.JSONReader
import com.github.fluidsonic.fluid.json.JSONWriter
import com.github.fluidsonic.fluid.json.readDoubleOrNull
import com.github.fluidsonic.fluid.json.writeDoubleOrNull
import pro.horovodovodo4ka.kodable.core.IKodable
import kotlin.reflect.KClass

object DoubleKodable : IKodable<Double> {
    override fun readValue(reader: JSONReader): Double = reader.readDouble()
    fun readValueOrNull(reader: JSONReader): Double? = reader.readDoubleOrNull()

    override fun writeValue(writer: JSONWriter, instance: Double) = writer.writeDouble(instance)
    fun writeValueOrNull(writer: JSONWriter, instance: Double?) = writer.writeDoubleOrNull(instance)

    override val list by lazy { super.list }
}

@JvmName("DoubleKodable")
fun KClass<Double>.kodable(): IKodable<Double> = DoubleKodable