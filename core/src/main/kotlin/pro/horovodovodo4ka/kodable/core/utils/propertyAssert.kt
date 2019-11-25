package pro.horovodovodo4ka.kodable.core.utils

import pro.horovodovodo4ka.kodable.core.types.KodableException

private fun assert(condition: Boolean, lazyMessage: () -> String) {
    if (!condition) lazyMessage().also { throw KodableException(it) }
}

fun propertyAssert(value: Any?, name: String, clz: String) {
    assert(value != null) { "Non nullable property '$name' didn't decoded for type '$clz'" }
}