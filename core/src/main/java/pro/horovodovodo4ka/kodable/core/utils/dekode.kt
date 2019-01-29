package pro.horovodovodo4ka.kodable.core.utils

import pro.horovodovodo4ka.kodable.core.IKodable
import pro.horovodovodo4ka.kodable.core.KodablePath
import pro.horovodovodo4ka.kodable.core.KodableReader

inline fun <reified T> IKodable<T>.dekode(string: String, path: KodablePath? = null): T {
    val reader = KodableReader.build(string)
    path?.also { println(it) }?.go(reader)
    return readValue(reader)
}