package pro.horovodovodo4ka.kodable.core.defaults

import com.github.fluidsonic.fluid.json.JSONReader
import com.github.fluidsonic.fluid.json.JSONWriter
import com.github.fluidsonic.fluid.json.readIntOrNull
import com.github.fluidsonic.fluid.json.writeIntOrNull
import pro.horovodovodo4ka.kodable.core.IKodable
import kotlin.reflect.KClass

object IntKodable : IKodable<Int> {
    override fun readValue(reader: JSONReader): Int = reader.readInt()
    fun readValueOrNull(reader: JSONReader): Int? = reader.readIntOrNull()

    override fun writeValue(writer: JSONWriter, instance: Int) = writer.writeInt(instance)
    fun writeValueOrNull(writer: JSONWriter, instance: Int?) = writer.writeIntOrNull(instance)

    override val list by lazy { super.list }
}

@JvmName("IntKodable")
fun KClass<Int>.kodable(): IKodable<Int> = IntKodable
