package dev.flammky.valorantcompanion.pvp.pregame

import dev.flammky.valorantcompanion.pvp.agent.ValorantAgentIdentity
import kotlinx.coroutines.Deferred

interface PreGameClient {

    fun fetchCurrentPreGameMatchData(): Deferred<Result<PreGameMatchData>>

    fun hasPreGameMatchData(): Deferred<Result<Boolean>>

    fun fetchPingMillis(): Deferred<Result<Map<String, Int>>>

    fun fetchPlayerMMRData(
        subjectPUUID: String
    ): Deferred<PreGameAsyncRequestResult<PreGamePlayerMMRData>>

    fun fetchUnlockedAgentsAsync(

    ): Deferred<PreGameAsyncRequestResult<List<ValorantAgentIdentity>>>

    fun dispose()

    fun lockAgent(
        matchID: String,
        agentID: String
    ): Deferred<PreGameAsyncRequestResult<PreGameMatchData>>

    fun selectAgent(
        matchID: String,
        agentID: String
    ): Deferred<PreGameAsyncRequestResult<PreGameMatchData>>
}