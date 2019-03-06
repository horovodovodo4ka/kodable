package pro.horovodovodo4ka.kodable.core

import com.github.fluidsonic.fluid.json.JSONReader
import com.github.fluidsonic.fluid.json.JSONToken.nullValue
import com.github.fluidsonic.fluid.json.JSONWriter
import com.github.fluidsonic.fluid.json.readListByElement
import com.github.fluidsonic.fluid.json.readMapByElementValue
import com.github.fluidsonic.fluid.json.writeListByElement
import com.github.fluidsonic.fluid.json.writeMapByElementValue

interface IKodable<Value> {
    fun readValue(reader: JSONReader): Value = throw Exception("IKodable: readValue() is not implemented in '$this'")
    fun readValueOrNull(reader: JSONReader): Value? = if (reader.nextToken != nullValue) readValue(reader) else reader.readNull()

    fun writeValue(writer: JSONWriter, instance: Value): Unit = throw Exception("IKodable: writeValue() is not implemented in '$this'")
    fun writeValueOrNull(writer: JSONWriter, instance: Value?) = instance?.run { writeValue(writer, instance) } ?: writer.writeNull()

    val list: IKodable<List<Value>> get() = ListKodable(this)
    val dictionary: IKodable<Map<String, Value>> get() = DictionaryKodable(this)
}

class ListKodable<Value>(private val valueKodable: IKodable<Value>) : IKodable<List<Value>> {
    override fun readValue(reader: JSONReader): List<Value> = reader.readListByElement { valueKodable.readValue(reader) }
    override fun writeValue(writer: JSONWriter, instance: List<Value>) = writer.writeListByElement(instance) { valueKodable.writeValue(this, it) }
}

class DictionaryKodable<Value>(private val valueKodable: IKodable<Value>) : IKodable<Map<String, Value>> {
    override fun readValue(reader: JSONReader): Map<String, Value> = reader.readMapByElementValue { valueKodable.readValue(reader) }
    override fun writeValue(writer: JSONWriter, instance: Map<String, Value>) = writer.writeMapByElementValue(instance) { valueKodable.writeValue(this, it) }

}