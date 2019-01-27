package pro.horovodovodo4ka.kodable.core.defaults

import pro.horovodovodo4ka.kodable.core.JSONReader
import pro.horovodovodo4ka.kodable.core.IKodable
import kotlin.reflect.KClass

object LongKodable : IKodable<Long> {
    override fun readValue(reader: JSONReader): Long = reader.readLong()
    override fun readValueOrNull(reader: JSONReader): Long? = reader.readLongOrNull()

    override val list by lazy { super.list }
}

@JvmName("LongKodable")
fun KClass<Long>.kodable(): IKodable<Long> = LongKodable