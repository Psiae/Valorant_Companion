package dev.flammky.valorantcompanion.pvp.loadout.internal

import dev.flammky.valorantcompanion.base.kt.castOrNull
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.completeWith

class FetchConflator {

    private val defs = mutableMapOf<String, Deferred<*>>()

    suspend fun <T> fetch(id: String, block: suspend () -> T): T {
        var new = true
        return synchronized(defs) {
            defs[id]
                ?.let { current ->
                    new = false
                    return@synchronized current
                }
            val def = CompletableDeferred<T>()
            defs[id] = def
            def
        }.run {
            castOrNull<CompletableDeferred<T>>()
                ?: throw IllegalArgumentException("Deferred type mismatch for given id=$id")
            if (new) {
                completeWith(runCatching { block() })
                check(synchronized(defs) { defs.remove(id) }?.isCompleted == true)
            }
            await()
        }
    }
}