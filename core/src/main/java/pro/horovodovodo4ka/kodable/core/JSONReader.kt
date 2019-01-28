package pro.horovodovodo4ka.kodable.core

import pro.horovodovodo4ka.kodable.core.implementations.StandardReader
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
import java.io.Closeable
import java.io.Reader
import java.io.StringReader

interface JSONReader : Closeable {

    val nextToken: JSONToken?

    fun readBoolean(): Boolean
    fun readDouble(): Double
    fun readListEnd()
    fun readListStart()
    fun readLong(): Long
    fun readMapEnd()
    fun readMapStart()
    fun readNull(): Nothing?
    fun readNumber(): Number
    fun readString(): String

    fun readByte(): Byte {
        val value = readLong()
        return when {
            value >= Byte.MAX_VALUE -> Byte.MAX_VALUE
            value <= Byte.MIN_VALUE -> Byte.MIN_VALUE
            else -> value.toByte()
        }
    }

    fun readFloat() = readDouble().toFloat()

    fun readInt(): Int {
        val value = readLong()
        return when {
            value >= Int.MAX_VALUE -> Int.MAX_VALUE
            value <= Int.MIN_VALUE -> Int.MIN_VALUE
            else -> value.toInt()
        }
    }

    fun readMapKey() = readString()

    fun readShort(): Short {
        val value = readLong()
        return when {
            value >= Short.MAX_VALUE -> Short.MAX_VALUE
            value <= Short.MIN_VALUE -> Short.MIN_VALUE
            else -> value.toShort()
        }
    }

    fun skipValue() {
        val token = nextToken
        when (token) {
            booleanValue -> readBoolean()
            listStart -> readElementsFromList { skipValue() }
            mapKey -> readMapKey()
            mapStart -> readElementsFromMap { skipValue() }
            nullValue -> readNull()
            numberValue -> readNumber()
            stringValue -> readString()
            else -> throw JSONException("Cannot skip value if next token is '$token'")
        }
    }

    fun readBooleanOrNull() =
        if (nextToken != nullValue) readBoolean() else readNull()

    fun readDoubleOrNull() =
        if (nextToken != nullValue) readDouble() else readNull()

    fun readLongOrNull() =
        if (nextToken != nullValue) readLong() else readNull()

    fun readNumberOrNull() =
        if (nextToken != nullValue) readNumber() else readNull()

    fun readStringOrNull() =
        if (nextToken != nullValue) readString() else readNull()

    fun readByteOrNull() =
        if (nextToken != nullValue) readByte() else readNull()

    fun readFloatOrNull() =
        if (nextToken != nullValue) readFloat() else readNull()

    fun readIntOrNull() =
        if (nextToken != nullValue) readInt() else readNull()

    fun readShortOrNull() =
        if (nextToken != nullValue) readShort() else readNull()

    fun readElementsFromMap(readElement: JSONReader.(key: String) -> Unit) {
        readMapStart()
        while (nextToken != mapEnd)
            readElement(this, readMapKey())
        readMapEnd()
    }

    fun readElementsFromList(readElement: JSONReader.(Int) -> Unit) {
        readListStart()
        var counter = 0
        while (nextToken != listEnd)
            readElement(this, counter++)
        readListEnd()
    }

    companion object {
        fun build(source: Reader): JSONReader = StandardReader(TextInput(source))
        fun build(source: String): JSONReader = build(StringReader(source))
    }
}
