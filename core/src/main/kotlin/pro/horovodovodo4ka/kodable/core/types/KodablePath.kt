package pro.horovodovodo4ka.kodable.core.types

import com.github.fluidsonic.fluid.json.JSONReader
import com.github.fluidsonic.fluid.json.JSONToken.listEnd
import com.github.fluidsonic.fluid.json.JSONToken.mapEnd
import pro.horovodovodo4ka.kodable.core.types.KodablePath.PathToken.ListElement
import pro.horovodovodo4ka.kodable.core.types.KodablePath.PathToken.ObjectElement

class KodablePath(path: String) {

    sealed class PathToken {
        abstract fun process(reader: JSONReader): Boolean

        class ObjectElement(val key: String) : PathToken() {
            override fun process(reader: JSONReader): Boolean {
                with(reader) {
                    readMapStart()
                    while (nextToken != mapEnd) {
                        if (readMapKey() == key) return true
                        skipValue()
                    }
                    readMapEnd()
                }
                return false
            }

            override fun toString(): String = ".$key"
        }

        class ListElement(val index: Int) : PathToken() {
            override fun process(reader: JSONReader): Boolean {
                with(reader) {
                    readListStart()
                    var counter = 0
                    while (nextToken != listEnd) {
                        if (index == counter) return true
                        counter++
                        skipValue()
                    }
                    readListEnd()
                }
                return false
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

    internal fun go(reader: JSONReader) {
        stack.mapIndexed { idx, elm -> idx to elm }
            .firstOrNull { !it.second.process(reader) }
            ?.also {
                val (idx, _) = it
                val path = stack.take(idx + 1).joinToString("")
                throw Exception("KodablePath: '$path' <- this token not found")
            }
    }

    override fun toString(): String = stack.joinToString("") { it.toString() }
}

fun String.kodablePath() = KodablePath(this)