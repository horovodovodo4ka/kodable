package pro.horovodovodo4ka.kodable.core.json

import pro.horovodovodo4ka.kodable.core.json.JsonEntity.`null`
import java.io.Reader

interface JsonReader {
    fun readBoolean(): Boolean
    fun readNumber(): Number
    fun readString(): String
    fun readNull(): Nothing?

    fun skipValue()

    fun iterateObjectWithPrefetch(prefetch: JsonReader.(property: String) -> Unit) : JsonReader
    fun iterateObject(block: JsonReader.(property: String) -> Unit)

    fun iterateArray(block: JsonReader.(index: Int) -> Unit)

    fun nextType(): JsonEntity

    companion object
}

fun JsonReader.isNextNull(): Boolean = nextType() == `null`

