package pro.horovodovodo4ka.kodable.core.defaults

import pro.horovodovodo4ka.kodable.core.JSONReader
import pro.horovodovodo4ka.kodable.core.IKodable
import kotlin.reflect.KClass

@JvmName("EnumKodable")
inline fun <reified T : Enum<T>> KClass<T>.kodable(): IKodable<T> {
    return object : IKodable<T> {
        override fun readValue(reader: JSONReader): T {
//            enumValues<T>().map { it.declaringClass. }
            return enumValueOf(reader.readString())
        }
    }
}