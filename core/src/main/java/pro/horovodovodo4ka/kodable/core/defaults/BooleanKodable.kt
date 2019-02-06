package pro.horovodovodo4ka.kodable.core.defaults

import com.github.fluidsonic.fluid.json.JSONReader
import com.github.fluidsonic.fluid.json.JSONWriter
import com.github.fluidsonic.fluid.json.readBooleanOrNull
import com.github.fluidsonic.fluid.json.writeBooleanOrNull
import pro.horovodovodo4ka.kodable.core.IKodable
import kotlin.reflect.KClass

object BooleanKodable : IKodable<Boolean> {
    override fun readValue(reader: JSONReader): Boolean = reader.readBoolean()
    override fun readValueOrNull(reader: JSONReader): Boolean? = reader.readBooleanOrNull()

    override fun writeValue(writer: JSONWriter, instance: Boolean) = writer.writeBoolean(instance)
    override fun writeValueOrNull(writer: JSONWriter, instance: Boolean?) = writer.writeBooleanOrNull(instance)

    override val list by lazy { super.list }
}

@JvmName("BooleanKodable")
fun KClass<Boolean>.kodable(): IKodable<Boolean> = BooleanKodable