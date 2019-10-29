package pro.horovodovodo4ka.kodable.core.defaults

import io.fluidsonic.json.JsonReader
import io.fluidsonic.json.JsonWriter
import io.fluidsonic.json.readBooleanOrNull
import io.fluidsonic.json.writeBooleanOrNull
import pro.horovodovodo4ka.kodable.core.IKodable
import kotlin.reflect.KClass

object BooleanKodable : IKodable<Boolean> {
    override fun readValue(reader: JsonReader): Boolean = reader.readBoolean()
    fun readValueOrNull(reader: JsonReader): Boolean? = reader.readBooleanOrNull()

    override fun writeValue(writer: JsonWriter, instance: Boolean) = writer.writeBoolean(instance)
    fun writeValueOrNull(writer: JsonWriter, instance: Boolean?) = writer.writeBooleanOrNull(instance)

    override val list by lazy { super.list }
}

@JvmName("BooleanKodable")
fun KClass<Boolean>.kodable(): IKodable<Boolean> = BooleanKodable