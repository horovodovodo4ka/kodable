package pro.horovodovodo4ka.kodable.core.utils

import io.fluidsonic.json.JsonReader
import io.fluidsonic.json.JsonWriter
import pro.horovodovodo4ka.kodable.core.IKodable
import pro.horovodovodo4ka.kodable.core.types.KodablePath
import pro.horovodovodo4ka.kodable.core.writeValueOrNull
import java.io.StringWriter

fun <T> IKodable<T>.dekode(string: String, path: KodablePath? = null): T = with(JsonReader.build(string)) {
    use {
        path?.go(it)
        readValue(it)
    }
}

fun <T> IKodable<T>.enkode(instance: T?): String = with(StringWriter()) {
    use {
        JsonWriter.build(it).use { writer -> writeValueOrNull(writer, instance) }
    }
    toString()
}

