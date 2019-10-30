package pro.horovodovodo4ka.kodable.core.defaults

import pro.horovodovodo4ka.kodable.core.IKodable
import pro.horovodovodo4ka.kodable.core.json.JsonReader
import pro.horovodovodo4ka.kodable.core.json.JsonWriter
import kotlin.reflect.KClass

object NumberKodable : IKodable<Number> {
    override fun readValue(reader: JsonReader): Number = reader.readNumber()

    override fun writeValue(writer: JsonWriter, instance: Number) = writer.writeNumber(instance)

    override val list by lazy { super.list }
}

@JvmName("NumberKodable")
fun KClass<Number>.kodable(): IKodable<Number> = NumberKodable