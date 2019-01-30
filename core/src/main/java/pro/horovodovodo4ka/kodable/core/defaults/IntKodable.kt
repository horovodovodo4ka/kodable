package pro.horovodovodo4ka.kodable.core.defaults

import com.github.fluidsonic.fluid.json.JSONReader
import com.github.fluidsonic.fluid.json.readIntOrNull
import pro.horovodovodo4ka.kodable.core.IKodable
import kotlin.reflect.KClass

object IntKodable : IKodable<Int> {
    override fun readValue(reader: JSONReader): Int = reader.readInt()
    override fun readValueOrNull(reader: JSONReader): Int? = reader.readIntOrNull()

    override val list by lazy { super.list }
}

@JvmName("IntKodable")
fun KClass<Int>.kodable(): IKodable<Int> = IntKodable
