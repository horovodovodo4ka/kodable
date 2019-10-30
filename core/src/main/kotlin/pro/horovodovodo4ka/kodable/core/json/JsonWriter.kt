package pro.horovodovodo4ka.kodable.core.json

import java.io.StringWriter
import java.io.Writer

interface JsonWriter {
    fun writeBoolean(value: Boolean)
    fun writeNumber(value: Number)
    fun writeString(value: String)
    fun writeNull()

    fun iterateObject(properties: Sequence<Pair<String, JsonWriter.() -> Unit>>)
    fun iterateArray(elements: Sequence<JsonWriter.() -> Unit>)

    companion object {
        operator fun invoke(output: Writer): JsonWriter = DefaultJsonWriter(output)
        operator fun invoke(block: JsonWriter.() -> Unit): String = StringWriter()
            .run {
                use {
                    block(JsonWriter(it))
                }
                toString()
            }
    }
}

fun objectProperty(name: String, block: JsonWriter.() -> Unit) = name to block
fun arrayElement(block: JsonWriter.() -> Unit) = block