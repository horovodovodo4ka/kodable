package pro.horovodovodo4ka.kodable.core

interface Kodable<Value> {
    fun readValue(reader: JSONReader): Value
    fun readValueOrNull(reader: JSONReader): Value? = if (reader.nextToken != null) readValue(reader) else reader.readNull()

    val list: Kodable<List<Value>> get() = ListKodable(this)
}

class ListKodable<Value>(private val valueKodable: Kodable<Value>) : Kodable<List<Value>> {
    override fun readValue(reader: JSONReader): List<Value> = mutableListOf<Value>().apply { reader.readElementsFromList { add(valueKodable.readValue(reader)) } }
}

