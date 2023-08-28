package dev.flammky.valorantcompanion.base.kt

inline fun <T: Any, R> T.sync(lock: Any = this, block: T.() -> R) = synchronized(lock) { block() }