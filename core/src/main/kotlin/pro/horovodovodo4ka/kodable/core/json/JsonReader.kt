package pro.horovodovodo4ka.kodable.core.json

import pro.horovodovodo4ka.kodable.core.json.JsonEntity.`null`
import java.io.InputStream

interface JsonReader {
    fun readBoolean(): Boolean
    fun readNumber(): Number
    fun readString(): String
    fun readNull(): Nothing?

    fun iterateObject(block: JsonReader.(property: String) -> Unit)
    fun iterateArray(block: JsonReader.() -> Unit)
    fun skipValue()

    fun nextType() : JsonEntity
    fun <T> isolateValue(block: () -> T): T

    companion object {
        operator fun invoke(input: InputStream): JsonReader = DefaultJsonReader(input)
    }
}

fun JsonReader.isNextNull() : Boolean = nextType() == `null`