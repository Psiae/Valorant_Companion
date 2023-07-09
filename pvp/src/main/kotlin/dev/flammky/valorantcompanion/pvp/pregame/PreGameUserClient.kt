package dev.flammky.valorantcompanion.pvp.pregame

import dev.flammky.valorantcompanion.pvp.agent.ValorantAgentIdentity
import kotlinx.coroutines.Deferred

interface PreGameUserClient {

    fun fetchCurrentPreGameMatchData(): Deferred<Result<PreGameMatchData>>

    fun hasPreGameMatchDataAsync(): Deferred<Result<Boolean>>

    fun fetchCurrentPreGameMatchId(): Deferred<PreGameFetchRequestResult<String>>

    fun createMatchClient(matchID: String): PreGameUserMatchClient

    fun fetchPingMillis(): Deferred<Result<Map<String, Int>>>

    fun fetchPlayerMMRData(
        subjectPUUID: String
    ): Deferred<PreGameFetchRequestResult<PreGamePlayerMMRData>>

    fun fetchUnlockedAgentsAsync(

    ): Deferred<PreGameFetchRequestResult<List<ValorantAgentIdentity>>>

    fun dispose()

    fun lockAgent(
        matchID: String,
        agentID: String
    ): Deferred<PreGameFetchRequestResult<PreGameMatchData>>

    fun selectAgent(
        matchID: String,
        agentID: String
    ): Deferred<PreGameFetchRequestResult<PreGameMatchData>>
}