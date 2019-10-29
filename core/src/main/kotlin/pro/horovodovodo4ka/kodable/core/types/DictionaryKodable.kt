package pro.horovodovodo4ka.kodable.core.types

import io.fluidsonic.json.JsonReader
import io.fluidsonic.json.JsonWriter
import io.fluidsonic.json.readMapByElementValue
import io.fluidsonic.json.writeMapByElementValue
import pro.horovodovodo4ka.kodable.core.IKodable

/**
 * Default implementation for `List<ValueType>`
 */
internal class DictionaryKodable<ValueType>(private val valueKodable: IKodable<ValueType>) : IKodable<Map<String, ValueType>> {

    override fun readValue(reader: JsonReader): Map<String, ValueType> = reader.readMapByElementValue { valueKodable.readValue(reader) }

    override fun writeValue(writer: JsonWriter, instance: Map<String, ValueType>) = writer.writeMapByElementValue(instance) { valueKodable.writeValue(this, it) }
}