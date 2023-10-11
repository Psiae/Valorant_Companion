package dev.flammky.valorantcompanion.base.kt

fun String.substringAfterOrNull(
    delimiter: String
): String? {
    val index = indexOf(delimiter)
    return if (index == -1) null else substring(index + delimiter.length, length)
}

fun String.suffix(str: String, ignoreCase: Boolean) = if (!endsWith(str, ignoreCase)) this + str else this
fun String.prefix(str: String, ignoreCase: Boolean) = if (!startsWith(str, ignoreCase)) this + str else this
