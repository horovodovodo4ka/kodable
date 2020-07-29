package pro.horovodovodo4ka.kodable.core.json

import pro.horovodovodo4ka.kodable.core.json.JsonWriterOption.CustomPredicate
import pro.horovodovodo4ka.kodable.core.json.JsonWriterOption.SkipObjectNullProperties
import pro.horovodovodo4ka.kodable.core.json.StructureCharacter.BEGIN_ARRAY
import pro.horovodovodo4ka.kodable.core.json.StructureCharacter.BEGIN_OBJECT
import pro.horovodovodo4ka.kodable.core.json.StructureCharacter.END_ARRAY
import pro.horovodovodo4ka.kodable.core.json.StructureCharacter.END_OBJECT
import pro.horovodovodo4ka.kodable.core.json.StructureCharacter.NAME_SEPARATOR
import pro.horovodovodo4ka.kodable.core.json.StructureCharacter.VALUE_SEPARATOR
import java.io.StringWriter
import java.io.Writer

operator fun JsonWriter.Companion.invoke(output: Writer, options: List<JsonWriterOption> = emptyList()): JsonWriter = DefaultJsonWriter(output, options)
operator fun JsonWriter.Companion.invoke(options: List<JsonWriterOption> = emptyList(), block: JsonWriter.() -> Unit): String = StringWriter()
    .run {
        use {
            block(JsonWriter(it, options))
        }
        toString()
    }

private class DefaultJsonWriter(private val output: Writer, override val options: List<JsonWriterOption>) : JsonWriter {

    override fun writeBoolean(value: Boolean) {
        prependCache = null

        output.append(if (value) "true" else "false")
    }

    override fun writeNumber(value: Number) {
        prependCache = null

        output.append(value.toString())
    }

    override fun writeString(value: String) {
        prependCache = null

        output.append('"')
        for (char in value) {
            when (char) {
                '"', '\\' -> output.append('\\').append(char)
                '\t' -> output.append('\\').append('t')
                '\r' -> output.append('\\').append('r')
                '\n' -> output.append('\\').append('n')
                '\b' -> output.append('\\').append('b')
                '\u000C' -> output.append('\\').append('f')
                in controls -> {
                    output
                        .append('\\')
                        .append('u')
                        .append('0')
                        .append('0')

                    val int = char.toInt()

                    if (int >= 0x10) {
                        output.append('1')
                    } else {
                        output.append('0')
                    }
                    output.append(hexDigits[int and 0xF])
                }
                else -> output.append(char)
            }
        }
        output.append('"')
    }

    override fun writeNull() {
        prependCache = null

        output.append("null")
    }

    private var prependCache: Sequence<ObjectPropertyWithPredicate>? = null
    override fun prependObject(propertiesWithPredicates: Sequence<ObjectPropertyWithPredicate>) {
        prependCache = (prependCache ?: emptySequence()) + propertiesWithPredicates
    }

    override fun iterateObject(propertiesWithPredicates: Sequence<ObjectPropertyWithPredicate>) {
        output.append(BEGIN_OBJECT.char)

        val allProps = ((prependCache ?: emptySequence()) + propertiesWithPredicates).toMutableList()

        options.filterIsInstance<CustomPredicate>().forEach { option ->
            allProps.removeAll { !option.predicate(it.second) }
        }

        val iterator = allProps.iterator()

        while (iterator.hasNext()) {
            val item = iterator.next()

            writeString(item.first)

            output.append(NAME_SEPARATOR.char)

            item.third(this)

            if (iterator.hasNext()) output.append(VALUE_SEPARATOR.char)
        }

        output.append(END_OBJECT.char)
    }

    override fun iterateArray(elements: Sequence<JsonWriter.() -> Unit>) {
        prependCache = null

        output.append(BEGIN_ARRAY.char)

        val iterator = elements.iterator()

        while (iterator.hasNext()) {
            val item = iterator.next()

            item(this)

            if (iterator.hasNext()) output.append(VALUE_SEPARATOR.char)
        }

        output.append(END_ARRAY.char)
    }
}