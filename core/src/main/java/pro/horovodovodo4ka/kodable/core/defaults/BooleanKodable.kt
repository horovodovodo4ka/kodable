package pro.horovodovodo4ka.kodable.core.defaults

import com.github.fluidsonic.fluid.json.JSONReader
import com.github.fluidsonic.fluid.json.readBooleanOrNull
import pro.horovodovodo4ka.kodable.core.IKodable
import kotlin.reflect.KClass

object BooleanKodable : IKodable<Boolean> {
    override fun readValue(reader: JSONReader): Boolean = reader.readBoolean()
    override fun readValueOrNull(reader: JSONReader): Boolean? = reader.readBooleanOrNull()

    override val list by lazy { super.list }
}

@JvmName("BooleanKodable")
fun KClass<Boolean>.kodable(): IKodable<Boolean> = BooleanKodable