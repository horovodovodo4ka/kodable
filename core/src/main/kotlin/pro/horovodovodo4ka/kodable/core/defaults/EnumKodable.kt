package pro.horovodovodo4ka.kodable.core.defaults

import pro.horovodovodo4ka.kodable.core.IKodable
import pro.horovodovodo4ka.kodable.core.json.JsonReader
import pro.horovodovodo4ka.kodable.core.json.JsonWriter
import kotlin.reflect.KClass

@JvmName("EnumKodable")
inline fun <reified T : Enum<T>> KClass<T>.kodable(): IKodable<T> = object : IKodable<T> {
    override fun readValue(reader: JsonReader): T = enumValueOf(reader.readString())
    override fun writeValue(writer: JsonWriter, instance: T) = writer.writeString(instance.name)
}