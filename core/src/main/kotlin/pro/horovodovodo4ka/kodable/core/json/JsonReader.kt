package pro.horovodovodo4ka.kodable.core.json

import java.io.InputStream

interface JsonReader {
    fun readBoolean(): Boolean
    fun readNumber(): Number
    fun readString(): String
    fun readNull(): Nothing?

    fun iterateObject(block: JsonReader.(property: String) -> Unit)
    fun iterateArray(block: JsonReader.() -> Unit)

    fun <T> isolateValue(block: () -> T): T

    fun onCharacterRead(char: Char)

    companion object {
        operator fun invoke(input: InputStream): JsonReader = DefaultJsonReader(input)
    }
}
