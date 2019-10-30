package pro.horovodovodo4ka.kodable.core


import pro.horovodovodo4ka.kodable.core.json.JsonReader
import pro.horovodovodo4ka.kodable.core.json.JsonWriter
import pro.horovodovodo4ka.kodable.core.json.isNextNull
import pro.horovodovodo4ka.kodable.core.types.DictionaryKodable
import pro.horovodovodo4ka.kodable.core.types.ListKodable

interface IKodable<ValueType> {

    fun readValue(reader: JsonReader): ValueType = throw Exception("IKodable: readValue() is not implemented in '$this'")

    fun writeValue(writer: JsonWriter, instance: ValueType): Unit = throw Exception("IKodable: writeValue() is not implemented in '$this'")

    /**
     * Provides kodable for `List<ValueType>`
     */
    val list: IKodable<List<ValueType>> get() = ListKodable(this)

    /**
     * Provides kodable for `Map<String, ValueType>`
     */
    val dictionary: IKodable<Map<String, ValueType>> get() = DictionaryKodable(this)
}

fun <ValueType> IKodable<ValueType>.readValueOrNull(reader: JsonReader): ValueType? = if (!reader.isNextNull()) readValue(reader) else reader.readNull()

fun <ValueType> IKodable<ValueType>.writeValueOrNull(writer: JsonWriter, instance: ValueType?) = instance?.run { writeValue(writer, instance) } ?: writer.writeNull()

