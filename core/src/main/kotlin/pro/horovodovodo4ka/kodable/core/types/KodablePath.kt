package pro.horovodovodo4ka.kodable.core.types

import pro.horovodovodo4ka.kodable.core.json.JsonReader
import pro.horovodovodo4ka.kodable.core.types.KodablePath.PathToken.ListElement
import pro.horovodovodo4ka.kodable.core.types.KodablePath.PathToken.ObjectElement

class KodablePath(path: String) {

    sealed class PathToken {
        abstract fun process(reader: JsonReader): Boolean

        class ObjectElement(val key: String) : PathToken() {
            override fun process(reader: JsonReader): Boolean {
                return runCatching {
                    reader.iterateObject {
                        if (it == key) throw KodableException("Token found, stop skipping")
                        skipValue()
                    }
                }.isFailure
            }

            override fun toString(): String = ".$key"
        }

        class ListElement(val index: Int) : PathToken() {
            override fun process(reader: JsonReader): Boolean {
                return runCatching {
                    reader.iterateArray {
                        if (index == it) throw KodableException()
                        skipValue()
                    }
                }.isFailure
            }

            override fun toString(): String = "[$index]"
        }
    }

    private val stack: List<PathToken>

    init {
        stack = path
            .split(".", "[")
            .filter { it.isNotEmpty() }
            .map {
                Regex("^([0-9]+)\\]$").find(it)?.run { ListElement(groupValues[1].toInt()) } ?: ObjectElement(it)
            }
    }

    internal fun go(reader: JsonReader) {
        stack.mapIndexed { idx, elm -> idx to elm }
            .firstOrNull { !it.second.process(reader) }
            ?.also {
                val (idx, _) = it
                val path = stack.take(idx + 1).joinToString("")
                throw KodableException("KodablePath: '$path' <- this token not found")
            }
    }

    override fun toString(): String = stack.joinToString("") { it.toString() }
}

fun String.kodablePath() = KodablePath(this)