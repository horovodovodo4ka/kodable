package pro.horovodovodo4ka.kodable.core.json

class JsonReaderRecorder(val recordedReader: JsonReader): JsonReader by recordedReader {
    private var buffer = StringBuilder()
    override fun onCharacterRead(char: Char) {
        recordedReader.onCharacterRead(char)
    }
}