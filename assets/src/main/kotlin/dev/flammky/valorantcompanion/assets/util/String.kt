package dev.flammky.valorantcompanion.assets.util

fun String.suffix(str: String) = if (!endsWith(str)) this + str else this
fun String.prefix(str: String) = if (!startsWith(str)) this + str else this