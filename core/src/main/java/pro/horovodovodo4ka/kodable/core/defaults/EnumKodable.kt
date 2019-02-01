package pro.horovodovodo4ka.kodable.core.defaults

import com.github.fluidsonic.fluid.json.JSONReader
import com.github.fluidsonic.fluid.json.JSONWriter
import pro.horovodovodo4ka.kodable.core.IKodable
import kotlin.reflect.KClass

@JvmName("EnumKodable")
inline fun <reified T : Enum<T>> KClass<T>.kodable(): IKodable<T> = object : IKodable<T> {
    override fun readValue(reader: JSONReader): T = enumValueOf(reader.readString())
    override fun writeValue(writer: JSONWriter, instance: T) = writer.writeString(instance.name)
}