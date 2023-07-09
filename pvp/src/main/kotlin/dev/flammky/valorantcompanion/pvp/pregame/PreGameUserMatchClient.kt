package dev.flammky.valorantcompanion.pvp.pregame

import kotlinx.coroutines.Deferred

abstract class PreGameUserMatchClient(
    val user: String,
    val matchId: String
) {

    abstract fun fetchMatchInfoAsync(): Deferred<PreGameFetchRequestResult<PreGameMatchData>>

    abstract fun lockAgentAsync(id: String): Deferred<PreGameFetchRequestResult<PreGameMatchData>>

    abstract fun selectAgentAsync(id: String): Deferred<PreGameFetchRequestResult<PreGameMatchData>>

    abstract fun dispose()
}