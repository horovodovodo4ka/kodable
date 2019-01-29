package pro.horovodovodo4ka.kodable.core.utils

fun propertyAssert(value: Any?, name: String, clz: String) {
    assert(value == null) { "Non nullable property '$name' didn't decoded for type '$clz'" }
}