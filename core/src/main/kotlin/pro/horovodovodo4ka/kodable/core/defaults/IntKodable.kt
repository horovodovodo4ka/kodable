package pro.horovodovodo4ka.kodable.core.defaults

import pro.horovodovodo4ka.kodable.core.IKodable
import pro.horovodovodo4ka.kodable.core.json.JsonReader
import pro.horovodovodo4ka.kodable.core.json.JsonWriter
import kotlin.reflect.KClass

object IntKodable : IKodable<Int> {
    override fun readValue(reader: JsonReader): Int = reader.readNumber().toInt()

    override fun writeValue(writer: JsonWriter, instance: Int) = writer.writeNumber(instance)

    override val list by lazy { super.list }
}

@JvmName("IntKodable")
fun KClass<Int>.kodable(): IKodable<Int> = IntKodable
