package pro.horovodovodo4ka.kodable.core.defaults

import pro.horovodovodo4ka.kodable.core.IKodable
import pro.horovodovodo4ka.kodable.core.json.JsonReader
import pro.horovodovodo4ka.kodable.core.json.JsonWriter
import kotlin.reflect.KClass

object FloatKodable : IKodable<Float> {
    override fun readValue(reader: JsonReader): Float = reader.readNumber().toFloat()

    override fun writeValue(writer: JsonWriter, instance: Float) = writer.writeNumber(instance)

    override val list by lazy { super.list }
}

@JvmName("FloatKodable")
fun KClass<Float>.kodable(): IKodable<Float> = FloatKodable