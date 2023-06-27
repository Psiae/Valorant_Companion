package dev.flammky.valorantcompanion.pvp.ingame

import kotlinx.coroutines.Deferred

interface InGameUserClient {

    fun fetchUserCurrentMatchIDAsync(): Deferred<InGameFetchRequestResult<String>>

    fun createMatchClient(matchId: String): InGameUserMatchClient

    fun dispose()
}