package pro.horovodovodo4ka.kodable.core

import pro.horovodovodo4ka.kodable.core.types.JSONToken.nullValue

interface IKodable<Value> {
    fun readValue(reader: KodableReader): Value = throw Exception("IKodable: readValue() is not implemented in $this")
    fun readValueOrNull(reader: KodableReader): Value? = if (reader.nextToken != nullValue) readValue(reader) else reader.readNull()

    val list: IKodable<List<Value>> get() = ListKodable(this)
}

class ListKodable<Value>(private val valueKodable: IKodable<Value>) : IKodable<List<Value>> {
    override fun readValue(reader: KodableReader): List<Value> = mutableListOf<Value>().apply { reader.readElementsFromList { add(valueKodable.readValue(reader)) } }
}

