package pro.horovodovodo4ka.kodable.core.defaults

import io.fluidsonic.json.JsonReader
import io.fluidsonic.json.JsonWriter
import io.fluidsonic.json.readFloatOrNull
import io.fluidsonic.json.writeFloatOrNull
import pro.horovodovodo4ka.kodable.core.IKodable
import kotlin.reflect.KClass

object FloatKodable : IKodable<Float> {
    override fun readValue(reader: JsonReader): Float = reader.readFloat()
    fun readValueOrNull(reader: JsonReader): Float? = reader.readFloatOrNull()

    override fun writeValue(writer: JsonWriter, instance: Float) = writer.writeFloat(instance)
    fun writeValueOrNull(writer: JsonWriter, instance: Float?) = writer.writeFloatOrNull(instance)

    override val list by lazy { super.list }
}

@JvmName("FloatKodable")
fun KClass<Float>.kodable(): IKodable<Float> = FloatKodable