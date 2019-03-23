package pro.horovodovodo4ka.kodable.core.defaults

import com.github.fluidsonic.fluid.json.JSONReader
import com.github.fluidsonic.fluid.json.JSONWriter
import com.github.fluidsonic.fluid.json.readFloatOrNull
import com.github.fluidsonic.fluid.json.writeFloatOrNull
import pro.horovodovodo4ka.kodable.core.IKodable
import kotlin.reflect.KClass

object FloatKodable : IKodable<Float> {
    override fun readValue(reader: JSONReader): Float = reader.readFloat()
    fun readValueOrNull(reader: JSONReader): Float? = reader.readFloatOrNull()

    override fun writeValue(writer: JSONWriter, instance: Float) = writer.writeFloat(instance)
    fun writeValueOrNull(writer: JSONWriter, instance: Float?) = writer.writeFloatOrNull(instance)

    override val list by lazy { super.list }
}

@JvmName("FloatKodable")
fun KClass<Float>.kodable(): IKodable<Float> = FloatKodable