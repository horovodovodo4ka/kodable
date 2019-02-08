package pro.horovodovodo4ka.kodable.core.utils

import com.github.fluidsonic.fluid.json.JSONReader
import com.github.fluidsonic.fluid.json.JSONWriter
import pro.horovodovodo4ka.kodable.core.IKodable
import pro.horovodovodo4ka.kodable.core.KodablePath
import java.io.StringWriter

fun <T> IKodable<T>.dekode(string: String, path: KodablePath? = null): T {
    val reader = JSONReader.build(string)
    path?.go(reader)
    return readValue(reader)
}

fun <T> IKodable<T>.enkode(instance: T?) = StringWriter().run {
    use {
        JSONWriter.build(it).use { writer ->
            writeValueOrNull(writer, instance)
        }
    }
    toString()
}
