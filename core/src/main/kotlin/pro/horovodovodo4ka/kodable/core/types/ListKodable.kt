package pro.horovodovodo4ka.kodable.core.types

import pro.horovodovodo4ka.kodable.core.IKodable
import pro.horovodovodo4ka.kodable.core.json.JsonReader
import pro.horovodovodo4ka.kodable.core.json.JsonWriter
import pro.horovodovodo4ka.kodable.core.json.arrayElement

/**
 * Default implementation for `List<ValueType>`
 */
internal class ListKodable<ValueType>(private val valueKodable: IKodable<ValueType>) : IKodable<List<ValueType>> {

    override fun readValue(reader: JsonReader): List<ValueType> {
        val elements = mutableListOf<ValueType>()

        reader.iterateArray { elements += valueKodable.readValue(this) }

        return elements.toList()
    }

    override fun writeValue(writer: JsonWriter, instance: List<ValueType>) {
        val elements = instance
            .asSequence()
            .map {
                arrayElement { valueKodable.writeValue(this, it) }
            }

        writer.iterateArray(elements)
    }
}