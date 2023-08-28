package dev.flammky.valorantcompanion.base.kt.coroutines

import kotlinx.coroutines.Deferred
import kotlinx.coroutines.cancel

suspend inline fun <T> Deferred<T>.awaitOrCancelOnException(): T {
    return try {
        await()
    } catch (e: Exception) {
        cancel("Exception while awaiting for deferred", e)
        throw e
    }
}

