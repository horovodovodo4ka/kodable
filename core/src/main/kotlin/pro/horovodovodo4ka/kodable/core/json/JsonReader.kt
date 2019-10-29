package pro.horovodovodo4ka.kodable.core.json

interface JsonReader {
    fun readBoolean(): Boolean
    fun readNumber(): Number
    fun readString(): String
    fun readNull(): Nothing?

    fun readArrayStart()
    fun readArrayEnd()

    fun readObjectStart()
    fun readObjectEnd()
}