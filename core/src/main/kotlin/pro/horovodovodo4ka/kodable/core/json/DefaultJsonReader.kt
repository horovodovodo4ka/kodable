package pro.horovodovodo4ka.kodable.core.json

import pro.horovodovodo4ka.kodable.core.json.JsonEntity.`null`
import pro.horovodovodo4ka.kodable.core.json.JsonEntity.`object`
import pro.horovodovodo4ka.kodable.core.json.JsonEntity.array
import pro.horovodovodo4ka.kodable.core.json.JsonEntity.boolean
import pro.horovodovodo4ka.kodable.core.json.JsonEntity.eof
import pro.horovodovodo4ka.kodable.core.json.JsonEntity.number
import pro.horovodovodo4ka.kodable.core.json.JsonEntity.string
import pro.horovodovodo4ka.kodable.core.json.JsonEntity.undefined
import pro.horovodovodo4ka.kodable.core.json.StructureCharacter.BEGIN_ARRAY
import pro.horovodovodo4ka.kodable.core.json.StructureCharacter.BEGIN_OBJECT
import pro.horovodovodo4ka.kodable.core.json.StructureCharacter.END_ARRAY
import pro.horovodovodo4ka.kodable.core.json.StructureCharacter.END_OBJECT
import pro.horovodovodo4ka.kodable.core.json.StructureCharacter.NAME_SEPARATOR
import pro.horovodovodo4ka.kodable.core.json.StructureCharacter.VALUE_SEPARATOR
import java.io.InputStream

open class DefaultJsonReader(private val input: InputStream) : JsonReader {

    companion object {
        const val chunkSize = 1024
    }

    private lateinit var buffer: String
    private var cursor: Int = -1
    private var chunkCursor: Int = 0

    private fun readFromStream() {
        buffer = input.readNBytes(chunkSize).toString(charset = Charsets.UTF_8)
        chunkCursor += buffer.length
    }

    private var currentChar: Char? = null

    private var isOutside = true

    private val relativeCursor: Int get() = cursor - chunkCursor + buffer.length

    private fun readNextDirty(): Char? {
        cursor++
        if (!::buffer.isInitialized) readFromStream()

        if (relativeCursor > buffer.length) {
            readFromStream()
        }

        val currentChar = buffer.getOrNull(relativeCursor)
        this.currentChar = currentChar
        return currentChar
    }

    private fun readNext(): Char? {
        while (true) {
            val char = readNextDirty()
            if (isOutside && char != null && char in WhiteSpaces) continue
            return char
        }
    }

    private fun skipWitespaces() {
        while (true) {
            val char = peek()
            if (char == null || char !in WhiteSpaces) break
            readNextDirty()
        }
    }

    private fun readNextStrict(): Char {
        return readNext() ?: throw Exception("Unexpected EOF @ $cursor")
    }

    private fun peek(): Char? {
        if (!::buffer.isInitialized) readNext()
        return currentChar
    }

    private fun peekStrict(): Char {
        return peek() ?: throw Exception("Unexpected EOF @ $cursor")
    }

    private fun peekExpecting(vararg expected: Char): Char {
        val next = peekStrict()
        if (next !in expected) throw Exception("Unexpected character @ $cursor: '$next' is not one of: '${expected.joinToString("', '")}'")
        return next
    }

    private fun peekExpectingSoft(vararg expected: Char, block: (Char) -> Unit): Char? {
        val char = peek()
        if (char != null && char in expected) block(char)
        return char
    }

    private fun readExpecting(vararg expected: Char): Char {
        readNextStrict()
        return peekExpecting(*expected)
    }

    private fun readExpectingSoft(vararg expected: Char, block: (Char) -> Unit): Char? {
        readNext()
        return peekExpectingSoft(*expected, block = block)
    }

    private fun validateValueEnd() {
        val nextToken = currentChar
        if (nextToken != null && nextToken !in valueEndScope) throw Exception("Value end expected @ $cursor")
    }

    override fun <T> isolateValue(block: () -> T): T {
        isOutside = false
        return block().also {
            isOutside = true
            skipWitespaces()
        }
    }

    override fun readBoolean(): Boolean = isolateValue {
        val found: Boolean
        when (peekExpecting('t', 'f')) {
            't' -> {
                readExpecting('r')
                readExpecting('u')
                readExpecting('e')
                found = true
            }
            'f' -> {
                readExpecting('a')
                readExpecting('l')
                readExpecting('s')
                readExpecting('e')
                found = false
            }
            else -> TODO()
        }

        readNext()

        validateValueEnd()

        found
    }

    override fun readNumber(): Number = isolateValue {
        var char: Char = peekStrict()
        val result = StringBuilder()

        // minus
        if (char == '-') {
            result.append(char)
            char = readNextStrict()
        }

        // int part
        when (peekExpecting(*digits)) {
            // if leading zero then float point
            '0' -> {
                result.append('0')
                char = readExpecting('.', 'e', 'E')
            }
            in '1'..'9' -> {
                do {
                    result.append(char)
                    char = readNextStrict()
                } while (char in digits)
            }
        }

        // dot
        if (char == '.') {
            do {
                result.append(char)
                char = readNextStrict()
            } while (char in digits)
        }

        // exp part
        if (char == 'e' || char == 'E') {
            result.append(char)

            char = readExpecting(*(digits + '-' + '+'))
            result.append(char)

            char = readExpecting(*digits)

            do {
                result.append(char)
                char = readNext() ?: break
            } while (char in digits)
        }

        validateValueEnd()

        result.toString().toDouble()
    }

    override fun readString(): String = isolateValue {
        val result = StringBuilder()

        peekExpecting('"')

        var char: Char = readNextStrict()

        loop@ do {
            when (char) {
                '"' -> break@loop
                '\\' -> {
                    char = readNextStrict()
                    when (char) {
                        '\\', '/' -> {
                            result.append(char)
                        }
                        '"' -> {
                            result.append(char)
                        }
                        'b' -> result.append('\b')
                        'f' -> result.append('\u000C')
                        't' -> result.append('\t')
                        'n' -> result.append('\n')
                        'r' -> result.append('\r')
                        'u' -> {
                            val char1 = readExpecting(*hexDigits).parseHexDigit() shl 12
                            val char2 = readExpecting(*hexDigits).parseHexDigit() shl 8
                            val char3 = readExpecting(*hexDigits).parseHexDigit() shl 4
                            val char4 = readExpecting(*hexDigits).parseHexDigit()

                            result.append((char1 or char2 or char3 or char4).toChar())
                        }
                        else -> throw Exception("Incorrect escape sequence @ $cursor")
                    }
                }
                in controls -> throw Exception("Unsupported escape sequence @ $cursor")
                else -> result.append(char)
            }

            char = readNextStrict()
        } while (true)

        readNext()

        validateValueEnd()

        result.toString()
    }

    override fun readNull(): Nothing? = isolateValue {
        peekExpecting('n')
        readExpecting('u')
        readExpecting('l')
        readExpecting('l')
        readNext()
        validateValueEnd()
        null
    }

    private fun readArrayStart() {
        peekExpecting(BEGIN_ARRAY.char)
        readNext()
    }

    private fun readArrayEnd() {
        peekExpecting(END_ARRAY.char)
        readNext()
    }

    private fun readObjectStart() {
        peekExpecting(BEGIN_OBJECT.char)
        readNext()
    }

    private fun readObjectEnd() {
        peekExpecting(END_OBJECT.char)
        readNext()
    }

    override fun iterateObject(block: JsonReader.(property: String) -> Unit) {
        readObjectStart()

        while (true) {
            val key = readString()

            peekExpecting(NAME_SEPARATOR.char)
            readNext()

            block(this, key)

            if (peek() != VALUE_SEPARATOR.char) break
            readNext()
        }

        readObjectEnd()
    }

    override fun iterateArray(block: JsonReader.() -> Unit) {
        readArrayStart()

        while (true) {
            block(this)

            if (peek() != VALUE_SEPARATOR.char) break
            readNext()
        }

        readArrayEnd()
    }

    override fun nextType(): JsonEntity = when (peek()) {
        null -> eof
        'n' -> `null`
        'f', 't' -> boolean
        '\"' -> string
        in digits, '-' -> number
        BEGIN_ARRAY.char -> array
        BEGIN_OBJECT.char -> `object`
        else -> undefined
    }

    override fun skipValue() {
        when(nextType()) {
            string -> readString()
            boolean -> readBoolean()
            number -> readNumber()
            `null` -> readNull()
            array -> iterateArray { skipValue() }
            `object` -> iterateObject { skipValue() }
            eof -> Unit
            undefined -> Unit
        }
    }
}

val valueStartScope: CharArray = WhiteSpaces + arrayOf(BEGIN_ARRAY.char, BEGIN_OBJECT.char, NAME_SEPARATOR.char).toCharArray()
val valueEndScope: CharArray = WhiteSpaces + arrayOf(END_ARRAY.char, END_OBJECT.char, VALUE_SEPARATOR.char, NAME_SEPARATOR.char).toCharArray()

val digits: CharArray = arrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9').toCharArray()

val hexAlphanumeric: CharArray = arrayOf('a', 'b', 'c', 'd', 'e', 'f').toCharArray()
val hexAlphanumericCapital: CharArray = arrayOf('A', 'B', 'C', 'D', 'E', 'F').toCharArray()
val hexDigits: CharArray = digits + hexAlphanumeric + hexAlphanumericCapital

val controls = arrayOf(
    0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0A, 0x0B, 0x0C, 0x0D, 0x0E, 0x0F,
    0x10, 0x11, 0x12, 0x13, 0x14, 0x15, 0x16, 0x17, 0x18, 0x19, 0x1A, 0x1B, 0x1C, 0x1D, 0x1E, 0x1F
).map { it.toChar() }.toCharArray()

private fun Char.parseHexDigit() =
    when (this) {
        in digits ->
            this - digits.first()

        in hexAlphanumeric ->
            this - hexAlphanumeric.first() + 10

        in hexAlphanumericCapital ->
            this - hexAlphanumericCapital.first() + 10

        else -> throw IllegalArgumentException("$this is not a hex digit.")
    }