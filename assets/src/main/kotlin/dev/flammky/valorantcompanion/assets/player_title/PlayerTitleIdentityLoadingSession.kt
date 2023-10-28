package dev.flammky.valorantcompanion.assets.player_title

import dev.flammky.valorantcompanion.base.kt.sync
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.withContext

abstract class PlayerTitleIdentityLoadingSession(
    val titleUuid: String
) {

    abstract suspend fun awaitResult(): Result<PlayerTitleIdentity>
}

class DelegatedPlayerTitleIdentityLoadingSession(
    private val actual: PlayerTitleIdentityLoadingSession
): PlayerTitleIdentityLoadingSession(actual.titleUuid) {

    private val lifetime = SupervisorJob()

    override suspend fun awaitResult(): Result<PlayerTitleIdentity> {
        return withContext(lifetime) { actual.awaitResult() }
    }
}

class ConflatingPlayerTitleIdentityLoadingSession(
    uuid: String
) : PlayerTitleIdentityLoadingSession(uuid) {

    private val result = CompletableDeferred<Result<PlayerTitleIdentity>>()
    private val waiters = mutableSetOf<Job>()
    private val _startFlag = Job()
    private val _killFlag = Job()

    val startFlag: Job
        get() = _startFlag

    val killFlag: Job
        get() = _killFlag

    override suspend fun awaitResult(): Result<PlayerTitleIdentity> {
        val waiter = Job()
        val result = try {
            waiterEnter(waiter)
            result.await()
        } finally {
            waiterExit(waiter)
        }
        return result
    }

    private fun waiterEnter(
        waiter: Job
    ) {
        waiters.sync {
            if (waiters.add(waiter)) {
                if (waiters.size == 1) {
                    start()
                }
            }
        }
    }

    private fun waiterExit(
        waiter: Job
    ) {
        waiters.sync {
            if (waiters.remove(waiter)) {
                if (waiters.size == 0) {
                    kill()
                }
            }
        }
    }

    private fun start() {
        _startFlag.complete()
    }

    private fun kill() {
        _killFlag.complete()
    }

    fun complete(
        data: PlayerTitleIdentity
    ) {
        result.complete(Result.success(data))
    }

    fun completeExceptionally(
        exception: java.lang.Exception
    ) {
        result.complete(Result.failure(exception))
    }

    fun end() {
        result.cancel()
    }
}