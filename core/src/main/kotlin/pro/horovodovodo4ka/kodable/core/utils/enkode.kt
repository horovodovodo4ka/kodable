package pro.horovodovodo4ka.kodable.core.utils

import pro.horovodovodo4ka.kodable.core.IKodable
import pro.horovodovodo4ka.kodable.core.json.JsonWriter
import pro.horovodovodo4ka.kodable.core.json.JsonWriterOption
import pro.horovodovodo4ka.kodable.core.json.invoke
import pro.horovodovodo4ka.kodable.core.writeValueOrNull

fun <T : Any> IKodable<T>.enkode(instance: T?, vararg options: JsonWriterOption): String = JsonWriter(options.toList()) { writeValueOrNull(this, instance) }