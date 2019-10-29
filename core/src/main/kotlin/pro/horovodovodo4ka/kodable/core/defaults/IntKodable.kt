package pro.horovodovodo4ka.kodable.core.defaults

import io.fluidsonic.json.JsonReader
import io.fluidsonic.json.JsonWriter
import io.fluidsonic.json.readIntOrNull
import io.fluidsonic.json.writeIntOrNull
import pro.horovodovodo4ka.kodable.core.IKodable
import kotlin.reflect.KClass

object IntKodable : IKodable<Int> {
    override fun readValue(reader: JsonReader): Int = reader.readInt()
    fun readValueOrNull(reader: JsonReader): Int? = reader.readIntOrNull()

    override fun writeValue(writer: JsonWriter, instance: Int) = writer.writeInt(instance)
    fun writeValueOrNull(writer: JsonWriter, instance: Int?) = writer.writeIntOrNull(instance)

    override val list by lazy { super.list }
}

@JvmName("IntKodable")
fun KClass<Int>.kodable(): IKodable<Int> = IntKodable
