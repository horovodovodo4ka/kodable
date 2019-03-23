package pro.horovodovodo4ka.kodable.core.types

import com.github.fluidsonic.fluid.json.JSONReader
import com.github.fluidsonic.fluid.json.JSONWriter
import com.github.fluidsonic.fluid.json.readMapByElementValue
import com.github.fluidsonic.fluid.json.writeMapByElementValue
import pro.horovodovodo4ka.kodable.core.IKodable

/**
 * Default implementation for `List<ValueType>`
 */
internal class DictionaryKodable<ValueType>(private val valueKodable: IKodable<ValueType>) : IKodable<Map<String, ValueType>> {

    override fun readValue(reader: JSONReader): Map<String, ValueType> = reader.readMapByElementValue { valueKodable.readValue(reader) }

    override fun writeValue(writer: JSONWriter, instance: Map<String, ValueType>) = writer.writeMapByElementValue(instance) { valueKodable.writeValue(this, it) }
}