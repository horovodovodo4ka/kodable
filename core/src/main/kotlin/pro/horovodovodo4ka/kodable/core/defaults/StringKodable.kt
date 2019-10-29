package pro.horovodovodo4ka.kodable.core.defaults

import io.fluidsonic.json.JsonReader
import io.fluidsonic.json.JsonWriter
import io.fluidsonic.json.readStringOrNull
import io.fluidsonic.json.writeStringOrNull
import pro.horovodovodo4ka.kodable.core.IKodable
import kotlin.reflect.KClass

object StringKodable : IKodable<String> {
    override fun readValue(reader: JsonReader): String = reader.readString()
    fun readValueOrNull(reader: JsonReader): String? = reader.readStringOrNull()

    override fun writeValue(writer: JsonWriter, instance: String) = writer.writeString(instance)
    fun writeValueOrNull(writer: JsonWriter, instance: String?) = writer.writeStringOrNull(instance)

    override val list by lazy { super.list }
}

@JvmName("StringKodable")
fun KClass<String>.kodable(): IKodable<String> = StringKodable
