package pro.horovodovodo4ka.kodable.core.defaults

import pro.horovodovodo4ka.kodable.core.JSONReader
import pro.horovodovodo4ka.kodable.core.Kodable
import kotlin.reflect.KClass

@JvmName("EnumKodable")
inline fun <reified T : Enum<T>> KClass<T>.kodable(): Kodable<T> {
    return object : Kodable<T> {
        override fun readValue(reader: JSONReader): T {
//            enumValues<T>().map { it.declaringClass. }
            return enumValueOf(reader.readString())
        }
    }
}