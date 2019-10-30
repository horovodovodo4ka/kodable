package pro.horovodovodo4ka.kodable.core.json

import pro.horovodovodo4ka.kodable.core.json.StructureCharacter.BEGIN_ARRAY
import pro.horovodovodo4ka.kodable.core.json.StructureCharacter.BEGIN_OBJECT
import pro.horovodovodo4ka.kodable.core.json.StructureCharacter.END_ARRAY
import pro.horovodovodo4ka.kodable.core.json.StructureCharacter.END_OBJECT
import pro.horovodovodo4ka.kodable.core.json.StructureCharacter.NAME_SEPARATOR
import pro.horovodovodo4ka.kodable.core.json.StructureCharacter.VALUE_SEPARATOR
import java.io.Writer

internal class DefaultJsonWriter(private val output: Writer) : JsonWriter {
    override fun writeBoolean(value: Boolean) {
        output.append(if (value) "true" else "false")
    }

    override fun writeNumber(value: Number) {
        output.append(value.toString())
    }

    override fun writeString(value: String) {
        output.append('"')
        for (char in value) {
            when(char) {
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
        output.append("null")
    }

    override fun iterateObject(properties: Sequence<Pair<String, JsonWriter.() -> Unit>>) {
        output.append(BEGIN_OBJECT.char)

        val iterator = properties.iterator()

        while (iterator.hasNext()) {
            val item = iterator.next()

            writeString(item.first)

            output.append(NAME_SEPARATOR.char)

            item.second(this)

            if (iterator.hasNext()) output.append(VALUE_SEPARATOR.char)
        }

        output.append(END_OBJECT.char)
    }

    override fun iterateArray(elements: Sequence<JsonWriter.() -> Unit>) {
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