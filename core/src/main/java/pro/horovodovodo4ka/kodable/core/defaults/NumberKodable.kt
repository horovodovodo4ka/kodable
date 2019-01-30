package pro.horovodovodo4ka.kodable.core.defaults

import com.github.fluidsonic.fluid.json.JSONReader
import com.github.fluidsonic.fluid.json.readNumberOrNull
import pro.horovodovodo4ka.kodable.core.IKodable
import kotlin.reflect.KClass

object NumberKodable : IKodable<Number> {
    override fun readValue(reader: JSONReader): Number = reader.readNumber()
    override fun readValueOrNull(reader: JSONReader): Number? = reader.readNumberOrNull()

    override val list by lazy { super.list }
}

@JvmName("NumberKodable")
fun KClass<Number>.kodable(): IKodable<Number> = NumberKodable