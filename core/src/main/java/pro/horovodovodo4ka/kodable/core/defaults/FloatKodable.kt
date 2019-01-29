package pro.horovodovodo4ka.kodable.core.defaults

import pro.horovodovodo4ka.kodable.core.IKodable
import pro.horovodovodo4ka.kodable.core.KodableReader
import kotlin.reflect.KClass

object FloatKodable : IKodable<Float> {
    override fun readValue(reader: KodableReader): Float = reader.readFloat()
    override fun readValueOrNull(reader: KodableReader): Float? = reader.readFloatOrNull()

    override val list by lazy { super.list }
}

@JvmName("FloatKodable")
fun KClass<Float>.kodable(): IKodable<Float> = FloatKodable