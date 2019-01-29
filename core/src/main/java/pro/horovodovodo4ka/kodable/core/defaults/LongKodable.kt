package pro.horovodovodo4ka.kodable.core.defaults

import pro.horovodovodo4ka.kodable.core.IKodable
import pro.horovodovodo4ka.kodable.core.KodableReader
import kotlin.reflect.KClass

object LongKodable : IKodable<Long> {
    override fun readValue(reader: KodableReader): Long = reader.readLong()
    override fun readValueOrNull(reader: KodableReader): Long? = reader.readLongOrNull()

    override val list by lazy { super.list }
}

@JvmName("LongKodable")
fun KClass<Long>.kodable(): IKodable<Long> = LongKodable