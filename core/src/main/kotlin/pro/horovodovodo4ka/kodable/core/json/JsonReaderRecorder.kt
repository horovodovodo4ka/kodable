package pro.horovodovodo4ka.kodable.core.json

private class JsonReaderRecorder(private val recordedReader: JsonReader) : JsonReader by recordedReader {
    private var buffer = StringBuilder()
    override fun onCharacterRead(char: Char) {
        buffer.append(char)
        recordedReader.onCharacterRead(char)
    }

    fun recordedSnapshot(): JsonReader = JsonReader(buffer.toString().byteInputStream())
}

fun record(jsonReader: JsonReader, block: JsonReader.() -> Unit): JsonReader = JsonReaderRecorder(jsonReader)
    .run {
        block(this)
        recordedSnapshot()
    }