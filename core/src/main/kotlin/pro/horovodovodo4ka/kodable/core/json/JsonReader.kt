package pro.horovodovodo4ka.kodable.core.json

import pro.horovodovodo4ka.kodable.core.json.JsonEntity.`null`
import java.io.Reader

interface JsonReader {
    fun readBoolean(): Boolean
    fun readNumber(): Number
    fun readString(): String
    fun readNull(): Nothing?

    fun iterateObject(block: JsonReader.(property: String) -> Unit)
    fun iterateArray(block: JsonReader.(index: Int) -> Unit)
    fun skipValue()

    fun nextType(): JsonEntity
    fun snapshot(block: JsonReader.() -> Unit): String

    companion object {
        operator fun invoke(input: Reader): JsonReader = DefaultJsonReader(input)
        operator fun invoke(inputString: String): JsonReader = invoke(inputString.reader())
    }
}

fun JsonReader.isNextNull(): Boolean = nextType() == `null`

