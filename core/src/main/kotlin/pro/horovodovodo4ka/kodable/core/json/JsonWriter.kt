package pro.horovodovodo4ka.kodable.core.json

interface JsonWriter {
    fun writeBoolean(value: Boolean)
    fun writeNumber(value: Number)
    fun writeString(value: String)
    fun writeNull()

    fun writeArrayStart()
    fun writeArrayEnd()

    fun readObjectStart()
    fun readObjectEnd()
}