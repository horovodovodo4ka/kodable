package pro.horovodovodo4ka.kodable.core.types

import io.fluidsonic.json.JsonReader
import io.fluidsonic.json.JsonWriter
import io.fluidsonic.json.readListByElement
import io.fluidsonic.json.writeListByElement
import pro.horovodovodo4ka.kodable.core.IKodable

/**
 * Default implementation for `List<ValueType>`
 */
internal class ListKodable<ValueType>(private val valueKodable: IKodable<ValueType>) : IKodable<List<ValueType>> {

    override fun readValue(reader: JsonReader): List<ValueType> = reader.readListByElement { valueKodable.readValue(reader) }

    override fun writeValue(writer: JsonWriter, instance: List<ValueType>) = writer.writeListByElement(instance) { valueKodable.writeValue(this, it) }
}