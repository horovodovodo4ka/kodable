package pro.horovodovodo4ka.kodable.core.utils

import com.github.fluidsonic.fluid.json.JSONReader
import pro.horovodovodo4ka.kodable.core.IKodable
import pro.horovodovodo4ka.kodable.core.KodablePath

inline fun <reified T> IKodable<T>.dekode(string: String, path: KodablePath? = null): T {
    val reader = JSONReader.build(string)
    path?.go(reader)
    return readValue(reader)
}
