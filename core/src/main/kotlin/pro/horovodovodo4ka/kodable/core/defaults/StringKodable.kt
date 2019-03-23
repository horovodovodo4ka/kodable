package pro.horovodovodo4ka.kodable.core.defaults

import com.github.fluidsonic.fluid.json.JSONReader
import com.github.fluidsonic.fluid.json.JSONWriter
import com.github.fluidsonic.fluid.json.readStringOrNull
import com.github.fluidsonic.fluid.json.writeStringOrNull
import pro.horovodovodo4ka.kodable.core.IKodable
import kotlin.reflect.KClass

object StringKodable : IKodable<String> {
    override fun readValue(reader: JSONReader): String = reader.readString()
    fun readValueOrNull(reader: JSONReader): String? = reader.readStringOrNull()

    override fun writeValue(writer: JSONWriter, instance: String) = writer.writeString(instance)
    fun writeValueOrNull(writer: JSONWriter, instance: String?) = writer.writeStringOrNull(instance)

    override val list by lazy { super.list }
}

@JvmName("StringKodable")
fun KClass<String>.kodable(): IKodable<String> = StringKodable
