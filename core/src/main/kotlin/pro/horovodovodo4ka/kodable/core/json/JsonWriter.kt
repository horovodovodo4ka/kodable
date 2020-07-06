package pro.horovodovodo4ka.kodable.core.json

typealias ObjectPropertyWithPredicate = Triple<String, Any?, JsonWriter.() -> Unit>
typealias ObjectProperty = Pair<String, Any?>

interface JsonWriter {
    fun writeBoolean(value: Boolean)
    fun writeNumber(value: Number)
    fun writeString(value: String)
    fun writeNull()

    fun prependObject(propertiesWithPredicates: Sequence<ObjectPropertyWithPredicate>)

    fun iterateObject(propertiesWithPredicates: Sequence<ObjectPropertyWithPredicate>)
    fun iterateArray(elements: Sequence<JsonWriter.() -> Unit>)

    val options: List<JsonWriterOption>

    companion object
}

fun objectProperty(name: String, value: Any?, block: JsonWriter.() -> Unit) = Triple(name, value, block)
fun arrayElement(block: JsonWriter.() -> Unit) = block

sealed class JsonWriterOption {
    open class CustomPredicate(val predicate: (Any?) -> Boolean) : JsonWriterOption()
    object SkipObjectNullProperties : CustomPredicate(predicate = { it != null })
}