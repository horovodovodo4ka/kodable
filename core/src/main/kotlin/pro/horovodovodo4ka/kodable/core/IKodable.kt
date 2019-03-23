package pro.horovodovodo4ka.kodable.core

import com.github.fluidsonic.fluid.json.JSONReader
import com.github.fluidsonic.fluid.json.JSONToken.nullValue
import com.github.fluidsonic.fluid.json.JSONWriter
import pro.horovodovodo4ka.kodable.core.types.DictionaryKodable
import pro.horovodovodo4ka.kodable.core.types.ListKodable

interface IKodable<ValueType> {

    fun readValue(reader: JSONReader): ValueType = throw Exception("IKodable: readValue() is not implemented in '$this'")

    fun writeValue(writer: JSONWriter, instance: ValueType): Unit = throw Exception("IKodable: writeValue() is not implemented in '$this'")


    /**
     * Provides kodable for `List<ValueType>`
     */
    val list: IKodable<List<ValueType>> get() = ListKodable(this)

    /**
     * Provides kodable for `Map<String, ValueType>`
     */
    val dictionary: IKodable<Map<String, ValueType>> get() = DictionaryKodable(this)
}

fun <ValueType> IKodable<ValueType>.readValueOrNull(reader: JSONReader): ValueType? = if (reader.nextToken != nullValue) readValue(reader) else reader.readNull()

fun <ValueType> IKodable<ValueType>.writeValueOrNull(writer: JSONWriter, instance: ValueType?) = instance?.run { writeValue(writer, instance) } ?: writer.writeNull()

