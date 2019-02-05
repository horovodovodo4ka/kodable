package pro.horovodovodo4ka.kodable.core.utils

import com.github.fluidsonic.fluid.json.JSONReader
import com.github.fluidsonic.fluid.json.JSONWriter
import pro.horovodovodo4ka.kodable.core.IKodable
import pro.horovodovodo4ka.kodable.core.KodablePath
import pro.horovodovodo4ka.kodable.core.invoke
import java.io.StringWriter

inline fun <reified T> IKodable<T>.dekode(string: String, path: KodablePath? = null): T {
    val reader = JSONReader(string)
    path?.go(reader)
    return readValue(reader)
}

inline fun <reified T> IKodable<T>.enkode(instance: T?) = StringWriter()
    .run {
        use {
            JSONWriter(this).use { writer ->
                writeValueOrNull(writer, instance)
            }
        }
        toString()
    }
