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
import java.io.Reader

operator fun JsonReader.Companion.invoke(input: Reader): JsonReader = DefaultJsonReader(input)
operator fun JsonReader.Companion.invoke(inputString: String): JsonReader = invoke(inputString.reader())

private class ReadDelegate {
    private val buffer = StringBuilder()
    override fun toString(): String = buffer.toString()

    fun onRead(char: Char) {
        buffer.append(char)
    }
}

private interface JsonReaderWithCharReading : JsonReader {
    val cursorPositionForPrint: Int

    var readDelegate: ReadDelegate?

    override fun iterateObjectWithPrefetch(prefetch: JsonReader.(property: String) -> Unit): JsonReader {
        val shift = cursorPositionForPrint + 1
        val proxy = ReadDelegate()
        readDelegate = proxy
        iterateObject(prefetch)
        readDelegate = null
        return DefaultJsonReader(proxy.toString().reader(), shift)
    }
}

private class DefaultJsonReader(private val input: Reader, private val cursorShift: Int = 0) : JsonReaderWithCharReading {
    override var readDelegate: ReadDelegate? = null

    companion object {
        const val chunkSize = 1024
    }

    private val buffer = CharArray(chunkSize)
    private var bufferLen = -1
    private var cursor: Int = -1
    private var chunkCursor: Int = 0

    private var currentChar: Char? = null
    private var isOutside = true
    private val relativeCursor: Int get() = cursor - chunkCursor + bufferLen

    init {
        readNext()
    }

    override val cursorPositionForPrint
        get() = cursor + cursorShift

    private fun readFromStream() {
        bufferLen = input.read(buffer, 0, chunkSize)
        chunkCursor += bufferLen
    }

    private fun readNextDirty(): Char? {
        peek()?.also { readDelegate?.onRead(it) }

        cursor++

        // means eof
        if (bufferLen == 0) {
            return this.currentChar
        }

        if (bufferLen < 0 || relativeCursor >= bufferLen) {
            readFromStream()
        }

        return if (relativeCursor >= bufferLen) {
            this.currentChar = null
            null
        } else {
            buffer.getOrNull(relativeCursor).also { this.currentChar = it }
        }
    }

    private fun readNext(): Char? {
        readNextDirty()
        if (isOutside) skipWhitespaces()
        return peek()
    }

    private fun skipWhitespaces() {
        while (true) {
            val char = peek()
            if (char == null || char !in WhiteSpaces) break
            readNextDirty()
        }
    }

    private fun readNextStrict(): Char {
        return readNext()
            ?: throw Exception("Unexpected EOF @ $cursorPositionForPrint")
    }

    private fun peek(): Char? {
        return currentChar
    }

    private fun peekStrict(): Char {
        return peek()
            ?: throw Exception("Unexpected EOF @ $cursorPositionForPrint")
    }

    private fun peekExpecting(vararg expected: Char): Char {
        val next = peekStrict()
        if (next !in expected)
            throw Exception("Unexpected character @ $cursorPositionForPrint: '$next' is not one of: '${expected.joinToString("', '")}'")
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
        if (nextToken != null && nextToken !in valueEndScope) throw Exception("Value end expected @ $cursorPositionForPrint")
    }

    private fun <T> isolateValue(block: () -> T): T {
        isOutside = false
        return block().also {
            isOutside = true
            skipWhitespaces()
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
            else -> throw IllegalStateException()
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
            // if leading zero then float point or end
            '0' -> {
                result.append('0')
                readExpectingSoft('.', 'e', 'E') { char = it }
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
                        't' -> result.append('\t')
                        'n' -> result.append('\n')
                        'r' -> result.append('\r')
                        'b' -> result.append('\b')
                        'f' -> result.append('\u000C')
                        'u' -> {
                            val char1 = readExpecting(*hexDigits).parseHexDigit() shl 12
                            val char2 = readExpecting(*hexDigits).parseHexDigit() shl 8
                            val char3 = readExpecting(*hexDigits).parseHexDigit() shl 4
                            val char4 = readExpecting(*hexDigits).parseHexDigit()

                            result.append((char1 or char2 or char3 or char4).toChar())
                        }
                        else -> throw Exception("Incorrect escape sequence @ $cursorPositionForPrint")
                    }
                }
                in controls -> throw Exception("Unsupported escape sequence @ $cursorPositionForPrint")
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
            if (peek() == END_OBJECT.char) break

            val key = readString()

            peekExpecting(NAME_SEPARATOR.char)
            readNext()

            block(this, key)

            if (peek() != VALUE_SEPARATOR.char) break
            readNext()
        }

        readObjectEnd()
    }

    override fun iterateArray(block: JsonReader.(index: Int) -> Unit) {
        readArrayStart()

        var index = 0
        while (true) {
            if (peek() == END_ARRAY.char) break

            block(this, index)

            if (peek() != VALUE_SEPARATOR.char) break

            readNext()
            index++
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
        when (nextType()) {
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
val hexDigits: CharArray = digits + hexAlphanumericCapital + hexAlphanumeric

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