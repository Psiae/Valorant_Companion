package dev.flammky.valorantcompanion.pvp.ingame

import kotlinx.coroutines.Deferred

interface InGameUserClient {

    // maybe make the String nullable or make a result class ?
    fun fetchUserCurrentMatchIDAsync(): Deferred<InGameFetchRequestResult<String>>

    fun createMatchClient(matchId: String): InGameUserMatchClient

    fun fetchPingMillisAsync(): Deferred<InGameFetchRequestResult<Map<String, Int>>>

    fun dispose()
}