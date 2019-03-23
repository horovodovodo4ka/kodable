package pro.horovodovodo4ka.kodable.core.utils

private val camel = Regex("""(([A-Z])([a-z]+))""")
fun String.toSnakeCase() = replace(camel) {
    val first = "_" + it.groupValues[2].toLowerCase()
    val others = it.groupValues[3]
    first + others
}