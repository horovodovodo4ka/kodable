package pro.horovodovodo4ka.kodable.core

import pro.horovodovodo4ka.kodable.core.KodablePath.PathToken.ListElement
import pro.horovodovodo4ka.kodable.core.KodablePath.PathToken.ObjectElement
import pro.horovodovodo4ka.kodable.core.types.JSONToken.mapEnd

class KodablePath(path: String) {

    sealed class PathToken {
        abstract fun process(reader: KodableReader)

        class ObjectElement(val key: String) : PathToken() {
            override fun process(reader: KodableReader) {
                with(reader) {
                    readMapStart()
                    while (nextToken != mapEnd) {
                        if (readMapKey() == key) return
                        skipValue()
                    }
                    readMapEnd()
                }
                throw Exception()
            }
        }

        class ListElement(val index: Int) : PathToken() {
            override fun process(reader: KodableReader) {
                with(reader) {
                    readListStart()
                    var counter = 0
                    while (nextToken != mapEnd) {
                        if (counter++ == index) return
                        skipValue()
                    }
                    readListEnd()
                }
                throw Exception()
            }
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

    fun go(reader: KodableReader) {
        stack.forEach { it.process(reader) }
    }

    override fun toString(): String {
        return stack.joinToString(" -> ") {
            when (it) {
                is ListElement -> it.index.toString()
                is ObjectElement -> it.key
            }
        }
    }
}
