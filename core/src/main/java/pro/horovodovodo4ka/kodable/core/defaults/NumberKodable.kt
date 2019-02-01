package pro.horovodovodo4ka.kodable.core.defaults

import com.github.fluidsonic.fluid.json.JSONReader
import com.github.fluidsonic.fluid.json.JSONWriter
import com.github.fluidsonic.fluid.json.readNumberOrNull
import com.github.fluidsonic.fluid.json.writeNumberOrNull
import pro.horovodovodo4ka.kodable.core.IKodable
import kotlin.reflect.KClass

object NumberKodable : IKodable<Number> {
    override fun readValue(reader: JSONReader): Number = reader.readNumber()
    override fun readValueOrNull(reader: JSONReader): Number? = reader.readNumberOrNull()

    override fun writeValue(writer: JSONWriter, instance: Number) = writer.writeNumber(instance)
    override fun writeValueOrNull(writer: JSONWriter, instance: Number?) = writer.writeNumberOrNull(instance)

    override val list by lazy { super.list }
}

@JvmName("NumberKodable")
fun KClass<Number>.kodable(): IKodable<Number> = NumberKodable