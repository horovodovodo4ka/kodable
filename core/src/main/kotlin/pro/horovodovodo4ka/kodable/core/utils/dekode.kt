package pro.horovodovodo4ka.kodable.core.utils

import com.github.fluidsonic.fluid.json.JSONReader
import com.github.fluidsonic.fluid.json.JSONWriter
import pro.horovodovodo4ka.kodable.core.IKodable
import pro.horovodovodo4ka.kodable.core.types.KodablePath
import pro.horovodovodo4ka.kodable.core.writeValueOrNull
import java.io.StringWriter

fun <T> IKodable<T>.dekode(string: String, path: KodablePath? = null): T = with(JSONReader.build(string)) {
    use {
        path?.go(it)
        readValue(it)
    }
}

fun <T> IKodable<T>.enkode(instance: T?): String = with(StringWriter()) {
    use {
        JSONWriter.build(it).use { writer -> writeValueOrNull(writer, instance) }
    }
    toString()
}

