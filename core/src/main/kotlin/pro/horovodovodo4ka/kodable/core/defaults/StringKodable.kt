package pro.horovodovodo4ka.kodable.core.defaults

import pro.horovodovodo4ka.kodable.core.IKodable
import pro.horovodovodo4ka.kodable.core.json.JsonReader
import pro.horovodovodo4ka.kodable.core.json.JsonWriter
import kotlin.reflect.KClass

object StringKodable : IKodable<String> {
    override fun readValue(reader: JsonReader): String = reader.readString()

    override fun writeValue(writer: JsonWriter, instance: String) = writer.writeString(instance)

    override val list by lazy { super.list }
}

@JvmName("StringKodable")
fun KClass<String>.kodable(): IKodable<String> = StringKodable
