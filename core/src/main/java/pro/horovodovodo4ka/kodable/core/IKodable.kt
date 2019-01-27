package pro.horovodovodo4ka.kodable.core

interface IKodable<Value> {
    fun readValue(reader: JSONReader): Value = throw Exception("IKodable: readValue() is not implemented in $this")
    fun readValueOrNull(reader: JSONReader): Value? = if (reader.nextToken != null) readValue(reader) else reader.readNull()

    val list: IKodable<List<Value>> get() = ListKodable(this)
}

class ListKodable<Value>(private val valueKodable: IKodable<Value>) : IKodable<List<Value>> {
    override fun readValue(reader: JSONReader): List<Value> = mutableListOf<Value>().apply { reader.readElementsFromList { add(valueKodable.readValue(reader)) } }
}

