package pro.horovodovodo4ka.kodable.core

import com.github.fluidsonic.fluid.json.JSONReader
import com.github.fluidsonic.fluid.json.JSONToken.nullValue
import com.github.fluidsonic.fluid.json.JSONWriter
import com.github.fluidsonic.fluid.json.isolateValueWrite
import com.github.fluidsonic.fluid.json.readListByElement
import com.github.fluidsonic.fluid.json.writeListByElement
import pro.horovodovodo4ka.kodable.core.KodableStrategy.camelCaseKeys
import java.io.StringWriter

interface IKodable<Value> {
    fun readValue(reader: JSONReader): Value = throw Exception("IKodable: readValue() is not implemented in '$this'")
    fun readValueOrNull(reader: JSONReader): Value? = if (reader.nextToken != nullValue) readValue(reader) else reader.readNull()

    fun writeValue(writer: JSONWriter, instance: Value): Unit = throw Exception("IKodable: writeValue() is not implemented in '$this'")
    fun writeValueOrNull(writer: JSONWriter, instance: Value?) = instance?.run { writeValue(writer, instance) } ?: writer.writeNull()

    val list: IKodable<List<Value>> get() = ListKodable(this)
}

class ListKodable<Value>(private val valueKodable: IKodable<Value>) : IKodable<List<Value>> {
    override fun readValue(reader: JSONReader): List<Value> = mutableListOf<Value>().apply { reader.readListByElement { add(valueKodable.readValue(reader)) } }
    override fun writeValue(writer: JSONWriter, instance: List<Value>) = writer.writeListByElement(instance) { valueKodable.writeValue(this, it) }
}

enum class KodableStrategy{
    camelCaseKeys
}

class KodableReader(string: String, vararg val strategy: KodableStrategy = emptyArray(), private val reader: JSONReader = JSONReader.build(string)) : JSONReader by reader {
    override fun readMapKey(): String {
        val key = reader.readMapKey()
        if (!strategy.contains(camelCaseKeys)) return key
        return key.toCamelCase()
    }
}

class KodableWriter(stringWriter: StringWriter, vararg val strategy: KodableStrategy = emptyArray(), private val writer: JSONWriter = JSONWriter.build(stringWriter)) : JSONWriter by writer {
    override fun writeMapKey(value: String) {
        if (!strategy.contains(camelCaseKeys)) writer.writeMapKey(value)
        writer.writeMapKey(value.toSnakeCase())
    }
}

operator fun JSONReader.Companion.invoke(string: String, vararg strategy: KodableStrategy = emptyArray()) = KodableReader(string, *strategy)
operator fun JSONWriter.Companion.invoke(stringWriter: StringWriter, vararg strategy: KodableStrategy = emptyArray()) = KodableWriter(stringWriter, *strategy)

private val camel = Regex("""(([A-Z])([a-z]+))""")
fun String.toSnakeCase() = replace(camel) {
    val first = "_" + it.groupValues[2].toLowerCase()
    val others = it.groupValues[3]
    first + others
}

private val snake = Regex("""([_\-]([^_\-]+))""")
fun String.toCamelCase() = replace(snake) { it.groupValues[2].capitalize() }