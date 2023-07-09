package dev.flammky.valorantcompanion.pvp.mmr

import dev.flammky.valorantcompanion.pvp.PVPAsyncRequestResult
import kotlinx.coroutines.Deferred

interface ValorantMMRUserClient {

    // non-cached version
    fun fetchSeasonalMMRAsync(
        season: String,
        subject: String
    ): Deferred<PVPAsyncRequestResult<FetchSeasonalMMRResult>>

    fun createMatchClient(matchID: String): ValorantMMRUserMatchClient

    fun dispose()
}