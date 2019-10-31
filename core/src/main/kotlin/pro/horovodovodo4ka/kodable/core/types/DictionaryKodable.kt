package pro.horovodovodo4ka.kodable.core.types

import pro.horovodovodo4ka.kodable.core.IKodable
import pro.horovodovodo4ka.kodable.core.json.JsonReader
import pro.horovodovodo4ka.kodable.core.json.JsonWriter
import pro.horovodovodo4ka.kodable.core.json.objectProperty

/**
 * Default implementation for `List<ValueType>`
 */
internal class DictionaryKodable<ValueType>(private val valueKodable: IKodable<ValueType>) : IKodable<Map<String, ValueType>> {

    override fun readValue(reader: JsonReader): Map<String, ValueType> {
        val result = mutableMapOf<String, ValueType>()

        reader.iterateObject { result[it] = valueKodable.readValue(reader) }

        return result.toMap()
    }

    override fun writeValue(writer: JsonWriter, instance: Map<String, ValueType>) {
        val elements = instance
            .asSequence()
            .map {
                objectProperty(it.key) { valueKodable.writeValue(this, it.value) }
            }

        writer.iterateObject(elements)
    }
}