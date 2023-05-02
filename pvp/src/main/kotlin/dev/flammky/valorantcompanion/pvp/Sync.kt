package dev.flammky.valorantcompanion.pvp

internal inline fun <R> sync(
    lock: Any,
    block: () -> R
): R = synchronized(lock) { block() }

internal inline fun <T: Any, R> T.sync(
    lock: Any = this,
    block: T.() -> R
): R = synchronized(lock) { block() }