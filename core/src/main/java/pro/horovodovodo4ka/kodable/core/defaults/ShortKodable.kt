package pro.horovodovodo4ka.kodable.core.defaults

import com.github.fluidsonic.fluid.json.JSONReader
import com.github.fluidsonic.fluid.json.readShortOrNull
import pro.horovodovodo4ka.kodable.core.IKodable
import kotlin.reflect.KClass

object ShortKodable : IKodable<Short> {
    override fun readValue(reader: JSONReader): Short = reader.readShort()
    override fun readValueOrNull(reader: JSONReader): Short? = reader.readShortOrNull()

    override val list by lazy { super.list }
}

@JvmName("ShortKodable")
fun KClass<Short>.kodable(): IKodable<Short> = ShortKodable