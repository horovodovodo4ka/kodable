package pro.horovodovodo4ka.kodable.core.utils

private val snake = Regex("""([_\-]([^_\-]+))""")
fun String.toCamelCase() = replace(snake) { it.groupValues[2].capitalize() }