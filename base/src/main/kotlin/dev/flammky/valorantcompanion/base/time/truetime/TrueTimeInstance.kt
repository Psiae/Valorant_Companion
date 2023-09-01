package dev.flammky.valorantcompanion.base.time.truetime

import kotlinx.coroutines.Deferred
import kotlin.time.Duration

interface TrueTimeInstance {

    fun fetchAsync(): Deferred<Result<Duration>>

    fun fetchUntilSuccessAsync(
        maxRetry: Int = -1,
    ): Deferred<Result<Duration>>

    fun dispose()

    fun deviceUptime(): Duration
}