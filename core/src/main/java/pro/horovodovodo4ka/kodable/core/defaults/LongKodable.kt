package pro.horovodovodo4ka.kodable.core.defaults

import com.github.fluidsonic.fluid.json.JSONReader
import com.github.fluidsonic.fluid.json.readLongOrNull
import pro.horovodovodo4ka.kodable.core.IKodable
import kotlin.reflect.KClass

object LongKodable : IKodable<Long> {
    override fun readValue(reader: JSONReader): Long = reader.readLong()
    override fun readValueOrNull(reader: JSONReader): Long? = reader.readLongOrNull()

    override val list by lazy { super.list }
}

@JvmName("LongKodable")
fun KClass<Long>.kodable(): IKodable<Long> = LongKodable