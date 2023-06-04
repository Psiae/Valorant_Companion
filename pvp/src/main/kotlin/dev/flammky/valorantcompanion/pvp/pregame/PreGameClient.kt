package dev.flammky.valorantcompanion.pvp.pregame

import kotlinx.coroutines.Deferred

interface PreGameClient {

    fun fetchCurrentPreGameMatchData(): Deferred<Result<PreGameMatchData>>

    fun hasPreGameMatchData(): Deferred<Result<Boolean>>

    fun fetchPingMillis(): Deferred<Result<Map<String, Int>>>

    fun dispose()

    fun lockAgent(
        agentID: String
    ): Deferred<Result<Boolean>>

    fun selectAgent(
        agentID: String
    ): Deferred<Result<Boolean>>
}