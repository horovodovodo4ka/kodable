package pro.horovodovodo4ka.kodable.core.types

import com.github.fluidsonic.fluid.json.JSONReader
import com.github.fluidsonic.fluid.json.JSONWriter
import com.github.fluidsonic.fluid.json.readListByElement
import com.github.fluidsonic.fluid.json.writeListByElement
import pro.horovodovodo4ka.kodable.core.IKodable

/**
 * Default implementation for `List<ValueType>`
 */
internal class ListKodable<ValueType>(private val valueKodable: IKodable<ValueType>) : IKodable<List<ValueType>> {

    override fun readValue(reader: JSONReader): List<ValueType> = reader.readListByElement { valueKodable.readValue(reader) }

    override fun writeValue(writer: JSONWriter, instance: List<ValueType>) = writer.writeListByElement(instance) { valueKodable.writeValue(this, it) }
}