package dev.flammky.valorantcompanion.base.kt

fun String.substringAfterOrNull(
    delimiter: String
): String? {
    val index = indexOf(delimiter)
    return if (index == -1) null else substring(index + delimiter.length, length)
}