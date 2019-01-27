package pro.horovodovodo4ka.kodable.core.defaults

import pro.horovodovodo4ka.kodable.core.JSONReader
import pro.horovodovodo4ka.kodable.core.IKodable
import kotlin.reflect.KClass

object FloatKodable : IKodable<Float> {
    override fun readValue(reader: JSONReader): Float = reader.readFloat()
    override fun readValueOrNull(reader: JSONReader): Float? = reader.readFloatOrNull()

    override val list by lazy { super.list }
}

@JvmName("FloatKodable")
fun KClass<Float>.kodable(): IKodable<Float> = FloatKodable