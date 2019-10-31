package pro.horovodovodo4ka.kodable.core.utils

import pro.horovodovodo4ka.kodable.core.IKodable
import pro.horovodovodo4ka.kodable.core.json.JsonReader
import pro.horovodovodo4ka.kodable.core.json.JsonWriter
import pro.horovodovodo4ka.kodable.core.json.invoke
import pro.horovodovodo4ka.kodable.core.types.KodablePath
import pro.horovodovodo4ka.kodable.core.writeValueOrNull

fun <T : Any> IKodable<T>.dekode(string: String, path: KodablePath? = null): T = with(JsonReader(string)) {
    path?.go(this)
    readValue(this)
}

fun <T : Any> IKodable<T>.enkode(instance: T?): String = JsonWriter { writeValueOrNull(this, instance) }

