package dev.flammky.valorantcompanion.pvp.util

tailrec fun String.removeDuplicatePrefix(prefix: String): String {
    return if (startsWith(prefix)) removeDuplicatePrefix(drop(prefix.length)) else this
}