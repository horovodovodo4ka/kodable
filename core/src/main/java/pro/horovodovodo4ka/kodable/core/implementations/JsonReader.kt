package pro.horovodovodo4ka.kodable.core.implementations

import pro.horovodovodo4ka.kodable.core.KodableReader
import pro.horovodovodo4ka.kodable.core.implementations.JsonReader.State.afterListElement
import pro.horovodovodo4ka.kodable.core.implementations.JsonReader.State.afterListElementSeparator
import pro.horovodovodo4ka.kodable.core.implementations.JsonReader.State.afterListStart
import pro.horovodovodo4ka.kodable.core.implementations.JsonReader.State.afterMapElement
import pro.horovodovodo4ka.kodable.core.implementations.JsonReader.State.afterMapElementSeparator
import pro.horovodovodo4ka.kodable.core.implementations.JsonReader.State.afterMapKey
import pro.horovodovodo4ka.kodable.core.implementations.JsonReader.State.afterMapKeySeparator
import pro.horovodovodo4ka.kodable.core.implementations.JsonReader.State.afterMapStart
import pro.horovodovodo4ka.kodable.core.implementations.JsonReader.State.closed
import pro.horovodovodo4ka.kodable.core.implementations.JsonReader.State.end
import pro.horovodovodo4ka.kodable.core.implementations.JsonReader.State.initial
import pro.horovodovodo4ka.kodable.core.types.Character
import pro.horovodovodo4ka.kodable.core.types.Character.Digit
import pro.horovodovodo4ka.kodable.core.types.Character.Letter
import pro.horovodovodo4ka.kodable.core.types.Character.Symbol
import pro.horovodovodo4ka.kodable.core.types.JSONException
import pro.horovodovodo4ka.kodable.core.types.JSONToken
import pro.horovodovodo4ka.kodable.core.types.JSONToken.booleanValue
import pro.horovodovodo4ka.kodable.core.types.JSONToken.listEnd
import pro.horovodovodo4ka.kodable.core.types.JSONToken.listStart
import pro.horovodovodo4ka.kodable.core.types.JSONToken.mapEnd
import pro.horovodovodo4ka.kodable.core.types.JSONToken.mapKey
import pro.horovodovodo4ka.kodable.core.types.JSONToken.mapStart
import pro.horovodovodo4ka.kodable.core.types.JSONToken.nullValue
import pro.horovodovodo4ka.kodable.core.types.JSONToken.numberValue
import pro.horovodovodo4ka.kodable.core.types.JSONToken.stringValue
import pro.horovodovodo4ka.kodable.core.types.TextInput
import java.io.IOException

internal class JsonReader(private val source: TextInput) : KodableReader {

    private val buffer = StringBuilder()
    private var peekedToken: JSONToken? = null
    private var peekedTokenIndex = -1
    private var state = initial
    private val stateStack = mutableListOf<State>()

    private fun assertNotClosed() {
        if (state == closed) {
            throw IOException("Cannot operate on a closed reader")
        }
    }

    override fun close() {
        if (state == closed) {
            return
        }

        state = closed

        source.close()
    }

    private fun finishValue() {
        val nextCharacter = source.peekCharacter()
        if (!Character.isValueBoundary(nextCharacter)) {
            throw unexpectedCharacter(
                nextCharacter,
                expected = "end of value",
                characterIndex = source.sourceIndex
            )
        }
    }

    override val nextToken: JSONToken?
        get() {
            assertNotClosed()

            if (peekedTokenIndex >= 0) {
                return peekedToken
            }

            val peekedToken = peekToken()
            this.peekedToken = peekedToken
            this.peekedTokenIndex = source.sourceIndex

            return peekedToken
        }

    private fun peekToken(): JSONToken? {
        val source = source

        while (true) {
            source.skipWhitespaceCharacters()

            val character = source.peekCharacter()

            @Suppress("NON_EXHAUSTIVE_WHEN")
            when (state) {
                afterListElement ->
                    when (character) {
                        Symbol.comma -> {
                            state = afterListElementSeparator
                            source.readCharacter()
                        }

                        Symbol.rightSquareBracket -> {
                            restoreState()
                            return listEnd
                        }

                        else -> throw unexpectedCharacter(character, expected = "',' or ']'")
                    }

                afterListElementSeparator -> {
                    state = afterListElement
                    return peekValueToken(expected = "a value")
                }

                afterListStart ->
                    return when (character) {
                        Symbol.rightSquareBracket -> {
                            restoreState()
                            listEnd
                        }
                        else -> {
                            state = afterListElement
                            peekValueToken(expected = "a value or ']'")
                        }
                    }

                afterMapElement ->
                    when (character) {
                        Symbol.comma -> {
                            state = afterMapElementSeparator
                            source.readCharacter()
                        }

                        Symbol.rightCurlyBracket -> {
                            restoreState()
                            return mapEnd
                        }

                        else -> throw unexpectedCharacter(character, expected = "',' or '}'")
                    }

                afterMapElementSeparator ->
                    when (character) {
                        Symbol.quotationMark -> {
                            state = afterMapKey
                            return mapKey
                        }

                        else -> throw unexpectedCharacter(character, expected = "'\"'")
                    }

                afterMapKey ->
                    when (character) {
                        Symbol.colon -> {
                            state = afterMapKeySeparator
                            source.readCharacter()
                        }

                        else -> throw unexpectedCharacter(character, expected = "':'")
                    }

                afterMapKeySeparator -> {
                    state = afterMapElement
                    return peekValueToken(expected = "a value")
                }

                afterMapStart ->
                    return when (character) {
                        Symbol.quotationMark -> {
                            state = afterMapKey
                            mapKey
                        }

                        Symbol.rightCurlyBracket -> {
                            restoreState()
                            mapEnd
                        }

                        else -> throw unexpectedCharacter(character, expected = "'\"' or '}'")
                    }

                end ->
                    when (character) {
                        Character.end ->
                            return null

                        else -> throw unexpectedCharacter(character, expected = "end of input")
                    }

                initial -> {
                    state = end
                    return peekValueToken(expected = "a value")
                }
            }
        }
    }

    private fun peekValueToken(expected: String): JSONToken? {
        val character = source.peekCharacter()
        return when (character) {
            Symbol.quotationMark ->
                stringValue

            Symbol.hyphenMinus,
            Digit.zero,
            Digit.one,
            Digit.two,
            Digit.three,
            Digit.four,
            Digit.five,
            Digit.six,
            Digit.seven,
            Digit.eight,
            Digit.nine ->
                numberValue

            Letter.f,
            Letter.t ->
                booleanValue

            Letter.n ->
                nullValue

            Symbol.leftCurlyBracket -> {
                saveState()
                state = afterMapStart

                mapStart
            }

            Symbol.leftSquareBracket -> {
                saveState()
                state = afterListStart

                listStart
            }

            else -> throw unexpectedCharacter(character, expected = expected)
        }
    }

    override fun readBoolean(): Boolean {
        readToken(booleanValue)

        val source = source
        if (source.peekCharacter() == Letter.t) {
            source.readCharacter(Letter.t)
            source.readCharacter(Letter.r)
            source.readCharacter(Letter.u)
            source.readCharacter(Letter.e)
            finishValue()

            return true
        } else {
            source.readCharacter(Letter.f)
            source.readCharacter(Letter.a)
            source.readCharacter(Letter.l)
            source.readCharacter(Letter.s)
            source.readCharacter(Letter.e)
            finishValue()

            return false
        }
    }

    override fun readDouble(): Double {
        readNumberIntoBuffer()

        return buffer.toString().toDouble()
    }

    override fun readFloat(): Float {
        readNumberIntoBuffer()

        return buffer.toString().toFloat()
    }

    override fun readListEnd() {
        readToken(listEnd)

        source.readCharacter(Symbol.rightSquareBracket)
    }

    override fun readListStart() {
        readToken(listStart)

        source.readCharacter(Symbol.leftSquareBracket)
    }

    override fun readLong(): Long {
        readToken(numberValue)

        return source.locked {
            readLongLocked()
        }
    }

    private fun readLongLocked(): Long {
        val startIndex = source.index

        val isNegative: Boolean
        val negativeLimit: Long

        val source = source
        var character = source.readCharacter()
        if (character == Symbol.hyphenMinus) {
            isNegative = true
            character = source.readCharacter(required = Character::isDigit) { "a digit" }
            negativeLimit = Long.MIN_VALUE
        } else {
            isNegative = false
            negativeLimit = -Long.MAX_VALUE
        }

        val minimumBeforeMultiplication = negativeLimit / 10
        var value = 0L

        if (character == Digit.zero) {
            character = source.readCharacter(required = { !Character.isDigit(it) }) {
                Character.toString(
                    Symbol.fullStop,
                    Letter.e,
                    Letter.E
                ) + " or end of number after a leading '0'"
            }
        } else {
            do {
                val digit = character - Digit.zero
                if (value < minimumBeforeMultiplication) {
                    value = negativeLimit

                    do character = source.readCharacter()
                    while (Character.isDigit(character))
                    break
                }

                value *= 10

                if (value < negativeLimit + digit) {
                    value = negativeLimit

                    do character = source.readCharacter()
                    while (Character.isDigit(character))
                    break
                }

                value -= digit
                character = source.readCharacter()
            } while (Character.isDigit(character))

            if (!isNegative) {
                value *= -1
            }
        }

        if (character == Symbol.fullStop) { // truncate decimal value
            source.readCharacter(required = Character::isDigit) { "a digit in decimal value of number" }

            do character = source.readCharacter()
            while (Character.isDigit(character))
        }

        if (character == Letter.e || character == Letter.E) { // oh no, an exponent!
            source.seekTo(startIndex)
            unreadToken(numberValue)

            return readDouble().toLong()
        }

        source.seekBackOneCharacter()
        finishValue()

        return value
    }

    override fun readMapEnd() {
        readToken(mapEnd)

        source.readCharacter(Symbol.rightCurlyBracket)
    }

    override fun readMapStart() {
        readToken(mapStart)

        source.readCharacter(Symbol.leftCurlyBracket)
    }

    override fun readNull(): Nothing? {
        readToken(nullValue)

        val source = source
        source.readCharacter(Letter.n)
        source.readCharacter(Letter.u)
        source.readCharacter(Letter.l)
        source.readCharacter(Letter.l)
        finishValue()

        return null
    }

    override fun readNumber(): Number {
        val shouldParseAsFloatingPoint = readNumberIntoBuffer()

        val stringValue = buffer.toString()
        if (!shouldParseAsFloatingPoint) {
            val value = stringValue.toLongOrNull()
            if (value != null) {
                return if (value in Int.MIN_VALUE..Int.MAX_VALUE) value.toInt() else value
            }
        }

        return stringValue.toDouble()
    }

    private fun readNumberIntoBuffer(): Boolean {
        readToken(numberValue)

        val buffer = buffer
        buffer.setLength(0)

        var shouldParseAsFloatingPoint = false
        val source = source
        var character = source.readCharacter()

        // consume optional minus sign
        if (character == Symbol.hyphenMinus) {
            buffer.append('-')
            character = source.readCharacter()
        }

        // consume integer value
        when (character) {
            Digit.zero -> {
                buffer.append('0')
                character = source.readCharacter(required = { !Character.isDigit(it) }) {
                    Character.toString(
                        Symbol.fullStop,
                        Letter.e,
                        Letter.E
                    ) + " or end of number after a leading '0'"
                }
            }

            Digit.one,
            Digit.two,
            Digit.three,
            Digit.four,
            Digit.five,
            Digit.six,
            Digit.seven,
            Digit.eight,
            Digit.nine ->
                do {
                    buffer.append(character.toChar())
                    character = source.readCharacter()
                } while (Character.isDigit(character))

            else ->
                throw unexpectedCharacter(
                    character,
                    expected = "a digit in integer value of number",
                    characterIndex = source.sourceIndex - 1
                )
        }

        // consume optional decimal separator and value
        if (character == Symbol.fullStop) {
            shouldParseAsFloatingPoint = true

            buffer.append('.')
            character = source.readCharacter(required = Character::isDigit) { "a digit in decimal value of number" }

            do {
                buffer.append(character.toChar())
                character = source.readCharacter()
            } while (Character.isDigit(character))
        }

        // consume optional exponent separator and value
        if (character == Letter.e || character == Letter.E) {
            shouldParseAsFloatingPoint = true

            buffer.append(character.toChar())

            character = source.peekCharacter()
            if (character == Symbol.plusSign || character == Symbol.hyphenMinus) {
                buffer.append(character.toChar())
                source.readCharacter()
            }

            character = source.readCharacter(required = Character::isDigit) { "a digit in exponent value of number" }

            do {
                buffer.append(character.toChar())
                character = source.readCharacter()
            } while (Character.isDigit(character))
        }

        source.seekBackOneCharacter()
        finishValue()

        return shouldParseAsFloatingPoint
    }

    override fun readString(): String {
        readToken(stringValue, mapKey)

        return source.locked { readStringLocked() }
    }

    private fun readStringLocked(): String {
        val source = source
        source.readCharacter(Symbol.quotationMark)

        val buffer = buffer
        buffer.setLength(0)

        var startIndex = source.index

        do {
            var character = source.readCharacter()
            when (character) {
                Symbol.reverseSolidus -> {
                    val endIndex = source.index - 1
                    if (endIndex > startIndex) {
                        buffer.append(source.buffer, startIndex, endIndex - startIndex)
                    }

                    character = source.readCharacter()
                    when (character) {
                        Symbol.reverseSolidus,
                        Symbol.solidus ->
                            buffer.append(character.toChar())

                        Symbol.quotationMark -> {
                            buffer.append(character.toChar())
                            character = 0
                        }

                        Letter.b -> buffer.append('\b')
                        Letter.f -> buffer.append('\u000C')
                        Letter.n -> buffer.append('\n')
                        Letter.r -> buffer.append('\r')
                        Letter.t -> buffer.append('\t')
                        Letter.u -> {
                            val digit1 = source.readCharacter(required = Character::isHexDigit) { "0-9, a-f or A-F" }
                            val digit2 = source.readCharacter(required = Character::isHexDigit) { "0-9, a-f or A-F" }
                            val digit3 = source.readCharacter(required = Character::isHexDigit) { "0-9, a-f or A-F" }
                            val digit4 = source.readCharacter(required = Character::isHexDigit) { "0-9, a-f or A-F" }

                            val decodedCharacter = (Character.parseHexDigit(digit1) shl 12) or
                                    (Character.parseHexDigit(digit2) shl 8) or
                                    (Character.parseHexDigit(digit3) shl 4) or
                                    Character.parseHexDigit(digit4)

                            buffer.append(decodedCharacter.toChar())
                        }
                        else -> throw unexpectedCharacter(character, "an escape sequence starting with '\\', '/', '\"', 'b', 'f', 'n', 'r', 't' or 'u'")
                    }

                    startIndex = source.index
                }

                0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0A, 0x0B, 0x0C, 0x0D, 0x0E, 0x0F,
                0x10, 0x11, 0x12, 0x13, 0x14, 0x15, 0x16, 0x17, 0x18, 0x19, 0x1A, 0x1B, 0x1C, 0x1D, 0x1E, 0x1F ->
                    throw unexpectedCharacter(character, "an escape sequence")

                Symbol.quotationMark ->
                    Unit

                Character.end ->
                    throw JSONException("unterminated string value")
            }
        } while (character != Symbol.quotationMark)

        val endIndex = source.index - 1
        if (endIndex > startIndex) {
            buffer.append(source.buffer, startIndex, endIndex - startIndex)
        }

        return buffer.toString()
    }

    private fun readToken(required: JSONToken) {
        val token = nextToken
        if (token != required) {
            throw JSONException.unexpectedToken(
                token,
                expected = "'$required'",
                characterIndex = peekedTokenIndex
            )
        }

        peekedToken = null
        peekedTokenIndex = -1
    }

    private fun readToken(required: JSONToken, alternative: JSONToken) {
        val token = nextToken
        if (token != required && token != alternative) {
            throw JSONException.unexpectedToken(
                token,
                expected = "'$required' or '$alternative'",
                characterIndex = peekedTokenIndex
            )
        }

        peekedToken = null
        peekedTokenIndex = -1
    }

    private fun restoreState() {
        state = stateStack.removeAt(stateStack.size - 1)
    }

    private fun saveState() {
        stateStack.add(state)
    }

    private fun unexpectedCharacter(character: Int, expected: String, characterIndex: Int = source.sourceIndex) =
        JSONException.unexpectedCharacter(character, expected = expected, characterIndex = characterIndex)

    private fun unreadToken(token: JSONToken) {
        peekedToken = token
        peekedTokenIndex = source.sourceIndex
    }

    private enum class State {

        afterListElementSeparator,
        afterListElement,
        afterListStart,
        afterMapElement,
        afterMapElementSeparator,
        afterMapKey,
        afterMapKeySeparator,
        afterMapStart,
        closed,
        end,
        initial,
    }
}
