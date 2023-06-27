package dev.flammky.valorantcompanion.pvp.ingame

import kotlinx.coroutines.Deferred

interface InGameUserMatchClient {

    fun fetchMatchInfoAsync(): Deferred<InGameFetchRequestResult<InGameMatchData>>

    fun dispose()
}