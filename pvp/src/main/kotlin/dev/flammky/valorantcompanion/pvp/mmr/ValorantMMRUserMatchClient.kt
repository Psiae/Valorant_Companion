package dev.flammky.valorantcompanion.pvp.mmr

import dev.flammky.valorantcompanion.pvp.PVPAsyncRequestResult
import kotlinx.coroutines.Deferred

interface ValorantMMRUserMatchClient {

    fun fetchMMRAsync(subject: String): Deferred<PVPAsyncRequestResult<FetchSeasonalMMRResult>>

    fun dispose()
}