package pro.horovodovodo4ka.kodable.core.json

fun JsonReader.record(block: JsonReader.() -> Unit): JsonReader = JsonReader(snapshot(block))