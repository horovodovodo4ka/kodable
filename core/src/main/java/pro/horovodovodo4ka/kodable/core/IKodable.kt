package pro.horovodovodo4ka.kodable.core

import com.github.fluidsonic.fluid.json.JSONReader
import com.github.fluidsonic.fluid.json.JSONReader.Companion
import com.github.fluidsonic.fluid.json.JSONToken.nullValue
import com.github.fluidsonic.fluid.json.JSONWriter
import com.github.fluidsonic.fluid.json.isolateValueWrite
import com.github.fluidsonic.fluid.json.readListByElement
import kotlin.text.RegexOption.IGNORE_CASE

interface IKodable<Value> {
    fun readValue(reader: JSONReader): Value = throw Exception("IKodable: readValue() is not implemented in '$this'")
    fun readValueOrNull(reader: JSONReader): Value? = if (reader.nextToken != nullValue) readValue(reader) else reader.readNull()

    fun writeValue(writer: JSONWriter, instance: Value): Unit = throw Exception("IKodable: writeValue() is not implemented in '$this'")
    fun writeValueOrNull(writer: JSONWriter, instance: Value?) = instance?.run { writeValue(writer, instance) } ?: writer.writeNull()

    val list: IKodable<List<Value>> get() = ListKodable(this)
}

class ListKodable<Value>(private val valueKodable: IKodable<Value>) : IKodable<List<Value>> {
    override fun readValue(reader: JSONReader): List<Value> = mutableListOf<Value>().apply { reader.readListByElement { add(valueKodable.readValue(reader)) } }

    override fun writeValue(writer: JSONWriter, instance: List<Value>) {
        writer.isolateValueWrite {
            writeListStart()
            instance.forEach { valueKodable.writeValue(this, it) }
            writeListEnd()
        }
    }
}

//class KodableStrategy(val snakeCaseAsCamelCase: Boolean = false)
//
//class KodableReader(string: String, val strategy: KodableStrategy = KodableStrategy(), private val reader: JSONReader = JSONReader.build(string)) : JSONReader by reader {
//    override fun readMapKey(): String {
//        val key = reader.readMapKey()
//        if (!strategy.snakeCaseAsCamelCase) return key
//        return key.snakeCaseAsCamelCase()
//    }
//}
//
//class KodableWriter(val strategy: KodableStrategy = KodableStrategy(), private val writer: JSONWriter = JSONWriter.build()) : JSONWriter by writer {
//    override fun writeMapKey(value: String) {
//        if (!strategy.snakeCaseAsCamelCase) writer.writeMapKey()
//        super.writeMapKey(value)
//    }
//}
//
//private val regex = Regex("""([_\-]([^_\-]+))""")
//fun String.snakeCaseAsCamelCase() = replace(regex) { it.groupValues[2].capitalize() }