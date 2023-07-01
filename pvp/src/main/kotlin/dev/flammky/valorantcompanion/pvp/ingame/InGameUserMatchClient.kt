package dev.flammky.valorantcompanion.pvp.ingame

import kotlinx.coroutines.Deferred

interface InGameUserMatchClient {

    val matchID: String

    fun fetchMatchInfoAsync(): Deferred<InGameFetchRequestResult<InGameMatchData>>

    fun dispose()
}