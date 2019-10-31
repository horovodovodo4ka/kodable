package pro.horovodovodo4ka.kodable.core.json

interface JsonWriter {
    fun writeBoolean(value: Boolean)
    fun writeNumber(value: Number)
    fun writeString(value: String)
    fun writeNull()

    fun prependObject(properties: Sequence<Pair<String, JsonWriter.() -> Unit>>)
    fun iterateObject(properties: Sequence<Pair<String, JsonWriter.() -> Unit>>)
    fun iterateArray(elements: Sequence<JsonWriter.() -> Unit>)

    companion object
}

fun objectProperty(name: String, block: JsonWriter.() -> Unit) = name to block
fun arrayElement(block: JsonWriter.() -> Unit) = block